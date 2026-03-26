package com.arangodb.serde.jackson;

import com.arangodb.ArangoDBException;
import com.arangodb.internal.serde.JacksonUtils;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

/**
 * Not shaded in arangodb-java-driver-shaded.
 */
public class JacksonMapperProvider {
    private static final Logger LOG = LoggerFactory.getLogger(JacksonMapperProvider.class);

    public static ObjectMapper load() {
        ServiceLoader<JsonFactory> sl = ServiceLoader.load(JsonFactory.class);
        Iterator<JsonFactory> iterator = sl.iterator();
        while (iterator.hasNext()) {
            JsonFactory jf;
            try {
                jf = iterator.next();
            } catch (ServiceConfigurationError e) {
                LOG.warn("ServiceLoader failed to load JsonFactory", e);
                continue;
            }
            if ("JSON".equals(jf.getFormatName())) {
                JacksonUtils.tryConfigureJsonFactory(jf);
                return new ObjectMapper(jf);
            }
            LOG.debug("JSON not supported by JsonFactory: {}", jf.getClass().getName());
        }

        throw new ArangoDBException("No JsonFactory found for content type JSON");
    }
}
