package com.arangodb.internal.serde;

import com.fasterxml.jackson.databind.ObjectMapper;

enum JsonMapperProvider implements MapperProvider {
    INSTANCE;

    @Override
    public ObjectMapper get() {
        return new ObjectMapper();
    }
}
