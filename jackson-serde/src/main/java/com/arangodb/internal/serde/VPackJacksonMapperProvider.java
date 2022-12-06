package com.arangodb.internal.serde;

import com.arangodb.jackson.dataformat.velocypack.VPackMapper;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Not shaded in arangodb-java-driver-shaded.
 */
enum VPackJacksonMapperProvider implements JacksonMapperProvider {
    INSTANCE;

    @Override
    public ObjectMapper get() {
        return new VPackMapper();
    }
}
