package com.arangodb.config;

import com.arangodb.Protocol;
import com.arangodb.entity.LoadBalancingStrategy;

import java.util.List;
import java.util.Optional;

public interface ArangoConfigProperties {

    // default values
    Protocol DEFAULT_PROTOCOL = Protocol.HTTP2_JSON;
    String DEFAULT_USER = "root";
    Integer DEFAULT_TIMEOUT = 0;
    Boolean DEFAULT_USE_SSL = false;
    Boolean DEFAULT_VERIFY_HOST = true;
    Integer DEFAULT_CHUNK_SIZE = 30_000;
    Boolean DEFAULT_ACQUIRE_HOST_LIST = false;
    Integer DEFAULT_ACQUIRE_HOST_LIST_INTERVAL = 60 * 60 * 1000; // hour
    LoadBalancingStrategy DEFAULT_LOAD_BALANCING_STRATEGY = LoadBalancingStrategy.NONE;
    Integer DEFAULT_RESPONSE_QUEUE_TIME_SAMPLES = 10;

    List<Host> getHosts();

    Protocol getProtocol();

    String getUser();

    Optional<String> getPassword();

    Optional<String> getJwt();

    Integer getTimeout();

    Boolean getUseSsl();

    Boolean getVerifyHost();

    Integer getVstChunkSize();

    Optional<Integer> getMaxConnections();

    Optional<Long> getConnectionTtl();

    Optional<Integer> getKeepAliveInterval();

    Boolean getAcquireHostList();

    Integer getAcquireHostListInterval();

    LoadBalancingStrategy getLoadBalancingStrategy();

    Integer getResponseQueueTimeSamples();

}
