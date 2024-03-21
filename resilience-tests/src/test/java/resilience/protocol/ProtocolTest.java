package resilience.protocol;

import com.arangodb.ArangoDB;
import com.arangodb.Protocol;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import resilience.TestUtils;
import resilience.utils.MemoryAppender;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.security.KeyStore;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class ProtocolTest extends TestUtils {
    private static final String SSL_TRUSTSTORE = "/example.truststore";
    private static final String SSL_TRUSTSTORE_PASSWORD = "12345678";

    private MemoryAppender logs;

    @BeforeEach
    void init() {
        logs = new MemoryAppender();
    }

    @AfterEach
    void shutdown() {
        logs.stop();
    }

    static Stream<Arguments> args() {
        return Stream.of(
                Arguments.of(Protocol.VST, "VstConnection"),
                Arguments.of(Protocol.HTTP_JSON, "LoggingHandler"),
                Arguments.of(Protocol.HTTP2_JSON, "Http2FrameLogger")
        );
    }

    @ParameterizedTest
    @MethodSource("args")
    void shouldUseConfiguredProtocol(Protocol p, String expectedLog) {
        assumeTrue(!p.equals(Protocol.VST) || isLessThanVersion(3, 12));
        ArangoDB adb = new ArangoDB.Builder()
                .host("localhost", 8529)
                .password("test")
                .protocol(p)
                .build();
        adb.getVersion();
        assertThat(logs.getLogs()).anyMatch(it -> it.getLoggerName().contains(expectedLog));
        adb.shutdown();
    }

    @Tag("ssl")
    @EnabledIfSystemProperty(named = "SslTest", matches = "true")
    @ParameterizedTest
    @MethodSource("args")
    void shouldUseConfiguredProtocolWithTLS(Protocol p, String expectedLog) throws Exception {
        assumeTrue(!p.equals(Protocol.VST) || isLessThanVersion(3, 12));
        ArangoDB adb = new ArangoDB.Builder()
                .host("localhost", 8529)
                .password("test")
                .protocol(p)
                .useSsl(true)
                .sslContext(sslContext())
                .build();
        adb.getVersion();
        assertThat(logs.getLogs()).anyMatch(it -> it.getLoggerName().contains(expectedLog));
        adb.shutdown();
    }

    private SSLContext sslContext() throws Exception{
        final KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        ks.load(this.getClass().getResourceAsStream(SSL_TRUSTSTORE), SSL_TRUSTSTORE_PASSWORD.toCharArray());

        final KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(ks, SSL_TRUSTSTORE_PASSWORD.toCharArray());

        final TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(ks);

        final SSLContext sc = SSLContext.getInstance("TLS");
        sc.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

        return sc;
    }

}
