package mp;

import com.arangodb.Compression;
import com.arangodb.Protocol;
import com.arangodb.config.ArangoConfigProperties;
import com.arangodb.config.HostDescription;
import com.arangodb.entity.LoadBalancingStrategy;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledInNativeImage;

import static org.assertj.core.api.Assertions.assertThat;

@DisabledInNativeImage
class ConfigMPTest {
    private final HostDescription hostA = new HostDescription("aaa", 1111);
    private final HostDescription hostB = new HostDescription("bbb", 2222);
    private final Protocol protocol = Protocol.HTTP_VPACK;
    private final String user = "testUser";
    private final String password = "testPassword";
    private final String jwt = "testJwt";
    private final Integer timeout = 9876;
    private final Boolean useSsl = true;
    private final String sslCertValue = "sslCertValue";
    private final String sslAlgorithm = "sslAlgorithm";
    private final String sslProtocol = "sslProtocol";
    private final Boolean verifyHost = false;
    private final Integer vstChunkSize = 1234;
    private final Boolean pipelining = true;
    private final Integer maxConnections = 123;
    private final Long connectionTtl = 12345L;
    private final Integer keepAliveInterval = 123456;
    private final Boolean acquireHostList = true;
    private final Integer acquireHostListInterval = 1234567;
    private final LoadBalancingStrategy loadBalancingStrategy = LoadBalancingStrategy.ROUND_ROBIN;
    private final Integer responseQueueTimeSamples = 12345678;
    private final Compression compression = Compression.GZIP;
    private final Integer compressionThreshold = 123456789;
    private final Integer compressionLevel = 9;
    private final String serdeProviderClass = "com.arangodb.serde.jsonb.JsonbSerdeProvider";

    @Test
    void readConfig() {
        ArangoConfigProperties config = ConfigUtilsMP.loadConfigMP("arangodb-config-test.properties", "adb");
        checkResult(config);
    }

    private void checkResult(ArangoConfigProperties config) {
        assertThat(config.getHosts())
                .isPresent()
                .get(InstanceOfAssertFactories.LIST)
                .containsExactly(hostA, hostB);
        assertThat(config.getProtocol()).hasValue(protocol);
        assertThat(config.getUser()).hasValue(user);
        assertThat(config.getPassword())
                .isPresent()
                .hasValue(password);
        assertThat(config.getJwt())
                .isPresent()
                .hasValue(jwt);
        assertThat(config.getTimeout()).hasValue(timeout);
        assertThat(config.getUseSsl()).hasValue(useSsl);
        assertThat(config.getSslCertValue()).hasValue(sslCertValue);
        assertThat(config.getSslAlgorithm()).hasValue(sslAlgorithm);
        assertThat(config.getSslProtocol()).hasValue(sslProtocol);
        assertThat(config.getVerifyHost()).hasValue(verifyHost);
        assertThat(config.getChunkSize()).hasValue(vstChunkSize);
        assertThat(config.getPipelining()).hasValue(pipelining);
        assertThat(config.getMaxConnections())
                .isPresent()
                .hasValue(maxConnections);
        assertThat(config.getConnectionTtl())
                .isPresent()
                .hasValue(connectionTtl);
        assertThat(config.getKeepAliveInterval())
                .isPresent()
                .hasValue(keepAliveInterval);
        assertThat(config.getAcquireHostList()).hasValue(acquireHostList);
        assertThat(config.getAcquireHostListInterval()).hasValue(acquireHostListInterval);
        assertThat(config.getLoadBalancingStrategy()).hasValue(loadBalancingStrategy);
        assertThat(config.getResponseQueueTimeSamples()).hasValue(responseQueueTimeSamples);
        assertThat(config.getCompression()).hasValue(compression);
        assertThat(config.getCompressionThreshold()).hasValue(compressionThreshold);
        assertThat(config.getCompressionLevel()).hasValue(compressionLevel);
        assertThat(config.getSerdeProviderClass()).isPresent().hasValue(serdeProviderClass);
    }
}
