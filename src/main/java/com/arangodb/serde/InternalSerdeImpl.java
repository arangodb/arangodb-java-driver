package com.arangodb.serde;

import com.arangodb.ArangoDBException;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.lang.reflect.Type;

final class InternalSerdeImpl extends JacksonSerdeImpl implements InternalSerde {

    InternalSerdeImpl(ObjectMapper mapper) {
        super(mapper);
        mapper.registerModule(InternalModule.INSTANCE.get());
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    @Override
    public String toJsonString(final byte[] content) {
        try {
            return SerdeUtils.INSTANCE.writeJson(mapper.readTree(content));
        } catch (IOException e) {
            throw new ArangoDBException(e);
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

    @Override
    public <T> T deserialize(final JsonNode node, final Type type) {
        try {
            return mapper.readerFor(mapper.constructType(type)).readValue(node);
        } catch (IOException e) {
            throw new ArangoDBException(e);
        }
    }

}
