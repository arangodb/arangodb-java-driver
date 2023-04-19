package arch;

import com.arangodb.ArangoDB;
import com.arangodb.ContentType;
import com.arangodb.Protocol;
import com.arangodb.config.ArangoConfigProperties;
import org.junit.jupiter.params.provider.Arguments;

import java.util.Arrays;
import java.util.stream.Stream;

public class BaseTest {
    private static final ArangoConfigProperties config = ConfigUtils.loadConfig();
    protected static final String TEST_DB = "java_driver_integration_tests";

    protected static ArangoDB createAdb() {
        return new ArangoDB.Builder()
                .loadProperties(config)
                .build();
    }

    protected static ArangoDB createAdb(ContentType contentType) {
        Protocol protocol = contentType == ContentType.VPACK ? Protocol.HTTP2_VPACK : Protocol.HTTP2_JSON;
        return new ArangoDB.Builder()
                .loadProperties(config)
                .protocol(protocol)
                .build();
    }

    protected static ArangoDB createAdb(Protocol protocol) {
        return new ArangoDB.Builder()
                .loadProperties(config)
                .protocol(protocol)
                .build();
    }

    protected static Stream<Arguments> adbByProtocol() {
        return Arrays.stream(Protocol.values())
                .map(BaseTest::createAdb)
                .map(Arguments::of);
    }

    protected static Stream<Arguments> adbByContentType() {
        return Arrays.stream(ContentType.values())
                .map(BaseTest::createAdb)
                .map(Arguments::of);
    }

}
