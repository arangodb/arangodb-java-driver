package com.arangodb.internal.serde;

import com.fasterxml.jackson.databind.ObjectMapper;

enum InternalJsonMapperProvider implements InternalMapperProvider {
    INSTANCE;

    @Override
    public ObjectMapper get() {
        return new ObjectMapper();
    }
}
