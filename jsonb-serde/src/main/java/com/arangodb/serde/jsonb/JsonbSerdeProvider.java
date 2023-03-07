package com.arangodb.serde.jsonb;

import com.arangodb.ContentType;
import com.arangodb.serde.ArangoSerdeProvider;
import jakarta.json.bind.JsonbConfig;

public class JsonbSerdeProvider implements ArangoSerdeProvider {

    /**
     * Creates a new JsonbSerde with default settings.
     *
     * @return the created JsonbSerde
     */
    @Override
    public JsonbSerde create() {
        return new JsonbSerde();
    }

    /**
     * Creates a new JsonbSerde using the provided configuration.
     *
     * @param config JsonbConfig to use
     * @return the created JsonbSerde
     */
    static JsonbSerde create(final JsonbConfig config) {
        return new JsonbSerde(config);
    }

    @Override
    public ContentType getContentType() {
        return ContentType.JSON;
    }

}
