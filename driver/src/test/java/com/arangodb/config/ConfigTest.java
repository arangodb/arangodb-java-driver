package com.arangodb.config;

import com.arangodb.Protocol;
import io.smallrye.config.SmallRyeConfig;
import io.smallrye.config.SmallRyeConfigBuilder;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ConfigTest {
    private Host hostA = new Host("aaa", 1111);
    private Host hostB = new Host("bbb", 2222);
    private Protocol protocol = Protocol.HTTP_VPACK;
    private String user = "testUser";
    private String password = "testPassword";
    private String jwt = "testJwt";
    private Boolean useSsl = true;


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
    void readConfig() {
        SmallRyeConfig cfg = new SmallRyeConfigBuilder()
                .addDefaultSources()
                .withMapping(ArangoConfigProperties.class, "adb")
                .withProfile("configTest")
                .build();

        ArangoConfigProperties config = cfg.getConfigMapping(ArangoConfigProperties.class, "adb");
        checkResult(config);
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
