package com.arangodb.serde;

import com.arangodb.ContentType;

public interface ArangoSerdeProvider {

    /**
     * @return a new serde instance
     */
    ArangoSerde create();

    /**
     * @return the supported content type
     */
    ContentType getContentType();
}
