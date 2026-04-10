package com.arangodb.internal.serde;

import com.arangodb.entity.ErrorEntity;
import com.arangodb.entity.MultiDocumentEntity;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.BeanProperty;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.exc.MismatchedInputException;


public class MultiDocumentEntityDeserializer extends ValueDeserializer<MultiDocumentEntity<?>> {
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
    public MultiDocumentEntity<?> deserialize(JsonParser p, DeserializationContext ctxt) {
        MultiDocumentEntity<Object> multiDocument = new MultiDocumentEntity<>();

        // silent=true returns an empty object
        if (p.currentToken() == JsonToken.START_OBJECT) {
            if (p.nextToken() == JsonToken.END_OBJECT) {
                return multiDocument;
            } else {
                throw MismatchedInputException.from(p, "Unexpected token sequence: START_OBJECT, " + p.currentToken());
            }
        }

        if (p.currentToken() != JsonToken.START_ARRAY) {
            throw MismatchedInputException.from(p, "Expected START_ARRAY but got " + p.currentToken());
        }
        p.nextToken();
        while (p.currentToken() != JsonToken.END_ARRAY) {
            if (p.currentToken() != JsonToken.START_OBJECT) {
                throw MismatchedInputException.from(p, "Expected START_OBJECT but got " + p.currentToken());
            }
            byte[] element = SerdeUtils.extractBytes(p);
            if (serde.isDocument(element)) {
                Object d = serde.deserializeUserData(element, containedType);
                multiDocument.getDocuments().add(d);
                multiDocument.getDocumentsAndErrors().add(d);
            } else {
                ErrorEntity e = serde.deserialize(element, ErrorEntity.class);
                multiDocument.getErrors().add(e);
                multiDocument.getDocumentsAndErrors().add(e);
            }
            p.nextToken();  // END_OBJECT
        }
        return multiDocument;
    }

    @Override
    public ValueDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) {
        return new MultiDocumentEntityDeserializer(serde, ctxt.getContextualType().containedType(0));
    }
}
