package com.arangodb.vst;

import com.arangodb.arch.UnstableApi;
import com.arangodb.vst.internal.AuthenticationRequest;
import com.arangodb.vst.internal.JwtAuthenticationRequest;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

public final class VstSerializers {

    static final JsonSerializer<AuthenticationRequest> AUTHENTICATION_REQUEST =
            new JsonSerializer<AuthenticationRequest>() {
                @Override
                public void serialize(@UnstableApi AuthenticationRequest value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
                    gen.writeStartArray();
                    gen.writeNumber(value.getVersion());
                    gen.writeNumber(value.getType());
                    gen.writeString(value.getEncryption());
                    gen.writeString(value.getUser());
                    gen.writeString(value.getPassword());
                    gen.writeEndArray();
                }
            };
    static final JsonSerializer<JwtAuthenticationRequest> JWT_AUTHENTICATION_REQUEST =
            new JsonSerializer<JwtAuthenticationRequest>() {
                @Override
                public void serialize(@UnstableApi JwtAuthenticationRequest value, JsonGenerator gen,
                                      SerializerProvider serializers) throws IOException {
                    gen.writeStartArray();
                    gen.writeNumber(value.getVersion());
                    gen.writeNumber(value.getType());
                    gen.writeString(value.getEncryption());
                    gen.writeString(value.getToken());
                    gen.writeEndArray();
                }
            };

}
