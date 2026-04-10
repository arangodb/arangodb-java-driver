package com.arangodb.internal.serde;

import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;

class UserDataSerializer extends ValueSerializer<Object> {
    private final InternalSerde serde;

    UserDataSerializer(InternalSerde serde) {
        this.serde = serde;
    }

    @Override
    public void serialize(Object value, JsonGenerator gen, SerializationContext ctxt) {
        if (value != null && JsonNode.class.isAssignableFrom(value.getClass())) {
            gen.writeTree((JsonNode) value);
        } else {
            gen.writeRawValue(new RawUserDataValue(serde.serializeUserData(value)));
        }
    }
}
