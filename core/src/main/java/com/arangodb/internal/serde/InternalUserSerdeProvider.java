package com.arangodb.internal.serde;

import com.arangodb.serde.ArangoSerde;
import com.arangodb.serde.ArangoSerdeProvider;

public class InternalUserSerdeProvider implements ArangoSerdeProvider {

    /**
     * Creates a new InternalSerde with default settings.
     *
     * @return the created InternalSerde
     */
    @Override
    public ArangoSerde create() {
        return new InternalUserSerde();
    }

}
