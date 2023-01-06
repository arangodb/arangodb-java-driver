package com.arangodb.config;

import com.arangodb.Protocol;
import com.arangodb.entity.LoadBalancingStrategy;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ConfigTest {
    private Host hostA = new Host("aaa", 1111);
    private Host hostB = new Host("bbb", 2222);
    private Protocol protocol = Protocol.HTTP_VPACK;
    private String user = "testUser";
    private String password = "testPassword";
    private String jwt = "testJwt";
    private Integer timeout = 9876;
    private Boolean useSsl = true;
    private Boolean verifyHost = false;
    private Integer vstChunkSize = 1234;
    private Integer maxConnections = 123;
    private Long connectionTtl = 12345L;
    private Integer keepAliveInterval = 123456;
    private Boolean acquireHostList = true;
    private Integer acquireHostListInterval = 1234567;
    private LoadBalancingStrategy loadBalancingStrategy = LoadBalancingStrategy.ROUND_ROBIN;
    private Integer responseQueueTimeSamples = 12345678;

    @Test
    void readConfig() {
        ArangoConfigProperties config = ConfigUtils.loadConfig("arangodb-config-test.properties", "adb");
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
        assertThat(config.getTimeout()).isEqualTo(timeout);
        assertThat(config.getUseSsl()).isEqualTo(useSsl);
        assertThat(config.getVerifyHost()).isEqualTo(verifyHost);
        assertThat(config.getVstChunkSize()).isEqualTo(vstChunkSize);
        assertThat(config.getMaxConnections())
                .isPresent()
                .hasValue(maxConnections);
        assertThat(config.getConnectionTtl())
                .isPresent()
                .hasValue(connectionTtl);
        assertThat(config.getKeepAliveInterval())
                .isPresent()
                .hasValue(keepAliveInterval);
        assertThat(config.getAcquireHostList()).isEqualTo(acquireHostList);
        assertThat(config.getAcquireHostListInterval()).isEqualTo(acquireHostListInterval);
        assertThat(config.getLoadBalancingStrategy()).isEqualTo(loadBalancingStrategy);
        assertThat(config.getResponseQueueTimeSamples()).isEqualTo(responseQueueTimeSamples);
    }
}
