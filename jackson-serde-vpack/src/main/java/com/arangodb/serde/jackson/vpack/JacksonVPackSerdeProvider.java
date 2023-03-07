package com.arangodb.serde.jackson.vpack;

import com.arangodb.ContentType;
import com.arangodb.serde.ArangoSerde;
import com.arangodb.serde.ArangoSerdeProvider;
import com.arangodb.serde.jackson.JacksonSerde;

public class JacksonVPackSerdeProvider implements ArangoSerdeProvider {
    @Override
    public ArangoSerde create() {
        return JacksonSerde.of(ContentType.VPACK);
    }

    @Override
    public ContentType getContentType() {
        return ContentType.VPACK;
    }
}
