package serde;

import com.arangodb.ArangoDB;
import com.arangodb.config.ArangoConfigProperties;
import com.arangodb.serde.jsonb.JsonbSerdeProvider;
import com.arangodb.util.RawJson;
import jakarta.json.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class JsonBSerdeTest {

    static Stream<Arguments> adbByContentType() {
        return Stream.of(new ArangoDB.Builder()
                        .loadProperties(ArangoConfigProperties.fromFile())
                        .serde(new JsonbSerdeProvider().create())
                        .build())
                .map(Arguments::of);
    }

    @ParameterizedTest
    @MethodSource("adbByContentType")
    void jsonNode(ArangoDB adb) {
        JsonObject doc = Json.createObjectBuilder()
                .add("foo", "bar")
                .build();
        JsonObject res = adb.db().query("return @d", JsonObject.class, Collections.singletonMap("d", doc)).next();
        assertThat(res.size()).isEqualTo(1);
        assertThat(res.getString("foo")).isEqualTo("bar");
        JsonValue value = adb.db().query("return @d.foo", JsonValue.class, Collections.singletonMap("d", doc)).next();
        assertThat(value)
                .isInstanceOf(JsonString.class)
                .extracting(v -> ((JsonString) v).getString())
                .isEqualTo("bar");
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
        JsonBPerson doc = new JsonBPerson("key", "Jim", 22);
        JsonBPerson res = adb.db().query("return @d", JsonBPerson.class, Collections.singletonMap("d", doc)).next();
        assertThat(res).isEqualTo(doc);
        String key = adb.db().query("return @d._key", String.class, Collections.singletonMap("d", doc)).next();
        assertThat(key).isEqualTo("key");
        String name = adb.db().query("return @d.firstName", String.class, Collections.singletonMap("d", doc)).next();
        assertThat(name).isEqualTo("Jim");
    }

}
