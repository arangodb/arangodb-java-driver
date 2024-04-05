package com.arangodb.internal.serde;

import com.arangodb.serde.SerdeContext;

import java.util.Optional;

public class SerdeContextImpl implements SerdeContext {
    private final String streamTransactionId;

    public SerdeContextImpl(String streamTransactionId) {
        this.streamTransactionId = streamTransactionId;
    }

    @Override
    public Optional<String> getStreamTransactionId() {
        return Optional.ofNullable(streamTransactionId);
    }
}
