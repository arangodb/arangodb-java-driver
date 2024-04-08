package com.arangodb.internal;

import com.arangodb.RequestContext;

import java.util.Optional;

public class RequestContextImpl implements RequestContext {
    private static final String TRANSACTION_ID = "x-arango-trx-id";

    private final String streamTransactionId;

    public RequestContextImpl() {
        this.streamTransactionId = null;
    }

    public RequestContextImpl(InternalRequest request) {
        this.streamTransactionId = request.getHeaderParam().get(TRANSACTION_ID);
    }

    @Override
    public Optional<String> getStreamTransactionId() {
        return Optional.ofNullable(streamTransactionId);
    }
}
