package com.arangodb.internal.serde;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

class UserDataSerializer extends JsonSerializer<Object> {
    private final InternalSerde serde;

    UserDataSerializer(InternalSerde serde) {
        this.serde = serde;
    }

    @Override
    public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value != null && JsonNode.class.isAssignableFrom(value.getClass())) {
            gen.writeTree((JsonNode) value);
        } else {
            // TODO: find a way to append raw bytes directly
            // see https://github.com/FasterXML/jackson-core/issues/914
            // TODO: check gen.getOutputContext()
            try (JsonParser parser = gen.getCodec().getFactory().createParser(serde.serializeUserData(value))) {
                gen.writeTree(parser.readValueAsTree());
            }
        }
    }
}
