package com.arangodb.serde;

/**
 * Context holding information about the current request and response.
 */
public interface SerdeContext {
    /**
     * @return the stream transaction id of the request (if any) or {@code null}
     */
    String getStreamTransactionId();
}
