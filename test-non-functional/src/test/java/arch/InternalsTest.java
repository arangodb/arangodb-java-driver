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

import static arch.ArchUtils.*;
import static com.tngtech.archunit.base.DescribedPredicate.*;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.assignableTo;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.core.domain.properties.CanBeAnnotated.Predicates.annotatedWith;
import static com.tngtech.archunit.lang.conditions.ArchConditions.be;
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
                            "com.arangodb.velocypack..",
                            "com.arangodb.shaded..")
                    )
            );

    /**
     * Tests whether the type and all its raw generic types do not extend or implement internal classes
     */
    private static final DescribedPredicate<JavaType> typePredicate =
            JavaTypeExt.rawTypes(not(assignableTo(resideInAPackage("..internal.."))));

    /**
     * Superclasses of types used in public API must either:
     * - not reside in internal packages, or
     * - be annotated with {@link UsedInApi}
     */
    private static final DescribedPredicate<JavaType> superclassesPredicate =
            JavaTypeExt.rawTypes(superclasses(or(
                    not(resideInAPackage("..internal..")),
                    annotatedWith(UsedInApi.class)
            )));

    /**
     * Classes in the public API must either:
     * - not extend or implement internal classes, or
     * - be annotated with {@link UnstableApi} and fulfil {@link #superclassesPredicate}
     */
    private static final DescribedPredicate<JavaClass> classPredicate =
            or(
                    typePredicate,
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
                    HasTypeExt.type(typePredicate),
                    and(
                            annotatedWith(UnstableApi.class),
                            HasTypeExt.type(superclassesPredicate)
                    )
            );

    /**
     * Methods in the public API must either:
     * - have return type that not extends or implement internal classes, or
     * - be annotated with {@link UnstableApi} and have return type that fulfils {@link #superclassesPredicate}
     */
    private static final DescribedPredicate<JavaMethod> methodReturnTypePredicate =
            or(
                    HasReturnTypeExt.returnType(typePredicate),
                    and(
                            annotatedWith(UnstableApi.class),
                            HasReturnTypeExt.returnType(superclassesPredicate)
                    )
            );

    /**
     * Parameters of methods in the public API must either:
     * - have type that not resides in internal classes, or
     * - be annotated with {@link UnstableApi} and have type annotated with {@link UsedInApi}
     */
    private static final DescribedPredicate<JavaCodeUnit> paramPredicate = haveParams(or(
            HasTypeExt.rawTypes(not(resideInAPackage("..internal.."))),
            and(
                    annotatedWith(UnstableApi.class),
                    HasTypeExt.rawTypes(or(
                            not(resideInAPackage("..internal..")),
                            annotatedWith(UsedInApi.class)
                    ))
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

    private static DescribedPredicate<JavaClass> superclasses(DescribedPredicate<? super JavaClass> predicate) {
        return new DescribedPredicate<>("superclasses " + predicate.getDescription()) {
            @Override
            public boolean test(JavaClass clazz) {
                return Stream.of(
                                Stream.of(clazz),
                                clazz.getAllRawSuperclasses().stream(),
                                clazz.getAllRawInterfaces().stream()
                        )
                        .flatMap(Function.identity())
                        .allMatch(predicate);
            }
        };
    }

    private static DescribedPredicate<JavaCodeUnit> haveParams(DescribedPredicate<? super JavaParameter> predicate) {
        return new DescribedPredicate<>("have params " + predicate.getDescription()) {
            @Override
            public boolean test(JavaCodeUnit method) {
                return method.getParameters().stream().allMatch(predicate);
            }
        };
    }

}
