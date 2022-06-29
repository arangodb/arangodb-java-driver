package com.arangodb.serde;

import com.arangodb.internal.velocystream.internal.AuthenticationRequest;
import com.arangodb.velocystream.Request;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.util.Map;

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

    static final JsonSerializer<Request> REQUEST = new JsonSerializer<Request>() {
        @Override
        public void serialize(Request value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeStartArray();
            gen.writeNumber(value.getVersion());
            gen.writeNumber(value.getType());
            gen.writeString(value.getDbName().get());
            gen.writeNumber(value.getRequestType().getType());
            gen.writeString(value.getRequest());
            gen.writeStartObject();
            for (final Map.Entry<String, String> entry : value.getQueryParam().entrySet()) {
                gen.writeStringField(entry.getKey(), entry.getValue());
            }
            gen.writeEndObject();
            gen.writeStartObject();
            for (final Map.Entry<String, String> entry : value.getHeaderParam().entrySet()) {
                gen.writeStringField(entry.getKey(), entry.getValue());
            }
            gen.writeEndObject();
            gen.writeEndArray();
        }
    };

}
