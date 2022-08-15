package com.arangodb.internal.serde;

import com.arangodb.entity.CollectionType;
import com.arangodb.entity.arangosearch.CollectionLink;
import com.arangodb.entity.arangosearch.FieldLink;
import com.arangodb.internal.velocystream.internal.AuthenticationRequest;
import com.arangodb.internal.velocystream.internal.JwtAuthenticationRequest;
import com.arangodb.util.RawBytes;
import com.arangodb.util.RawJson;
import com.arangodb.velocystream.Request;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public final class InternalSerializers {

    public static class CollectionSchemaRuleSerializer extends JsonSerializer<String> {
        @Override
        public void serialize(String value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeTree(SerdeUtils.INSTANCE.parseJson(value));
        }
    }

    public static class FieldLinksSerializer extends JsonSerializer<Collection<FieldLink>> {
        @Override
        public void serialize(Collection<FieldLink> value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            Map<String, FieldLink> mapLikeValue = new HashMap<>();
            for (FieldLink fl : value) {
                mapLikeValue.put(fl.getName(), fl);
            }
            gen.writeObject(mapLikeValue);
        }
    }

    public static class CollectionLinksSerializer extends JsonSerializer<Collection<CollectionLink>> {
        @Override
        public void serialize(Collection<CollectionLink> value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            Map<String, CollectionLink> mapLikeValue = new HashMap<>();
            for (CollectionLink cl : value) {
                mapLikeValue.put(cl.getName(), cl);
            }
            gen.writeObject(mapLikeValue);
        }
    }

    private InternalSerializers() {
    }

    static final JsonSerializer<RawJson> RAW_JSON_SERIALIZER = new JsonSerializer<RawJson>() {
        @Override
        public void serialize(RawJson value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeTree(SerdeUtils.INSTANCE.parseJson(value.getValue()));
        }
    };

    static final JsonSerializer<RawBytes> RAW_BYTES_SERIALIZER = new JsonSerializer<RawBytes>() {
        @Override
        public void serialize(RawBytes value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            // TODO: find a way to append raw bytes directly
            // see https://github.com/FasterXML/jackson-dataformats-binary/issues/331
            try (JsonParser parser = gen.getCodec().getFactory().createParser(value.getValue())) {
                gen.writeTree(parser.readValueAsTree());
            }
        }
    };

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

    static final JsonSerializer<JwtAuthenticationRequest> JWT_AUTHENTICATION_REQUEST = new JsonSerializer<JwtAuthenticationRequest>() {
        @Override
        public void serialize(JwtAuthenticationRequest value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeStartArray();
            gen.writeNumber(value.getVersion());
            gen.writeNumber(value.getType());
            gen.writeString(value.getEncryption());
            gen.writeString(value.getToken());
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

    static final JsonSerializer<CollectionType> COLLECTION_TYPE = new JsonSerializer<CollectionType>() {
        @Override
        public void serialize(CollectionType value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeNumber(value.getType());
        }
    };

}
