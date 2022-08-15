package com.arangodb.internal.serde;

import com.arangodb.internal.serde.JsonMapperProvider;
import com.arangodb.internal.serde.VPackMapperProvider;
import com.arangodb.serde.DataType;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.function.Supplier;

public interface MapperProvider extends Supplier<ObjectMapper> {
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
