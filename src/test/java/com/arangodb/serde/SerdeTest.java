package com.arangodb.serde;

import com.arangodb.internal.serde.InternalSerde;
import com.arangodb.internal.serde.SerdeUtils;
import com.arangodb.util.RawBytes;
import com.arangodb.util.RawJson;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;


import java.sql.Date;

import static org.assertj.core.api.Assertions.assertThat;


class SerdeTest {

    @ParameterizedTest
    @EnumSource(DataType.class)
    void rawJsonSerde(DataType type) {
        InternalSerde s = InternalSerde.of(type, null);
        ObjectNode node = JsonNodeFactory.instance.objectNode().put("foo", "bar");
        RawJson raw = RawJson.of(SerdeUtils.INSTANCE.writeJson(node));
        byte[] serialized = s.serialize(raw);
        RawJson deserialized = s.deserialize(serialized, RawJson.class);
        assertThat(deserialized).isEqualTo(raw);
    }

    @ParameterizedTest
    @EnumSource(DataType.class)
    void rawBytesSerde(DataType type) {
        InternalSerde s = InternalSerde.of(type, null);
        ObjectNode node = JsonNodeFactory.instance.objectNode().put("foo", "bar");
        RawBytes raw = RawBytes.of(s.serialize(node));
        byte[] serialized = s.serialize(raw);
        RawBytes deserialized = s.deserialize(serialized, RawBytes.class);
        assertThat(deserialized).isEqualTo(raw);
    }

    @ParameterizedTest
    @EnumSource(DataType.class)
    void utilDateSerde(DataType type) {
        InternalSerde s = InternalSerde.of(type, null);
        long ts = 1000000000000L;
        java.util.Date date = new java.util.Date(ts);
        byte[] ser = s.serialize(date);
        JsonNode node = s.parse(ser);
        assertThat(node.isLong()).isTrue();
        assertThat(node.longValue()).isEqualTo(ts);
        java.util.Date deser = s.deserialize(ser, java.util.Date.class);
        assertThat(deser).isEqualTo(date);
    }

    @ParameterizedTest
    @EnumSource(DataType.class)
    void sqlDateSerde(DataType type) {
        InternalSerde s = InternalSerde.of(type, null);
        long ts = 1000000000000L;
        java.sql.Date date = new Date(ts);
        byte[] ser = s.serialize(date);
        JsonNode node = s.parse(ser);
        assertThat(node.isLong()).isTrue();
        assertThat(node.longValue()).isEqualTo(ts);
        java.sql.Date deser = s.deserialize(ser, java.sql.Date.class);
        assertThat(deser).isEqualTo(date);
    }

}
