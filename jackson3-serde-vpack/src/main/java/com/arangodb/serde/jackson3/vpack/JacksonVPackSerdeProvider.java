package com.arangodb.serde.jackson3.vpack;

import com.arangodb.ContentType;
import com.arangodb.serde.ArangoSerde;
import com.arangodb.serde.ArangoSerdeProvider;
import com.arangodb.serde.jackson3.JacksonSerde;
import tools.jackson.dataformat.velocypack.VPackMapper;

public class JacksonVPackSerdeProvider implements ArangoSerdeProvider {
    @Override
    public ArangoSerde create() {
        return JacksonSerde.create(new VPackMapper());
    }

    @Override
    public ContentType getContentType() {
        return ContentType.VPACK;
    }
}
