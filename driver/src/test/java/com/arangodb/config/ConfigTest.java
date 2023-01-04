package com.arangodb.config;

import jakarta.enterprise.inject.se.SeContainer;
import jakarta.enterprise.inject.se.SeContainerInitializer;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperties;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ConfigTest {

    static class TestConfig {
        @Inject
        @ConfigProperties(prefix = "arangodb")
        ArangoConfigProperties arangodbConfig;
    }

    @BeforeAll
    static void setup() {
        // read config from META-INF/microprofile-config-configTest.properties
        System.setProperty("mp.config.profile", "configTest");
    }

    @AfterAll
    static void clear() {
        System.clearProperty("mp.config.profile");
    }

    @Test
    void progConfig() {
        Host hostA = new Host("aaa", 1111);
        Host hostB = new Host("bbb", 2222);
        ArangoConfigProperties config = new ArangoConfigProperties()
                .host(hostA, hostB);
        assertThat(config.getHosts()).containsExactly(hostA, hostB);
    }

    @Test
    void readConfigCDI() {
        try (SeContainer container = SeContainerInitializer.newInstance().initialize()) {
            ArangoConfigProperties config = container.select(TestConfig.class).get().arangodbConfig;
            assertThat(config.getHosts()).containsExactly(
                    new Host("aaa", 1111),
                    new Host("bbb", 2222)
            );
        }
    }

}
