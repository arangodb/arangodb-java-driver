package com.arangodb.serde;

import com.arangodb.entity.CollectionStatus;
import com.arangodb.entity.CollectionType;
import com.arangodb.velocystream.Response;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

public final class InternalDeserializers {
    private InternalDeserializers() {
    }

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
    static final JsonDeserializer<Response> RESPONSE = new JsonDeserializer<Response>() {
        @Override
        public Response deserialize(final JsonParser p, final DeserializationContext ctxt) throws IOException {
            final Response response = new Response();
            Iterator<JsonNode> it = ((ArrayNode) p.readValueAsTree()).iterator();
            response.setVersion(it.next().intValue());
            response.setType(it.next().intValue());
            response.setResponseCode(it.next().intValue());
            if (it.hasNext()) {
                response.setMeta(ctxt.readTreeAsValue(it.next(), Map.class));
            }
            return response;
        }
    };


}
