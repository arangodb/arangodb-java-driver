package com.arangodb.serde.jackson3;

import com.arangodb.serde.ArangoSerde;
import com.arangodb.RequestContext;
import com.arangodb.serde.jackson3.internal.JacksonSerdeImpl;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ObjectMapper;

import java.util.function.Consumer;

import static com.arangodb.serde.jackson3.internal.JacksonSerdeImpl.SERDE_CONTEXT_ATTRIBUTE_NAME;

/**
 * User data serde based on Jackson Databind. Not shaded in arangodb-java-driver-shaded.
 */
public interface JacksonSerde extends ArangoSerde {

    /**
     * Creates a new JacksonSerde using the provided ObjectMapper.
     *
     * @param mapper Jackson ObjectMapper to use
     * @return the created JacksonSerde
     */
    static JacksonSerde create(final ObjectMapper mapper) {
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

    /**
     * Allows configuring the underlying Jackson ObjectMapper
     *
     * @param configureFunction function to configure the Jackson ObjectMapper
     */
    JacksonSerde configure(final Consumer<ObjectMapper> configureFunction);

}
