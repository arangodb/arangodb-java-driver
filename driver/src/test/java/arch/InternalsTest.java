package arch;

import com.arangodb.arch.UnstableApi;
import com.arangodb.arch.UsedInApi;
import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.*;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import java.util.function.Function;
import java.util.stream.Stream;

import static com.tngtech.archunit.base.DescribedPredicate.and;
import static com.tngtech.archunit.base.DescribedPredicate.not;
import static com.tngtech.archunit.base.DescribedPredicate.or;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.assignableTo;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.core.domain.properties.CanBeAnnotated.Predicates.annotatedWith;
import static com.tngtech.archunit.core.domain.properties.HasReturnType.Predicates.rawReturnType;
import static com.tngtech.archunit.core.domain.properties.HasType.Predicates.rawType;
import static com.tngtech.archunit.lang.conditions.ArchConditions.*;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;

@AnalyzeClasses(packages = "com.arangodb..", importOptions = {ImportOption.DoNotIncludeTests.class})
public class InternalsTest {

    /**
     * Elements of public API are from all packages under {@link com.arangodb} except:
     * - internal packages
     * - dependencies packages
     */
    private static final DescribedPredicate<JavaClass> packageFilter =
            and(
                    not(JavaClass.Predicates.resideInAnyPackage(
                            "..internal..",
                            "com.arangodb.jackson..",
                            "com.arangodb.velocypack..")
                    )
            );

    /**
     * Superclasses of types used in public API must either:
     * - not reside in internal packages, or
     * - be annotated with {@link UsedInApi}
     */
    private static final DescribedPredicate<? super JavaClass> superclassesPredicate =
            haveSuperclasses(or(
                    not(resideInAPackage("..internal..")),
                    annotatedWith(UsedInApi.class)
            ));

    /**
     * Classes in the public API must either:
     * - not extend or implement internal classes, or
     * - be annotated with {@link UnstableApi} and fulfil {@link #superclassesPredicate}
     */
    private static final DescribedPredicate<JavaClass> classPredicate =
            or(
                    not(assignableTo(resideInAPackage("..internal.."))),
                    and(
                            annotatedWith(UnstableApi.class),
                            superclassesPredicate
                    )
            );

    /**
     * Fields in the public API must either:
     * - have type that not extends or implement internal classes, or
     * - be annotated with {@link UnstableApi} and have type that fulfils {@link #superclassesPredicate}
     */
    private static final DescribedPredicate<JavaField> fieldPredicate =
            or(
                    rawType(not(assignableTo(resideInAPackage("..internal..")))),
                    and(
                            annotatedWith(UnstableApi.class),
                            rawType(superclassesPredicate)
                    )
            );

    /**
     * Methods in the public API must either:
     * - have return type that not extends or implement internal classes, or
     * - be annotated with {@link UnstableApi} and have return type that fulfils {@link #superclassesPredicate}
     */
    private static final DescribedPredicate<JavaMethod> methodReturnTypePredicate =
            or(
                    rawReturnType(not(assignableTo(resideInAPackage("..internal..")))),
                    and(
                            annotatedWith(UnstableApi.class),
                            rawReturnType(superclassesPredicate)
                    )
            );

    /**
     * Parameters of methods in the public API must either:
     * - have type that not resides in internal classes, or
     * - be annotated with {@link UnstableApi} and have type annotated with {@link UsedInApi}
     */
    private static final DescribedPredicate<JavaCodeUnit> paramPredicate = haveParams(or(
            ofType(not(resideInAPackage("..internal.."))),
            and(
                    annotatedWith(UnstableApi.class),
                    ofType(annotatedWith(UsedInApi.class))
            )
    ));

    @ArchTest
    @SuppressWarnings("unused")
    public static final ArchRule noInternalsInApiFields = fields()
            .that().arePublic()
            .or().areProtected()
            .and().areDeclaredInClassesThat(packageFilter)
            .should(be(fieldPredicate));

    @ArchTest
    @SuppressWarnings("unused")
    public static final ArchRule noInternalsInApiClasses = classes()
            .that().arePublic()
            .or().areProtected()
            .and(packageFilter)
            .should(be(classPredicate));

    @ArchTest
    @SuppressWarnings("unused")
    public static final ArchRule noInternalsInApiMethods = methods()
            .that().arePublic()
            .or().areProtected()
            .and().areDeclaredInClassesThat(packageFilter)
            .should(be(methodReturnTypePredicate))
            .andShould(be(paramPredicate));

    @ArchTest
    @SuppressWarnings("unused")
    public static final ArchRule noInternalsInApiConstructors = constructors()
            .that().arePublic()
            .or().areProtected()
            .and().areDeclaredInClassesThat(packageFilter)
            .should(be(paramPredicate));

    private static DescribedPredicate<? super JavaClass> haveSuperclasses(
            DescribedPredicate<? super JavaClass> matchingPredicate
    ) {
        return new DescribedPredicate<JavaClass>("have self, superclasses or implemented interfaces "
                + matchingPredicate.getDescription()) {
            @Override
            public boolean test(JavaClass javaClass) {
                return Stream.of(
                                Stream.of(javaClass),
                                javaClass.getAllRawSuperclasses().stream(),
                                javaClass.getAllRawInterfaces().stream()
                        )
                        .flatMap(Function.identity())
                        .allMatch(matchingPredicate);
            }
        };
    }

    private static DescribedPredicate<? super JavaParameter> ofType(DescribedPredicate<? super JavaClass> predicate) {
        return new DescribedPredicate<JavaParameter>("of type " + predicate.getDescription()) {
            @Override
            public boolean test(JavaParameter javaParameter) {
                return predicate.test(javaParameter.getRawType());
            }
        };
    }

    private static DescribedPredicate<JavaCodeUnit> haveParams(DescribedPredicate<? super JavaParameter> predicate) {
        return new DescribedPredicate<JavaCodeUnit>("have params " + predicate.getDescription()) {
            @Override
            public boolean test(JavaCodeUnit method) {
                return method.getParameters().stream().allMatch(predicate);
            }
        };
    }

}
