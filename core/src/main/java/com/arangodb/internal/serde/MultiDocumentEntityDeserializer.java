package com.arangodb.internal.serde;

import com.arangodb.entity.ErrorEntity;
import com.arangodb.entity.MultiDocumentEntity;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;

import java.io.IOException;

public class MultiDocumentEntityDeserializer extends JsonDeserializer<MultiDocumentEntity<?>> implements ContextualDeserializer {
    private final JavaType containedType;
    private final InternalSerde serde;

    MultiDocumentEntityDeserializer(InternalSerde serde) {
        this(serde, null);
    }

    MultiDocumentEntityDeserializer(InternalSerde serde, JavaType containedType) {
        this.serde = serde;
        this.containedType = containedType;
    }

    @Override
    public MultiDocumentEntity<?> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        MultiDocumentEntity<Object> multiDocument = new MultiDocumentEntity<>();

        // silent=true returns an empty object
        if (p.currentToken() == JsonToken.START_OBJECT) {
            if (p.nextToken() == JsonToken.END_OBJECT) {
                return multiDocument;
            } else {
                throw new JsonMappingException(p, "Unexpected token sequence: START_OBJECT, " + p.currentToken());
            }
        }

        if (p.currentToken() != JsonToken.START_ARRAY) {
            throw new JsonMappingException(p, "Expected START_ARRAY but got " + p.currentToken());
        }
        p.nextToken();
        while (p.currentToken() != JsonToken.END_ARRAY) {
            if (p.currentToken() != JsonToken.START_OBJECT) {
                throw new JsonMappingException(p, "Expected START_OBJECT but got " + p.currentToken());
            }
            p.nextToken();
            if (p.currentToken() != JsonToken.FIELD_NAME) {
                throw new JsonMappingException(p, "Expected FIELD_NAME but got " + p.currentToken());
            }
            String fieldName = p.getText();
            // FIXME: this can potentially fail for: MultiDocumentEntity<T> getDocuments()
            // fix by scanning the 1st level field names and checking if any matches:
            // - "_id"
            // - "_key"
            // - "_rev"
            switch (fieldName) {
                case "_id":
                case "_key":
                case "_rev":
                case "_oldRev":
                case "new":
                case "old":
                    Object d = serde.deserializeUserData(p, containedType);
                    multiDocument.getDocuments().add(d);
                    multiDocument.getDocumentsAndErrors().add(d);
                    break;
                case "error":
                case "errorNum":
                case "errorMessage":
                    ErrorEntity e = ctxt.readValue(p, ErrorEntity.class);
                    multiDocument.getErrors().add(e);
                    multiDocument.getDocumentsAndErrors().add(e);
                    break;
                default:
                    throw new JsonMappingException(p, "Unrecognized field '" + fieldName + "'");
            }
            p.nextToken();  // END_OBJECT
        }
        return multiDocument;
    }

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) {
        return new MultiDocumentEntityDeserializer(serde, ctxt.getContextualType().containedType(0));
    }
}
