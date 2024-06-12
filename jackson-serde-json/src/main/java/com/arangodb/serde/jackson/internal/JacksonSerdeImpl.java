package com.arangodb.serde.jackson.internal;

import com.arangodb.RequestContext;
import com.arangodb.serde.jackson.JacksonSerde;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.cfg.ContextAttributes;

import java.io.IOException;
import java.util.Objects;
import java.util.function.Consumer;


/**
 * Not shaded in arangodb-java-driver-shaded.
 */
public final class JacksonSerdeImpl implements JacksonSerde {
    public static final String SERDE_CONTEXT_ATTRIBUTE_NAME = "arangoRequestContext";

    private final ObjectMapper mapper;

    public JacksonSerdeImpl(final ObjectMapper mapper) {
        this.mapper = mapper;
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.setAnnotationIntrospector(new ArangoSerdeAnnotationIntrospector());
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
    public <T> T deserialize(final byte[] content, final Class<T> type) {
        return deserialize(content, type, RequestContext.EMPTY);
    }

    @Override
    public <T> T deserialize(byte[] content, Class<T> type, RequestContext ctx) {
        Objects.requireNonNull(ctx);
        if (content == null || content.length == 0) {
            return null;
        }
        try {
            return mapper.readerFor(mapper.constructType(type))
                    .with(ContextAttributes.getEmpty().withPerCallAttribute(SERDE_CONTEXT_ATTRIBUTE_NAME, ctx))
                    .readValue(content);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public JacksonSerde configure(Consumer<ObjectMapper> configureFunction) {
        configureFunction.accept(mapper);
        return this;
    }

}
