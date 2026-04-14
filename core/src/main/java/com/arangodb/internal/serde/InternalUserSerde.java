package com.arangodb.internal.serde;

import com.arangodb.RequestContext;
import com.arangodb.serde.ArangoSerde;
import tools.jackson.core.*;
import tools.jackson.core.json.JsonFactory;
import tools.jackson.databind.cfg.ContextAttributes;
import tools.jackson.databind.json.JsonMapper;

final class InternalUserSerde implements ArangoSerde {
    public static final String SERDE_CONTEXT_ATTRIBUTE_NAME = "arangoRequestContext";

    private final JsonMapper mapper;

    InternalUserSerde() {
        var jsonFactory = JsonFactory.builder()
                .streamReadConstraints(StreamReadConstraints.builder()
                        .maxNumberLength(Integer.MAX_VALUE)
                        .maxStringLength(Integer.MAX_VALUE)
                        .maxNestingDepth(Integer.MAX_VALUE)
                        .maxNameLength(Integer.MAX_VALUE)
                        .maxDocumentLength(Long.MAX_VALUE)
                        .maxTokenCount(Integer.MAX_VALUE)
                        .build())
                .streamWriteConstraints(StreamWriteConstraints.builder()
                        .maxNestingDepth(Integer.MAX_VALUE)
                        .build())
                .build();

        var builder = JsonMapper.builder(jsonFactory)
                .deactivateDefaultTyping()
                .annotationIntrospector(new InternalUserSerdeAnnotationIntrospector());

        this.mapper = builder.build();
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
        if (content == null || content.length == 0) {
            return null;
        }
        return mapper.readerFor(mapper.constructType(type))
                .with(ContextAttributes.getEmpty().withPerCallAttribute(SERDE_CONTEXT_ATTRIBUTE_NAME, ctx))
                .readValue(content);
    }
}
