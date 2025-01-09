package com.arangodb.serde;

import com.arangodb.serde.jackson.*;
import com.arangodb.serde.jackson.json.JacksonJsonSerdeProvider;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * NB: excluded from shaded tests
 */
class JacksonInterferenceTest {

    private final ObjectMapper mapper = new ObjectMapper();
    private final ArangoSerde serde = new JacksonJsonSerdeProvider().create();

    private FooField fooField;
    private FooProp fooProp;

    static class FooField {
        @Id
        public String myId;
        @Key
        public String myKey;
        @Rev
        public String myRev;
        @From
        public String myFrom;
        @To
        public String myTo;
    }

    static class FooProp {
        public String myId;
        public String myKey;
        public String myRev;
        public String myFrom;
        public String myTo;

        @Id
        public String getMyId() {
            return myId;
        }

        @Id
        public void setMyId(String myId) {
            this.myId = myId;
        }

        @Key
        public String getMyKey() {
            return myKey;
        }

        @Key
        public void setMyKey(String myKey) {
            this.myKey = myKey;
        }

        @Rev
        public String getMyRev() {
            return myRev;
        }

        @Rev
        public void setMyRev(String myRev) {
            this.myRev = myRev;
        }

        @From
        public String getMyFrom() {
            return myFrom;
        }

        @From
        public void setMyFrom(String myFrom) {
            this.myFrom = myFrom;
        }

        @To
        public String getMyTo() {
            return myTo;
        }

        @To
        public void setMyTo(String myTo) {
            this.myTo = myTo;
        }
    }

    @BeforeEach
    void init() {
        fooField = new FooField();
        fooProp = new FooProp();

        fooField.myId = "myId";
        fooProp.myId = "myId";

        fooField.myKey = "myKey";
        fooProp.myKey = "myKey";

        fooField.myRev = "myRev";
        fooProp.myRev = "myRev";

        fooField.myFrom = "myFrom";
        fooProp.myFrom = "myFrom";

        fooField.myTo = "myTo";
        fooProp.myTo = "myTo";
    }

    @Test
    void serializeField() {
        // id
        testSerialize(fooField, "myId", fooField.myId, this::jacksonSerialize);
        testSerialize(fooField, "_id", fooField.myId, this::serdeSerialize);
        // key
        testSerialize(fooField, "myKey", fooField.myKey, this::jacksonSerialize);
        testSerialize(fooField, "_key", fooField.myKey, this::serdeSerialize);
        // rev
        testSerialize(fooField, "myRev", fooField.myRev, this::jacksonSerialize);
        testSerialize(fooField, "_rev", fooField.myRev, this::serdeSerialize);
        // from
        testSerialize(fooField, "myFrom", fooField.myFrom, this::jacksonSerialize);
        testSerialize(fooField, "_from", fooField.myFrom, this::serdeSerialize);
        // to
        testSerialize(fooField, "myTo", fooField.myTo, this::jacksonSerialize);
        testSerialize(fooField, "_to", fooField.myTo, this::serdeSerialize);
    }

    @Test
    void serializeProp() {
        // id
        testSerialize(fooProp, "myId", fooProp.myId, this::jacksonSerialize);
        testSerialize(fooProp, "_id", fooProp.myId, this::serdeSerialize);
        // key
        testSerialize(fooProp, "myKey", fooProp.myKey, this::jacksonSerialize);
        testSerialize(fooProp, "_key", fooProp.myKey, this::serdeSerialize);
        // rev
        testSerialize(fooProp, "myRev", fooProp.myRev, this::jacksonSerialize);
        testSerialize(fooProp, "_rev", fooProp.myRev, this::serdeSerialize);
        // from
        testSerialize(fooProp, "myFrom", fooProp.myFrom, this::jacksonSerialize);
        testSerialize(fooProp, "_from", fooProp.myFrom, this::serdeSerialize);
        // to
        testSerialize(fooProp, "myTo", fooProp.myTo, this::jacksonSerialize);
        testSerialize(fooProp, "_to", fooProp.myTo, this::serdeSerialize);
    }

