package com.arangodb.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledInNativeImage;

import static org.assertj.core.api.Assertions.assertThat;

@DisabledInNativeImage
class ConfigDefaultsTest {

    @Test
    void defaultValues() {
        ArangoConfigProperties config = ConfigUtils.loadConfigMP("arangodb.properties", "wrong");
        checkResult(config);
    }

    private void checkResult(ArangoConfigProperties config) {
        assertThat(config.getHosts()).isEmpty();
        assertThat(config.getProtocol()).isEmpty();
        assertThat(config.getUser()).isEmpty();
        assertThat(config.getPassword()).isNotPresent();
        assertThat(config.getJwt()).isNotPresent();
        assertThat(config.getTimeout()).isEmpty();
        assertThat(config.getUseSsl()).isEmpty();
        assertThat(config.getVerifyHost()).isEmpty();
        assertThat(config.getChunkSize()).isEmpty();
        assertThat(config.getMaxConnections()).isNotPresent();
        assertThat(config.getConnectionTtl()).isNotPresent();
        assertThat(config.getKeepAliveInterval()).isNotPresent();
        assertThat(config.getAcquireHostList()).isEmpty();
        assertThat(config.getAcquireHostListInterval()).isEmpty();
        assertThat(config.getLoadBalancingStrategy()).isEmpty();
        assertThat(config.getResponseQueueTimeSamples()).isEmpty();
    }

}
