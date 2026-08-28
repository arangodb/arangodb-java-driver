package com.arangodb.serde;

import com.arangodb.ContentType;
import com.arangodb.entity.BaseDocument;
import com.arangodb.entity.IndexEntity;
import com.arangodb.entity.NLists;
import com.arangodb.entity.VectorIndexParams;
import com.arangodb.entity.VectorIndexShardStatus;
import com.arangodb.entity.VectorIndexTrainingState;
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

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;


class SerdeTest {

    @ParameterizedTest
    @EnumSource(ContentType.class)
    void rawJsonSerde(ContentType type) {
        InternalSerde s = new InternalSerdeProvider(type).create();
        ObjectNode node = JsonNodeFactory.instance.objectNode().put("foo", "bar");
        RawJson raw = RawJson.of(SerdeUtils.INSTANCE.writeJson(node));
        byte[] serialized = s.serialize(raw);
        RawJson deserialized = s.deserialize(serialized, RawJson.class);
        assertThat(deserialized).isEqualTo(raw);
    }

    @ParameterizedTest
    @EnumSource(ContentType.class)
    void rawBytesSerde(ContentType type) {
        InternalSerde s = new InternalSerdeProvider(type).create();
        ObjectNode node = JsonNodeFactory.instance.objectNode().put("foo", "bar");
        RawBytes raw = RawBytes.of(s.serialize(node));
        byte[] serialized = s.serializeUserData(raw);
        RawBytes deserialized = s.deserializeUserData(serialized, RawBytes.class);
        assertThat(deserialized).isEqualTo(raw);
    }

    @ParameterizedTest
    @EnumSource(ContentType.class)
    void deserializeBaseDocumentWithNestedProperties(ContentType type) {
        InternalSerde s = new InternalSerdeProvider(type).create();
        RawJson json = RawJson.of("{\"foo\":\"aaa\",\"properties\":{\"foo\":\"bbb\"}}");
        BaseDocument deserialized = s.deserialize(s.serialize(json), BaseDocument.class);
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
        ObjectNode on = s.deserializeUserData(ser, ObjectNode.class);
        assertThat(on.get("foo").textValue()).isEqualTo("aaa");
        assertThat(on.get("properties").get("foo").textValue()).isEqualTo("bbb");
    }

    @ParameterizedTest
    @EnumSource(ContentType.class)
    void deserializeNull(ContentType type) {
        InternalSerde s = new InternalSerdeProvider(type).create();
        Void deser = s.deserialize((byte[]) null, Void.class);
        assertThat(deser).isNull();
    }

    @ParameterizedTest
    @EnumSource(ContentType.class)
    void deserializeNullUserSerde(ContentType type) {
        ArangoSerde s = ArangoSerdeProvider.of(type).create();
        Void deser = s.deserialize(null, Void.class);
        assertThat(deser).isNull();
    }

    @ParameterizedTest
    @EnumSource(ContentType.class)
    void deserializeEmpty(ContentType type) {
        InternalSerde s = new InternalSerdeProvider(type).create();
        Void deser = s.deserialize(new byte[0], Void.class);
        assertThat(deser).isNull();
    }

    @ParameterizedTest
    @EnumSource(ContentType.class)
    void deserializeEmptyUserSerde(ContentType type) {
        ArangoSerde s = ArangoSerdeProvider.of(type).create();
        Void deser = s.deserialize(new byte[0], Void.class);
        assertThat(deser).isNull();
    }

    @ParameterizedTest
    @EnumSource(ContentType.class)
    void vectorIndexFixedNListsSerdeAndLegacyApi(ContentType type) {
        InternalSerde s = new InternalSerdeProvider(type).create();
        VectorIndexParams params = new VectorIndexParams()
                .metric(VectorIndexParams.Metric.cosine)
                .dimension(4)
                .nLists(NLists.fixed(8))
                .numberOfDocsPerCentroid(100);

        ObjectNode serialized = s.deserialize(s.serialize(params), ObjectNode.class);
        assertThat(serialized.get("nLists").intValue()).isEqualTo(8);
        assertThat(serialized.get("numberOfDocsPerCentroid").intValue()).isEqualTo(100);

        VectorIndexParams deserialized = s.deserialize(s.serialize(params), VectorIndexParams.class);
        assertThat(deserialized).isEqualTo(params);
        assertThat(deserialized.getnLists()).isEqualTo(8);
        assertThat(deserialized.getNLists()).isEqualTo(NLists.fixed(8));
    }

