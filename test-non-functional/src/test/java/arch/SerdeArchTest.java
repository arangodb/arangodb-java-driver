package arch;

import com.tngtech.archunit.core.importer.ImportOption.DoNotIncludeTests;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;


@AnalyzeClasses(packages = "com.arangodb..", importOptions = {DoNotIncludeTests.class})
public class SerdeArchTest {

    @ArchTest
    public static final ArchRule noDependencyOnJsonbSerde = noClasses().that()
            .resideInAPackage("com.arangodb..").and()
            .resideOutsideOfPackage("com.arangodb.serde.jsonb..")
            .should().dependOnClassesThat()
            .resideInAPackage("com.arangodb.serde.jsonb..");

    @ArchTest
    public static final ArchRule noDependencyOnJacksonDataformatVelocypack = noClasses().that()
            .resideInAPackage("com.arangodb..").and()
            .resideOutsideOfPackage("com.arangodb.jackson.dataformat.velocypack..").and()
            .resideOutsideOfPackage("com.arangodb.serde.jackson..")
            .should().dependOnClassesThat()
            .resideInAPackage("com.arangodb.jackson.dataformat.velocypack..");

    @ArchTest
    public static final ArchRule noDependencyOnJsonB = noClasses().that()
            .resideInAPackage("com.arangodb..")
            .should().dependOnClassesThat()
            .resideInAPackage("com.arangodb.serde.jsonb..");

    @ArchTest
    public static final ArchRule noDependencyOnJacksonSerde = noClasses().that()
            .resideInAPackage("com.arangodb..").and()
            .resideOutsideOfPackage("com.arangodb.serde.jackson..")
            .should().dependOnClassesThat()
            .resideInAPackage("com.arangodb.serde.jackson..");

}
