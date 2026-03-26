package com.arangodb.serde;

import com.arangodb.RequestContext;

import java.util.Objects;

/**
 * Contract for serialization/deserialization of user data.
 * Implementations of this interface could be used for customizing serialization/deserialization of user related data
 * using serialization/deserialization libraries like:
 * - serialization libraries for specific JVM languages (e.g. Scala, Kotlin, ...)
 * - serialization libraries already in use in frameworks (e.g. JSON-B, Micronaut Serialization, ...)
 * - high performance serialization libraries (e.g. supporting compile-time data binding code generation)
 * - low-level libraries without support for data binding
 */
public interface ArangoSerde {

    /**
     * Serializes the object to UTF-8 byte encoded JSON string
     *
     * @param value object to serialize
     * @return serialized byte array
     */
    byte[] serialize(Object value);

    /**
     * Deserializes the content and binds it to the target data type.
     *
     * @param content     UTF-8 byte encoded JSON string
     * @param clazz   class of target data type
     * @return deserialized object
     */
    <T> T deserialize(byte[] content, Class<T> clazz);

    /**
     * Deserializes the content and binds it to the target data type.
     *
     * @param content     UTF-8 byte encoded JSON string
     * @param clazz   class of target data type
     * @param ctx     serde context, cannot be null
     * @return deserialized object
     */
    default <T> T deserialize(byte[] content, Class<T> clazz, RequestContext ctx) {
        Objects.requireNonNull(ctx);
        return deserialize(content, clazz);
    }
}
