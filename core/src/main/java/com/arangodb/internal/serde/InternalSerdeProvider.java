package com.arangodb.internal.serde;

import com.arangodb.ContentType;
import com.arangodb.serde.ArangoSerde;
import com.arangodb.serde.ArangoSerdeProvider;
import com.fasterxml.jackson.databind.Module;

public class InternalSerdeProvider implements ArangoSerdeProvider {

    private final ContentType contentType;

    /**
     * @param contentType serialization target data type
     */
    public InternalSerdeProvider(final ContentType contentType) {
        this.contentType = contentType;
    }

    /**
     * Creates a new InternalSerde with default settings.
     *
     * @return the created InternalSerde
     */
    @Override
    public InternalSerde create() {
        return create(null, null);
    }

    /**
     * Creates a new InternalSerde with default settings.
     *
     * @param userSerde      user serde
     * @param protocolModule optional Jackson module to support protocol specific types
     * @return the created InternalSerde
     */
    public InternalSerde create(ArangoSerde userSerde, Module protocolModule) {
        return new InternalSerdeImpl(InternalMapperProvider.of(contentType), userSerde, protocolModule);
    }

    @Override
    public ContentType getContentType() {
        return contentType;
    }

}
