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
    private Host hostA = new Host("aaa", 1111);
    private Host hostB = new Host("bbb", 2222);
    private Protocol protocol = Protocol.HTTP_VPACK;
    private String user = "testUser";
    private String password = "testPassword";
    private String jwt = "testJwt";
    private Boolean useSsl = true;


    static class TestConfig {
        @Inject
        @ConfigProperties(prefix = "adb")
        ArangoConfigProperties arangodbConfig;
    }

    @AfterEach
    void clear() {
        System.clearProperty("mp.config.profile");
    }

    @Test
    void progConfig() {
        ArangoConfigProperties config = new ArangoConfigProperties()
                .host(hostA, hostB)
                .protocol(protocol)
                .user(user)
                .password(password)
                .jwt(jwt)
                .useSsl(useSsl);
        checkResult(config);
    }

    @Test
    void readConfigCDI() {
        // read config from META-INF/microprofile-config-configTest.properties
        System.setProperty("mp.config.profile", "configTest");
        System.out.println(System.getProperty("mp.config.profile"));

        try (SeContainer container = SeContainerInitializer.newInstance().initialize()) {
            ArangoConfigProperties config = container.select(TestConfig.class).get().arangodbConfig;
            checkResult(config);
        }
    }

    private void checkResult(ArangoConfigProperties config) {
        assertThat(config.getHosts()).containsExactly(hostA, hostB);
        assertThat(config.getProtocol()).isEqualTo(protocol);
        assertThat(config.getUser()).isEqualTo(user);
        assertThat(config.getPassword())
                .isPresent()
                .hasValue(password);
        assertThat(config.getJwt())
                .isPresent()
                .hasValue(jwt);
        assertThat(config.getUseSsl()).isEqualTo(useSsl);
    }
}
