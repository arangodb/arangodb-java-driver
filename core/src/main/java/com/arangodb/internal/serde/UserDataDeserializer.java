package com.arangodb.internal.serde;

import tools.jackson.core.JsonParser;
import tools.jackson.databind.BeanProperty;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.jsontype.TypeDeserializer;

import java.lang.reflect.Type;

import static com.arangodb.internal.serde.SerdeUtils.convertToType;

class UserDataDeserializer extends ValueDeserializer<Object> {
    private final Type targetType;
    private final InternalSerde serde;

    UserDataDeserializer(final InternalSerde serde) {
        targetType = null;
        this.serde = serde;
    }

    private UserDataDeserializer(final JavaType targetType, final InternalSerde serde) {
        this.targetType = convertToType(targetType);
        this.serde = serde;
    }

    @Override
    public Object deserialize(JsonParser p, DeserializationContext ctxt) {
        Class<?> clazz = (Class<?>) targetType;
        if (SerdeUtils.isManagedClass(clazz)) {
            return p.readValueAs(clazz);
        } else {
            return serde.deserializeUserData(SerdeUtils.extractBytes(p), clazz);
        }
    }

    @Override
    public Object deserializeWithType(JsonParser p, DeserializationContext ctxt, TypeDeserializer typeDeserializer) {
        return deserialize(p, ctxt);
    }

    @Override
    public ValueDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) {
        return new UserDataDeserializer(ctxt.getContextualType(), serde);
    }

}
