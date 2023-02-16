package com.arangodb.serde.jackson;

import com.arangodb.ContentType;
import com.arangodb.serde.ArangoSerdeProvider;
import com.arangodb.serde.jackson.internal.JacksonMapperProvider;
import com.arangodb.serde.jackson.internal.JacksonSerdeImpl;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JacksonSerdeProvider implements ArangoSerdeProvider {

    /**
     * Creates a new JacksonSerde with default settings for the specified data type.
     * Registers all the Jackson modules ({@link com.fasterxml.jackson.databind.Module}) discovered via SPI.
     *
     * @param contentType serialization target data type
     * @return the created JacksonSerde
     */
    @Override
    public JacksonSerde of(final ContentType contentType) {
        JacksonSerde serde = create(JacksonMapperProvider.of(contentType));
        serde.configure(mapper -> mapper.registerModules(ObjectMapper.findModules()));
        return serde;
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
