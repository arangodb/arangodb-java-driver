package com.arangodb.serde;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.function.Consumer;

/**
 * User data serde based on Jackson Databind.
 */
public interface JacksonSerde extends ArangoSerde {

    /**
     * Creates a new JacksonSerde with default settings for the specified data type.
     *
     * @param dataType serialization target data type
     * @return the created JacksonSerde
     */
    static JacksonSerde of(final DataType dataType) {
        return create(MapperProvider.of(dataType));
    }

    /**
     * Creates a new JacksonSerde using the provided ObjectMapper.
     *
     * @param mapper   Jackson ObjectMapper to use
     * @return the created JacksonSerde
     */
    static JacksonSerde create(final ObjectMapper mapper) {
        return new JacksonSerdeImpl(mapper);
    }

    /**
     * Allows configuring the underlying Jackson ObjectMapper
     * @param configureFunction function to configure the Jackson ObjectMapper
     */
    void configure(final Consumer<ObjectMapper> configureFunction);

}
