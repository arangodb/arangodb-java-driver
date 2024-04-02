package com.arangodb.model;

public abstract class TransactionalOptions<T extends TransactionalOptions<T>> {

    abstract T getThis();

    private String streamTransactionId;

    public String getStreamTransactionId() {
        return streamTransactionId;
    }

    /**
     * @param streamTransactionId If set, the operation will be executed within the transaction.
     * @return options
     */
    public T streamTransactionId(final String streamTransactionId) {
        this.streamTransactionId = streamTransactionId;
        return getThis();
    }

}
