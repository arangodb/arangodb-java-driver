package com.arangodb.serde.jackson;

import com.arangodb.serde.ArangoSerde;
import com.arangodb.RequestContext;
import com.arangodb.serde.jackson.internal.JacksonSerdeImpl;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.json.JsonMapper;

import static com.arangodb.serde.jackson.internal.JacksonSerdeImpl.SERDE_CONTEXT_ATTRIBUTE_NAME;

/**
 * User data serde based on Jackson Databind. Not shaded in arangodb-java-driver-shaded.
 */
public interface JacksonSerde extends ArangoSerde {

    /**
     * Creates a new JacksonSerde.
     *
     * @return the created JacksonSerde
     */
    static JacksonSerde create() {
        return create(JsonMapper.builder()
                .annotationIntrospector(new ArangoAnnotationIntrospector())
                .build());
    }

    /**
     * Creates a new JacksonSerde using the provided JsonMapper.
     *
     * @param mapper Jackson JsonMapper to use
     * @return the created JacksonSerde
     */
    static JacksonSerde create(final JsonMapper mapper) {
        return new JacksonSerdeImpl(mapper);
    }

    /**
     * Extracts the {@link RequestContext} from the current {@link DeserializationContext}.
     *
     * @param ctx current Jackson {@link DeserializationContext}
     * @return current {@link RequestContext}
     */
    static RequestContext getRequestContext(DeserializationContext ctx) {
        return (RequestContext) ctx.getAttribute(SERDE_CONTEXT_ATTRIBUTE_NAME);
    }

}
