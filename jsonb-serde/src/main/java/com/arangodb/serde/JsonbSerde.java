package com.arangodb.serde;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbConfig;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

/**
 * User data serde based on Jakarta JSON Binding (JSON-B).
 */
public class JsonbSerde implements ArangoSerde {

    private final Jsonb jsonb;

    public JsonbSerde() {
        jsonb = JsonbBuilder.create();
    }

    public JsonbSerde(final JsonbConfig config) {
        jsonb = JsonbBuilder.create(config);
    }

    @Override
    public byte[] serialize(Object value) {
        return jsonb.toJson(value).getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public <T> T deserialize(byte[] content, Type type) {
        return jsonb.fromJson(new String(content, StandardCharsets.UTF_8), type);
    }

}
