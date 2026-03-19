package com.arangodb.serde.jackson3.internal;

import com.arangodb.RequestContext;
import com.arangodb.serde.jackson3.JacksonSerde;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.cfg.ContextAttributes;

import java.util.Objects;


/**
 * Not shaded in arangodb-java-driver-shaded.
 */
public final class JacksonSerdeImpl implements JacksonSerde {
    public static final String SERDE_CONTEXT_ATTRIBUTE_NAME = "arangoRequestContext";

    private final ObjectMapper mapper;

    public JacksonSerdeImpl(final ObjectMapper mapper) {
        this.mapper = mapper.rebuild()
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .annotationIntrospector(new ArangoSerdeAnnotationIntrospector())
                .build();
    }

    @Override
    public byte[] serialize(final Object value) {
        return mapper.writeValueAsBytes(value);
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
        return mapper.readerFor(mapper.constructType(type))
                .with(ContextAttributes.getEmpty().withPerCallAttribute(SERDE_CONTEXT_ATTRIBUTE_NAME, ctx))
                .readValue(content);
    }
}
