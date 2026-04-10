package com.arangodb.serde.jackson.json;

import com.arangodb.serde.ArangoSerde;
import com.arangodb.serde.ArangoSerdeProvider;
import com.arangodb.serde.jackson.JacksonSerde;
import tools.jackson.databind.json.JsonMapper;

public class JacksonJsonSerdeProvider implements ArangoSerdeProvider {
    @Override
    public ArangoSerde create() {
        return JacksonSerde.create(new JsonMapper());
    }
}
