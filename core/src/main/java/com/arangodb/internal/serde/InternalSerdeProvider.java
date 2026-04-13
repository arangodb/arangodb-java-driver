package com.arangodb.internal.serde;

import com.arangodb.serde.ArangoSerde;
import com.arangodb.serde.ArangoSerdeProvider;

public class InternalSerdeProvider implements ArangoSerdeProvider {

    /**
     * Creates a new InternalSerde with default settings.
     *
     * @return the created InternalSerde
     */
    @Override
    public InternalSerde create() {
        return create(null);
    }

    /**
     * Creates a new InternalSerde with default settings.
     *
     * @param userSerde user serde
     * @return the created InternalSerde
     */
    public InternalSerde create(ArangoSerde userSerde) {
        return new InternalSerdeImpl(userSerde);
    }

}
