package serde;

import com.arangodb.ArangoDB;
import com.arangodb.config.ArangoConfigProperties;
import com.arangodb.serde.ArangoSerde;
import com.arangodb.serde.jackson.internal.JacksonSerdeImpl;
import com.arangodb.serde.jackson.json.JacksonJsonSerdeProvider;
import com.arangodb.serde.jsonb.JsonbSerde;
import com.arangodb.serde.jsonb.JsonbSerdeProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

import static org.assertj.core.api.Assertions.assertThat;

public class SerdeConfigurationTest {
    private final VarHandle JACKSON_SERDE_IMPL_MAPPER;
    {
        try {
            JACKSON_SERDE_IMPL_MAPPER = MethodHandles
                    .privateLookupIn(JacksonSerdeImpl.class, MethodHandles.lookup())
                    .findVarHandle(JacksonSerdeImpl.class, "mapper", ObjectMapper.class);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }


    @Test
    void jsonSerdeProvider() {
        ArangoDB adb = new ArangoDB.Builder()
                .host("foo", 1111)
                .serdeProviderClass(JacksonJsonSerdeProvider.class)
                .build();

        ArangoSerde serde = adb.getSerde().getUserSerde();
        assertThat(serde).isInstanceOf(JacksonSerdeImpl.class);

        ObjectMapper mapper = (ObjectMapper) JACKSON_SERDE_IMPL_MAPPER.get(serde);
        assertThat(mapper.getFactory().getFormatName()).isEqualTo("JSON");
    }


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

    @Test
    void jackson3SerdeProvider() {
        ArangoDB adb = new ArangoDB.Builder()
                .host("foo", 1111)
                .serdeProviderClass(com.arangodb.serde.jackson3.json.JacksonJsonSerdeProvider.class)
                .build();

        ArangoSerde serde = adb.getSerde().getUserSerde();
        assertThat(serde).isInstanceOf(com.arangodb.serde.jackson3.internal.JacksonSerdeImpl.class);
    }

}
