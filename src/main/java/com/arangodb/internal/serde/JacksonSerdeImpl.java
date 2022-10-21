package com.arangodb.internal.serde;

import com.arangodb.ArangoDBException;
import com.arangodb.serde.JacksonSerde;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.function.Consumer;

public class JacksonSerdeImpl implements JacksonSerde {

    protected final ObjectMapper mapper;

    public JacksonSerdeImpl(final ObjectMapper mapper) {
        this.mapper = mapper;
        mapper.deactivateDefaultTyping();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
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
    public void configure(Consumer<ObjectMapper> configureFunction) {
        configureFunction.accept(mapper);
    }

}
