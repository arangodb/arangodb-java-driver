package com.arangodb.serde.jackson.internal;

import com.arangodb.ArangoDBException;
import com.arangodb.ContentType;
import com.arangodb.internal.serde.JacksonUtils;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ServiceLoader;
import java.util.function.Supplier;

/**
 * Not shaded in arangodb-java-driver-shaded.
 */
public interface JacksonMapperProvider extends Supplier<ObjectMapper> {
    Logger LOG = LoggerFactory.getLogger(JacksonMapperProvider.class);

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
                JacksonUtils.tryConfigureJsonFactory(jf);
                return new ObjectMapper(jf);
            }
            LOG.debug("Required format ({}) not supported by JsonFactory: {}", formatName, jf.getClass().getName());
        }

        throw new ArangoDBException("No JsonFactory found for content type: " + contentType);
    }
}
