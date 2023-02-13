package com.arangodb.internal.serde;

import com.arangodb.ContentType;
import com.arangodb.serde.ArangoSerde;
import com.arangodb.serde.ArangoSerdeProvider;
import com.fasterxml.jackson.databind.Module;

public class InternalSerdeProvider implements ArangoSerdeProvider {

    /**
     * Creates a new InternalSerde with default settings for the specified data type.
     *
     * @param contentType serialization target data type
     * @return the created InternalSerde
     */
    @Override
    public InternalSerde of(final ContentType contentType) {
        return create(contentType, null, null);
    }

    /**
     * Creates a new InternalSerde with default settings for the specified data type.
     *
     * @param contentType serialization target data type
     * @param userSerde user serde
     * @param protocolModule optional Jackson module to support protocol specific types
     * @return the created InternalSerde
     */
    public static InternalSerde create(final ContentType contentType, ArangoSerde userSerde, Module protocolModule) {
        return new InternalSerdeImpl(InternalMapperProvider.of(contentType), userSerde, protocolModule);
    }

}
