package com.arangodb.serde.jsonb;

import com.arangodb.ContentType;
import com.arangodb.serde.ArangoSerdeProvider;
import jakarta.json.bind.JsonbConfig;

import java.util.Objects;

public class JsonbSerdeProvider implements ArangoSerdeProvider {

    /**
     * Creates a new JsonbSerde with default settings.
     *
     * @return the created JsonbSerde
     */
    @Override
    public JsonbSerde of(final ContentType contentType) {
        if (Objects.requireNonNull(contentType) == ContentType.JSON) {
            return new JsonbSerde();
        }
        throw new IllegalArgumentException(contentType.toString());
    }

    /**
     * Creates a new JsonbSerde using the provided .
     *
     * @param config JsonbConfig to use
     * @return the created JsonbSerde
     */
    static JsonbSerde create(final JsonbConfig config) {
        return new JsonbSerde(config);
    }
}
