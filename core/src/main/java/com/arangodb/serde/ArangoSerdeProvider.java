package com.arangodb.serde;

import com.arangodb.ArangoDBException;
import com.arangodb.ContentType;
import com.arangodb.internal.serde.InternalSerdeProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

public interface ArangoSerdeProvider {

    static ArangoSerdeProvider of(ContentType contentType) {
        Logger LOG = LoggerFactory.getLogger(ArangoSerdeProvider.class);

        ServiceLoader<ArangoSerdeProvider> loader = ServiceLoader.load(ArangoSerdeProvider.class);
        ArangoSerdeProvider serdeProvider = null;
        Iterator<ArangoSerdeProvider> iterator = loader.iterator();
        while (iterator.hasNext()) {
            ArangoSerdeProvider p;
            try {
                p = iterator.next();
            } catch (ServiceConfigurationError e) {
                LOG.warn("ServiceLoader failed to load ArangoSerdeProvider", e);
                continue;
            }
            if (contentType.equals(p.getContentType())) {
                if (serdeProvider != null) {
                    throw new ArangoDBException("Found multiple serde providers! Please set explicitly the one to use.");
                }
                serdeProvider = p;
            }
        }
        if (serdeProvider == null) {
            LOG.warn("No ArangoSerdeProvider found, using InternalSerdeProvider. Please consider registering a custom " +
                    "ArangoSerdeProvider to avoid depending on internal classes which are not part of the public API.");
            serdeProvider = new InternalSerdeProvider(contentType);
        }
        return serdeProvider;
    }

    /**
     * @return a new serde instance
     */
    ArangoSerde create();

    /**
     * @return the supported content type
     */
    ContentType getContentType();
}
