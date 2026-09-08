package com.arangodb.serde;

import com.arangodb.ContentType;
import com.arangodb.entity.QueryEntity;
import com.arangodb.entity.QueryTrackingPropertiesEntity;
import com.arangodb.internal.serde.InternalSerde;
import com.arangodb.internal.serde.InternalSerdeProvider;
import com.arangodb.util.RawJson;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

class QueryTrackingSerdeTest {

    private static <T> T read(InternalSerde serde, String json, Class<T> type) {
        return serde.deserialize(serde.serialize(RawJson.of(json)), type);
    }

    @ParameterizedTest
    @EnumSource(ContentType.class)
    void trackingPropertiesRoundTrip(ContentType type) {
        InternalSerde serde = new InternalSerdeProvider(type).create();
        QueryTrackingPropertiesEntity properties = read(serde,
                "{\"enabled\":true,\"trackSlowQueries\":true,\"maxSlowQueries\":64,"
                        + "\"slowQueryThreshold\":10,\"slowStreamingQueryThreshold\":60,"
                        + "\"maxQueryStringLength\":4096}", QueryTrackingPropertiesEntity.class);
        assertThat(properties.getSlowQueryThreshold()).isEqualTo(10L);
        assertThat(properties.getSlowStreamingQueryThreshold()).isEqualTo(60L);

        properties.setSlowQueryThreshold(5L);
        properties.setSlowStreamingQueryThreshold(30L);
        ObjectNode body = serde.deserialize(serde.serialize(properties), ObjectNode.class);
        assertThat(body.get("slowQueryThreshold").longValue()).isEqualTo(5L);
        assertThat(body.get("slowStreamingQueryThreshold").longValue()).isEqualTo(30L);
        assertThat(body.has("properties")).isFalse();
        QueryTrackingPropertiesEntity copy = serde.deserialize(serde.serialize(properties),
                QueryTrackingPropertiesEntity.class);
        assertThat(copy).isEqualTo(properties);
        assertThat(copy.hashCode()).isEqualTo(properties.hashCode());
    }

    @ParameterizedTest
    @EnumSource(ContentType.class)
    void omittedStreamingThresholdStaysNullAndIsNotSerialized(ContentType type) {
        InternalSerde serde = new InternalSerdeProvider(type).create();
        QueryTrackingPropertiesEntity properties = read(serde, "{\"slowQueryThreshold\":10}",
                QueryTrackingPropertiesEntity.class);
        assertThat(properties.getSlowStreamingQueryThreshold()).isNull();
        ObjectNode body = serde.deserialize(serde.serialize(properties), ObjectNode.class);
        assertThat(body.has("slowStreamingQueryThreshold")).isFalse();
        assertThat(body.get("slowQueryThreshold").longValue()).isEqualTo(10L);

        properties.setSlowStreamingQueryThreshold(0L);
        body = serde.deserialize(serde.serialize(properties), ObjectNode.class);
        assertThat(body.get("slowStreamingQueryThreshold").longValue()).isZero();
        properties.setSlowStreamingQueryThreshold(null);
        body = serde.deserialize(serde.serialize(properties), ObjectNode.class);
        assertThat(body.has("slowStreamingQueryThreshold")).isFalse();
    }

    @ParameterizedTest
    @EnumSource(ContentType.class)
    void currentQueriesExposeModificationFlagAndWarningCount(ContentType type) {
        InternalSerde serde = new InternalSerdeProvider(type).create();
        QueryEntity[] queries = read(serde,
                "[{\"id\":\"1\",\"stream\":false,\"modificationQuery\":false,\"warnings\":0},"
                        + "{\"id\":\"2\",\"stream\":true,\"modificationQuery\":true,\"warnings\":2}]",
                QueryEntity[].class);
        assertThat(queries).hasSize(2);
        assertThat(queries[0].getModificationQuery()).isFalse();
        assertThat(queries[0].getWarnings()).isZero();
        assertThat(queries[0].getExitCode()).isNull();
        assertThat(queries[1].getStream()).isTrue();
        assertThat(queries[1].getModificationQuery()).isTrue();
        assertThat(queries[1].getWarnings()).isEqualTo(2L);
        assertThat(queries[1].getExitCode()).isNull();
    }

    @ParameterizedTest
    @EnumSource(ContentType.class)
    void slowQueriesExposeSuccessAndFailureExitCodes(ContentType type) {
        InternalSerde serde = new InternalSerdeProvider(type).create();
        QueryEntity[] queries = read(serde,
                "[{\"id\":\"1\",\"modificationQuery\":false,\"warnings\":0,\"exitCode\":0},"
                        + "{\"id\":\"2\",\"modificationQuery\":true,\"warnings\":3,\"exitCode\":1500}]",
                QueryEntity[].class);
        assertThat(queries).hasSize(2);
        assertThat(queries[0].getModificationQuery()).isFalse();
        assertThat(queries[0].getWarnings()).isZero();
        assertThat(queries[0].getExitCode()).isZero();
        assertThat(queries[1].getModificationQuery()).isTrue();
        assertThat(queries[1].getWarnings()).isEqualTo(3L);
        assertThat(queries[1].getExitCode()).isEqualTo(1500);
    }

