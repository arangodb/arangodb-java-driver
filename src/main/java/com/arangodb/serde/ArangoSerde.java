package com.arangodb.serde;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.lang.reflect.Type;

/**
 * Contract for serialization/deserialization of user data.
 * Implementations of this interface could be used for customizing serialization/deserialization of user related data
 * using serialization/deserialization libraries other than Jackson Databind, like:
 * - serialization libraries for specific JVM languages (e.g. Scala, Kotlin, ...)
 * - serialization libraries already in use in frameworks (e.g. JSON-B, Micronaut Serialization, ...)
 * - high performance serialization libraries (e.g. supporting compile-time databinding code generation)
 * - lower level libraries without support to data binding
 * <p>
 * This interface should not be directly implemented as an adapter to Jackson Databind. A more performant way to provide
 * custom implementations based on Jackson Databind is by extending {@link JacksonSerde}, which exposes additional
 * methods based on Jackson's types.
 * Furthermore, existing {@link JacksonSerde} implementations can be instantiated providing a custom configured Jackson
 * ObjectMapper, see {@link JacksonSerde#of(DataType, ObjectMapper)}.
 */
public interface ArangoSerde {

    /**
     * @return the data type supported by this implementation
     */
    DataType getDataType();

    /**
     * Serializes the object into the target data type. For data type {@link DataType#JSON}, the serialized JSON string
     * must be encoded into a byte array using the UTF-8 charset.
     *
     * @param value object to serialize
     * @return serialized byte array
     */
    byte[] serialize(Object value);

    /**
     * Deserializes the content and binds it to the target data type.
     * For data type {@link DataType#JSON}, the byte array is the JSON string encoded using the UTF-8 charset.
     *
     * @param content byte array to deserialize
     * @param clazz   class of target data type
     * @return deserialized object
     */
    default <T> T deserialize(byte[] content, Class<T> clazz) {
        return deserialize(content, (Type) clazz);
    }

    /**
     * Deserializes the content and binds it to the target data type.
     * For data type {@link DataType#JSON}, the byte array is the JSON string encoded using the UTF-8 charset.
     *
     * @param content byte array to deserialize
     * @param type    target data type
     * @return deserialized object
     */
    <T> T deserialize(byte[] content, Type type);

}
