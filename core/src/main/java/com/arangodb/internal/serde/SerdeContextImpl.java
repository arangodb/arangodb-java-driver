package com.arangodb.internal.serde;

import com.arangodb.serde.SerdeContext;

public class SerdeContextImpl implements SerdeContext {
    private final String streamTransactionId;

    public SerdeContextImpl(String streamTransactionId) {
        this.streamTransactionId = streamTransactionId;
    }

    @Override
    public String getStreamTransactionId() {
        return streamTransactionId;
    }
}
