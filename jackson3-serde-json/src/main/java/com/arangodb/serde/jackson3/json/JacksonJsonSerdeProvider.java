package com.arangodb.serde.jackson3.json;

import com.arangodb.ContentType;
import com.arangodb.serde.ArangoSerde;
import com.arangodb.serde.ArangoSerdeProvider;
import com.arangodb.serde.jackson3.JacksonSerde;
import tools.jackson.databind.json.JsonMapper;

public class JacksonJsonSerdeProvider implements ArangoSerdeProvider {
    @Override
    public ArangoSerde create() {
        return JacksonSerde.create(new JsonMapper());
    }

    @Override
    public ContentType getContentType() {
        return ContentType.JSON;
    }
}
