package serde;

import com.arangodb.ArangoDB;
import com.arangodb.config.ArangoConfigProperties;
import com.arangodb.serde.ArangoSerde;
import com.arangodb.serde.jsonb.JsonbSerde;
import com.arangodb.serde.jsonb.JsonbSerdeProvider;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SerdeConfigurationTest {

    @Test
    void jsonBSerdeProvider() {
        ArangoDB adb = new ArangoDB.Builder()
                .host("foo", 1111)
                .serdeProviderClass(JsonbSerdeProvider.class)
                .build();

        ArangoSerde serde = adb.getSerde().getUserSerde();
        assertThat(serde).isInstanceOf(JsonbSerde.class);
    }

    @Test
    void jsonBSerdeProviderFromConfigFile() {
        ArangoDB adb = new ArangoDB.Builder()
                .loadProperties(ArangoConfigProperties.fromFile("arangodb-serde-provider.properties"))
                .build();

        ArangoSerde serde = adb.getSerde().getUserSerde();
        assertThat(serde).isInstanceOf(JsonbSerde.class);
    }

}
