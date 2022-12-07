package com.arangodb.serde;

import com.arangodb.ContentType;
import com.arangodb.internal.serde.InternalSerde;
import com.arangodb.internal.serde.SerdeUtils;
import com.arangodb.shaded.fasterxml.jackson.databind.JsonNode;
import com.arangodb.shaded.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.arangodb.shaded.fasterxml.jackson.databind.node.ObjectNode;
import com.arangodb.util.RawBytes;
import com.arangodb.util.RawJson;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.sql.Date;

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

    @ParameterizedTest
    @EnumSource(ContentType.class)
    void utilDateSerde(ContentType type) {
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
    @EnumSource(ContentType.class)
    void sqlDateSerde(ContentType type) {
        InternalSerde s = InternalSerde.of(type, null);
        long ts = 1000000000000L;
        Date date = new Date(ts);
        byte[] ser = s.serialize(date);
        JsonNode node = s.parse(ser);
        assertThat(node.isLong()).isTrue();
        assertThat(node.longValue()).isEqualTo(ts);
        Date deser = s.deserialize(ser, Date.class);
        assertThat(deser).isEqualTo(date);
    }

}
