package com.arangodb.serde;

import com.arangodb.entity.BaseDocument;
import com.arangodb.internal.serde.InternalSerde;
import com.arangodb.internal.serde.InternalSerdeProvider;
import com.arangodb.internal.serde.SerdeUtils;
import com.arangodb.util.RawBytes;
import com.arangodb.util.RawJson;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.ObjectNode;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;


class SerdeTest {

    @Test
    void rawJsonSerde() {
        InternalSerde s = new InternalSerdeProvider().create();
        ObjectNode node = JsonNodeFactory.instance.objectNode().put("foo", "bar");
        RawJson raw = RawJson.of(SerdeUtils.INSTANCE.writeJson(node));
        byte[] serialized = s.serialize(raw);
        RawJson deserialized = s.deserialize(serialized, RawJson.class);
        assertThat(deserialized).isEqualTo(raw);
    }

    @Test
    void rawBytesSerde() {
        InternalSerde s = new InternalSerdeProvider().create();
        ObjectNode node = JsonNodeFactory.instance.objectNode().put("foo", "bar");
        RawBytes raw = RawBytes.of(s.serialize(node));
        byte[] serialized = s.serializeUserData(raw);
        RawBytes deserialized = s.deserializeUserData(serialized, RawBytes.class);
        assertThat(deserialized).isEqualTo(raw);
    }

    @Test
    void deserializeBaseDocumentWithNestedProperties() {
        InternalSerde s = new InternalSerdeProvider().create();
        RawJson json = RawJson.of("{\"foo\":\"aaa\",\"properties\":{\"foo\":\"bbb\"}}");
        BaseDocument deserialized = s.deserialize(s.serialize(json), BaseDocument.class);
        assertThat(deserialized.getAttribute("foo")).isEqualTo("aaa");
        assertThat(deserialized.getAttribute("properties"))
                .isInstanceOf(Map.class)
                .asInstanceOf(InstanceOfAssertFactories.MAP)
                .containsEntry("foo", "bbb");
    }

    @Test
    void serializeBaseDocumentWithNestedProperties() {
        InternalSerde s = new InternalSerdeProvider().create();
        BaseDocument doc = new BaseDocument();
        doc.addAttribute("foo", "aaa");
        doc.addAttribute("properties", Collections.singletonMap("foo", "bbb"));
        byte[] ser = s.serialize(doc);
        ObjectNode on = s.deserializeUserData(ser, ObjectNode.class);
        assertThat(on.get("foo").stringValue()).isEqualTo("aaa");
        assertThat(on.get("properties").get("foo").stringValue()).isEqualTo("bbb");
    }

    @Test
    void deserializeNull() {
        InternalSerde s = new InternalSerdeProvider().create();
        Void deser = s.deserialize((byte[]) null, Void.class);
        assertThat(deser).isNull();
    }

    @Test
    void deserializeNullUserSerde() {
        ArangoSerde s = ArangoSerdeProvider.load().create();
        Void deser = s.deserialize(null, Void.class);
        assertThat(deser).isNull();
    }

    @Test
    void deserializeEmpty() {
        InternalSerde s = new InternalSerdeProvider().create();
        Void deser = s.deserialize(new byte[0], Void.class);
        assertThat(deser).isNull();
    }

    @Test
    void deserializeEmptyUserSerde() {
        ArangoSerde s = ArangoSerdeProvider.load().create();
        Void deser = s.deserialize(new byte[0], Void.class);
        assertThat(deser).isNull();
    }
}
