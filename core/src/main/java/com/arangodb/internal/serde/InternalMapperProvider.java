package com.arangodb.internal.serde;

import com.arangodb.ArangoDBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.core.StreamReadConstraints;
import tools.jackson.core.StreamReadFeature;
import tools.jackson.core.StreamWriteConstraints;
import tools.jackson.core.TokenStreamFactory;
import tools.jackson.core.json.JsonFactory;
import tools.jackson.core.json.JsonWriteFeature;
import tools.jackson.databind.json.JsonMapper;

import java.util.Iterator;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

class InternalMapperProvider {
    private static final Logger LOG = LoggerFactory.getLogger(InternalMapperProvider.class);

    static JsonMapper load() {
        ServiceLoader<TokenStreamFactory> sl = ServiceLoader.load(TokenStreamFactory.class);
        Iterator<TokenStreamFactory> iterator = sl.iterator();
        while (iterator.hasNext()) {
            TokenStreamFactory tsf;
            try {
                tsf = iterator.next();
            } catch (ServiceConfigurationError e) {
                LOG.warn("ServiceLoader failed to load JsonFactory", e);
                continue;
            }
            if (!"JSON".equals(tsf.getFormatName())) {
                LOG.debug("JSON not supported by TokenStreamFactory: {}", tsf.getClass().getName());
            } else if (!(tsf instanceof JsonFactory jf)) {
                LOG.debug("TokenStreamFactory is not instance of JsonFactory: {}", tsf.getClass().getName());
            } else {
                return new JsonMapper(
                        jf.rebuild()
                                .disable(JsonWriteFeature.ESCAPE_FORWARD_SLASHES)
                                .disable(JsonWriteFeature.COMBINE_UNICODE_SURROGATES_IN_UTF8)
                                .enable(StreamReadFeature.INCLUDE_SOURCE_IN_LOCATION)
                                .streamReadConstraints(StreamReadConstraints.builder()
                                        .maxNumberLength(Integer.MAX_VALUE)
                                        .maxStringLength(Integer.MAX_VALUE)
                                        .maxNestingDepth(Integer.MAX_VALUE)
                                        .maxNameLength(Integer.MAX_VALUE)
                                        .maxDocumentLength(Long.MAX_VALUE)
                                        .maxTokenCount(Integer.MAX_VALUE)
                                        .build())
                                .streamWriteConstraints(StreamWriteConstraints.builder()
                                        .maxNestingDepth(Integer.MAX_VALUE)
                                        .build())
                                .build());
            }
        }
        throw new ArangoDBException("No JsonFactory found for content type JSON");
    }
}
