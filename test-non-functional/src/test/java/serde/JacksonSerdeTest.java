package serde;

import com.arangodb.ArangoDB;
import com.arangodb.config.ArangoConfigProperties;
import com.arangodb.serde.jackson.JacksonSerde;
import com.arangodb.util.RawJson;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.JsonNodeFactory;

import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class JacksonSerdeTest {
    private static ArangoDB adb;

    @BeforeAll
    static void init() {
        adb = new ArangoDB.Builder()
                .loadProperties(ArangoConfigProperties.fromFile())
                .serde(JacksonSerde.create())
                .build();
    }

    @AfterAll
    static void shutdown() {
        adb.shutdown();
    }

    @Test
    void shadedJsonNode() {
        // uses the internal serde
        JsonNode doc = JsonNodeFactory.instance
                .objectNode()
                .put("foo", "bar");
        JsonNode res = adb.db().query("return @d", JsonNode.class, Collections.singletonMap("d", doc)).next();
        assertThat(res.size()).isEqualTo(1);
        assertThat(res.get("foo").asText()).isEqualTo("bar");
        JsonNode value = adb.db().query("return @d.foo", JsonNode.class, Collections.singletonMap("d", doc)).next();
        assertThat(value.textValue()).isEqualTo("bar");
    }

    @Test
    void jsonNode() {
        // uses the user serde
        JsonNode doc = JsonNodeFactory.instance
                .objectNode()
                .put("foo", "bar");
        JsonNode res = adb.db().query("return @d", JsonNode.class, Collections.singletonMap("d", doc)).next();
        assertThat(res.size()).isEqualTo(1);
        assertThat(res.get("foo").asString()).isEqualTo("bar");
        JsonNode value = adb.db().query("return @d.foo", JsonNode.class, Collections.singletonMap("d", doc)).next();
        assertThat(value.stringValue()).isEqualTo("bar");
    }

    @Test
    void map() {
        Map<String, String> doc = Collections.singletonMap("foo", "bar");
        Map<?, ?> res = adb.db().query("return @d", Map.class, Collections.singletonMap("d", doc)).next();
        assertThat(res).hasSize(1);
        assertThat(res.get("foo")).isEqualTo("bar");
        String value = adb.db().query("return @d.foo", String.class, Collections.singletonMap("d", doc)).next();
        assertThat(value).isEqualTo("bar");
    }

    @Test
    void rawJson() {
        RawJson doc = RawJson.of("""
                {"foo":"bar"}""");
        RawJson res = adb.db().query("return @d", RawJson.class, Collections.singletonMap("d", doc)).next();
        assertThat(res.get()).isEqualTo(doc.get());
        RawJson value = adb.db().query("return @d.foo", RawJson.class, Collections.singletonMap("d", doc)).next();
        assertThat(value.get()).isEqualTo("\"bar\"");
    }

    @Test
    void person() {
        JacksonPerson doc = new JacksonPerson("key", "Jim", 22);
        JacksonPerson res = adb.db().query("return @d", JacksonPerson.class, Collections.singletonMap("d", doc)).next();
        assertThat(res).isEqualTo(doc);
        String key = adb.db().query("return @d._key", String.class, Collections.singletonMap("d", doc)).next();
        assertThat(key).isEqualTo("key");
        String name = adb.db().query("return @d.firstName", String.class, Collections.singletonMap("d", doc)).next();
        assertThat(name).isEqualTo("Jim");
    }

}
