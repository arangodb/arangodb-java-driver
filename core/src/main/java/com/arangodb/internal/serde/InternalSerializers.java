package com.arangodb.internal.serde;

import com.arangodb.entity.CollectionType;
import com.arangodb.entity.arangosearch.CollectionLink;
import com.arangodb.entity.arangosearch.FieldLink;
import com.arangodb.internal.ArangoRequestParam;
import com.arangodb.util.RawJson;
import com.arangodb.internal.InternalRequest;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class InternalSerializers {

    static final JsonSerializer<RawJson> RAW_JSON_SERIALIZER = new JsonSerializer<RawJson>() {
        @Override
        public void serialize(RawJson value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            if (JsonFactory.FORMAT_NAME_JSON.equals(gen.getCodec().getFactory().getFormatName())) {
                gen.writeRawValue(new RawUserDataValue(value.get().getBytes(StandardCharsets.UTF_8)));
            } else {
                try (JsonParser parser = SerdeUtils.INSTANCE.getJsonMapper().getFactory().createParser(value.get())) {
                    parser.nextToken();
                    gen.copyCurrentStructure(parser);
                }
            }
        }
    };
    static final JsonSerializer<InternalRequest> REQUEST = new JsonSerializer<InternalRequest>() {
        @Override
        public void serialize(InternalRequest value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeStartArray();
            gen.writeNumber(value.getVersion());
            gen.writeNumber(value.getType());
            gen.writeString(Optional.ofNullable(value.getDbName()).orElse(ArangoRequestParam.SYSTEM));
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
