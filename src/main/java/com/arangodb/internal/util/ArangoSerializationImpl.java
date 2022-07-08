package com.arangodb.internal.util;

import com.arangodb.ArangoDBException;
import com.arangodb.serde.ArangoSerde;
import com.arangodb.util.ArangoSerialization;

import java.lang.reflect.Type;

public class ArangoSerializationImpl implements ArangoSerialization {

    private final ArangoSerde serde;

    public ArangoSerializationImpl(final ArangoSerde serde) {
        super();
        this.serde = serde;
    }

    @Override
    public byte[] serialize(final Object entity) throws ArangoDBException {
        return serde.serialize(entity);
    }

    @Override
    public <T> T deserialize(byte[] content, Type type) {
        return serde.deserialize(content, type);
    }

}
