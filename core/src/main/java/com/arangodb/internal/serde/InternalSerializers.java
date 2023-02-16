package com.arangodb.internal.serde;

import com.arangodb.entity.CollectionType;
import com.arangodb.entity.arangosearch.CollectionLink;
import com.arangodb.entity.arangosearch.FieldLink;
import com.arangodb.util.RawBytes;
import com.arangodb.util.RawJson;
import com.arangodb.internal.InternalRequest;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public final class InternalSerializers {

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
    static final JsonSerializer<InternalRequest> REQUEST = new JsonSerializer<InternalRequest>() {
        @Override
        public void serialize(InternalRequest value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeStartArray();
            gen.writeNumber(value.getVersion());
            gen.writeNumber(value.getType());
            gen.writeString(value.getDbName().get());
            gen.writeNumber(value.getRequestType().getType());
            gen.writeString(value.getPath());
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

    private InternalSerializers() {
    }

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

}
