package com.arangodb.serde.jackson.internal;

import com.arangodb.internal.serde.SerdeContextImpl;
import com.arangodb.serde.SerdeContext;
import com.arangodb.serde.jackson.JacksonSerde;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.cfg.ContextAttributes;

import java.io.IOException;
import java.util.Objects;
import java.util.function.Consumer;

import static com.arangodb.internal.serde.SerdeUtils.SERDE_CONTEXT_ATTRIBUTE_NAME;

/**
 * Not shaded in arangodb-java-driver-shaded.
 */
public final class JacksonSerdeImpl implements JacksonSerde {

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
        return deserialize(content, type, SerdeContextImpl.EMPTY);
    }

    @Override
    public <T> T deserialize(byte[] content, Class<T> type, SerdeContext ctx) {
        Objects.requireNonNull(ctx);
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
