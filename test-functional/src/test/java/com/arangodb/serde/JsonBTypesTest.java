package com.arangodb.serde;

import com.arangodb.ArangoDatabase;
import com.arangodb.BaseJunit5;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

public class JsonBTypesTest extends BaseJunit5 {

    @BeforeAll
    static void init() {
        BaseJunit5.initDB();
    }

    @ParameterizedTest
    @MethodSource("dbs")
    void jsonNode(ArangoDatabase db) {
        JsonObject doc = Json.createObjectBuilder()
                .add("foo", "bar")
                .build();
        JsonObject res = db.query("return @d", JsonObject.class, Collections.singletonMap("d", doc)).next();
        assertThat(res.size()).isEqualTo(1);
        assertThat(res.getString("foo")).isEqualTo("bar");
        JsonValue value = db.query("return @d.foo", JsonValue.class, Collections.singletonMap("d", doc)).next();
        assertThat(value)
                .isInstanceOf(JsonString.class)
                .extracting(v -> ((JsonString) v).getString())
                .isEqualTo("bar");
    }

}
