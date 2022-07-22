package com.arangodb.serde;

import com.arangodb.util.RawBytes;
import com.arangodb.util.RawJson;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;


import static org.assertj.core.api.Assertions.assertThat;


class SerdeTest {

    @ParameterizedTest
    @EnumSource(DataType.class)
    void rawJsonSerde(DataType type) {
        InternalSerde s = InternalSerde.of(type);
        ObjectNode node = JsonNodeFactory.instance.objectNode().put("foo", "bar");
        RawJson raw = RawJson.of(SerdeUtils.INSTANCE.writeJson(node));
        byte[] serialized = s.serialize(raw);
        RawJson deserialized = s.deserialize(serialized, RawJson.class);
        assertThat(deserialized).isEqualTo(raw);
    }

    @ParameterizedTest
    @EnumSource(DataType.class)
    void rawBytesSerde(DataType type) {
        InternalSerde s = InternalSerde.of(type);
        ObjectNode node = JsonNodeFactory.instance.objectNode().put("foo", "bar");
        RawBytes raw = RawBytes.of(s.serialize(node));
        byte[] serialized = s.serialize(raw);
        RawBytes deserialized = s.deserialize(serialized, RawBytes.class);
        assertThat(deserialized).isEqualTo(raw);
    }

}
