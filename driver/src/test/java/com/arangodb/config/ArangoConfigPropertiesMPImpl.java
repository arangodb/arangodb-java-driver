package com.arangodb.config;

import com.arangodb.Protocol;
import com.arangodb.entity.LoadBalancingStrategy;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Implementation of ArangoConfigProperties compatible with MicroProfile Config.
 */
public final class ArangoConfigPropertiesMPImpl implements ArangoConfigProperties {
    private Optional<List<HostDescription>> hosts;
    private Optional<Protocol> protocol;
    private Optional<String> user;
    private Optional<String> password;
    private Optional<String> jwt;
    private Optional<Integer> timeout;
    private Optional<Boolean> useSsl;
    private Optional<Boolean> verifyHost;
    private Optional<Integer> chunkSize;
    private Optional<Integer> maxConnections;
    private Optional<Long> connectionTtl;
    private Optional<Integer> keepAliveInterval;
    private Optional<Boolean> acquireHostList;
    private Optional<Integer> acquireHostListInterval;
    private Optional<LoadBalancingStrategy> loadBalancingStrategy;
    private Optional<Integer> responseQueueTimeSamples;

    @Override
    public Optional<List<HostDescription>> getHosts() {
        return hosts;
    }

    @Override
    public Optional<Protocol> getProtocol() {
        return protocol;
    }

    @Override
    public Optional<String> getUser() {
        return user;
    }

    @Override
    public Optional<String> getPassword() {
        return password;
    }

    @Override
    public Optional<String> getJwt() {
        return jwt;
    }

    @Override
    public Optional<Integer> getTimeout() {
        return timeout;
    }

    @Override
    public Optional<Boolean> getUseSsl() {
        return useSsl;
    }

    @Override
    public Optional<Boolean> getVerifyHost() {
        return verifyHost;
    }

    @Override
    public Optional<Integer> getChunkSize() {
        return chunkSize;
    }

    @Override
    public Optional<Integer> getMaxConnections() {
        return maxConnections;
    }

    @Override
    public Optional<Long> getConnectionTtl() {
        return connectionTtl;
    }

    @Override
    public Optional<Integer> getKeepAliveInterval() {
        return keepAliveInterval;
    }

    @Override
    public Optional<Boolean> getAcquireHostList() {
        return acquireHostList;
    }

    @Override
    public Optional<Integer> getAcquireHostListInterval() {
        return acquireHostListInterval;
    }

    @Override
    public Optional<LoadBalancingStrategy> getLoadBalancingStrategy() {
        return loadBalancingStrategy;
    }

    @Override
    public Optional<Integer> getResponseQueueTimeSamples() {
        return responseQueueTimeSamples;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ArangoConfigPropertiesMPImpl that = (ArangoConfigPropertiesMPImpl) o;
        return Objects.equals(hosts, that.hosts) && Objects.equals(protocol, that.protocol) && Objects.equals(user, that.user) && Objects.equals(password, that.password) && Objects.equals(jwt, that.jwt) && Objects.equals(timeout, that.timeout) && Objects.equals(useSsl, that.useSsl) && Objects.equals(verifyHost, that.verifyHost) && Objects.equals(chunkSize, that.chunkSize) && Objects.equals(maxConnections, that.maxConnections) && Objects.equals(connectionTtl, that.connectionTtl) && Objects.equals(keepAliveInterval, that.keepAliveInterval) && Objects.equals(acquireHostList, that.acquireHostList) && Objects.equals(acquireHostListInterval, that.acquireHostListInterval) && Objects.equals(loadBalancingStrategy, that.loadBalancingStrategy) && Objects.equals(responseQueueTimeSamples, that.responseQueueTimeSamples);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hosts, protocol, user, password, jwt, timeout, useSsl, verifyHost, chunkSize, maxConnections, connectionTtl, keepAliveInterval, acquireHostList, acquireHostListInterval, loadBalancingStrategy, responseQueueTimeSamples);
    }

    @Override
    public String toString() {
        return "ArangoConfigPropertiesImpl{" +
                "hosts=" + hosts +
                ", protocol=" + protocol +
                ", user=" + user +
                ", password=" + password +
                ", jwt=" + jwt +
                ", timeout=" + timeout +
                ", useSsl=" + useSsl +
                ", verifyHost=" + verifyHost +
                ", chunkSize=" + chunkSize +
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
