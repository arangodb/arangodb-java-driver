package com.arangodb.util;

public interface ArangoSerialization extends ArangoDeserializer {
    byte[] serialize(final Object entity);
}
