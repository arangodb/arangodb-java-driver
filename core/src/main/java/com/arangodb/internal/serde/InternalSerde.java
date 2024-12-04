package com.arangodb.internal.serde;

import com.arangodb.arch.UsedInApi;
import com.arangodb.serde.ArangoSerde;
import com.arangodb.ContentType;
import com.fasterxml.jackson.databind.JsonNode;

import java.lang.reflect.Type;

@UsedInApi
public interface InternalSerde extends ArangoSerde {

    /**
     * Used for logging and debugging.
     *
     * @param content byte array
     * @return JSON string
     * @implSpec return {@code "[Unparsable data]"} in case of parsing exception
     */
    String toJsonString(byte[] content);

    /**
     * Extract the nested content pointed by the json pointer.
     * Used for extracting nested user data.
     *
     * @param content     byte array
     * @param jsonPointer location of data to be extracted
     * @return byte array
     */
    byte[] extract(byte[] content, String jsonPointer);

    /**
     * Deserializes the content and binds it to the target data type.
     * For data type {@link ContentType#JSON}, the byte array is the JSON string encoded using the UTF-8 charset.
     *
     * @param content byte array to deserialize
     * @param type    target data type
     * @return deserialized object
     */
    <T> T deserialize(byte[] content, Type type);

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

    /**
     * Parses the content.
     *
     * @param content VPack or byte encoded JSON string
     * @return root of the parsed tree
     */
    JsonNode parse(byte[] content);

    /**
     * Parses the content at json pointer.
     *
     * @param content     VPack or byte encoded JSON string
     * @param jsonPointer location of data to be parsed
     * @return root of the parsed tree
     */
    JsonNode parse(byte[] content, String jsonPointer);

    /**
     * Deserializes the content at json pointer and binds it to the target data type.
     * For data type {@link ContentType#JSON}, the byte array is the JSON string encoded using the UTF-8 charset.
     *
     * @param content     byte array to deserialize
     * @param jsonPointer location of data to be deserialized
     * @param clazz       class of target data type
     * @return deserialized object
     */
    default <T> T deserialize(byte[] content, String jsonPointer, Class<T> clazz) {
        return deserialize(content, jsonPointer, (Type) clazz);
    }

    /**
     * Deserializes the content at json pointer and binds it to the target data type.
     * For data type {@link ContentType#JSON}, the byte array is the JSON string encoded using the UTF-8 charset.
     *
     * @param content     byte array to deserialize
     * @param jsonPointer location of data to be deserialized
     * @param type        target data type
     * @return deserialized object
     */
    default <T> T deserialize(byte[] content, String jsonPointer, Type type) {
        return deserialize(extract(content, jsonPointer), type);
    }

    /**
     * Serializes the object into the target data type, using the user serde.
     *
     * @param value object to serialize
     * @return serialized byte array
     */
    byte[] serializeUserData(Object value);

    /**
     * Serializes each element in the collection using the user serde.
     *
     * @param value objects to serialize
     * @return serialized byte array
     */
    byte[] serializeCollectionUserData(Iterable<?> value);

    /**
     * Deserializes the content and binds it to the target data type, using the user serde.
     *
     * @param content byte array to deserialize
     * @param clazz   class of target data type
     * @return deserialized object
     */
    <T> T deserializeUserData(byte[] content, Class<T> clazz);

    /**
     * Deserializes the content and binds it to the target data type, using the user serde.
     *
     * @param content byte array to deserialize
     * @param type    target data type
     * @return deserialized object
     */
    <T> T deserializeUserData(byte[] content, Type type);

    /**
     * @return the user serde
     */
    ArangoSerde getUserSerde();
}
