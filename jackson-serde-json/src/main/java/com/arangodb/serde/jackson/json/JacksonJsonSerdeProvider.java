package com.arangodb.serde.jackson.json;

import com.arangodb.ContentType;
import com.arangodb.serde.ArangoSerde;
import com.arangodb.serde.ArangoSerdeProvider;
import com.arangodb.serde.jackson.JacksonSerde;

public class JacksonJsonSerdeProvider implements ArangoSerdeProvider {
    @Override
    public ArangoSerde create() {
        return JacksonSerde.of(ContentType.JSON);
    }

    @Override
    public ContentType getContentType() {
        return ContentType.JSON;
    }
}
