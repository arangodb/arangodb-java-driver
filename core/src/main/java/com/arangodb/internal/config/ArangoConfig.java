package com.arangodb.internal.config;

import com.arangodb.ArangoDBException;
import com.arangodb.ContentType;
import com.arangodb.Protocol;
import com.arangodb.config.ArangoConfigProperties;
import com.arangodb.config.HostDescription;
import com.arangodb.entity.LoadBalancingStrategy;
import com.arangodb.internal.ArangoDefaults;
import com.arangodb.internal.serde.ContentTypeFactory;
import com.arangodb.internal.serde.InternalSerde;
import com.arangodb.internal.serde.InternalSerdeProvider;
import com.arangodb.serde.ArangoSerde;
import com.arangodb.serde.ArangoSerdeProvider;
import com.fasterxml.jackson.databind.Module;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

public class ArangoConfig {
    private final List<HostDescription> hosts = new ArrayList<>();
    private Protocol protocol;
    private Integer timeout;
    private String user;
    private String password;
    private String jwt;
    private Boolean useSsl;
    private SSLContext sslContext;
    private Boolean verifyHost;
    private Integer chunkSize;
    private Integer maxConnections;
    private Long connectionTtl;
    private Integer keepAliveInterval;
    private Boolean acquireHostList;
    private Integer acquireHostListInterval;
    private LoadBalancingStrategy loadBalancingStrategy;
    private InternalSerde internalSerde;
    private ArangoSerde userDataSerde;
    private Integer responseQueueTimeSamples;
    private Module protocolModule;
    private Executor asyncExecutor;

    private static final Logger LOG = LoggerFactory.getLogger(ArangoConfig.class);

    private static ArangoSerdeProvider serdeProvider(ContentType contentType) {
        ServiceLoader<ArangoSerdeProvider> loader = ServiceLoader.load(ArangoSerdeProvider.class);
        ArangoSerdeProvider serdeProvider = null;
        for (ArangoSerdeProvider p : loader) {
            if (contentType.equals(p.getContentType())) {
                if (serdeProvider != null) {
                    throw new ArangoDBException("Found multiple serde providers! Please set explicitly the one to use.");
                }
                serdeProvider = p;
            }
        }
        if (serdeProvider == null) {
            LOG.warn("No ArangoSerdeProvider found, using InternalSerdeProvider. Please consider registering a custom " +
                    "ArangoSerdeProvider to avoid depending on internal classes which are not part of the public API.");
            serdeProvider = new InternalSerdeProvider(contentType);
        }
        return serdeProvider;
    }

    public ArangoConfig() {
        // load default properties
        loadProperties(new ArangoConfigProperties() {
        });
    }

    public void loadProperties(final ArangoConfigProperties properties) {
        hosts.addAll(properties.getHosts().orElse(ArangoDefaults.DEFAULT_HOSTS).stream()
                .map(it -> new HostDescription(it.getHost(), it.getPort()))
                .collect(Collectors.toList()));
        protocol = properties.getProtocol().orElse(ArangoDefaults.DEFAULT_PROTOCOL);
        timeout = properties.getTimeout().orElse(ArangoDefaults.DEFAULT_TIMEOUT);
        user = properties.getUser().orElse(ArangoDefaults.DEFAULT_USER);
        // FIXME: make password field Optional
        password = properties.getPassword().orElse(null);
        // FIXME: make jwt field Optional
        jwt = properties.getJwt().orElse(null);
        useSsl = properties.getUseSsl().orElse(ArangoDefaults.DEFAULT_USE_SSL);
        verifyHost = properties.getVerifyHost().orElse(ArangoDefaults.DEFAULT_VERIFY_HOST);
        chunkSize = properties.getChunkSize().orElse(ArangoDefaults.DEFAULT_CHUNK_SIZE);
        // FIXME: make maxConnections field Optional
        maxConnections = properties.getMaxConnections().orElse(null);
        // FIXME: make connectionTtl field Optional
        connectionTtl = properties.getConnectionTtl().orElse(null);
        // FIXME: make keepAliveInterval field Optional
        keepAliveInterval = properties.getKeepAliveInterval().orElse(null);
        acquireHostList = properties.getAcquireHostList().orElse(ArangoDefaults.DEFAULT_ACQUIRE_HOST_LIST);
        acquireHostListInterval = properties.getAcquireHostListInterval().orElse(ArangoDefaults.DEFAULT_ACQUIRE_HOST_LIST_INTERVAL);
        loadBalancingStrategy = properties.getLoadBalancingStrategy().orElse(ArangoDefaults.DEFAULT_LOAD_BALANCING_STRATEGY);
        responseQueueTimeSamples = properties.getResponseQueueTimeSamples().orElse(ArangoDefaults.DEFAULT_RESPONSE_QUEUE_TIME_SAMPLES);
    }

