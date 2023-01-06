package com.arangodb.config;

import io.smallrye.config.SmallRyeConfig;
import io.smallrye.config.SmallRyeConfigBuilder;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ConfigDefaultsTest {

    @Test
    void defaultValues() {
        SmallRyeConfig cfg = new SmallRyeConfigBuilder()
                .addDefaultSources()
                .withMapping(ArangoConfigProperties.class, "arangodb")
                .build();

        ArangoConfigProperties config = cfg.getConfigMapping(ArangoConfigProperties.class, "arangodb");
        checkResult(config);
    }

    private void checkResult(ArangoConfigProperties config) {
        assertThat(config.getHosts()).isEmpty();
        assertThat(config.getProtocol()).isEqualTo(ArangoConfigProperties.DEFAULT_PROTOCOL);
        assertThat(config.getUser()).isEqualTo(ArangoConfigProperties.DEFAULT_USER);
        assertThat(config.getPassword()).isNotPresent();
        assertThat(config.getJwt()).isNotPresent();
        assertThat(config.getTimeout()).isEqualTo(ArangoConfigProperties.DEFAULT_TIMEOUT);
        assertThat(config.getUseSsl()).isEqualTo(ArangoConfigProperties.DEFAULT_USE_SSL);
        assertThat(config.getVerifyHost()).isEqualTo(ArangoConfigProperties.DEFAULT_VERIFY_HOST);
        assertThat(config.getVstChunkSize()).isEqualTo(ArangoConfigProperties.DEFAULT_CHUNK_SIZE);
        assertThat(config.getMaxConnections()).isNotPresent();
        assertThat(config.getConnectionTtl()).isNotPresent();
        assertThat(config.getKeepAliveInterval()).isNotPresent();
        assertThat(config.getAcquireHostList()).isEqualTo(ArangoConfigProperties.DEFAULT_ACQUIRE_HOST_LIST);
        assertThat(config.getAcquireHostListInterval()).isEqualTo(ArangoConfigProperties.DEFAULT_ACQUIRE_HOST_LIST_INTERVAL);
        assertThat(config.getLoadBalancingStrategy()).isEqualTo(ArangoConfigProperties.DEFAULT_LOAD_BALANCING_STRATEGY);
        assertThat(config.getResponseQueueTimeSamples()).isEqualTo(ArangoConfigProperties.DEFAULT_RESPONSE_QUEUE_TIME_SAMPLES);
    }

}
