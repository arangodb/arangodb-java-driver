package mp;

import com.arangodb.Compression;
import com.arangodb.Protocol;
import com.arangodb.config.ArangoConfigProperties;
import com.arangodb.config.HostDescription;
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
    private Optional<String> sslCertValue;
    private Optional<String> sslAlgorithm;
    private Optional<String> sslProtocol;
    private Optional<Boolean> verifyHost;
    private Optional<Integer> chunkSize;
    private Optional<Boolean> pipelining;
    private Optional<Integer> maxConnections;
    private Optional<Long> connectionTtl;
    private Optional<Integer> keepAliveInterval;
    private Optional<Boolean> acquireHostList;
    private Optional<Integer> acquireHostListInterval;
    private Optional<LoadBalancingStrategy> loadBalancingStrategy;
    private Optional<Integer> responseQueueTimeSamples;
    private Optional<Compression> compression;
    private Optional<Integer> compressionThreshold;
    private Optional<Integer> compressionLevel;
    private Optional<String> serdeProviderClass;

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
    public Optional<String> getSslCertValue() {
        return sslCertValue;
    }

    @Override
    public Optional<String> getSslAlgorithm() {
        return sslAlgorithm;
    }

    @Override
    public Optional<String> getSslProtocol() {
        return sslProtocol;
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
    public Optional<Boolean> getPipelining() {
        return pipelining;
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
    public Optional<Compression> getCompression() {
        return compression;
    }

    @Override
    public Optional<Integer> getCompressionThreshold() {
        return compressionThreshold;
    }

    @Override
    public Optional<Integer> getCompressionLevel() {
        return compressionLevel;
    }

    @Override
    public Optional<String> getSerdeProviderClass() {
        return serdeProviderClass;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ArangoConfigPropertiesMPImpl that = (ArangoConfigPropertiesMPImpl) o;
        return Objects.equals(hosts, that.hosts) && Objects.equals(protocol, that.protocol) && Objects.equals(user, that.user) && Objects.equals(password, that.password) && Objects.equals(jwt, that.jwt) && Objects.equals(timeout, that.timeout) && Objects.equals(useSsl, that.useSsl) && Objects.equals(verifyHost, that.verifyHost) && Objects.equals(chunkSize, that.chunkSize) && Objects.equals(pipelining, that.pipelining) && Objects.equals(maxConnections, that.maxConnections) && Objects.equals(connectionTtl, that.connectionTtl) && Objects.equals(keepAliveInterval, that.keepAliveInterval) && Objects.equals(acquireHostList, that.acquireHostList) && Objects.equals(acquireHostListInterval, that.acquireHostListInterval) && Objects.equals(loadBalancingStrategy, that.loadBalancingStrategy) && Objects.equals(responseQueueTimeSamples, that.responseQueueTimeSamples) && Objects.equals(compression, that.compression) && Objects.equals(compressionThreshold, that.compressionThreshold) && Objects.equals(compressionLevel, that.compressionLevel) && Objects.equals(serdeProviderClass, that.serdeProviderClass);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hosts, protocol, user, password, jwt, timeout, useSsl, verifyHost, chunkSize, pipelining, maxConnections, connectionTtl, keepAliveInterval, acquireHostList, acquireHostListInterval, loadBalancingStrategy, responseQueueTimeSamples, compression, compressionThreshold, compressionLevel, serdeProviderClass);
    }

    @Override
    public String toString() {
        return "ArangoConfigPropertiesMPImpl{" +
                "hosts=" + hosts +
                ", protocol=" + protocol +
                ", user=" + user +
                ", password=" + password +
                ", jwt=" + jwt +
                ", timeout=" + timeout +
                ", useSsl=" + useSsl +
                ", verifyHost=" + verifyHost +
                ", chunkSize=" + chunkSize +
                ", pipelining=" + pipelining +
                ", maxConnections=" + maxConnections +
                ", connectionTtl=" + connectionTtl +
                ", keepAliveInterval=" + keepAliveInterval +
                ", acquireHostList=" + acquireHostList +
                ", acquireHostListInterval=" + acquireHostListInterval +
                ", loadBalancingStrategy=" + loadBalancingStrategy +
                ", responseQueueTimeSamples=" + responseQueueTimeSamples +
                ", compression=" + compression +
                ", compressionThreshold=" + compressionThreshold +
                ", compressionLevel=" + compressionLevel +
                ", serdeProviderClass=" + serdeProviderClass +
                '}';
    }
}