    @ParameterizedTest
    @EnumSource(ContentType.class)
    void vectorIndexOmittedNListsSerde(ContentType type) {
        InternalSerde s = new InternalSerdeProvider(type).create();
        VectorIndexParams params = new VectorIndexParams()
                .metric(VectorIndexParams.Metric.l2)
                .dimension(3);

        ObjectNode serialized = s.deserialize(s.serialize(params), ObjectNode.class);
        assertThat(serialized.has("nLists")).isFalse();
        assertThat(s.deserialize(s.serialize(params), VectorIndexParams.class)).isEqualTo(params);
    }

    @ParameterizedTest
    @EnumSource(ContentType.class)
    void vectorIndexScalingNListsSerde(ContentType type) {
        InternalSerde s = new InternalSerdeProvider(type).create();
        NLists.ObjectNLists scaling = NLists.scaling()
                .strategy(NLists.ObjectNLists.Strategy.autoSqrt)
                .multiplier(4)
                .minNLists(2)
                .tiers(
                        new NLists.ObjectNLists.Tier(1_000_000, 16_384),
                        new NLists.ObjectNLists.Tier(10_000_000, 65_536),
                        new NLists.ObjectNLists.Tier(300_000_000, 131_072));
        VectorIndexParams params = new VectorIndexParams()
                .metric(VectorIndexParams.Metric.innerProduct)
                .dimension(8)
                .nLists(scaling)
                .factory("IVF{},SQ4")
                .numberOfDocsPerCentroid(64);

        ObjectNode serialized = s.deserialize(s.serialize(params), ObjectNode.class);
        assertThat(serialized.at("/nLists/strategy").textValue()).isEqualTo("autoSqrt");
        assertThat(serialized.at("/nLists/multiplier").intValue()).isEqualTo(4);
        assertThat(serialized.at("/nLists/minNLists").intValue()).isEqualTo(2);
        assertThat(serialized.at("/nLists/tiers/0/threshold").intValue()).isEqualTo(1_000_000);
        assertThat(serialized.at("/nLists/tiers/1/fixedValue").intValue()).isEqualTo(65_536);
        assertThat(serialized.at("/nLists/tiers/2/fixedValue").intValue()).isEqualTo(131_072);
        assertThat(serialized.get("factory").textValue()).isEqualTo("IVF{},SQ4");

        VectorIndexParams deserialized = s.deserialize(s.serialize(params), VectorIndexParams.class);
        assertThat(deserialized).isEqualTo(params);
        assertThat(deserialized.getnLists()).isNull();
        assertThat(((NLists.ObjectNLists) deserialized.getNLists()).getTiers())
                .containsExactlyElementsOf(scaling.getTiers());
    }

    @ParameterizedTest
    @EnumSource(ContentType.class)
    void vectorIndexTrainingMetadataSerde(ContentType type) {
        InternalSerde s = new InternalSerdeProvider(type).create();
        for (VectorIndexTrainingState state : VectorIndexTrainingState.values()) {
            String json = "{\"id\":\"coll/68\",\"name\":\"vector_l2\",\"type\":\"vector\","
                    + "\"params\":{\"metric\":\"l2\",\"dimension\":8,\"nLists\":{"
                    + "\"strategy\":\"autoSqrt\",\"multiplier\":4,\"minNLists\":2}},"
                    + "\"trainingState\":\"" + state.name()
                    + "\",\"errorMessage\":\"training detail\",\"shards\":{\"s10042\":{"
                    + "\"trainingState\":\"" + state.name()
                    + "\",\"error\":\"shard detail\",\"resolvedNLists\":17}}}";

            IndexEntity index = s.deserialize(s.serialize(RawJson.of(json)), IndexEntity.class);
            assertThat(index.getParams()).isEqualTo(new VectorIndexParams()
                    .metric(VectorIndexParams.Metric.l2)
                    .dimension(8)
                    .nLists(NLists.scaling()
                            .strategy(NLists.ObjectNLists.Strategy.autoSqrt)
                            .multiplier(4)
                            .minNLists(2)));
            assertThat(index.getTrainingState()).isEqualTo(state);
            assertThat(index.getErrorMessage()).isEqualTo("training detail");
            assertThat(index.getShards()).containsOnlyKeys("s10042");
            VectorIndexShardStatus shard = index.getShards().get("s10042");
            assertThat(shard.getTrainingState()).isEqualTo(state);
            assertThat(shard.getError()).isEqualTo("shard detail");
            assertThat(shard.getResolvedNLists()).isEqualTo(17);
        }
    }
}