    @Test
    void deserializeField() throws IOException {
        // id
        testDeserialize("myId", FooField.class, foo -> foo.myId, this::jacksonDeserialize);
        testDeserialize("_id", FooField.class, foo -> foo.myId, this::serdeDeserialize);
        // key
        testDeserialize("myKey", FooField.class, foo -> foo.myKey, this::jacksonDeserialize);
        testDeserialize("_key", FooField.class, foo -> foo.myKey, this::serdeDeserialize);
        // rev
        testDeserialize("myRev", FooField.class, foo -> foo.myRev, this::jacksonDeserialize);
        testDeserialize("_rev", FooField.class, foo -> foo.myRev, this::serdeDeserialize);
        // from
        testDeserialize("myFrom", FooField.class, foo -> foo.myFrom, this::jacksonDeserialize);
        testDeserialize("_from", FooField.class, foo -> foo.myFrom, this::serdeDeserialize);
        // to
        testDeserialize("myTo", FooField.class, foo -> foo.myTo, this::jacksonDeserialize);
        testDeserialize("_to", FooField.class, foo -> foo.myTo, this::serdeDeserialize);
    }

    @Test
    void deserializeProp() throws IOException {
        // id
        testDeserialize("myId", FooProp.class, FooProp::getMyId, this::jacksonDeserialize);
        testDeserialize("_id", FooProp.class, FooProp::getMyId, this::serdeDeserialize);
        // key
        testDeserialize("myKey", FooProp.class, FooProp::getMyKey, this::jacksonDeserialize);
        testDeserialize("_key", FooProp.class, FooProp::getMyKey, this::serdeDeserialize);
        // rev
        testDeserialize("myRev", FooProp.class, FooProp::getMyRev, this::jacksonDeserialize);
        testDeserialize("_rev", FooProp.class, FooProp::getMyRev, this::serdeDeserialize);
        // from
        testDeserialize("myFrom", FooProp.class, FooProp::getMyFrom, this::jacksonDeserialize);
        testDeserialize("_from", FooProp.class, FooProp::getMyFrom, this::serdeDeserialize);
        // to
        testDeserialize("myTo", FooProp.class, FooProp::getMyTo, this::jacksonDeserialize);
        testDeserialize("_to", FooProp.class, FooProp::getMyTo, this::serdeDeserialize);
    }

    void testSerialize(Object data, String fieldName, String expectedValue, Function<Object, JsonNode> serializer) {
        JsonNode jn = serializer.apply(data).get(fieldName);
        assertThat(jn).isNotNull();
        assertThat(jn.textValue()).isEqualTo(expectedValue);
    }

    <T> void testDeserialize(String fieldName, Class<T> clazz, Function<T, String> getter,
                             BiFunction<byte[], Class<T>, T> deserializer) throws IOException {
        String fieldValue = UUID.randomUUID().toString();
        ObjectNode on = JsonNodeFactory.instance.objectNode().put(fieldName, fieldValue);
        byte[] bytes = mapper.writeValueAsBytes(on);
        T deser = deserializer.apply(bytes, clazz);
        assertThat(getter.apply(deser)).isEqualTo(fieldValue);
    }

    private JsonNode jacksonSerialize(Object data) {
        try {
            return mapper.readTree(mapper.writeValueAsBytes(data));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private JsonNode serdeSerialize(Object data) {
        try {
            return mapper.readTree(serde.serialize(data));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private <T> T jacksonDeserialize(byte[] bytes, Class<T> clazz) {
        try {
            return mapper.readValue(bytes, clazz);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private <T> T serdeDeserialize(byte[] bytes, Class<T> clazz) {
        return serde.deserialize(bytes, clazz);
    }
}
