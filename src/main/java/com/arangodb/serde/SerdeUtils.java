package com.arangodb.serde;

import com.arangodb.ArangoDBException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public enum SerdeUtils {
    INSTANCE;

    private ObjectMapper jsonMapper = new ObjectMapper();

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

}
