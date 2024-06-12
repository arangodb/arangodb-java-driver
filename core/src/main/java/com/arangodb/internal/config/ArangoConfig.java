package com.arangodb.internal.config;

import com.arangodb.Compression;
import com.arangodb.Protocol;
import com.arangodb.arch.UsedInApi;
import com.arangodb.config.ArangoConfigProperties;
import com.arangodb.config.HostDescription;
import com.arangodb.config.ProtocolConfig;
import com.arangodb.entity.LoadBalancingStrategy;
import com.arangodb.internal.ArangoDefaults;
import com.arangodb.internal.serde.ContentTypeFactory;
import com.arangodb.internal.serde.InternalSerde;
import com.arangodb.internal.serde.InternalSerdeProvider;
import com.arangodb.serde.ArangoSerde;
import com.arangodb.serde.ArangoSerdeProvider;
import com.fasterxml.jackson.databind.Module;

import javax.net.ssl.SSLContext;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

@UsedInApi
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
    private Compression compression;
    private Integer compressionThreshold;
    private Integer compressionLevel;
    private ProtocolConfig protocolConfig;

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
        compression = properties.getCompression().orElse(ArangoDefaults.DEFAULT_COMPRESSION);
        compressionThreshold = properties.getCompressionThreshold().orElse(ArangoDefaults.DEFAULT_COMPRESSION_THRESHOLD);
        compressionLevel = properties.getCompressionLevel().orElse(ArangoDefaults.DEFAULT_COMPRESSION_LEVEL);
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
        if (connectionTtl == null && getProtocol() != Protocol.VST) {
            connectionTtl = ArangoDefaults.DEFAULT_CONNECTION_TTL_HTTP;
        }
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
            userDataSerde = ArangoSerdeProvider.of(ContentTypeFactory.of(getProtocol())).create();
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

    public Compression getCompression() {
        return compression;
    }

    public void setCompression(Compression compression) {
        this.compression = compression;
    }

    public Integer getCompressionThreshold() {
        return compressionThreshold;
    }

    public void setCompressionThreshold(Integer compressionThreshold) {
        this.compressionThreshold = compressionThreshold;
    }

    public Integer getCompressionLevel() {
        return compressionLevel;
    }

    public void setCompressionLevel(Integer compressionLevel) {
        this.compressionLevel = compressionLevel;
    }

    public ProtocolConfig getProtocolConfig() {
        return protocolConfig;
    }

    public void setProtocolConfig(ProtocolConfig protocolConfig) {
        this.protocolConfig = protocolConfig;
    }
}
