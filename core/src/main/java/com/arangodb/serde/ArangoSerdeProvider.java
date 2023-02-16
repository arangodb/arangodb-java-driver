package com.arangodb.serde;

import com.arangodb.ContentType;

public interface ArangoSerdeProvider {

    /**
     * Returns a serde instance for the given content type
     *
     * @param contentType content type
     * @return serde instance
     */
    ArangoSerde of(final ContentType contentType);

}
