package com.arangodb.serde;

import jakarta.json.bind.JsonbConfig;

/**
 * User data serde based on Jakarta JSON Binding (JSON-B).
 */
public interface JsonbSerde extends ArangoSerde {

    /**
     * Creates a new JsonbSerde with default settings.
     *
     * @return the created JsonbSerde
     */
    static JsonbSerde create() {
        return new JsonbSerdeImpl();
    }

    /**
     * Creates a new JsonbSerde using the provided .
     *
     * @param config JsonbConfig to use
     * @return the created JsonbSerde
     */
    static JsonbSerde of(final JsonbConfig config) {
        return new JsonbSerdeImpl(config);
    }

}
