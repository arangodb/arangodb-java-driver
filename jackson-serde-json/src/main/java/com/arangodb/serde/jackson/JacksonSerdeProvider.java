package com.arangodb.serde.jackson;

import com.arangodb.serde.ArangoSerde;
import com.arangodb.serde.ArangoSerdeProvider;

public class JacksonSerdeProvider implements ArangoSerdeProvider {
    @Override
    public ArangoSerde create() {
        return JacksonSerde.create();
    }
}
