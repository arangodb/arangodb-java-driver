package com.arangodb.internal.config;

import com.arangodb.ArangoDBException;
import com.arangodb.Compression;
import com.arangodb.Protocol;
import com.arangodb.config.ArangoConfigProperties;
import com.arangodb.config.HostDescription;
import com.arangodb.entity.LoadBalancingStrategy;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

/**
 * ArangoConfigProperties implementation that reads configuration entries from local file. Properties path prefix can be
 * configured, so that it is possible to distinguish configurations for multiple driver instances in the same file.
 */
public final class ArangoConfigPropertiesImpl implements ArangoConfigProperties {
    private static final String DEFAULT_PREFIX = "arangodb";
    private static final String DEFAULT_PROPERTY_FILE = "arangodb.properties";
    private final Properties properties;
    private final String prefix;

    public ArangoConfigPropertiesImpl() {
        this(DEFAULT_PROPERTY_FILE, DEFAULT_PREFIX);
    }

    public ArangoConfigPropertiesImpl(final String fileName) {
        this(fileName, DEFAULT_PREFIX);
    }

    public ArangoConfigPropertiesImpl(final String fileName, final String prefix) {
        this(initProperties(fileName), prefix);
    }

    public ArangoConfigPropertiesImpl(final Properties properties) {
        this(properties, DEFAULT_PREFIX);
    }

    public ArangoConfigPropertiesImpl(final Properties properties, final String prefix) {
        this.properties = properties;
        this.prefix = initPrefix(prefix);
    }

    private static Properties initProperties(String fileName) {
        Properties p = new Properties();
        try (InputStream is = ArangoConfigPropertiesImpl.class.getClassLoader().getResourceAsStream(fileName)) {
            p.load(is);
        } catch (Exception e) {
            throw ArangoDBException.of("Got exception while reading properties file " + fileName, e);
        }
        return p;
    }

    private String initPrefix(String p) {
        if (p == null) {
            return "";
        } else {
            return p + ".";
        }
    }

    private String getProperty(String key) {
        return properties.getProperty(prefix + key);
    }

    @Override
    public Optional<List<HostDescription>> getHosts() {
        return Optional.ofNullable(getProperty(KEY_HOSTS))
                .map(s -> {
                    List<HostDescription> hostDescriptions = new ArrayList<>();
                    String[] hosts = s.split(",");
                    for (String host : hosts) {
                        hostDescriptions.add(HostDescription.parse(host));
                    }
                    return hostDescriptions;
                });
    }

    @Override
    public Optional<Protocol> getProtocol() {
        return Optional.ofNullable(getProperty(KEY_PROTOCOL)).map(Protocol::valueOf);
    }

    @Override
    public Optional<String> getUser() {
        return Optional.ofNullable(getProperty(KEY_USER));
    }

    @Override
    public Optional<String> getPassword() {
        return Optional.ofNullable(getProperty(KEY_PASSWORD));
    }

    @Override
    public Optional<String> getJwt() {
        return Optional.ofNullable(getProperty(KEY_JWT));
    }

    @Override
    public Optional<Integer> getTimeout() {
        return Optional.ofNullable(getProperty(KEY_TIMEOUT)).map(Integer::valueOf);
    }

    @Override
    public Optional<Boolean> getUseSsl() {
        return Optional.ofNullable(getProperty(KEY_USE_SSL)).map(Boolean::valueOf);
    }

    @Override
    public Optional<String> getSslCertValue() {
        return Optional.ofNullable(getProperty(KEY_SSL_CERT_VALUE));
    }

    @Override
    public Optional<String> getSslAlgorithm() {
        return Optional.ofNullable(getProperty(KEY_SSL_ALGORITHM));
    }

    @Override
    public Optional<String> getSslProtocol() {
        return Optional.ofNullable(getProperty(KEY_SSL_PROTOCOL));
    }

    @Override
    public Optional<Boolean> getVerifyHost() {
        return Optional.ofNullable(getProperty(KEY_VERIFY_HOST)).map(Boolean::valueOf);
    }

    @Override
    public Optional<Integer> getChunkSize() {
        return Optional.ofNullable(getProperty(KEY_CHUNK_SIZE)).map(Integer::valueOf);
    }

    @Override
    public Optional<Boolean> getPipelining() {
        return Optional.ofNullable(getProperty(KEY_PIPELINING)).map(Boolean::valueOf);
    }

    @Override
    public Optional<Integer> getMaxConnections() {
        return Optional.ofNullable(getProperty(KEY_MAX_CONNECTIONS)).map(Integer::valueOf);
    }

    @Override
    public Optional<Long> getConnectionTtl() {
        return Optional.ofNullable(getProperty(KEY_CONNECTION_TTL)).map(Long::valueOf);
    }

    @Override
    public Optional<Integer> getKeepAliveInterval() {
        return Optional.ofNullable(getProperty(KEY_KEEP_ALIVE_INTERVAL)).map(Integer::valueOf);
    }

    @Override
    public Optional<Boolean> getAcquireHostList() {
        return Optional.ofNullable(getProperty(KEY_ACQUIRE_HOST_LIST)).map(Boolean::valueOf);
    }

    @Override
    public Optional<Integer> getAcquireHostListInterval() {
        return Optional.ofNullable(getProperty(KEY_ACQUIRE_HOST_LIST_INTERVAL)).map(Integer::valueOf);
    }

    @Override
    public Optional<LoadBalancingStrategy> getLoadBalancingStrategy() {
        return Optional.ofNullable(getProperty(KEY_LOAD_BALANCING_STRATEGY)).map(LoadBalancingStrategy::valueOf);
    }

    @Override
    public Optional<Integer> getResponseQueueTimeSamples() {
        return Optional.ofNullable(getProperty(KEY_RESPONSE_QUEUE_TIME_SAMPLES)).map(Integer::valueOf);
    }

    @Override
    public Optional<Compression> getCompression() {
        return Optional.ofNullable(getProperty(KEY_COMPRESSION)).map(Compression::valueOf);
    }

    @Override
    public Optional<Integer> getCompressionThreshold() {
        return Optional.ofNullable(getProperty(KEY_COMPRESSION_THRESHOLD)).map(Integer::valueOf);
    }

    @Override
    public Optional<Integer> getCompressionLevel() {
        return Optional.ofNullable(getProperty(KEY_COMPRESSION_LEVEL)).map(Integer::valueOf);
    }

    @Override
    public Optional<String> getSerdeProviderClass() {
        return Optional.ofNullable(getProperty(KEY_SERDE_PROVIDER_CLASS));
    }

    @Override
    public String toString() {
        return "ArangoConfigPropertiesImpl{" +
                "prefix='" + prefix + '\'' +
                ", properties=" + properties +
                '}';
    }
}
