package arch;

import com.arangodb.ArangoDB;
import com.arangodb.shaded.fasterxml.jackson.databind.JsonNode;
import com.arangodb.shaded.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.arangodb.util.RawJson;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class SerdeTest extends BaseTest {

    @ParameterizedTest
    @MethodSource("adbByContentType")
    void shadedJsonNode(ArangoDB adb) {
        // uses the internal serde
        JsonNode doc = JsonNodeFactory.instance
                .objectNode()
                .put("foo", "bar");
        JsonNode res = adb.db().query("return @d", Collections.singletonMap("d", doc), JsonNode.class).next();
        assertThat(res.size()).isEqualTo(1);
        assertThat(res.get("foo").asText()).isEqualTo("bar");
        JsonNode value = adb.db().query("return @d.foo", Collections.singletonMap("d", doc), JsonNode.class).next();
        assertThat(value.textValue()).isEqualTo("bar");
    }

    @ParameterizedTest
    @MethodSource("adbByContentType")
    void jsonNode(ArangoDB adb) {
        // uses the user serde
        com.fasterxml.jackson.databind.JsonNode doc = com.fasterxml.jackson.databind.node.JsonNodeFactory.instance
                .objectNode()
                .put("foo", "bar");
        com.fasterxml.jackson.databind.JsonNode res = adb.db().query("return @d", Collections.singletonMap("d", doc),
                com.fasterxml.jackson.databind.JsonNode.class).next();
        assertThat(res.size()).isEqualTo(1);
        assertThat(res.get("foo").asText()).isEqualTo("bar");
        com.fasterxml.jackson.databind.JsonNode value = adb.db().query("return @d.foo", Collections.singletonMap("d", doc),
                com.fasterxml.jackson.databind.JsonNode.class).next();
        assertThat(value.textValue()).isEqualTo("bar");
    }

    @ParameterizedTest
    @MethodSource("adbByContentType")
    void map(ArangoDB adb) {
        Map<String, String> doc = Collections.singletonMap("foo", "bar");
        Map<?, ?> res = adb.db().query("return @d", Collections.singletonMap("d", doc), Map.class).next();
        assertThat(res).hasSize(1);
        assertThat(res.get("foo")).isEqualTo("bar");
        String value = adb.db().query("return @d.foo", Collections.singletonMap("d", doc), String.class).next();
        assertThat(value).isEqualTo("bar");
    }

    @ParameterizedTest
    @MethodSource("adbByContentType")
    void rawJson(ArangoDB adb) {
        RawJson doc = RawJson.of("""
                {"foo":"bar"}""");
        RawJson res = adb.db().query("return @d", Collections.singletonMap("d", doc), RawJson.class).next();
        assertThat(res.getValue()).isEqualTo(doc.getValue());
        RawJson value = adb.db().query("return @d.foo", Collections.singletonMap("d", doc), RawJson.class).next();
        assertThat(value.getValue()).isEqualTo("\"bar\"");
    }

    @ParameterizedTest
    @MethodSource("adbByContentType")
    void person(ArangoDB adb) {
        Person doc = new Person("key", "Jim", 22);
        Person res = adb.db().query("return @d", Collections.singletonMap("d", doc), Person.class).next();
        assertThat(res).isEqualTo(doc);
        String key = adb.db().query("return @d._key", Collections.singletonMap("d", doc), String.class).next();
        assertThat(key).isEqualTo("key");
        String name = adb.db().query("return @d.firstName", Collections.singletonMap("d", doc), String.class).next();
        assertThat(name).isEqualTo("Jim");
    }

}
