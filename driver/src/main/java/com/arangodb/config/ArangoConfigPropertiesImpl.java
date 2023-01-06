package com.arangodb.config;

import com.arangodb.Protocol;
import com.arangodb.entity.LoadBalancingStrategy;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class ArangoConfigPropertiesImpl implements ArangoConfigProperties {
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

    @Override
    public List<Host> getHosts() {
        return resolve(hosts, Collections.emptyList());
    }

    @Override
    public Protocol getProtocol() {
        return resolve(protocol, DEFAULT_PROTOCOL);
    }

    @Override
    public String getUser() {
        return resolve(user, DEFAULT_USER);
    }

    @Override
    public Optional<String> getPassword() {
        return resolve(password);
    }

    @Override
    public Optional<String> getJwt() {
        return resolve(jwt);
    }

    @Override
    public Integer getTimeout() {
        return resolve(timeout, DEFAULT_TIMEOUT);
    }

    @Override
    public Boolean getUseSsl() {
        return resolve(useSsl, DEFAULT_USE_SSL);
    }

    @Override
    public Boolean getVerifyHost() {
        return resolve(verifyHost, DEFAULT_VERIFY_HOST);
    }

    @Override
    public Integer getVstChunkSize() {
        return resolve(vstChunkSize, DEFAULT_CHUNK_SIZE);
    }

    @Override
    public Optional<Integer> getMaxConnections() {
        return resolve(maxConnections);
    }

    @Override
    public Optional<Long> getConnectionTtl() {
        return resolve(connectionTtl);
    }

    @Override
    public Optional<Integer> getKeepAliveInterval() {
        return resolve(keepAliveInterval);
    }

    @Override
    public Boolean getAcquireHostList() {
        return resolve(acquireHostList, DEFAULT_ACQUIRE_HOST_LIST);
    }

    @Override
    public Integer getAcquireHostListInterval() {
        return resolve(acquireHostListInterval, DEFAULT_ACQUIRE_HOST_LIST_INTERVAL);
    }

    @Override
    public LoadBalancingStrategy getLoadBalancingStrategy() {
        return resolve(loadBalancingStrategy, DEFAULT_LOAD_BALANCING_STRATEGY);
    }

    @Override
    public Integer getResponseQueueTimeSamples() {
        return resolve(responseQueueTimeSamples, DEFAULT_RESPONSE_QUEUE_TIME_SAMPLES);
    }

    private <T> T resolve(Optional<T> field, T defValue) {
        return field == null ? defValue : field.orElse(defValue);
    }

    private <T> Optional<T> resolve(Optional<T> field) {
        return field == null ? Optional.empty() : field;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ArangoConfigPropertiesImpl that = (ArangoConfigPropertiesImpl) o;
        return Objects.equals(hosts, that.hosts) && Objects.equals(protocol, that.protocol) && Objects.equals(user, that.user) && Objects.equals(password, that.password) && Objects.equals(jwt, that.jwt) && Objects.equals(timeout, that.timeout) && Objects.equals(useSsl, that.useSsl) && Objects.equals(verifyHost, that.verifyHost) && Objects.equals(vstChunkSize, that.vstChunkSize) && Objects.equals(maxConnections, that.maxConnections) && Objects.equals(connectionTtl, that.connectionTtl) && Objects.equals(keepAliveInterval, that.keepAliveInterval) && Objects.equals(acquireHostList, that.acquireHostList) && Objects.equals(acquireHostListInterval, that.acquireHostListInterval) && Objects.equals(loadBalancingStrategy, that.loadBalancingStrategy) && Objects.equals(responseQueueTimeSamples, that.responseQueueTimeSamples);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hosts, protocol, user, password, jwt, timeout, useSsl, verifyHost, vstChunkSize, maxConnections, connectionTtl, keepAliveInterval, acquireHostList, acquireHostListInterval, loadBalancingStrategy, responseQueueTimeSamples);
    }

    @Override
    public String toString() {
        return "ArangoConfigPropertiesImpl{" +
                "hosts=" + hosts +
                ", protocol=" + protocol +
                ", user=" + user +
                ", password=<redacted>" +
                ", jwt=<redacted>" +
                ", timeout=" + timeout +
                ", useSsl=" + useSsl +
                ", verifyHost=" + verifyHost +
                ", vstChunkSize=" + vstChunkSize +
                ", maxConnections=" + maxConnections +
                ", connectionTtl=" + connectionTtl +
                ", keepAliveInterval=" + keepAliveInterval +
                ", acquireHostList=" + acquireHostList +
                ", acquireHostListInterval=" + acquireHostListInterval +
                ", loadBalancingStrategy=" + loadBalancingStrategy +
                ", responseQueueTimeSamples=" + responseQueueTimeSamples +
                '}';
    }
}
