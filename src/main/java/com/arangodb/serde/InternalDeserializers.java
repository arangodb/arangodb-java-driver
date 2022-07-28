package com.arangodb.serde;

import com.arangodb.entity.CollectionStatus;
import com.arangodb.entity.CollectionType;
import com.arangodb.entity.ReplicationFactor;
import com.arangodb.entity.arangosearch.CollectionLink;
import com.arangodb.entity.arangosearch.FieldLink;
import com.arangodb.util.RawBytes;
import com.arangodb.util.RawJson;
import com.arangodb.velocystream.Response;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

public final class InternalDeserializers {

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


    private InternalDeserializers() {
    }

    static final JsonDeserializer<RawJson> RAW_JSON_DESERIALIZER = new JsonDeserializer<RawJson>() {
        @Override
        public RawJson deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            // TODO: find a way to access raw bytes directly
            return RawJson.of(SerdeUtils.INSTANCE.writeJson(p.readValueAsTree()));
        }
    };

    static final JsonDeserializer<RawBytes> RAW_BYTES_DESERIALIZER = new JsonDeserializer<RawBytes>() {
        @Override
        public RawBytes deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            // TODO: find a way to access raw bytes directly
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            try (JsonGenerator g = p.getCodec().getFactory().createGenerator(os)) {
                g.writeTree(p.readValueAsTree());
            }
            return RawBytes.of(os.toByteArray());
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

    static final JsonDeserializer<Response> RESPONSE = new JsonDeserializer<Response>() {
        @Override
        public Response deserialize(final JsonParser p, final DeserializationContext ctxt) throws IOException {
            final Response response = new Response();
            Iterator<JsonNode> it = ((ArrayNode) p.readValueAsTree()).iterator();
            response.setVersion(it.next().intValue());
            response.setType(it.next().intValue());
            response.setResponseCode(it.next().intValue());
            if (it.hasNext()) {
                response.setMeta(readTreeAsValue(p, ctxt, it.next(), Map.class));
            }
            return response;
        }
    };


}
