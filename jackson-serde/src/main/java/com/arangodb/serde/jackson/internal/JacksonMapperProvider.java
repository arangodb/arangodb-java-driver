package com.arangodb.serde.jackson.internal;

import com.arangodb.ContentType;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.function.Supplier;

/**
 * Not shaded in arangodb-java-driver-shaded.
 */
public interface JacksonMapperProvider extends Supplier<ObjectMapper> {
    static ObjectMapper of(final ContentType contentType) {
        if (contentType == ContentType.JSON) {
            return JsonJacksonMapperProvider.INSTANCE.get();
        } else if (contentType == ContentType.VPACK) {
            return VPackJacksonMapperProvider.INSTANCE.get();
        } else {
            throw new IllegalArgumentException("Unexpected value: " + contentType);
        }
    }
}
