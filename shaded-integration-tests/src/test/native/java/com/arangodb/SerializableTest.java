package com.arangodb;

import com.arangodb.entity.ErrorEntity;
import com.arangodb.internal.net.ArangoDBRedirectException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SerializableTest {

    @Test
    void serializeArangoDBException() throws IOException, ClassNotFoundException {
        ObjectMapper mapper = new ObjectMapper();
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
    void serializeArangoDBRedirectException() throws IOException, ClassNotFoundException {
        ArangoDBRedirectException e = new ArangoDBRedirectException("foo", "bar");
        ArangoDBRedirectException e2 = roundTrip(e);
        assertThat(e2.getMessage()).isEqualTo(e.getMessage());
        assertThat(e2.getLocation()).isEqualTo(e.getLocation());
    }

    @Test
    void serializeArangoDBMultipleException() throws IOException, ClassNotFoundException {
        List<Throwable> exceptions = Collections.singletonList(new RuntimeException("foo"));
        ArangoDBMultipleException e = new ArangoDBMultipleException(exceptions);
        ArangoDBMultipleException e2 = roundTrip(e);
        assertThat(e2.getExceptions()).hasSize(1);
        assertThat(e2.getExceptions().iterator().next().getMessage()).isEqualTo("foo");
    }

    private <T> T roundTrip(T input) throws IOException, ClassNotFoundException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(os);
        objectOutputStream.writeObject(input);

        InputStream is = new ByteArrayInputStream(os.toByteArray());
        ObjectInputStream objectInputStream = new ObjectInputStream(is);
        T output = (T) objectInputStream.readObject();
        objectInputStream.close();

        return output;
    }
}
