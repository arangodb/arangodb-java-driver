package com.arangodb.config;

import com.arangodb.Protocol;
import com.arangodb.entity.LoadBalancingStrategy;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public final class ArangoConfigProperties {

    // default values
    public static final Protocol DEFAULT_PROTOCOL = Protocol.HTTP2_JSON;
    public static final String DEFAULT_USER = "root";
    public static final Integer DEFAULT_TIMEOUT = 0;
    public static final Boolean DEFAULT_USE_SSL = false;
    public static final Boolean DEFAULT_VERIFY_HOST = true;
    public static final Integer DEFAULT_CHUNK_SIZE = 30_000;
    public static final Boolean DEFAULT_ACQUIRE_HOST_LIST = false;
    public static final Integer DEFAULT_ACQUIRE_HOST_LIST_INTERVAL = 60 * 60 * 1000; // hour
    public static final LoadBalancingStrategy DEFAULT_LOAD_BALANCING_STRATEGY = LoadBalancingStrategy.NONE;
    public static final Integer DEFAULT_RESPONSE_QUEUE_TIME_SAMPLES = 10;

    private Optional<List<Host>> hosts;
    private Optional<Protocol> protocol;
    private Optional<String> user;
    private Optional<String> password;
    private Optional<String> jwt;
    private Optional<Integer> timeout;
    private Optional<Boolean> useSsl;
    private Optional<Boolean> verifyHost;
    private Optional<Integer> vstChunkSize;
    private Optional<Integer> maxConnections;
    private Optional<Long> connectionTtl;
    private Optional<Integer> keepAliveInterval;
    private Optional<Boolean> acquireHostList;
    private Optional<Integer> acquireHostListInterval;
    private Optional<LoadBalancingStrategy> loadBalancingStrategy;
    private Optional<Integer> responseQueueTimeSamples;

    public List<Host> getHosts() {
        return resolve(hosts, Collections.emptyList());
    }

    public Protocol getProtocol() {
        return resolve(protocol, DEFAULT_PROTOCOL);
    }

    public String getUser() {
        return resolve(user, DEFAULT_USER);
    }

    public Optional<String> getPassword() {
        return resolve(password);
    }

    public Optional<String> getJwt() {
        return resolve(jwt);
    }

    public Integer getTimeout() {
        return resolve(timeout, DEFAULT_TIMEOUT);
    }

    public Boolean getUseSsl() {
        return resolve(useSsl, DEFAULT_USE_SSL);
    }

    public Boolean getVerifyHost() {
        return resolve(verifyHost, DEFAULT_VERIFY_HOST);
    }

    public Integer getVstChunkSize() {
        return resolve(vstChunkSize, DEFAULT_CHUNK_SIZE);
    }

    public Optional<Integer> getMaxConnections() {
        return resolve(maxConnections);
    }

    public Optional<Long> getConnectionTtl() {
        return resolve(connectionTtl);
    }

    public Optional<Integer> getKeepAliveInterval() {
        return resolve(keepAliveInterval);
    }

    public Boolean getAcquireHostList() {
        return resolve(acquireHostList, DEFAULT_ACQUIRE_HOST_LIST);
    }

    public Integer getAcquireHostListInterval() {
        return resolve(acquireHostListInterval, DEFAULT_ACQUIRE_HOST_LIST_INTERVAL);
    }

    public LoadBalancingStrategy getLoadBalancingStrategy() {
        return resolve(loadBalancingStrategy, DEFAULT_LOAD_BALANCING_STRATEGY);
    }

    public Integer getResponseQueueTimeSamples() {
        return resolve(responseQueueTimeSamples, DEFAULT_RESPONSE_QUEUE_TIME_SAMPLES);
    }

    private <T> T resolve(Optional<T> field, T defValue) {
        return field == null ? defValue : field.orElse(defValue);
    }

    private <T> Optional<T> resolve(Optional<T> field) {
        return field == null ? Optional.empty() : field;
    }
}
