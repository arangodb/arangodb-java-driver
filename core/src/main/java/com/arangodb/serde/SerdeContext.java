package com.arangodb.serde;

import com.arangodb.internal.serde.SerdeContextImpl;

/**
 * Context holding information about the current request and response.
 */
public interface SerdeContext {

    SerdeContext EMPTY = new SerdeContextImpl(null);

    /**
     * @return the stream transaction id of the request (if any) or {@code null}
     */
    String getStreamTransactionId();
}
