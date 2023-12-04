package resilience.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import com.arangodb.ArangoDB;
import com.arangodb.Protocol;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import resilience.SingleServerTest;

import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class RequestLoggingTest extends SingleServerTest {
    private final static ObjectMapper mapper = new ObjectMapper();

    @ParameterizedTest
    @EnumSource(Protocol.class)
    void requestLogging(Protocol protocol) {
        ArangoDB adb = new ArangoDB.Builder()
                .host("172.28.0.1", 8529)
                .password("test")
                .protocol(protocol)
                .build();

        adb.db().query("RETURN \"hello\"", String.class).next();

        assertThat(logs.getLogs())
                .filteredOn(it -> it.getLoggerName().equals("com.arangodb.internal.net.Communication"))
                .map(ILoggingEvent::getFormattedMessage)
                .anySatisfy(it -> {
                    assertThat(it).contains("Send Request");
                    assertThat(reqId(it)).isEqualTo(0);
                    assertThat(meta(it))
                            .contains("requestType=POST")
                            .contains("database='_system'")
                            .contains("url='/_api/cursor'")
                            .doesNotContainIgnoringCase("authorization");
                    assertThat(body(it))
                            .containsEntry("query", "RETURN \"hello\"");
                })
                .anySatisfy(it -> {
                    assertThat(it).contains("Received Response");
                    assertThat(reqId(it)).isEqualTo(0);
                    assertThat(meta(it)).contains("statusCode=201");
                    assertThat(body(it))
                            .containsEntry("code", 201)
                            .containsEntry("result", Collections.singletonList("hello"));
                });

        adb.shutdown();
    }

    private Integer reqId(String log) {
        return Integer.parseInt(log.substring(log.indexOf("[id=") + 4, log.indexOf("]")));
    }

    private String meta(String log) {
        return log.substring(log.indexOf("]: ") + 3, log.indexOf("} {") + 1);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> body(String log) throws JsonProcessingException {
        return mapper.readValue(log.substring(log.indexOf("} {") + 2), Map.class);
    }

}
