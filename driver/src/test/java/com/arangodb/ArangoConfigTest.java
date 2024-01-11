package com.arangodb;

import com.arangodb.internal.ArangoDefaults;
import com.arangodb.internal.config.ArangoConfig;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ArangoConfigTest {
    @Test
    void defaultValues() {
        ArangoConfig cfg = new ArangoConfig();
        assertThat(cfg.getHosts()).isEqualTo(ArangoDefaults.DEFAULT_HOSTS);
        assertThat(cfg.getProtocol()).isEqualTo(Protocol.HTTP2_JSON);
        assertThat(cfg.getTimeout()).isEqualTo(ArangoDefaults.DEFAULT_TIMEOUT);
        assertThat(cfg.getUser()).isEqualTo(ArangoDefaults.DEFAULT_USER);
        assertThat(cfg.getPassword()).isNull();
        assertThat(cfg.getJwt()).isNull();
        assertThat(cfg.getUseSsl()).isEqualTo(ArangoDefaults.DEFAULT_USE_SSL);
        assertThat(cfg.getSslContext()).isNull();
        assertThat(cfg.getVerifyHost()).isEqualTo(ArangoDefaults.DEFAULT_VERIFY_HOST);
        assertThat(cfg.getChunkSize()).isEqualTo(ArangoDefaults.DEFAULT_CHUNK_SIZE);
        assertThat(cfg.getMaxConnections()).isEqualTo(ArangoDefaults.MAX_CONNECTIONS_HTTP2_DEFAULT);
        assertThat(cfg.getConnectionTtl()).isNull();
        assertThat(cfg.getKeepAliveInterval()).isNull();
        assertThat(cfg.getAcquireHostList()).isEqualTo(ArangoDefaults.DEFAULT_ACQUIRE_HOST_LIST);
        assertThat(cfg.getAcquireHostListInterval()).isEqualTo(ArangoDefaults.DEFAULT_ACQUIRE_HOST_LIST_INTERVAL);
        assertThat(cfg.getLoadBalancingStrategy()).isEqualTo(ArangoDefaults.DEFAULT_LOAD_BALANCING_STRATEGY);
        assertThat(cfg.getResponseQueueTimeSamples()).isEqualTo(ArangoDefaults.DEFAULT_RESPONSE_QUEUE_TIME_SAMPLES);
        assertThat(cfg.getAsyncExecutor()).isNull();
        assertThat(cfg.getCompression()).isEqualTo(ArangoDefaults.DEFAULT_COMPRESSION);
        assertThat(cfg.getCompressionThreshold()).isEqualTo(ArangoDefaults.DEFAULT_COMPRESSION_THRESHOLD);
        assertThat(cfg.getCompressionLevel()).isEqualTo(ArangoDefaults.DEFAULT_COMPRESSION_LEVEL);
    }
}
