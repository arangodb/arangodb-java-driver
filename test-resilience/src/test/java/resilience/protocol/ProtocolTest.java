package resilience.protocol;

import ch.qos.logback.classic.Level;
import com.arangodb.ArangoDB;
import com.arangodb.Protocol;
import io.netty.handler.codec.http2.Http2FrameLogger;
import io.netty.handler.logging.LoggingHandler;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import resilience.TestUtils;
import resilience.utils.MemoryAppender;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class ProtocolTest extends TestUtils {
    private static final Map<Class<?>, Level> logLevels = new HashMap<>();

    static {
        logLevels.put(LoggingHandler.class, Level.DEBUG);
        logLevels.put(Http2FrameLogger.class, Level.DEBUG);
    }

    private MemoryAppender logs;

    public ProtocolTest() {
        super(logLevels);
    }

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
                Arguments.of(Protocol.HTTP_JSON, "LoggingHandler"),
                Arguments.of(Protocol.HTTP2_JSON, "Http2FrameLogger")
        );
    }

    @ParameterizedTest
    @MethodSource("args")
    void shouldUseConfiguredProtocol(Protocol p, String expectedLog) {
        ArangoDB adb = new ArangoDB.Builder()
                .host("172.28.0.1", 8529)
                .password("test")
                .protocol(p)
                .build();
        adb.getVersion();
        assertThat(logs.getLogs()).anyMatch(it -> it.getLoggerName().contains(expectedLog));
        adb.shutdown();
    }

}
