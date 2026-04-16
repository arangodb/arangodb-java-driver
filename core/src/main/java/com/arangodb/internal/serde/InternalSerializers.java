package com.arangodb.internal.serde;

import com.arangodb.entity.CollectionType;
import com.arangodb.entity.arangosearch.CollectionLink;
import com.arangodb.entity.arangosearch.FieldLink;
import com.arangodb.internal.ArangoRequestParam;
import com.arangodb.util.RawJson;
import com.arangodb.internal.InternalRequest;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class InternalSerializers {

    static final ValueSerializer<RawJson> RAW_JSON_SERIALIZER = new ValueSerializer<>() {
        @Override
        public void serialize(RawJson value, JsonGenerator gen, SerializationContext ctxt) {
            gen.writeRawValue(new RawUserDataValue(value.get().getBytes(StandardCharsets.UTF_8)));
        }
    };
    static final ValueSerializer<InternalRequest> REQUEST = new ValueSerializer<>() {
        @Override
        public void serialize(InternalRequest value, JsonGenerator gen, SerializationContext ctxt) {
            gen.writeStartArray();
            gen.writeNumber(value.getVersion());
            gen.writeNumber(value.getType());
            gen.writeString(Optional.ofNullable(value.getDbName()).orElse(ArangoRequestParam.SYSTEM));
            gen.writeNumber(value.getRequestType().getType());
            gen.writeString(value.getPath());
            gen.writeStartObject();
            for (final Map.Entry<String, String> entry : value.getQueryParam().entrySet()) {
                gen.writeStringProperty(entry.getKey(), entry.getValue());
            }
            gen.writeEndObject();
            gen.writeStartObject();
            for (final Map.Entry<String, String> entry : value.getHeaderParam().entrySet()) {
                gen.writeStringProperty(entry.getKey(), entry.getValue());
            }
            gen.writeEndObject();
            gen.writeEndArray();
        }
    };
    static final ValueSerializer<CollectionType> COLLECTION_TYPE = new ValueSerializer<>() {
        @Override
        public void serialize(CollectionType value, JsonGenerator gen, SerializationContext ctxt) {
            gen.writeNumber(value.getType());
        }
    };

    private InternalSerializers() {
    }

    public static class CollectionSchemaRuleSerializer extends ValueSerializer<String> {
        @Override
        public void serialize(String value, JsonGenerator gen, SerializationContext ctxt) {
            gen.writeTree(SerdeUtils.parseJson(value));
        }
    }

    public static class FieldLinksSerializer extends ValueSerializer<Collection<FieldLink>> {
        @Override
        public void serialize(Collection<FieldLink> value, JsonGenerator gen, SerializationContext ctxt) {
            Map<String, FieldLink> mapLikeValue = new HashMap<>();
            for (FieldLink fl : value) {
                mapLikeValue.put(fl.getName(), fl);
            }
            ctxt.writeValue(gen, mapLikeValue);
        }
    }

    public static class CollectionLinksSerializer extends ValueSerializer<Collection<CollectionLink>> {
        @Override
        public void serialize(Collection<CollectionLink> value, JsonGenerator gen, SerializationContext ctxt) {
            Map<String, CollectionLink> mapLikeValue = new HashMap<>();
            for (CollectionLink cl : value) {
                mapLikeValue.put(cl.getName(), cl);
            }
            ctxt.writeValue(gen, mapLikeValue);
        }
    }

}
