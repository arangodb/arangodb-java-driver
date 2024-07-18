package serde;

import com.arangodb.ArangoDB;
import com.arangodb.ContentType;
import com.arangodb.Protocol;
import com.arangodb.config.ArangoConfigProperties;
import com.arangodb.internal.serde.InternalSerdeProvider;
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

class InternalSerdeTest {

    static Stream<Arguments> adbByContentType() {
        return Stream.of(ContentType.values())
                .map(ct -> new ArangoDB.Builder()
                        .loadProperties(ArangoConfigProperties.fromFile())
                        .protocol(ContentType.VPACK.equals(ct) ? Protocol.HTTP2_VPACK : Protocol.HTTP2_JSON)
                        .serde(new InternalSerdeProvider(ct).create())
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
        InternalSerdePerson doc = new InternalSerdePerson("key", "Jim", 22);
        InternalSerdePerson res = adb.db().query("return @d", InternalSerdePerson.class, Collections.singletonMap("d", doc)).next();
        assertThat(res).isEqualTo(doc);
        String key = adb.db().query("return @d._key", String.class, Collections.singletonMap("d", doc)).next();
        assertThat(key).isEqualTo("key");
        String name = adb.db().query("return @d.firstName", String.class, Collections.singletonMap("d", doc)).next();
        assertThat(name).isEqualTo("Jim");
    }

}
