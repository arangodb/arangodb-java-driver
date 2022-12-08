package com.arangodb.serde.jackson.internal;

import com.arangodb.serde.jackson.JacksonSerde;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.function.Consumer;

/**
 * Not shaded in arangodb-java-driver-shaded.
 */
public final class JacksonSerdeImpl implements JacksonSerde {

    private final ObjectMapper mapper;

    public JacksonSerdeImpl(final ObjectMapper mapper) {
        this.mapper = mapper;
        try {
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        } catch (Exception e) {
            // to be safe in case the provided Jackson version does not support the methods above
            e.printStackTrace();
        }
    }

    @Override
    public byte[] serialize(final Object value) {
        try {
            return mapper.writeValueAsBytes(value);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T> T deserialize(final byte[] content, final Type type) {
        try {
            return mapper.readerFor(mapper.constructType(type)).readValue(content);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void configure(Consumer<ObjectMapper> configureFunction) {
        configureFunction.accept(mapper);
    }

}
