package com.arangodb.internal.serde;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Not shaded in arangodb-java-driver-shaded.
 */
enum JsonJacksonMapperProvider implements JacksonMapperProvider {
    INSTANCE;

    @Override
    public ObjectMapper get() {
        return new ObjectMapper();
    }
}
