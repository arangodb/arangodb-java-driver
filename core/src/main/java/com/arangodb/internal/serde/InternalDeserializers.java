package com.arangodb.internal.serde;

import com.arangodb.entity.CollectionType;
import com.arangodb.entity.InvertedIndexPrimarySort;
import com.arangodb.entity.ReplicationFactor;
import com.arangodb.entity.arangosearch.CollectionLink;
import com.arangodb.entity.arangosearch.FieldLink;
import com.arangodb.util.RawBytes;
import com.arangodb.util.RawJson;
import tools.jackson.core.JsonParser;
import tools.jackson.core.TreeNode;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.node.*;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public final class InternalDeserializers {

    static final ValueDeserializer<RawJson> RAW_JSON_DESERIALIZER = new ValueDeserializer<>() {
        @Override
        public RawJson deserialize(JsonParser p, DeserializationContext ctxt) {
            return RawJson.of(new String(SerdeUtils.extractBytes(p), StandardCharsets.UTF_8));
        }
    };

    static final ValueDeserializer<RawBytes> RAW_BYTES_DESERIALIZER = new ValueDeserializer<>() {
        @Override
        public RawBytes deserialize(JsonParser p, DeserializationContext ctxt) {
            return RawBytes.of(SerdeUtils.extractBytes(p));
        }
    };

    static final ValueDeserializer<CollectionType> COLLECTION_TYPE = new ValueDeserializer<>() {
        @Override
        public CollectionType deserialize(final JsonParser p, final DeserializationContext ctxt) {
            return CollectionType.fromType(p.getIntValue());
        }
    };

    static final ValueDeserializer<ReplicationFactor> REPLICATION_FACTOR = new ValueDeserializer<>() {
        @Override
        public ReplicationFactor deserialize(final JsonParser p, final DeserializationContext ctxt) {
            TreeNode node = p.readValueAsTree();
            if (node instanceof NumericNode) {
                return ReplicationFactor.of(((NumericNode) node).intValue());
            } else if (node instanceof StringNode && "satellite".equals(((StringNode) node).stringValue())) {
                return ReplicationFactor.ofSatellite();
            } else throw new IllegalArgumentException();
        }
    };

    static final ValueDeserializer<InvertedIndexPrimarySort.Field> INVERTED_INDEX_PRIMARY_SORT_FIELD = new ValueDeserializer<>() {
        @Override
        public InvertedIndexPrimarySort.Field deserialize(final JsonParser p, final DeserializationContext ctxt) {
            ObjectNode tree = p.readValueAsTree();
            String field = tree.get("field").stringValue();
            InvertedIndexPrimarySort.Field.Direction direction = tree.get("asc").booleanValue() ?
                    InvertedIndexPrimarySort.Field.Direction.asc : InvertedIndexPrimarySort.Field.Direction.desc;
            return new InvertedIndexPrimarySort.Field(field, direction);
        }
    };

    private InternalDeserializers() {
    }

    private static <T> T readTreeAsValue(JsonParser p, DeserializationContext ctxt, JsonNode n, Class<T> targetType) {
        return ctxt.readTreeAsValue(n, targetType);
    }

    public static class CollectionLinksDeserializer extends ValueDeserializer<Collection<CollectionLink>> {
        @Override
        public Collection<CollectionLink> deserialize(JsonParser p, DeserializationContext ctxt) {
            Collection<CollectionLink> out = new ArrayList<>();
            ObjectNode tree = p.readValueAsTree();
            for (Map.Entry<String, JsonNode> e : tree.properties()) {
                ObjectNode v = (ObjectNode) e.getValue();
                v.put("name", e.getKey());
                out.add(readTreeAsValue(p, ctxt, v, CollectionLink.class));
            }
            return out;
        }
    }

    public static class FieldLinksDeserializer extends ValueDeserializer<FieldLink[]> {

        @Override
        public FieldLink[] deserialize(JsonParser p, DeserializationContext ctxt) {
            Collection<FieldLink> out = new ArrayList<>();
            ObjectNode tree = p.readValueAsTree();
            for (Map.Entry<String, JsonNode> e : tree.properties()) {
                ObjectNode v = (ObjectNode) e.getValue();
                v.put("name", e.getKey());
                out.add(readTreeAsValue(p, ctxt, v, FieldLink.class));
            }
            return out.toArray(new FieldLink[0]);
        }
    }

    public static class CollectionSchemaRuleDeserializer extends ValueDeserializer<String> {
        @Override
        public String deserialize(JsonParser p, DeserializationContext ctxt) {
            return SerdeUtils.writeJson(p.readValueAsTree());
        }
    }

}
