package arch;

import com.tngtech.archunit.core.importer.ImportOption.DoNotIncludeTests;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;


@AnalyzeClasses(packages = "com.arangodb..", importOptions = {DoNotIncludeTests.class})
public class RelocationsTest {

    @ArchTest
    public static final ArchRule nettyRelocation = noClasses().that()
            .resideInAPackage("com.arangodb..")
            .should().dependOnClassesThat()
            .resideInAPackage("io.netty..");

    @ArchTest
    public static final ArchRule vertxRelocation = noClasses().that()
            .resideInAPackage("com.arangodb..")
            .should().dependOnClassesThat()
            .resideInAPackage("io.vertx..");

    @ArchTest
    public static final ArchRule jacksonRelocation = noClasses().that()
            .resideInAPackage("com.arangodb..").and()
            .resideOutsideOfPackage("com.arangodb.jackson.dataformat.velocypack..").and()
            .resideOutsideOfPackage("com.arangodb.serde.jackson..")
            .should().dependOnClassesThat()
            .resideInAPackage("com.fasterxml.jackson..");

    @ArchTest
    public static final ArchRule jacksonDataformatVelocypackRelocation = noClasses().that()
            .resideInAPackage("com.arangodb..").and()
            .resideOutsideOfPackage("com.arangodb.jackson.dataformat.velocypack..").and()
            .resideOutsideOfPackage("com.arangodb.serde.jackson..")
            .should().dependOnClassesThat()
            .resideInAPackage("com.arangodb.jackson.dataformat.velocypack..");

    @ArchTest
    public static final ArchRule noJsonbDependency = noClasses().that()
            .resideInAPackage("com.arangodb..")
            .should().dependOnClassesThat()
            .resideInAPackage("com.arangodb.serde.jsonb..");

}
