package com.arangodb.config;

import com.arangodb.Compression;
import com.arangodb.Protocol;
import com.arangodb.entity.LoadBalancingStrategy;
import com.arangodb.internal.config.ArangoConfigPropertiesImpl;

import java.util.List;
import java.util.Optional;
import java.util.Properties;

public interface ArangoConfigProperties {

    //region configuration properties keys
    String KEY_HOSTS = "hosts";
    String KEY_PROTOCOL = "protocol";
    String KEY_USER = "user";
    String KEY_PASSWORD = "password";
    String KEY_JWT = "jwt";
    String KEY_TIMEOUT = "timeout";
    String KEY_USE_SSL = "useSsl";
    String KEY_SSL_CERT_VALUE = "sslCertValue";
    String KEY_SSL_ALGORITHM = "sslAlgorithm";
    String KEY_SSL_PROTOCOL = "sslProtocol";
    String KEY_SSL_TRUST_STORE_PATH = "sslTrustStorePath";
    String KEY_SSL_TRUST_STORE_PASSWORD = "sslTrustStorePassword";
    String KEY_SSL_TRUST_STORE_TYPE = "sslTrustStoreType";
    String KEY_VERIFY_HOST = "verifyHost";
    String KEY_CHUNK_SIZE = "chunkSize";
    String KEY_PIPELINING = "pipelining";
    String KEY_CONNECTION_WINDOW_SIZE = "connectionWindowSize";
    String KEY_INITIAL_WINDOW_SIZE = "initialWindowSize";
    String KEY_MAX_CONNECTIONS = "maxConnections";
    String KEY_CONNECTION_TTL = "connectionTtl";
    String KEY_KEEP_ALIVE_INTERVAL = "keepAliveInterval";
    String KEY_ACQUIRE_HOST_LIST = "acquireHostList";
    String KEY_ACQUIRE_HOST_LIST_INTERVAL = "acquireHostListInterval";
    String KEY_LOAD_BALANCING_STRATEGY = "loadBalancingStrategy";
    String KEY_RESPONSE_QUEUE_TIME_SAMPLES = "responseQueueTimeSamples";
    String KEY_COMPRESSION = "compression";
    String KEY_COMPRESSION_THRESHOLD = "compressionThreshold";
    String KEY_COMPRESSION_LEVEL = "compressionLevel";
    String KEY_SERDE_PROVIDER_CLASS = "serdeProviderClass";
    //endregion

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

    default Optional<String> getSslCertValue() {
        return Optional.empty();
    }

    default Optional<String> getSslAlgorithm() {
        return Optional.empty();
    }

    default Optional<String> getSslProtocol() {
        return Optional.empty();
    }

    default Optional<String> getSslTrustStorePath() {
        return Optional.empty();
    }

    default Optional<String> getSslTrustStorePassword() {
        return Optional.empty();
    }

    default Optional<String> getSslTrustStoreType() {
        return Optional.empty();
    }

    default Optional<Boolean> getVerifyHost() {
        return Optional.empty();
    }

    default Optional<Integer> getChunkSize() {
        return Optional.empty();
    }

    default Optional<Boolean> getPipelining() {
        return Optional.empty();
    }

    default Optional<Integer> getConnectionWindowSize() {
        return Optional.empty();
    }

    default Optional<Integer> getInitialWindowSize() {
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
