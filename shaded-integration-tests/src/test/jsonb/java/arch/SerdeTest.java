package arch;

import com.arangodb.ArangoDB;
import com.arangodb.util.RawJson;
import jakarta.json.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class SerdeTest extends BaseTest {

    @ParameterizedTest
    @MethodSource("adbByContentType")
    void jsonNode(ArangoDB adb) {
        JsonObject doc = Json.createObjectBuilder()
                .add("foo", "bar")
                .build();
        JsonObject res = adb.db().query("return @d", Collections.singletonMap("d", doc), JsonObject.class).next();
        assertThat(res.size()).isEqualTo(1);
        assertThat(res.getString("foo")).isEqualTo("bar");
        JsonValue value = adb.db().query("return @d.foo", Collections.singletonMap("d", doc), JsonValue.class).next();
        assertThat(value)
                .isInstanceOf(JsonString.class)
                .extracting(v -> ((JsonString) v).getString())
                .isEqualTo("bar");
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
