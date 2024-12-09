package com.arangodb.internal.serde;

import com.arangodb.entity.CollectionStatus;
import com.arangodb.entity.CollectionType;
import com.arangodb.entity.InvertedIndexPrimarySort;
import com.arangodb.entity.ReplicationFactor;
import com.arangodb.entity.arangosearch.CollectionLink;
import com.arangodb.entity.arangosearch.FieldLink;
import com.arangodb.util.RawBytes;
import com.arangodb.util.RawJson;
import com.arangodb.internal.InternalResponse;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.*;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

public final class InternalDeserializers {

    static final JsonDeserializer<RawJson> RAW_JSON_DESERIALIZER = new JsonDeserializer<RawJson>() {
        @Override
        public RawJson deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            if (JsonFactory.FORMAT_NAME_JSON.equals(p.getCodec().getFactory().getFormatName())) {
                return RawJson.of(new String(SerdeUtils.extractBytes(p), StandardCharsets.UTF_8));
            } else {
                StringWriter w = new StringWriter();
                try (JsonGenerator gen = SerdeUtils.INSTANCE.getJsonMapper().getFactory().createGenerator(w)) {
                    gen.copyCurrentStructure(p);
                    gen.flush();
                }
                return RawJson.of(w.toString());
            }
        }
    };

    static final JsonDeserializer<RawBytes> RAW_BYTES_DESERIALIZER = new JsonDeserializer<RawBytes>() {
        @Override
        public RawBytes deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            return RawBytes.of(SerdeUtils.extractBytes(p));
        }
    };

    static final JsonDeserializer<CollectionStatus> COLLECTION_STATUS = new JsonDeserializer<CollectionStatus>() {
        @Override
        public CollectionStatus deserialize(final JsonParser p, final DeserializationContext ctxt) throws IOException {
            return CollectionStatus.fromStatus(p.getIntValue());
        }
    };

    static final JsonDeserializer<CollectionType> COLLECTION_TYPE = new JsonDeserializer<CollectionType>() {
        @Override
        public CollectionType deserialize(final JsonParser p, final DeserializationContext ctxt) throws IOException {
            return CollectionType.fromType(p.getIntValue());
        }
    };

    static final JsonDeserializer<ReplicationFactor> REPLICATION_FACTOR = new JsonDeserializer<ReplicationFactor>() {
        @Override
        public ReplicationFactor deserialize(final JsonParser p, final DeserializationContext ctxt) throws IOException {
            TreeNode node = p.readValueAsTree();
            if (node instanceof NumericNode) {
                return ReplicationFactor.of(((NumericNode) node).intValue());
            } else if (node instanceof TextNode && "satellite".equals(((TextNode) node).textValue())) {
                return ReplicationFactor.ofSatellite();
            } else throw new IllegalArgumentException();
        }
    };

    @SuppressWarnings("unchecked")
    static final JsonDeserializer<InternalResponse> RESPONSE = new JsonDeserializer<InternalResponse>() {
        @Override
        public InternalResponse deserialize(final JsonParser p, final DeserializationContext ctxt) throws IOException {
            final InternalResponse response = new InternalResponse();
            Iterator<JsonNode> it = ((ArrayNode) p.readValueAsTree()).iterator();
            response.setVersion(it.next().intValue());
            response.setType(it.next().intValue());
            response.setResponseCode(it.next().intValue());
            if (it.hasNext()) {
                response.putMetas(readTreeAsValue(p, ctxt, it.next(), Map.class));
            }
            return response;
        }
    };

    static final JsonDeserializer<InvertedIndexPrimarySort.Field> INVERTED_INDEX_PRIMARY_SORT_FIELD = new JsonDeserializer<InvertedIndexPrimarySort.Field>() {
        @Override
        public InvertedIndexPrimarySort.Field deserialize(final JsonParser p, final DeserializationContext ctxt) throws IOException {
            ObjectNode tree = p.readValueAsTree();
            String field = tree.get("field").textValue();
            InvertedIndexPrimarySort.Field.Direction direction = tree.get("asc").booleanValue() ?
                    InvertedIndexPrimarySort.Field.Direction.asc : InvertedIndexPrimarySort.Field.Direction.desc;
            return new InvertedIndexPrimarySort.Field(field, direction);
        }
    };

    private InternalDeserializers() {
    }

    private static <T> T readTreeAsValue(JsonParser p, DeserializationContext ctxt, JsonNode n, Class<T> targetType) throws IOException {
        try (TreeTraversingParser t = new TreeTraversingParser(n, p.getCodec())) {
            t.nextToken();
            return ctxt.readValue(t, targetType);
        }
    }

    public static class CollectionLinksDeserializer extends JsonDeserializer<Collection<CollectionLink>> {

        @Override
        public Collection<CollectionLink> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            Collection<CollectionLink> out = new ArrayList<>();
            ObjectNode tree = p.readValueAsTree();
            Iterator<Map.Entry<String, JsonNode>> it = tree.fields();
            while (it.hasNext()) {
                Map.Entry<String, JsonNode> e = it.next();
                ObjectNode v = (ObjectNode) e.getValue();
                v.put("name", e.getKey());
                out.add(readTreeAsValue(p, ctxt, v, CollectionLink.class));
            }
            return out;
        }
    }

    public static class FieldLinksDeserializer extends JsonDeserializer<FieldLink[]> {

        @Override
        public FieldLink[] deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            Collection<FieldLink> out = new ArrayList<>();
            ObjectNode tree = p.readValueAsTree();
            Iterator<Map.Entry<String, JsonNode>> it = tree.fields();
            while (it.hasNext()) {
                Map.Entry<String, JsonNode> e = it.next();
                ObjectNode v = (ObjectNode) e.getValue();
                v.put("name", e.getKey());
                out.add(readTreeAsValue(p, ctxt, v, FieldLink.class));
            }
            return out.toArray(new FieldLink[0]);
        }
    }

    public static class CollectionSchemaRuleDeserializer extends JsonDeserializer<String> {
        @Override
        public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            return SerdeUtils.INSTANCE.writeJson(p.readValueAsTree());
        }
    }

}
