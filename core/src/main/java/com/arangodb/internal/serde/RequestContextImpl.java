package com.arangodb.internal.serde;

import com.arangodb.serde.RequestContext;

import java.util.Optional;

public class RequestContextImpl implements RequestContext {
    private final String streamTransactionId;

    public RequestContextImpl(String streamTransactionId) {
        this.streamTransactionId = streamTransactionId;
    }

    @Override
    public Optional<String> getStreamTransactionId() {
        return Optional.ofNullable(streamTransactionId);
    }
}
