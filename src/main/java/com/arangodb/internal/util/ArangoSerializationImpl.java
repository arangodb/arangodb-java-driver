package com.arangodb.internal.util;

import com.arangodb.ArangoDBException;
import com.arangodb.serde.ArangoSerde;
import com.arangodb.util.ArangoDeserializer;
import com.arangodb.util.ArangoSerialization;
import com.arangodb.velocypack.VPackSlice;

import java.lang.reflect.Type;

public class ArangoSerializationImpl implements ArangoSerialization {

    private final ArangoDeserializer deserializer;
    private final ArangoSerde serde;

    public ArangoSerializationImpl(final ArangoDeserializer deserializer, final ArangoSerde serde) {
        super();
        this.deserializer = deserializer;
        this.serde = serde;
    }

    @Override
    public byte[] serialize(final Object entity) throws ArangoDBException {
        return serde.serialize(entity);
    }

    @Override
    public <T> T deserialize(final VPackSlice vpack, final Type type) throws ArangoDBException {
        return deserializer.deserialize(vpack, type);
    }

}
