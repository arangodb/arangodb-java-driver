package com.arangodb.serde;

import com.arangodb.ContentType;
import com.arangodb.internal.serde.InternalSerdeProvider;
import com.arangodb.serde.jackson.JacksonSerde;
import com.arangodb.util.SlowTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class JacksonConfigurationTest {

    @SlowTest
    @ParameterizedTest
    @EnumSource(ContentType.class)
    void bigStringInternalSerde(ContentType type) {
        ArangoSerde s = new InternalSerdeProvider(type).create();

        StringBuilder sb = new StringBuilder();
        while (sb.length() < 40_000_000) {
            sb.append(UUID.randomUUID());
        }
        String in = sb.toString();
        byte[] bytes = s.serialize(in);
        String out = s.deserialize(bytes, String.class, SerdeContext.EMPTY);
        assertThat(out).isEqualTo(in);
    }

    @SlowTest
    @ParameterizedTest
    @EnumSource(ContentType.class)
    void bigStringUserSerde(ContentType type) {
        ArangoSerde s = JacksonSerde.of(type);

        StringBuilder sb = new StringBuilder();
        while (sb.length() < 40_000_000) {
            sb.append(UUID.randomUUID());
        }
        String in = sb.toString();
        byte[] bytes = s.serialize(in);
        String out = s.deserialize(bytes, String.class);
        assertThat(out).isEqualTo(in);
    }



}
