package com.arangodb.serde;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.function.Supplier;

interface MapperProvider extends Supplier<ObjectMapper> {
    static ObjectMapper of(final DataType dataType) {
        if (dataType == DataType.JSON) {
            return JsonMapperProvider.INSTANCE.get();
        } else if (dataType == DataType.VPACK) {
            return VPackMapperProvider.INSTANCE.get();
        } else {
            throw new IllegalArgumentException("Unexpected value: " + dataType);
        }
    }
}
