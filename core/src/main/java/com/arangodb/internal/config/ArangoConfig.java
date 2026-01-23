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
import javax.net.ssl.TrustManagerFactory;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
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
    private Optional<String> sslCertValue;
    private Optional<String> sslAlgorithm;
    private String sslProtocol;
    private Optional<String> sslTrustStorePath;
    private Optional<String> sslTrustStorePassword;
    private String sslTrustStoreType;
    private SSLContext sslContext;
    private Boolean verifyHost;
    private Integer chunkSize;
    private Boolean pipelining;
    private Integer connectionWindowSize;
    private Integer initialWindowSize;
    private Integer maxConnections;
    private Long connectionTtl;
    private Integer keepAliveInterval;
    private Boolean acquireHostList;
    private Integer acquireHostListInterval;
    private LoadBalancingStrategy loadBalancingStrategy;
    private InternalSerde internalSerde;
    private ArangoSerde userDataSerde;
    private Class<? extends ArangoSerdeProvider> serdeProviderClass;
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
        sslCertValue = properties.getSslCertValue();
        sslAlgorithm = properties.getSslAlgorithm();
        sslProtocol = properties.getSslProtocol().orElse(ArangoDefaults.DEFAULT_SSL_PROTOCOL);
        sslTrustStorePath = properties.getSslTrustStorePath();
        sslTrustStorePassword = properties.getSslTrustStorePassword();
        sslTrustStoreType = properties.getSslTrustStoreType().orElse(ArangoDefaults.DEFAULT_SSL_TRUST_STORE_TYPE);
        verifyHost = properties.getVerifyHost().orElse(ArangoDefaults.DEFAULT_VERIFY_HOST);
        chunkSize = properties.getChunkSize().orElse(ArangoDefaults.DEFAULT_CHUNK_SIZE);
        pipelining = properties.getPipelining().orElse(ArangoDefaults.DEFAULT_PIPELINING);
        connectionWindowSize = properties.getConnectionWindowSize().orElse(ArangoDefaults.DEFAULT_CONNECTION_WINDOW_SIZE);
        initialWindowSize = properties.getInitialWindowSize().orElse(ArangoDefaults.DEFAULT_INITIAL_WINDOW_SIZE);
        // FIXME: make maxConnections field Optional
        maxConnections = properties.getMaxConnections().orElse(null);
        connectionTtl = properties.getConnectionTtl().orElse(ArangoDefaults.DEFAULT_CONNECTION_TTL_HTTP);
        // FIXME: make keepAliveInterval field Optional
        keepAliveInterval = properties.getKeepAliveInterval().orElse(null);
        acquireHostList = properties.getAcquireHostList().orElse(ArangoDefaults.DEFAULT_ACQUIRE_HOST_LIST);
        acquireHostListInterval = properties.getAcquireHostListInterval().orElse(ArangoDefaults.DEFAULT_ACQUIRE_HOST_LIST_INTERVAL);
        loadBalancingStrategy = properties.getLoadBalancingStrategy().orElse(ArangoDefaults.DEFAULT_LOAD_BALANCING_STRATEGY);
        responseQueueTimeSamples = properties.getResponseQueueTimeSamples().orElse(ArangoDefaults.DEFAULT_RESPONSE_QUEUE_TIME_SAMPLES);
        compression = properties.getCompression().orElse(ArangoDefaults.DEFAULT_COMPRESSION);
        compressionThreshold = properties.getCompressionThreshold().orElse(ArangoDefaults.DEFAULT_COMPRESSION_THRESHOLD);
        compressionLevel = properties.getCompressionLevel().orElse(ArangoDefaults.DEFAULT_COMPRESSION_LEVEL);
        serdeProviderClass = properties.getSerdeProviderClass().map((String className) -> {
            try {
                //noinspection unchecked
                return (Class<? extends ArangoSerdeProvider>) Class.forName(className);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }).orElse(null);
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

    public void setSslCertValue(String sslCertValue) {
        this.sslCertValue = Optional.ofNullable(sslCertValue);
    }

    public void setSslAlgorithm(String sslAlgorithm) {
        this.sslAlgorithm = Optional.ofNullable(sslAlgorithm);
    }

    public void setSslProtocol(String sslProtocol) {
        this.sslProtocol = sslProtocol;
    }

    public void setSslTrustStorePath(String sslTrustStorePath) {
        this.sslTrustStorePath = Optional.ofNullable(sslTrustStorePath);
    }

    public void setSslTrustStorePassword(String sslTrustStorePassword) {
        this.sslTrustStorePassword = Optional.ofNullable(sslTrustStorePassword);
    }

    public void setSslTrustStoreType(String sslTrustStoreType) {
        this.sslTrustStoreType = sslTrustStoreType;
    }

    public SSLContext getSslContext() {
        if (sslContext == null) {
            sslContext = createSslContext();
        }
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

    public Boolean getPipelining() {
        return pipelining;
    }

    public void setPipelining(Boolean pipelining) {
        this.pipelining = pipelining;
    }

    public Integer getConnectionWindowSize() {
        return connectionWindowSize;
    }

    public void setConnectionWindowSize(Integer connectionWindowSize) {
        this.connectionWindowSize = connectionWindowSize;
    }

    public Integer getInitialWindowSize() {
        return initialWindowSize;
    }

    public void setInitialWindowSize(Integer initialWindowSize) {
        this.initialWindowSize = initialWindowSize;
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

    public Class<? extends ArangoSerdeProvider> getSerdeProviderClass() {
        return serdeProviderClass;
    }

    public ArangoSerde getUserDataSerde() {
        if (userDataSerde != null) {
            return userDataSerde;
        } else if (serdeProviderClass != null) {
            try {
                return serdeProviderClass.getDeclaredConstructor().newInstance().create();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                     NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        } else {
            return ArangoSerdeProvider.of(ContentTypeFactory.of(getProtocol())).create();
        }
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

    public void setUserDataSerdeProvider(Class<? extends ArangoSerdeProvider> serdeProviderClass) {
        this.serdeProviderClass = serdeProviderClass;
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

    private SSLContext createSslContext() {
        try {
            if (sslCertValue.isPresent()) {
                ByteArrayInputStream is = new ByteArrayInputStream(Base64.getDecoder().decode(sslCertValue.get()));
                Certificate cert = CertificateFactory.getInstance("X.509").generateCertificate(is);
                KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
                ks.load(null);
                ks.setCertificateEntry("arangodb", cert);
                return createSslContext(ks);
            } else if (sslTrustStorePath.isPresent()) {
                KeyStore ks = KeyStore.getInstance(sslTrustStoreType);
                try (InputStream is = Files.newInputStream(Paths.get(sslTrustStorePath.get()))) {
                    ks.load(is, sslTrustStorePassword.map(String::toCharArray).orElse(null));
                }
                return createSslContext(ks);
            } else {
                return SSLContext.getDefault();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private SSLContext createSslContext(KeyStore ks) {
        try {
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(sslAlgorithm.orElseGet(TrustManagerFactory::getDefaultAlgorithm));
            tmf.init(ks);
            SSLContext sc = SSLContext.getInstance(sslProtocol);
            sc.init(null, tmf.getTrustManagers(), null);
            return sc;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
