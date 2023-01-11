package com.arangodb.config;

import com.arangodb.Protocol;
import com.arangodb.entity.LoadBalancingStrategy;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.graalvm.nativeimage.ImageInfo;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

class ConfigTest {
    private final HostDescription hostA = new HostDescription("aaa", 1111);
    private final HostDescription hostB = new HostDescription("bbb", 2222);
    private final Protocol protocol = Protocol.HTTP_VPACK;
    private final String user = "testUser";
    private final String password = "testPassword";
    private final String jwt = "testJwt";
    private final Integer timeout = 9876;
    private final Boolean useSsl = true;
    private final Boolean verifyHost = false;
    private final Integer vstChunkSize = 1234;
    private final Integer maxConnections = 123;
    private final Long connectionTtl = 12345L;
    private final Integer keepAliveInterval = 123456;
    private final Boolean acquireHostList = true;
    private final Integer acquireHostListInterval = 1234567;
    private final LoadBalancingStrategy loadBalancingStrategy = LoadBalancingStrategy.ROUND_ROBIN;
    private final Integer responseQueueTimeSamples = 12345678;

    @BeforeAll
    static void beforeClass() {
        assumeFalse(ImageInfo.inImageCode(), "skipped in native mode");
    }

    @Test
    void readConfig() {
        ArangoConfigProperties config = ConfigUtils.loadConfigMP("arangodb-config-test.properties", "adb");
        checkResult(config);
    }

    private void checkResult(ArangoConfigProperties config) {
        assertThat(config.getHosts())
                .isPresent()
                .get(InstanceOfAssertFactories.LIST)
                .containsExactly(hostA, hostB);
        assertThat(config.getProtocol()).hasValue(protocol);
        assertThat(config.getUser()).hasValue(user);
        assertThat(config.getPassword())
                .isPresent()
                .hasValue(password);
        assertThat(config.getJwt())
                .isPresent()
                .hasValue(jwt);
        assertThat(config.getTimeout()).hasValue(timeout);
        assertThat(config.getUseSsl()).hasValue(useSsl);
        assertThat(config.getVerifyHost()).hasValue(verifyHost);
        assertThat(config.getChunkSize()).hasValue(vstChunkSize);
        assertThat(config.getMaxConnections())
                .isPresent()
                .hasValue(maxConnections);
        assertThat(config.getConnectionTtl())
                .isPresent()
                .hasValue(connectionTtl);
        assertThat(config.getKeepAliveInterval())
                .isPresent()
                .hasValue(keepAliveInterval);
        assertThat(config.getAcquireHostList()).hasValue(acquireHostList);
        assertThat(config.getAcquireHostListInterval()).hasValue(acquireHostListInterval);
        assertThat(config.getLoadBalancingStrategy()).hasValue(loadBalancingStrategy);
        assertThat(config.getResponseQueueTimeSamples()).hasValue(responseQueueTimeSamples);
    }
}
