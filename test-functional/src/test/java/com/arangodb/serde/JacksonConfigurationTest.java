package com.arangodb.serde;

import com.arangodb.internal.serde.InternalSerdeProvider;
import com.arangodb.serde.jackson.JacksonSerde;
import com.arangodb.util.SlowTest;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class JacksonConfigurationTest {

    @SlowTest
    @Test
    void bigStringInternalSerde() {
        ArangoSerde s = new InternalSerdeProvider().create();

        StringBuilder sb = new StringBuilder();
        while (sb.length() < 40_000_000) {
            sb.append(UUID.randomUUID());
        }
        String in = sb.toString();
        byte[] bytes = s.serialize(in);
        String out = s.deserialize(bytes, String.class);
        assertThat(out).isEqualTo(in);
    }

    @SlowTest
    @Test
    void bigStringUserSerde() {
        ArangoSerde s = JacksonSerde.load();

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
