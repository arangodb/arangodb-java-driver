package com.arangodb.serde;

import com.arangodb.ArangoDBException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.lang.reflect.Type;

class JacksonSerdeImpl implements JacksonSerde {

    protected final DataType dataType;
    protected final ObjectMapper mapper;

    JacksonSerdeImpl(final DataType dataType, final ObjectMapper mapper) {
        this.dataType = dataType;
        this.mapper = mapper;
    }

    @Override
    public DataType getDataType() {
        return dataType;
    }

    @Override
    public byte[] serialize(final Object value) {
        try {
            return mapper.writeValueAsBytes(value);
        } catch (JsonProcessingException e) {
            throw new ArangoDBException(e);
        }
    }

    @Override
    public <T> T deserialize(final byte[] content, final Type type) {
        try {
            return mapper.readerFor(mapper.constructType(type)).readValue(content);
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
