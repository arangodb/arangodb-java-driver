package com.arangodb.serde;

import com.arangodb.jackson.dataformat.velocypack.VPackMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.lang.reflect.Type;
import java.util.function.Consumer;

/**
 * Contract for serialization/deserialization of user data, based on Jackson Databind.
 * In comparison to {@link ArangoSerde}, this API improves the deserialization performance by allowing reusing the JSON
 * tree already parsed by the root deserializer.
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
            return of(dataType, new ObjectMapper());
        } else if (dataType == DataType.VPACK) {
            return of(dataType, new VPackMapper());
        } else {
            throw new IllegalStateException("Unexpected value: " + dataType);
        }
    }

    /**
     * Creates a new JacksonSerde using the provided ObjectMapper.
     *
     * @param dataType serialization target data type
     * @param mapper   Jackson ObjectMapper to use
     * @return the created JacksonSerde
     */
    static JacksonSerde of(final DataType dataType, final ObjectMapper mapper) {
        return new JacksonSerdeImpl(dataType, mapper);
    }

    /**
     * Allows configuring the underlying Jackson ObjectMapper
     * @param configureFunction function to configure the Jackson ObjectMapper
     */
    void configure(final Consumer<ObjectMapper> configureFunction);

    /**
     * Deserializes the parsed json node and binds it to the target data type.
     *
     * @param node  parsed json node
     * @param clazz class of target data type
     * @return deserialized object
     */
    default <T> T deserialize(JsonNode node, Class<T> clazz) {
        return deserialize(node, (Type) clazz);
    }

    /**
     * Deserializes the parsed json node and binds it to the target data type.
     *
     * @param node parsed json node
     * @param type target data type
     * @return deserialized object
     */
    <T> T deserialize(JsonNode node, Type type);

}
