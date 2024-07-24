package com.arangodb;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.arangodb.vst.internal.VstConnection;
import io.netty.handler.codec.http2.Http2FrameLogger;
import io.netty.handler.logging.LoggingHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.LoggerFactory;
import utils.MemoryAppender;
import utils.TestUtils;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.security.KeyStore;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * SSL variant of <a href=test-resilience/src/test/java/resilience/protocol/ProtocolTest.java>ProtocolTest resilience test</a>
 */
public class ProtocolTest extends BaseTest {
    private static final String SSL_TRUSTSTORE = "/example.truststore";
    private static final String SSL_TRUSTSTORE_PASSWORD = "12345678";
    private static final Map<Class<?>, Level> logLevels = new HashMap<>();
    private final Map<Class<?>, Level> originalLogLevels = new HashMap<>();

    static {
        logLevels.put(VstConnection.class, Level.DEBUG);
        logLevels.put(LoggingHandler.class, Level.DEBUG);
        logLevels.put(Http2FrameLogger.class, Level.DEBUG);
    }

    private MemoryAppender logs;

    @BeforeEach
    void setLogLevels() {
        logs = new MemoryAppender();
        logLevels.forEach((clazz, level) -> {
            Logger logger = (Logger) LoggerFactory.getLogger(clazz);
            originalLogLevels.put(clazz, logger.getLevel());
            logger.setLevel(level);
        });
    }

    @AfterEach
    void resetLogLevels() {
        originalLogLevels.forEach((clazz, level) -> {
            Logger logger = (Logger) LoggerFactory.getLogger(clazz);
            logger.setLevel(level);
        });
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
    void shouldUseConfiguredProtocolWithTLS(Protocol p, String expectedLog) throws Exception {
        assumeTrue(!p.equals(Protocol.VST) || TestUtils.isLessThanVersion(version.getVersion(), 3, 12, 0));
        ArangoDB adb = new ArangoDB.Builder()
                .host("172.28.0.1", 8529)
                .password("test")
                .protocol(p)
                .useSsl(true)
                .sslContext(sslContext())
                .verifyHost(false)
                .build();
        adb.getVersion();
        assertThat(logs.getLogs()).anyMatch(it -> it.getLoggerName().contains(expectedLog));
        adb.shutdown();
    }

    private SSLContext sslContext() throws Exception {
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
