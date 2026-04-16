package com.arangodb;

import com.arangodb.entity.BaseDocument;
import com.arangodb.entity.BaseEdgeDocument;
import com.arangodb.entity.ErrorEntity;
import com.arangodb.internal.net.ArangoDBRedirectException;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.JsonNodeFactory;

import java.io.*;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SerializableTest {

    @Test
    void serializeArangoDBException() {
        JsonMapper mapper = new JsonMapper();
        JsonNode jn = JsonNodeFactory.instance.objectNode()
                .put("errorMessage", "boomError")
                .put("exception", "boomException")
                .put("code", 11)
                .put("errorNum", 22);
        ErrorEntity ee = mapper.readerFor(ErrorEntity.class).readValue(jn);
        ArangoDBException e = new ArangoDBException(ee);

        ArangoDBException e2 = roundTrip(e);
        assertThat(e2.getException()).isEqualTo(e.getException());
        assertThat(e2.getResponseCode()).isEqualTo(e.getResponseCode());
        assertThat(e2.getErrorNum()).isEqualTo(e.getErrorNum());
        assertThat(e2.getRequestId()).isEqualTo(e.getRequestId());
    }

    @Test
    void serializeArangoDBRedirectException() {
        ArangoDBRedirectException e = new ArangoDBRedirectException("foo", "bar");
        ArangoDBRedirectException e2 = roundTrip(e);
        assertThat(e2.getMessage()).isEqualTo(e.getMessage());
        assertThat(e2.getLocation()).isEqualTo(e.getLocation());
    }

    @Test
    void serializeArangoDBMultipleException() {
        List<Throwable> exceptions = Collections.singletonList(new RuntimeException("foo"));
        ArangoDBMultipleException e = new ArangoDBMultipleException(exceptions);
        ArangoDBMultipleException e2 = roundTrip(e);
        assertThat(e2.getExceptions()).hasSize(1);
        assertThat(e2.getExceptions().getFirst().getMessage()).isEqualTo("foo");
    }

    @Test
    void serializeBaseDocument() {
        BaseDocument doc = new BaseDocument();
        doc.setKey("test");
        doc.setId("id");
        doc.setRevision("revision");
        doc.addAttribute("foo", "bar");
        BaseDocument doc2 = roundTrip(doc);
        assertThat(doc2).isEqualTo(doc);
    }

    @Test
    void serializeBaseEdgeDocument() {
        BaseEdgeDocument doc = new BaseEdgeDocument();
        doc.setKey("test");
        doc.setId("id");
        doc.setRevision("revision");
        doc.setFrom("from");
        doc.setTo("to");
        doc.addAttribute("foo", "bar");
        BaseDocument doc2 = roundTrip(doc);
        assertThat(doc2).isEqualTo(doc);
    }

    private <T> T roundTrip(T input) {
        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(os);
            objectOutputStream.writeObject(input);

            InputStream is = new ByteArrayInputStream(os.toByteArray());
            ObjectInputStream objectInputStream = new ObjectInputStream(is);
            @SuppressWarnings("unchecked") T output = (T) objectInputStream.readObject();
            objectInputStream.close();

            return output;
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
