package com.arangodb.internal.serde;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;

import java.io.IOException;
import java.lang.reflect.Type;

import static com.arangodb.internal.serde.SerdeUtils.convertToType;

class UserDataDeserializer extends JsonDeserializer<Object> implements ContextualDeserializer {
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
    public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        Class<?> clazz = (Class<?>) targetType;
        if (SerdeUtils.isManagedClass(clazz)) {
            return p.readValueAs(clazz);
        } else {
            return serde.deserializeUserData(SerdeUtils.extractBytes(p), clazz);
        }
    }

    @Override
    public Object deserializeWithType(JsonParser p, DeserializationContext ctxt, TypeDeserializer typeDeserializer) throws IOException {
        return deserialize(p, ctxt);
    }

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) {
        return new UserDataDeserializer(ctxt.getContextualType(), serde);
    }

}
