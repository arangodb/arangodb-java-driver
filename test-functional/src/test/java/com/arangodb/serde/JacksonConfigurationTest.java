package com.arangodb.serde;

import com.arangodb.RequestContext;
import com.arangodb.internal.serde.InternalUserSerdeProvider;
import com.arangodb.serde.jackson.JacksonSerde;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class JacksonConfigurationTest {

    @Test
    void bigStringInternalSerde() {
        ArangoSerde s = new InternalUserSerdeProvider().create();

        StringBuilder sb = new StringBuilder();
        while (sb.length() < 40_000_000) {
            sb.append(UUID.randomUUID());
        }
        String in = sb.toString();
        byte[] bytes = s.serialize(in);
        String out = s.deserialize(bytes, String.class, RequestContext.EMPTY);
        assertThat(out).isEqualTo(in);
    }

    @Test
    void bigStringUserSerde() {
        ArangoSerde s = JacksonSerde.create();

        StringBuilder sb = new StringBuilder();
        while (sb.length() < 40_000_000) {
            sb.append(UUID.randomUUID());
        }
        String in = sb.toString();
        byte[] bytes = s.serialize(in);
        String out = s.deserialize(bytes, String.class, RequestContext.EMPTY);
        assertThat(out).isEqualTo(in);
    }


}
