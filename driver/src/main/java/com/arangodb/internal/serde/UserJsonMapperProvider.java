package com.arangodb.internal.serde;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Not shaded in arangodb-java-driver-shaded.
 */
enum UserJsonMapperProvider implements MapperProvider {
    INSTANCE;

    @Override
    public ObjectMapper get() {
        return new ObjectMapper();
    }
}
