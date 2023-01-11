package com.arangodb.config;

import com.arangodb.Protocol;
import com.arangodb.entity.LoadBalancingStrategy;

import java.util.List;
import java.util.Optional;

public interface ArangoConfigProperties {

    default Optional<List<HostDescription>> getHosts() {
        return Optional.empty();
    }

    default Optional<Protocol> getProtocol() {
        return Optional.empty();
    }

    default Optional<String> getUser() {
        return Optional.empty();
    }

    default Optional<String> getPassword() {
        return Optional.empty();
    }

    default Optional<String> getJwt() {
        return Optional.empty();
    }

    default Optional<Integer> getTimeout() {
        return Optional.empty();
    }

    default Optional<Boolean> getUseSsl() {
        return Optional.empty();
    }

    default Optional<Boolean> getVerifyHost() {
        return Optional.empty();
    }

    default Optional<Integer> getChunkSize() {
        return Optional.empty();
    }

    default Optional<Integer> getMaxConnections() {
        return Optional.empty();
    }

    default Optional<Long> getConnectionTtl() {
        return Optional.empty();
    }

    default Optional<Integer> getKeepAliveInterval() {
        return Optional.empty();
    }

    default Optional<Boolean> getAcquireHostList() {
        return Optional.empty();
    }

    default Optional<Integer> getAcquireHostListInterval() {
        return Optional.empty();
    }

    default Optional<LoadBalancingStrategy> getLoadBalancingStrategy() {
        return Optional.empty();
    }

    default Optional<Integer> getResponseQueueTimeSamples() {
        return Optional.empty();
    }

}
