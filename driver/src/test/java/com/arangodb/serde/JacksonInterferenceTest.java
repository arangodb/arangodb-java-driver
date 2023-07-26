package com.arangodb.serde;

import com.arangodb.serde.jackson.Id;
import com.arangodb.serde.jackson.Key;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JacksonInterferenceTest {

    private final ObjectMapper mapper = new ObjectMapper();

    static class Foo {
        @Id
        public String myId;

        @Key
        public String myKey;

        Foo(String id, String key) {
            myId = id;
            myKey = key;
        }
    }

    @Test
    void serialize() {
        Foo foo = new Foo("foo", "bar");
        ObjectNode node = mapper.convertValue(foo, ObjectNode.class);
//        assertThat(node.get("myId").textValue()).isEqualTo("foo");
        assertThat(node.get("myKey").textValue()).isEqualTo("bar");
    }
}
