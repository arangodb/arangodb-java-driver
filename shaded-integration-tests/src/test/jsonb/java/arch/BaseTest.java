package arch;

import com.arangodb.ArangoDB;
import com.arangodb.ContentType;
import com.arangodb.DbName;
import com.arangodb.Protocol;
import com.arangodb.config.ArangoConfigProperties;
import com.arangodb.internal.serde.ContentTypeFactory;
import org.junit.jupiter.params.provider.Arguments;

import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Stream;

public class BaseTest {
    private static final ArangoConfigProperties config = ConfigUtils.loadConfig();
    protected static final DbName TEST_DB = DbName.of("java_driver_integration_tests");
    protected static final String HOST = "172.28.0.1";
    protected static final int PORT = 8529;
    protected static final String PASSWD = "test";
    private static final Serde serde = Serde.valueOf(System.getProperty("serde").toUpperCase(Locale.ROOT));

    private enum Serde {
        JACKSON, JSONB
    }

    protected static ArangoDB createAdb() {
        return new ArangoDB.Builder()
                .host(HOST, PORT)
                .password(PASSWD)
                .build();
    }

    protected static ArangoDB createAdb(ContentType contentType) {
        Protocol protocol = contentType == ContentType.VPACK ? Protocol.HTTP2_VPACK : Protocol.HTTP2_JSON;
        return new ArangoDB.Builder()
                .host(HOST, PORT)
                .password(PASSWD)
                .useProtocol(protocol)
                .build();
    }

    protected static ArangoDB createAdb(Protocol protocol) {
        return new ArangoDB.Builder()
                .host(HOST, PORT)
                .password(PASSWD)
                .useProtocol(protocol)
                .build();
    }

    protected static Stream<Arguments> adbByProtocol() {
        return Arrays.stream(Protocol.values())
                .filter(BaseTest::isProtocolSupported)
                .map(BaseTest::createAdb)
                .map(Arguments::of);
    }

    protected static Stream<Arguments> adbByContentType() {
        return Arrays.stream(ContentType.values())
                .filter(BaseTest::isContentTypeSupported)
                .map(BaseTest::createAdb)
                .map(Arguments::of);
    }

    private static boolean isProtocolSupported(Protocol protocol) {
        return isContentTypeSupported(ContentTypeFactory.of(protocol));
    }

    private static boolean isContentTypeSupported(ContentType contentType) {
        if (serde == Serde.JACKSON) {
            return true;
        } else {
            return contentType == ContentType.JSON;
        }
    }
}
