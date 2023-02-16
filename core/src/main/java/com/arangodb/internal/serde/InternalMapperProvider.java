package com.arangodb.internal.serde;

import com.arangodb.ArangoDBException;
import com.arangodb.ContentType;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ServiceLoader;
import java.util.function.Supplier;

public interface InternalMapperProvider extends Supplier<ObjectMapper> {
    static ObjectMapper of(final ContentType contentType) {
        String formatName;
        if (contentType == ContentType.JSON) {
            formatName = "JSON";
        } else if (contentType == ContentType.VPACK) {
            formatName = "Velocypack";
        } else {
            throw new IllegalArgumentException("Unexpected value: " + contentType);
        }

        ServiceLoader<JsonFactory> sl = ServiceLoader.load(JsonFactory.class);
        for (JsonFactory jf : sl) {
            if(formatName.equals(jf.getFormatName())){
                return new ObjectMapper(jf);
            }
        }

        throw new ArangoDBException("No JsonFactory found for content type: " + contentType);
    }
}