    public List<HostDescription> getHosts() {
        return hosts;
    }

    public void addHost(HostDescription hostDescription) {
        hosts.add(hostDescription);
    }

    public Protocol getProtocol() {
        return protocol;
    }

    public void setProtocol(Protocol protocol) {
        this.protocol = protocol;
    }

    public Integer getTimeout() {
        return timeout;
    }

    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getJwt() {
        return jwt;
    }

    public void setJwt(String jwt) {
        this.jwt = jwt;
    }

    public Boolean getUseSsl() {
        return useSsl;
    }

    public void setUseSsl(Boolean useSsl) {
        this.useSsl = useSsl;
    }

    public SSLContext getSslContext() {
        return sslContext;
    }

    public void setSslContext(SSLContext sslContext) {
        this.sslContext = sslContext;
    }

    public Boolean getVerifyHost() {
        return verifyHost;
    }

    public void setVerifyHost(Boolean verifyHost) {
        this.verifyHost = verifyHost;
    }

    public Integer getChunkSize() {
        return chunkSize;
    }

    public void setChunkSize(Integer chunkSize) {
        this.chunkSize = chunkSize;
    }

    public Integer getMaxConnections() {
        if (maxConnections == null) {
            maxConnections = getDefaultMaxConnections();
        }
        return maxConnections;
    }

    private int getDefaultMaxConnections() {
        int defaultMaxConnections;
        switch (getProtocol()) {
            case VST:
                defaultMaxConnections = ArangoDefaults.MAX_CONNECTIONS_VST_DEFAULT;
                break;
            case HTTP_JSON:
            case HTTP_VPACK:
                defaultMaxConnections = ArangoDefaults.MAX_CONNECTIONS_HTTP_DEFAULT;
                break;
            case HTTP2_JSON:
            case HTTP2_VPACK:
                defaultMaxConnections = ArangoDefaults.MAX_CONNECTIONS_HTTP2_DEFAULT;
                break;
            default:
                throw new IllegalArgumentException();
        }
        return defaultMaxConnections;
    }

    public void setMaxConnections(Integer maxConnections) {
        this.maxConnections = maxConnections;
    }

    public Long getConnectionTtl() {
        return connectionTtl;
    }

    public void setConnectionTtl(Long connectionTtl) {
        this.connectionTtl = connectionTtl;
    }

    public Integer getKeepAliveInterval() {
        return keepAliveInterval;
    }

    public void setKeepAliveInterval(Integer keepAliveInterval) {
        this.keepAliveInterval = keepAliveInterval;
    }

    public Boolean getAcquireHostList() {
        return acquireHostList;
    }

    public void setAcquireHostList(Boolean acquireHostList) {
        this.acquireHostList = acquireHostList;
    }

    public Integer getAcquireHostListInterval() {
        return acquireHostListInterval;
    }

    public void setAcquireHostListInterval(Integer acquireHostListInterval) {
        this.acquireHostListInterval = acquireHostListInterval;
    }

    public LoadBalancingStrategy getLoadBalancingStrategy() {
        return loadBalancingStrategy;
    }

    public void setLoadBalancingStrategy(LoadBalancingStrategy loadBalancingStrategy) {
        this.loadBalancingStrategy = loadBalancingStrategy;
    }

    public ArangoSerde getUserDataSerde() {
        if (userDataSerde == null) {
            userDataSerde = serdeProvider(ContentTypeFactory.of(getProtocol())).create();
        }
        return userDataSerde;
    }

    public InternalSerde getInternalSerde() {
        if (internalSerde == null) {
            internalSerde = new InternalSerdeProvider(ContentTypeFactory.of(getProtocol())).create(getUserDataSerde(), protocolModule);
        }
        return internalSerde;
    }

    public void setUserDataSerde(ArangoSerde userDataSerde) {
        this.userDataSerde = userDataSerde;
    }

    public Integer getResponseQueueTimeSamples() {
        return responseQueueTimeSamples;
    }

    public void setResponseQueueTimeSamples(Integer responseQueueTimeSamples) {
        this.responseQueueTimeSamples = responseQueueTimeSamples;
    }

    public void setProtocolModule(Module m) {
        protocolModule = m;
    }

    public Executor getAsyncExecutor() {
        return asyncExecutor;
    }

    public void setAsyncExecutor(Executor asyncExecutor) {
        this.asyncExecutor = asyncExecutor;
    }
}
