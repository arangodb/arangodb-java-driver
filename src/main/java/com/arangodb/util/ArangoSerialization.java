package com.arangodb.util;

import java.lang.reflect.Type;

public interface ArangoSerialization {
    byte[] serialize(final Object entity);
    <T> T deserialize(byte[] content, Type type);
}
