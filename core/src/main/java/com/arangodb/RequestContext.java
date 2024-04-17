package com.arangodb;

import com.arangodb.internal.RequestContextImpl;

import java.util.Optional;

/**
 * Context holding information about the current request and response.
 */
public interface RequestContext {

    RequestContext EMPTY = new RequestContextImpl();

    /**
     * @return the stream transaction id of the request (if any) or {@code null}
     */
    Optional<String> getStreamTransactionId();
}
