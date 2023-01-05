package com.arangodb.config;

import com.arangodb.Protocol;
import jakarta.enterprise.inject.se.SeContainer;
import jakarta.enterprise.inject.se.SeContainerInitializer;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Disabled
class ConfigTest {

    static class TestConfig {
        @Inject
        @ConfigProperties(prefix = "arangodb")
        ArangoConfigProperties arangodbConfig;
    }

    @AfterEach
    void clear() {
        System.clearProperty("mp.config.profile");
    }

    @Test
    void progConfig() {
        Host hostA = new Host("aaa", 1111);
        Host hostB = new Host("bbb", 2222);
        String user = "testUser";
        ArangoConfigProperties config = new ArangoConfigProperties()
                .host(hostA, hostB)
                .protocol(Protocol.HTTP_VPACK)
                .user(user);
        assertThat(config.getHosts()).containsExactly(hostA, hostB);
        assertThat(config.getProtocol()).isEqualTo(Protocol.HTTP_VPACK);
        assertThat(config.getUser()).isEqualTo(user);
    }

    @Test
    void readConfigCDI() {
        // read config from META-INF/microprofile-config-configTest.properties
        System.setProperty("mp.config.profile", "configTest");
        System.out.println(System.getProperty("mp.config.profile"));

        Host hostA = new Host("aaa", 1111);
        Host hostB = new Host("bbb", 2222);
        String user = "testUser";
        try (SeContainer container = SeContainerInitializer.newInstance().initialize()) {
            ArangoConfigProperties config = container.select(TestConfig.class).get().arangodbConfig;
            assertThat(config.getHosts()).containsExactly(hostA, hostB);
            assertThat(config.getProtocol()).isEqualTo(Protocol.HTTP_VPACK);
            assertThat(config.getUser()).isEqualTo(user);
        }
    }

}
