package com.arangodb.internal.config;

import com.arangodb.ArangoDBException;
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

    public  ArangoConfigPropertiesImpl(final String fileName, final String prefix) {
        properties = initProperties(fileName);
        this.prefix = initPrefix(prefix);
    }

    private String initPrefix(String p) {
        if (p == null) {
            return "";
        } else {
            return p + ".";
        }
    }

    private Properties initProperties(String fileName) {
        Properties p = new Properties();
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(fileName)) {
            p.load(is);
        } catch (Exception e) {
            throw new ArangoDBException("Got exception while reading properties file " + fileName, e);
        }
        return p;
    }

    private String getProperty(String key) {
        return properties.getProperty(prefix + key);
    }

    @Override
    public Optional<List<HostDescription>> getHosts() {
        return Optional.ofNullable(getProperty("hosts"))
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
        return Optional.ofNullable(getProperty("protocol")).map(Protocol::valueOf);
    }

    @Override
    public Optional<String> getUser() {
        return Optional.ofNullable(getProperty("user"));
    }

    @Override
    public Optional<String> getPassword() {
        return Optional.ofNullable(getProperty("password"));
    }

    @Override
    public Optional<String> getJwt() {
        return Optional.ofNullable(getProperty("jwt"));
    }

    @Override
    public Optional<Integer> getTimeout() {
        return Optional.ofNullable(getProperty("timeout")).map(Integer::valueOf);
    }

    @Override
    public Optional<Boolean> getUseSsl() {
        return Optional.ofNullable(getProperty("useSsl")).map(Boolean::valueOf);
    }

    @Override
    public Optional<Boolean> getVerifyHost() {
        return Optional.ofNullable(getProperty("verifyHost")).map(Boolean::valueOf);
    }

    @Override
    public Optional<Integer> getChunkSize() {
        return Optional.ofNullable(getProperty("chunkSize")).map(Integer::valueOf);
    }

    @Override
    public Optional<Integer> getMaxConnections() {
        return Optional.ofNullable(getProperty("maxConnections")).map(Integer::valueOf);
    }

    @Override
    public Optional<Long> getConnectionTtl() {
        return Optional.ofNullable(getProperty("connectionTtl")).map(Long::valueOf);
    }

    @Override
    public Optional<Integer> getKeepAliveInterval() {
        return Optional.ofNullable(getProperty("keepAliveInterval")).map(Integer::valueOf);
    }

    @Override
    public Optional<Boolean> getAcquireHostList() {
        return Optional.ofNullable(getProperty("acquireHostList")).map(Boolean::valueOf);
    }

    @Override
    public Optional<Integer> getAcquireHostListInterval() {
        return Optional.ofNullable(getProperty("acquireHostListInterval")).map(Integer::valueOf);
    }

    @Override
    public Optional<LoadBalancingStrategy> getLoadBalancingStrategy() {
        return Optional.ofNullable(getProperty("loadBalancingStrategy")).map(LoadBalancingStrategy::valueOf);
    }

    @Override
    public Optional<Integer> getResponseQueueTimeSamples() {
        return Optional.ofNullable(getProperty("responseQueueTimeSamples")).map(Integer::valueOf);
    }

}
