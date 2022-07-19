package com.arangodb.serde;

import com.arangodb.ArangoDBException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

public enum SerdeUtils {
    INSTANCE;

    private final ObjectMapper jsonMapper = new ObjectMapper();

    /**
     * Parse a JSON string.
     *
     * @param json JSON string to parse
     * @return root of the parsed tree
     */
    public JsonNode parseJson(final String json) {
        try {
            return jsonMapper.readTree(json);
        } catch (JsonProcessingException e) {
            throw new ArangoDBException(e);
        }
    }

    /**
     * @param data JsonNode
     * @return JSON string
     */
    public String writeJson(final JsonNode data) {
        try {
            return jsonMapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            throw new ArangoDBException(e);
        }
    }

    public Type constructListType(Class<?> clazz) {
        return TypeFactory.defaultInstance().constructCollectionType(List.class, clazz);
    }

    public Type constructMapType(Class<?> keyClazz, Class<?> valueClazz) {
        return TypeFactory.defaultInstance().constructMapType(Map.class, keyClazz, valueClazz);
    }

}