    @ParameterizedTest
    @EnumSource(ContentType.class)
    void missingAndExplicitlyNullQueryAttributesStayNull(ContentType type) {
        InternalSerde serde = new InternalSerdeProvider(type).create();
        for (String json : new String[]{"{\"id\":\"1\"}",
                "{\"id\":\"1\",\"modificationQuery\":null,\"warnings\":null,\"exitCode\":null}"}) {
            QueryEntity query = read(serde, json, QueryEntity.class);
            assertThat(query.getModificationQuery()).isNull();
            assertThat(query.getWarnings()).isNull();
            assertThat(query.getExitCode()).isNull();
        }
    }

    @ParameterizedTest
    @EnumSource(ContentType.class)
    void queryAttributesRoundTripAndRetainWireNames(ContentType type) {
        InternalSerde serde = new InternalSerdeProvider(type).create();
        QueryEntity query = read(serde,
                "{\"id\":\"1\",\"modificationQuery\":true,\"warnings\":2,\"exitCode\":1500}",
                QueryEntity.class);
        ObjectNode body = serde.deserialize(serde.serialize(query), ObjectNode.class);
        assertThat(body.get("modificationQuery").booleanValue()).isTrue();
        assertThat(body.get("warnings").longValue()).isEqualTo(2L);
        assertThat(body.get("exitCode").intValue()).isEqualTo(1500);
        assertThat(body.has("errorNum")).isFalse();
        QueryEntity copy = serde.deserialize(serde.serialize(query), QueryEntity.class);
        assertThat(copy).isEqualTo(query);
        assertThat(copy.hashCode()).isEqualTo(query.hashCode());
    }

    @ParameterizedTest
    @EnumSource(ContentType.class)
    void warningCountsDoNotOverflowAnInteger(ContentType type) {
        InternalSerde serde = new InternalSerdeProvider(type).create();
        QueryEntity query = read(serde, "{\"warnings\":2147483648}", QueryEntity.class);
        assertThat(query.getWarnings()).isEqualTo(2147483648L);
    }

    @ParameterizedTest
    @EnumSource(ContentType.class)
    void streamingThresholdParticipatesInEqualityAndHashCode(ContentType type) {
        InternalSerde serde = new InternalSerdeProvider(type).create();
        QueryTrackingPropertiesEntity first = read(serde, "{\"slowStreamingQueryThreshold\":30}",
                QueryTrackingPropertiesEntity.class);
        QueryTrackingPropertiesEntity same = read(serde, "{\"slowStreamingQueryThreshold\":30}",
                QueryTrackingPropertiesEntity.class);
        QueryTrackingPropertiesEntity different = read(serde, "{\"slowStreamingQueryThreshold\":31}",
                QueryTrackingPropertiesEntity.class);
        assertThat(first).isEqualTo(same);
        assertThat(first.hashCode()).isEqualTo(same.hashCode());
        assertThat(first).isNotEqualTo(different);
        // These concrete values differ in only one hash component.
        assertThat(first.hashCode()).isNotEqualTo(different.hashCode());
    }

    @ParameterizedTest
    @EnumSource(ContentType.class)
    void eachQueryAttributeParticipatesInEqualityAndHashCode(ContentType type) {
        InternalSerde serde = new InternalSerdeProvider(type).create();
        QueryEntity first = read(serde,
                "{\"modificationQuery\":false,\"warnings\":0,\"exitCode\":0}", QueryEntity.class);
        QueryEntity same = read(serde,
                "{\"modificationQuery\":false,\"warnings\":0,\"exitCode\":0}", QueryEntity.class);
        assertThat(first).isEqualTo(same);
        assertThat(first.hashCode()).isEqualTo(same.hashCode());
        for (String json : new String[]{
                "{\"modificationQuery\":true,\"warnings\":0,\"exitCode\":0}",
                "{\"modificationQuery\":false,\"warnings\":1,\"exitCode\":0}",
                "{\"modificationQuery\":false,\"warnings\":0,\"exitCode\":1}"}) {
            QueryEntity different = read(serde, json, QueryEntity.class);
            assertThat(first).isNotEqualTo(different);
            // These concrete values differ in only one hash component.
            assertThat(first.hashCode()).isNotEqualTo(different.hashCode());
        }
        QueryEntity missing = read(serde, "{}", QueryEntity.class);
        assertThat(missing).isNotEqualTo(first);
    }
}
