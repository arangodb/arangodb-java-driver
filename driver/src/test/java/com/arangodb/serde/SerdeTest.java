package com.arangodb.serde;

import com.arangodb.ContentType;
import com.arangodb.internal.serde.InternalSerde;
import com.arangodb.internal.serde.SerdeUtils;
import com.arangodb.util.RawBytes;
import com.arangodb.util.RawJson;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;


class SerdeTest {

    @ParameterizedTest
    @EnumSource(ContentType.class)
    void rawJsonSerde(ContentType type) {
        InternalSerde s = InternalSerde.of(type, null);
        ObjectNode node = JsonNodeFactory.instance.objectNode().put("foo", "bar");
        RawJson raw = RawJson.of(SerdeUtils.INSTANCE.writeJson(node));
        byte[] serialized = s.serialize(raw);
        RawJson deserialized = s.deserialize(serialized, RawJson.class);
        assertThat(deserialized).isEqualTo(raw);
    }

    @ParameterizedTest
    @EnumSource(ContentType.class)
    void rawBytesSerde(ContentType type) {
        InternalSerde s = InternalSerde.of(type, null);
        ObjectNode node = JsonNodeFactory.instance.objectNode().put("foo", "bar");
        RawBytes raw = RawBytes.of(s.serialize(node));
        byte[] serialized = s.serialize(raw);
        RawBytes deserialized = s.deserialize(serialized, RawBytes.class);
        assertThat(deserialized).isEqualTo(raw);
    }

}
