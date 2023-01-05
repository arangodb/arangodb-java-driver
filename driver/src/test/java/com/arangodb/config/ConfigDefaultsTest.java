package com.arangodb.config;

import jakarta.enterprise.inject.se.SeContainer;
import jakarta.enterprise.inject.se.SeContainerInitializer;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperties;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Disabled
class ConfigDefaultsTest {

    static class TestConfig {
        @Inject
        @ConfigProperties(prefix = "arangodb")
        ArangoConfigProperties arangodbConfig;
    }

    @Test
    void defaultValuesCDI() {
        try (SeContainer container = SeContainerInitializer.newInstance().initialize()) {
            ArangoConfigProperties config = container.select(TestConfig.class).get().arangodbConfig;
            assertThat(config.getHosts()).isEmpty();
            assertThat(config.getProtocol()).isEqualTo(ArangoConfigProperties.DEFAULT_PROTOCOL);
            assertThat(config.getUser()).isEqualTo(ArangoConfigProperties.DEFAULT_USER);
        }
    }

    @Test
    void defaultValuesProg() {
        ArangoConfigProperties config = new ArangoConfigProperties();
        assertThat(config.getHosts()).isEmpty();
        assertThat(config.getProtocol()).isEqualTo(ArangoConfigProperties.DEFAULT_PROTOCOL);
        assertThat(config.getUser()).isEqualTo(ArangoConfigProperties.DEFAULT_USER);
    }

}
