package com.arangodb.serde;

import com.arangodb.internal.ArangoResponseField;
import com.arangodb.jackson.dataformat.velocypack.VPackMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.lang.reflect.Type;

public interface InternalSerde extends JacksonSerde {

    /**
     * Creates a new InternalSerde with default settings for the specified data type.
     *
     * @param dataType serialization target data type
     * @return the created InternalSerde
     */
    static InternalSerde of(final DataType dataType) {
        if (dataType == DataType.JSON) {
            return new InternalSerdeImpl(dataType, new ObjectMapper());
        } else if (dataType == DataType.VPACK) {
            return new InternalSerdeImpl(dataType, new VPackMapper());
        } else {
            throw new IllegalStateException("Unexpected value: " + dataType);
        }
    }

    /**
     * Used for logging and debugging.
     *
     * @param content byte array
     * @return JSON string
     */
    String toJsonString(byte[] content);

    /**
     * Extract the nested content pointed by the json pointer.
     * Used for extracting nested user data.
     *
     * @param content     byte array
     * @param jsonPointer location of user data
     * @return byte array
     */
    byte[] extract(byte[] content, String jsonPointer);

    /**
     * TODO
     */
    JsonNode parse(byte[] content);

    /**
     * TODO
     */
    JsonNode parse(byte[] content, String jsonPointer);

    /**
     * TODO
     */
    default <T> T deserialize(byte[] content, String jsonPointer, Class<T> clazz){
        return deserialize(content, jsonPointer, (Type) clazz);
    }

    /**
     * TODO
     */
    default <T> T deserialize(byte[] content, String jsonPointer, Type type) {
        return deserialize(parse(content, jsonPointer), type);
    }

}
