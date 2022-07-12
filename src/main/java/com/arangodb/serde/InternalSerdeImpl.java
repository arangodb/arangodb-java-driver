package com.arangodb.serde;

import com.arangodb.ArangoDBException;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

class InternalSerdeImpl extends JacksonSerdeImpl implements InternalSerde {

    InternalSerdeImpl(DataType dataType, ObjectMapper mapper) {
        super(dataType, mapper);
        mapper.registerModule(InternalModule.INSTANCE.get());
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    @Override
    // FIXME: refactor to:
    // return SerdeUtils.INSTANCE.writeJson(mapper.readTree(content));
    // afterwards dataType should not be needed anymore
    public String toJsonString(final byte[] content) {
        switch (dataType) {
            case JSON:
                return new String(content, StandardCharsets.UTF_8);
            case VPACK:
                try {
                    JsonNode tree = mapper.readTree(content);
                    return SerdeUtils.INSTANCE.writeJson(tree);
                } catch (IOException e) {
                    throw new ArangoDBException(e);
                }
            default:
                throw new IllegalArgumentException("Unexpected value: " + dataType);
        }
    }

    @Override
    public byte[] extract(final byte[] content, final String jsonPointer) {
        try {
            JsonNode target = parse(content).at(jsonPointer);
            return mapper.writeValueAsBytes(target);
        } catch (IOException e) {
            throw new ArangoDBException(e);
        }
    }

    @Override
    public JsonNode parse(byte[] content) {
        try {
            return mapper.readTree(content);
        } catch (IOException e) {
            throw new ArangoDBException(e);
        }
    }

    @Override
    public JsonNode parse(byte[] content, String jsonPointer) {
        try {
            return mapper.readTree(content).at(jsonPointer);
        } catch (IOException e) {
            throw new ArangoDBException(e);
        }
    }

}
