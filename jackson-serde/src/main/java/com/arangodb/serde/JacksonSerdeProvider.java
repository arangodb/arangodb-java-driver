package com.arangodb.serde;

import com.arangodb.ContentType;
import com.arangodb.internal.serde.JacksonSerdeImpl;
import com.arangodb.internal.serde.JacksonMapperProvider;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JacksonSerdeProvider implements ArangoSerdeProvider {

    /**
     * Creates a new JacksonSerde with default settings for the specified data type.
     *
     * @param contentType serialization target data type
     * @return the created JacksonSerde
     */
    @Override
    public JacksonSerde of(final ContentType contentType) {
        return create(JacksonMapperProvider.of(contentType));
    }

    /**
     * Creates a new JacksonSerde using the provided ObjectMapper.
     *
     * @param mapper Jackson ObjectMapper to use
     * @return the created JacksonSerde
     */
    static JacksonSerde create(final ObjectMapper mapper) {
        return new JacksonSerdeImpl(mapper);
    }

}
