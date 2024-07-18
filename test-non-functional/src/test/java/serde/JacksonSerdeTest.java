package serde;

import com.arangodb.ArangoDB;
import com.arangodb.ContentType;
import com.arangodb.Protocol;
import com.arangodb.config.ArangoConfigProperties;
import com.arangodb.internal.serde.InternalSerdeProvider;
import com.arangodb.serde.jackson.JacksonSerde;
import com.arangodb.serde.jackson.json.JacksonJsonSerdeProvider;
import com.arangodb.serde.jackson.vpack.JacksonVPackSerdeProvider;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.arangodb.util.RawJson;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class JacksonSerdeTest {

    static Stream<Arguments> adbByContentType() {
        return Stream.of(ContentType.values())
                .map(ct -> new ArangoDB.Builder()
                        .loadProperties(ArangoConfigProperties.fromFile())
                        .protocol(ContentType.VPACK.equals(ct) ? Protocol.HTTP2_VPACK : Protocol.HTTP2_JSON)
                        .serde(JacksonSerde.of(ct))
                        .build())
                .map(Arguments::of);
    }

    @ParameterizedTest
    @MethodSource("adbByContentType")
    void shadedJsonNode(ArangoDB adb) {
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

    @ParameterizedTest
    @MethodSource("adbByContentType")
    void jsonNode(ArangoDB adb) {
        // uses the user serde
        com.fasterxml.jackson.databind.JsonNode doc = com.fasterxml.jackson.databind.node.JsonNodeFactory.instance
                .objectNode()
                .put("foo", "bar");
        com.fasterxml.jackson.databind.JsonNode res = adb.db().query("return @d", com.fasterxml.jackson.databind.JsonNode.class, Collections.singletonMap("d", doc)).next();
        assertThat(res.size()).isEqualTo(1);
        assertThat(res.get("foo").asText()).isEqualTo("bar");
        com.fasterxml.jackson.databind.JsonNode value = adb.db().query("return @d.foo", com.fasterxml.jackson.databind.JsonNode.class, Collections.singletonMap("d", doc)).next();
        assertThat(value.textValue()).isEqualTo("bar");
    }

    @ParameterizedTest
    @MethodSource("adbByContentType")
    void map(ArangoDB adb) {
        Map<String, String> doc = Collections.singletonMap("foo", "bar");
        Map<?, ?> res = adb.db().query("return @d", Map.class, Collections.singletonMap("d", doc)).next();
        assertThat(res).hasSize(1);
        assertThat(res.get("foo")).isEqualTo("bar");
        String value = adb.db().query("return @d.foo", String.class, Collections.singletonMap("d", doc)).next();
        assertThat(value).isEqualTo("bar");
    }

    @ParameterizedTest
    @MethodSource("adbByContentType")
    void rawJson(ArangoDB adb) {
        RawJson doc = RawJson.of("""
                {"foo":"bar"}""");
        RawJson res = adb.db().query("return @d", RawJson.class, Collections.singletonMap("d", doc)).next();
        assertThat(res.get()).isEqualTo(doc.get());
        RawJson value = adb.db().query("return @d.foo", RawJson.class, Collections.singletonMap("d", doc)).next();
        assertThat(value.get()).isEqualTo("\"bar\"");
    }

    @ParameterizedTest
    @MethodSource("adbByContentType")
    void person(ArangoDB adb) {
        JacksonPerson doc = new JacksonPerson("key", "Jim", 22);
        JacksonPerson res = adb.db().query("return @d", JacksonPerson.class, Collections.singletonMap("d", doc)).next();
        assertThat(res).isEqualTo(doc);
        String key = adb.db().query("return @d._key", String.class, Collections.singletonMap("d", doc)).next();
        assertThat(key).isEqualTo("key");
        String name = adb.db().query("return @d.firstName", String.class, Collections.singletonMap("d", doc)).next();
        assertThat(name).isEqualTo("Jim");
    }

}
