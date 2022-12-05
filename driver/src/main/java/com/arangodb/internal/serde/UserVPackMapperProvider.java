package com.arangodb.internal.serde;

import com.arangodb.jackson.dataformat.velocypack.VPackMapper;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Not shaded in arangodb-java-driver-shaded.
 */
enum UserVPackMapperProvider implements MapperProvider {
    INSTANCE;

    @Override
    public ObjectMapper get() {
        return new VPackMapper();
    }
}
