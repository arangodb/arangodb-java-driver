package arch;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.core.importer.ImportOption.Predefined.DO_NOT_INCLUDE_TESTS;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static org.junit.jupiter.api.Assumptions.assumeTrue;


public class ShadedArchTest {
    private final JavaClasses importedClasses = new ClassFileImporter()
            .withImportOption(DO_NOT_INCLUDE_TESTS)
            .importPackages("com.arangodb..");

    private final boolean shaded = isShaded();

    private static boolean isShaded() {
        boolean shaded;
        try {
            Class.forName("com.arangodb.shaded.fasterxml.jackson.databind.JsonNode");
            shaded = true;
        } catch (ClassNotFoundException e) {
            shaded = false;
        }
        return shaded;
    }

    @BeforeEach
    void checkShaded() {
        assumeTrue(shaded, "not shaded driver");
    }

    @Test
    public void nettyRelocation() {
        noClasses().that()
                .resideInAPackage("com.arangodb..")
                .should().dependOnClassesThat()
                .resideInAPackage("io.netty..")
                .check(importedClasses);
    }

    @Test
    public void vertxRelocation() {
        noClasses().that()
                .resideInAPackage("com.arangodb..")
                .should().dependOnClassesThat()
                .resideInAPackage("io.vertx..")
                .check(importedClasses);
    }

    @Test
    public void jacksonRelocation() {
        noClasses().that()
                .resideInAPackage("com.arangodb..").and()
                .resideOutsideOfPackage("com.arangodb.jackson.dataformat.velocypack..").and()
                .resideOutsideOfPackage("com.arangodb.serde.jackson..")
                .should().dependOnClassesThat()
                .resideInAPackage("com.fasterxml.jackson..")
                .check(importedClasses);
    }

    @Test
    public void noJacksonDependency() {
        noClasses().that()
                .resideInAPackage("com.arangodb..").and()
                .resideOutsideOfPackages(
                        "com.arangodb.jackson.dataformat.velocypack..",
                        "com.arangodb.serde.jackson..")
                .should().dependOnClassesThat()
                .resideInAPackage("com.fasterxml.jackson..")
                .check(importedClasses);
    }

    @Test
    public void noJacksonDataformatVelocypackDependency() {
        noClasses().that()
                .resideInAPackage("com.arangodb..").and()
                .resideOutsideOfPackage("com.arangodb.jackson.dataformat.velocypack..")
                .should().dependOnClassesThat()
                .resideInAPackage("com.arangodb.jackson.dataformat.velocypack..")
                .check(importedClasses);
    }

}
