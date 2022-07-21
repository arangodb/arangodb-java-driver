package com.arangodb.serde;

import com.arangodb.jackson.dataformat.velocypack.VPackMapper;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.function.Consumer;

/**
 * Contract for serialization/deserialization of user data, based on Jackson Databind.
 */
public interface JacksonSerde extends ArangoSerde {

    /**
     * Creates a new JacksonSerde with default settings for the specified data type.
     *
     * @param dataType serialization target data type
     * @return the created JacksonSerde
     */
    static JacksonSerde of(final DataType dataType) {
        if (dataType == DataType.JSON) {
            return of(new ObjectMapper());
        } else if (dataType == DataType.VPACK) {
            return of(new VPackMapper());
        } else {
            throw new IllegalArgumentException("Unexpected value: " + dataType);
        }
    }

    /**
     * Creates a new JacksonSerde using the provided ObjectMapper.
     *
     * @param mapper   Jackson ObjectMapper to use
     * @return the created JacksonSerde
     */
    static JacksonSerde of(final ObjectMapper mapper) {
        return new JacksonSerdeImpl(mapper);
    }

    /**
     * Allows configuring the underlying Jackson ObjectMapper
     * @param configureFunction function to configure the Jackson ObjectMapper
     */
    void configure(final Consumer<ObjectMapper> configureFunction);

}
