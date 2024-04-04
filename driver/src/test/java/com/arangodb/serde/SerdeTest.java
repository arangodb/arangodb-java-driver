package com.arangodb.serde;

import com.arangodb.ContentType;
import com.arangodb.entity.BaseDocument;
import com.arangodb.internal.serde.InternalSerde;
import com.arangodb.internal.serde.InternalSerdeProvider;
import com.arangodb.internal.serde.SerdeUtils;
import com.arangodb.util.RawBytes;
import com.arangodb.util.RawJson;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;


class SerdeTest {

    @ParameterizedTest
    @EnumSource(ContentType.class)
    void rawJsonSerde(ContentType type) {
        InternalSerde s = new InternalSerdeProvider(type).create();
        ObjectNode node = JsonNodeFactory.instance.objectNode().put("foo", "bar");
        RawJson raw = RawJson.of(SerdeUtils.INSTANCE.writeJson(node));
        byte[] serialized = s.serialize(raw);
        RawJson deserialized = s.deserialize(serialized, RawJson.class, SerdeContext.EMPTY);
        assertThat(deserialized).isEqualTo(raw);
    }

    @ParameterizedTest
    @EnumSource(ContentType.class)
    void rawBytesSerde(ContentType type) {
        InternalSerde s = new InternalSerdeProvider(type).create();
        ObjectNode node = JsonNodeFactory.instance.objectNode().put("foo", "bar");
        RawBytes raw = RawBytes.of(s.serialize(node));
        byte[] serialized = s.serialize(raw);
        RawBytes deserialized = s.deserialize(serialized, RawBytes.class, SerdeContext.EMPTY);
        assertThat(deserialized).isEqualTo(raw);
    }

    @ParameterizedTest
    @EnumSource(ContentType.class)
    void deserializeBaseDocumentWithNestedProperties(ContentType type) {
        InternalSerde s = new InternalSerdeProvider(type).create();
        RawJson json = RawJson.of("{\"foo\":\"aaa\",\"properties\":{\"foo\":\"bbb\"}}");
        BaseDocument deserialized = s.deserialize(s.serialize(json), BaseDocument.class, SerdeContext.EMPTY);
        assertThat(deserialized.getAttribute("foo")).isEqualTo("aaa");
        assertThat(deserialized.getAttribute("properties"))
                .isInstanceOf(Map.class)
                .asInstanceOf(InstanceOfAssertFactories.MAP)
                .containsEntry("foo", "bbb");
    }

    @ParameterizedTest
    @EnumSource(ContentType.class)
    void serializeBaseDocumentWithNestedProperties(ContentType type) {
        InternalSerde s = new InternalSerdeProvider(type).create();
        BaseDocument doc = new BaseDocument();
        doc.addAttribute("foo", "aaa");
        doc.addAttribute("properties", Collections.singletonMap("foo", "bbb"));
        byte[] ser = s.serialize(doc);
        ObjectNode on = s.deserializeUserData(ser, ObjectNode.class, SerdeContext.EMPTY);
        assertThat(on.get("foo").textValue()).isEqualTo("aaa");
        assertThat(on.get("properties").get("foo").textValue()).isEqualTo("bbb");
    }

}
