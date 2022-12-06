package com.arangodb.internal.serde;

import com.arangodb.jackson.dataformat.velocypack.VPackMapper;
import com.fasterxml.jackson.databind.ObjectMapper;

enum InternalVPackMapperProvider implements InternalMapperProvider {
    INSTANCE;

    @Override
    public ObjectMapper get() {
        return new VPackMapper();
    }
}
