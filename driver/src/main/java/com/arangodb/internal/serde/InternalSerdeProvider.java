package com.arangodb.internal.serde;

import com.arangodb.ContentType;
import com.arangodb.serde.ArangoSerde;
import com.arangodb.serde.ArangoSerdeProvider;

public class InternalSerdeProvider implements ArangoSerdeProvider {

    /**
     * Creates a new InternalSerde with default settings for the specified data type.
     *
     * @param contentType serialization target data type
     * @return the created InternalSerde
     */
    @Override
    public InternalSerde of(final ContentType contentType) {
        return create(contentType, null);
    }

    /**
     * Creates a new InternalSerde with default settings for the specified data type.
     *
     * @param contentType serialization target data type
     * @return the created InternalSerde
     */
    public static InternalSerde create(final ContentType contentType, ArangoSerde userSerde) {
        return new InternalSerdeImpl(InternalMapperProvider.of(contentType), userSerde);
    }

}
