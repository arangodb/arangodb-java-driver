package com.arangodb.serde;

import com.arangodb.internal.velocystream.internal.AuthenticationRequest;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

final class InternalSerializers {

    private InternalSerializers() {
    }

    static final JsonSerializer<AuthenticationRequest> AUTHENTICATION_REQUEST = new JsonSerializer<AuthenticationRequest>() {
        @Override
        public void serialize(AuthenticationRequest value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeStartArray();
            gen.writeNumber(value.getVersion());
            gen.writeNumber(value.getType());
            gen.writeString(value.getEncryption());
            gen.writeString(value.getUser());
            gen.writeString(value.getPassword());
            gen.writeEndArray();
        }
    };

}
