package resilience.compression;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import com.arangodb.ArangoDB;
import com.arangodb.Compression;
import com.arangodb.Protocol;
import io.netty.handler.codec.http2.Http2FrameLogger;
import org.junit.jupiter.params.ParameterizedTest;
import resilience.ClusterTest;
import resilience.utils.ProtocolSource;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * @author Michele Rastelli
 */
class CompressionTest extends ClusterTest {

    CompressionTest() {
        super(Collections.singletonMap(Http2FrameLogger.class, Level.DEBUG));
    }

    @ParameterizedTest
    @ProtocolSource
    void gzip(Protocol protocol) {
        doTest(protocol, Compression.GZIP);
    }

    @ParameterizedTest
    @ProtocolSource
    void deflate(Protocol protocol) {
        doTest(protocol, Compression.DEFLATE);
    }

    void doTest(Protocol protocol, Compression compression) {
        assumeTrue(protocol != Protocol.HTTP_VPACK, "hex dumps logs"); // FIXME
        assumeTrue(protocol != Protocol.HTTP_JSON, "hex dumps logs");  // FIXME

        // FIXME:
        // When using HTTP_VPACK or HTTP_JSON, the logs are hex dumps.
        // Implement a way to check the content-encoding and accept-encoding headers from these logs.

        ArangoDB adb = dbBuilder()
                .protocol(protocol)
                .compression(compression)
                .compressionThreshold(0)
                .build();

        List<String> data = IntStream.range(0, 500)
                .mapToObj(i -> UUID.randomUUID().toString())
                .collect(Collectors.toList());

        adb.db().query("FOR i IN @data RETURN i", String.class,
                Collections.singletonMap("data", data)).asListRemaining();

        adb.shutdown();

        String compressionLC = compression.toString().toLowerCase(Locale.ROOT);

        // request
        assertThat(logs.getLogs())
                .map(ILoggingEvent::getFormattedMessage)
                .anyMatch(l -> l.contains("content-encoding: " + compressionLC) && l.contains("accept-encoding: " + compressionLC));

        // response
        assertThat(logs.getLogs())
                .map(ILoggingEvent::getFormattedMessage)
                .anyMatch(l -> l.contains("content-encoding: " + compressionLC) && l.contains("server: ArangoDB"));
    }

}
