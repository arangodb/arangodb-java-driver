import com.arangodb.ArangoDB;
import com.arangodb.ContentType;
import com.arangodb.config.ArangoConfigProperties;
import com.arangodb.entity.ArangoDBVersion;
import com.arangodb.serde.jackson.JacksonSerde;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

public class ConfigurationTest {

    @Test
    void fallbackHost() {
        final ArangoDB arangoDB = new ArangoDB.Builder()
                .loadProperties(ArangoConfigProperties.fromFile())
                .serde(JacksonSerde.of(ContentType.JSON))
                .host("not-accessible", 8529)
                .host("172.28.0.1", 8529)
                .build();
        final ArangoDBVersion version = arangoDB.getVersion();
        assertThat(version).isNotNull();
    }

    @Test
    void loadPropertiesWithPrefix() {
        ArangoDB adb = new ArangoDB.Builder()
                .loadProperties(ArangoConfigProperties.fromFile("arangodb-with-prefix.properties", "adb"))
                .serde(JacksonSerde.of(ContentType.JSON))
                .build();
        adb.getVersion();
        adb.shutdown();
    }

    @Test
    void loadConfigFromPropertiesWithPrefix() {
        Properties props = new Properties();
        props.setProperty("adb.hosts", "172.28.0.1:8529");
        props.setProperty("adb.password", "test");
        ArangoDB adb = new ArangoDB.Builder()
                .loadProperties(ArangoConfigProperties.fromProperties(props, "adb"))
                .serde(JacksonSerde.of(ContentType.JSON))
                .build();
        adb.getVersion();
        adb.shutdown();
    }

}
