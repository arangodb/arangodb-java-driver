package com.arangodb;

import com.arangodb.entity.BaseDocument;
import com.arangodb.entity.ErrorEntity;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import org.junit.jupiter.api.Test;

import java.io.*;

import static org.assertj.core.api.Assertions.assertThat;

public class SerializableTest {

    @Test
    public void serializeBaseDocument() throws IOException, ClassNotFoundException {
        BaseDocument bd = new BaseDocument("poaids");
        bd.setId("apdso/02193");
        bd.setRevision("poip");
        bd.addAttribute("aaa", "bbb");

        BaseDocument bd2 = roundTrip(bd);
        assertThat(bd).isEqualTo(bd2);
    }

    @Test
    public void serializeArangoDBException() throws IOException, ClassNotFoundException {
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
