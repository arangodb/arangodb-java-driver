package com.arangodb.internal.serde;

import com.arangodb.ContentType;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.function.Supplier;

/**
 * Not shaded in arangodb-java-driver-shaded.
 */
public interface UserMapperProvider extends Supplier<ObjectMapper> {
    static ObjectMapper of(final ContentType contentType) {
        if (contentType == ContentType.JSON) {
            return UserJsonMapperProvider.INSTANCE.get();
        } else if (contentType == ContentType.VPACK) {
            return UserVPackMapperProvider.INSTANCE.get();
        } else {
            throw new IllegalArgumentException("Unexpected value: " + contentType);
        }
    }
}
