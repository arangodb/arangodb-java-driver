package com.arangodb;

import org.junit.jupiter.api.Test;
import ru.lanwen.verbalregex.VerbalExpression;

import static org.assertj.core.api.Assertions.assertThat;

class PackageVersionTest {

    @Test
    void packageVersion() {
        VerbalExpression testRegex = VerbalExpression.regex()
                .startOfLine()
                // major
                .digit().atLeast(1)
                .then(".")
                // minor
                .digit().atLeast(1)
                .then(".")
                // patch
                .digit().atLeast(1)
                .maybe("-SNAPSHOT")
                .endOfLine()
                .build();
        assertThat(PackageVersion.VERSION).matches(testRegex.toString());
    }
}
