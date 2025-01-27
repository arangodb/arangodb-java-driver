package com.arangodb.config;

import com.arangodb.Compression;
import com.arangodb.Protocol;
import com.arangodb.entity.LoadBalancingStrategy;
import com.arangodb.internal.config.ArangoConfigPropertiesImpl;

import java.util.List;
import java.util.Optional;
import java.util.Properties;

public interface ArangoConfigProperties {

    /**
     * Reads properties from file arangodb.properties.
     * Properties must be prefixed with @{code "arangodb"}, eg. @{code "arangodb.hosts=localhost:8529"}.
     */
    static ArangoConfigProperties fromFile() {
        return new ArangoConfigPropertiesImpl();
    }

    /**
     * Reads properties from file {@code fileName}.
     * Properties must be prefixed with @{code "arangodb"}, eg. @{code "arangodb.hosts=localhost:8529"}.
     */
    static ArangoConfigProperties fromFile(final String fileName) {
        return new ArangoConfigPropertiesImpl(fileName);
    }

    /**
     * Reads properties from file {@code fileName}.
     * Properties must be prefixed with @{code prefix}, eg. @{code "<prefix>.hosts=localhost:8529"}.
     */
    static ArangoConfigProperties fromFile(final String fileName, final String prefix) {
        return new ArangoConfigPropertiesImpl(fileName, prefix);
    }

    /**
     * Creates {@code ArangoConfigProperties} from Java properties ({@link java.util.Properties}).
     * Properties must be prefixed with @{code "arangodb"}, eg. @{code "arangodb.hosts=localhost:8529"}.
     */
    static ArangoConfigProperties fromProperties(final Properties properties) {
        return new ArangoConfigPropertiesImpl(properties);
    }

    /**
     * Creates {@code ArangoConfigProperties} from Java properties ({@link java.util.Properties}).
     * Properties must be prefixed with @{code prefix}, eg. @{code "<prefix>.hosts=localhost:8529"}.
     */
    static ArangoConfigProperties fromProperties(final Properties properties, final String prefix) {
        return new ArangoConfigPropertiesImpl(properties, prefix);
    }

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

    default Optional<Compression> getCompression() {
        return Optional.empty();
    }

    default Optional<Integer> getCompressionThreshold() {
        return Optional.empty();
    }

    default Optional<Integer> getCompressionLevel() {
        return Optional.empty();
    }

    default Optional<String> getSerdeProviderClass() {
        return Optional.empty();
    }

}
