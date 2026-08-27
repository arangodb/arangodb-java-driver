/*
 * DISCLAIMER
 *
 * Copyright 2026 ArangoDB GmbH, Cologne, Germany
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Copyright holder is ArangoDB GmbH, Cologne, Germany
 */

package com.arangodb;

import com.arangodb.entity.BaseDocument;
import com.arangodb.entity.IndexEntity;
import com.arangodb.entity.IndexType;
import com.arangodb.entity.NLists;
import com.arangodb.entity.VectorIndexParams;
import com.arangodb.entity.VectorIndexTrainingState;
import com.arangodb.model.IndexListOptions;
import com.arangodb.model.VectorIndexOptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

final class VectorIndexTestSupport {

    private static final int DIMENSION = 4;

    interface Operations {
        String collectionName();

        void clean();

        void insert(List<BaseDocument> documents);

        IndexEntity ensure(Iterable<String> fields, VectorIndexOptions options);

        IndexEntity get(String id);

        Collection<IndexEntity> indexes(IndexListOptions options);

        String delete(String id);

        void malformedParamsRequest();

        void ensureOnUnknownCollection(VectorIndexOptions options);
    }

    static void fixedModeEveryMetric(final Operations ops, final VectorIndexParams.Metric metric) {
        ops.clean();
        String field = "fixed_" + metric;
        ops.insert(vectorDocuments(field, 12));

        VectorIndexParams params = new VectorIndexParams()
                .metric(metric)
                .dimension(DIMENSION)
                .nLists(NLists.fixed(2))
                .factory("IVF2,Flat")
                .defaultNProbe(2)
                .trainingIterations(2)
                .numberOfDocsPerCentroid(4);
        VectorIndexOptions options = new VectorIndexOptions()
                .name("vector_" + metric)
                .parallelism(2)
                .inBackground(false)
                .sparse(true)
                .storedValues("label", "category")
                .params(params);

        IndexEntity createResponse = ops.ensure(Collections.singletonList(field), options);
        assertThat(createResponse.getIsNewlyCreated()).isTrue();
        IndexEntity created = waitUntilReady(ops, createResponse);
        assertVectorIndex(created, field, params);
        assertThat(created.getSparse()).isTrue();
        assertThat(created.getStoredValues()).containsExactlyInAnyOrder("label", "category");

        IndexEntity existing = ops.ensure(Collections.singletonList(field), options);
        assertThat(existing.getIsNewlyCreated()).isFalse();
        IndexEntity byHandle = ops.get(created.getId());
        assertVectorIndex(byHandle, field, params);
        assertThat(byHandle.getName()).isEqualTo(created.getName());
        String key = created.getId().substring(created.getId().indexOf('/') + 1);
        assertThat(ops.get(key).getId()).isEqualTo(created.getId());

        IndexEntity listed = findById(ops.indexes(new IndexListOptions().withHidden(true).withStats(true)),
                created.getId());
        assertThat(listed.getTrainingState()).isEqualTo(VectorIndexTrainingState.ready);
        assertReadyShardDetails(listed, ops.collectionName(), 2);

        assertThat(ops.delete(key)).isEqualTo(created.getId());
        assertResponseCode(catchThrowable(() -> ops.get(created.getId())), 404);
    }

    static void minimalModeAndServerDefaults(final Operations ops) {
        ops.clean();
        String field = "minimal";
        ops.insert(vectorDocuments(field, 25));

        VectorIndexParams requested = new VectorIndexParams()
                .metric(VectorIndexParams.Metric.cosine)
                .dimension(DIMENSION);
        IndexEntity created = waitUntilReady(ops, ops.ensure(Collections.singletonList(field),
                new VectorIndexOptions().params(requested)));

        VectorIndexParams actual = created.getParams();
        assertThat(actual.getMetric()).isEqualTo(VectorIndexParams.Metric.cosine);
        assertThat(actual.getDimension()).isEqualTo(DIMENSION);
        assertThat(actual.getDefaultNProbe()).isEqualTo(1);
        assertThat(actual.getTrainingIterations()).isEqualTo(25);
        assertThat(actual.getNumberOfDocsPerCentroid()).isEqualTo(100);
        assertThat(actual.getFactory()).isNull();
        assertThat(actual.getnLists()).isNull();
        assertDefaultScaling((NLists.ObjectNLists) actual.getNLists());
        assertThat(created.getSparse()).isFalse();
        assertThat(created.getTrainingState()).isEqualTo(VectorIndexTrainingState.ready);
    }

    static void explicitScalingModeAndShardDetails(final Operations ops) {
        ops.clean();
        String field = "scaling";
        ops.insert(vectorDocuments(field, 12));

        NLists.ObjectNLists scaling = NLists.scaling()
                .strategy(NLists.ObjectNLists.Strategy.autoSqrt)
                .multiplier(1)
                .minNLists(2)
                .tiers(
                        new NLists.ObjectNLists.Tier(10, 2),
                        new NLists.ObjectNLists.Tier(1_000, 32));
        VectorIndexParams params = new VectorIndexParams()
                .metric(VectorIndexParams.Metric.l2)
                .dimension(DIMENSION)
                .nLists(scaling)
                .factory("IVF{},Flat")
                .defaultNProbe(3)
                .trainingIterations(2)
                .numberOfDocsPerCentroid(4);

        IndexEntity created = waitUntilReady(ops, ops.ensure(Collections.singletonList(field),
                new VectorIndexOptions().name("scaling_index").sparse(true).params(params)));
        assertVectorIndex(created, field, params);
        assertThat(created.getParams().getnLists()).isNull();
        assertThat(((NLists.ObjectNLists) created.getParams().getNLists()).getTiers())
                .containsExactlyElementsOf(scaling.getTiers());
        assertThat(created.getParams().getFactory()).isEqualTo("IVF{},Flat");

        IndexEntity listed = findById(ops.indexes(new IndexListOptions().withHidden(true)), created.getId());
        assertReadyShardDetails(listed, ops.collectionName(), 2);
    }

    static void creationBeforeDataAndAutomaticSparseTraining(final Operations ops) {
        ops.clean();
        String field = "late_vectors";
        VectorIndexParams params = new VectorIndexParams()
                .metric(VectorIndexParams.Metric.innerProduct)
                .dimension(DIMENSION)
                .nLists(NLists.fixed(2))
                .trainingIterations(2)
                .numberOfDocsPerCentroid(4);
        IndexEntity created = ops.ensure(Collections.singletonList(field), new VectorIndexOptions()
                .name("late_index")
                .inBackground(true)
                .sparse(true)
                .params(params));
        assertThat(created.getTrainingState()).isEqualTo(VectorIndexTrainingState.unusable);
        assertThat(created.getErrorMessage()).isNotBlank();

        BaseDocument missing = new BaseDocument();
        BaseDocument nullVector = new BaseDocument();
        nullVector.addAttribute(field, null);
        ops.insert(Arrays.asList(missing, nullVector));
        assertThat(ops.get(created.getId()).getTrainingState()).isEqualTo(VectorIndexTrainingState.unusable);

        ops.insert(vectorDocuments(field, 8));
        IndexEntity ready = waitUntilReady(ops, created);
        assertThat(ready.getTrainingState()).isEqualTo(VectorIndexTrainingState.ready);
        assertThat(ready.getErrorMessage()).isNullOrEmpty();
    }

    static void foregroundAndBackgroundUnusableResponses(final Operations ops) {
        ops.clean();
        VectorIndexParams params = new VectorIndexParams()
                .metric(VectorIndexParams.Metric.l2)
                .dimension(DIMENSION);
        IndexEntity foreground = ops.ensure(Collections.singletonList("foreground"), new VectorIndexOptions()
                .name("foreground_unusable")
                .inBackground(false)
                .params(params));
        IndexEntity background = ops.ensure(Collections.singletonList("background"), new VectorIndexOptions()
                .name("background_unusable")
                .inBackground(true)
                .params(params));

        assertUnusable(foreground);
        assertUnusable(background);
        Collection<IndexEntity> listed = ops.indexes(new IndexListOptions().withHidden(true));
        for (IndexEntity index : Arrays.asList(findById(listed, foreground.getId()),
                findById(listed, background.getId()))) {
            assertUnusable(index);
            assertThat(index.getShards()).isNotEmpty();
            assertThat(index.getShards().values()).allSatisfy(shard -> {
                assertThat(shard.getTrainingState()).isEqualTo(VectorIndexTrainingState.unusable);
                assertThat(shard.getError()).isNotBlank();
                assertThat(shard.getResolvedNLists()).isZero();
            });
        }
    }

    static void serverValidationErrors(final Operations ops) {
        ops.clean();
        assertResponseCode(catchThrowable(() -> ops.ensure(Collections.singletonList("v"),
                new VectorIndexOptions())), 400);
        assertResponseCode(catchThrowable(ops::malformedParamsRequest), 400);

        VectorIndexParams fixed = fixedParams();
        assertResponseCode(catchThrowable(() -> ops.ensure(Collections.emptyList(),
                new VectorIndexOptions().params(fixed))), 400);
        assertResponseCode(catchThrowable(() -> ops.ensure(Arrays.asList("v1", "v2"),
                new VectorIndexOptions().params(fixed))), 400);
        assertResponseCode(catchThrowable(() -> ops.ensure(Collections.singletonList("v"),
                new VectorIndexOptions().params(new VectorIndexParams().dimension(DIMENSION).nLists(NLists.fixed(2))))), 400);
        assertResponseCode(catchThrowable(() -> ops.ensure(Collections.singletonList("v"),
                new VectorIndexOptions().params(new VectorIndexParams()
                        .metric(VectorIndexParams.Metric.l2).nLists(NLists.fixed(2))))), 400);
        assertResponseCode(catchThrowable(() -> ops.ensure(Collections.singletonList("v"),
                new VectorIndexOptions().params(new VectorIndexParams()
                        .metric(VectorIndexParams.Metric.l2).dimension(DIMENSION).nLists(NLists.fixed(0))))), 400);

        assertInvalidScaling(ops, NLists.scaling());
        assertInvalidScaling(ops, NLists.scaling()
                .strategy(NLists.ObjectNLists.Strategy.autoSqrt).multiplier(0).minNLists(2));
        assertInvalidScaling(ops, NLists.scaling()
                .strategy(NLists.ObjectNLists.Strategy.autoSqrt).multiplier(1).minNLists(0));
        assertInvalidScaling(ops, validScaling().tiers(new NLists.ObjectNLists.Tier(0, 1)));
        assertInvalidScaling(ops, validScaling().tiers(new NLists.ObjectNLists.Tier(1, 0)));

        VectorIndexParams invalidSample = fixedParams().numberOfDocsPerCentroid(0);
        assertResponseCode(catchThrowable(() -> ops.ensure(Collections.singletonList("v"),
                new VectorIndexOptions().params(invalidSample))), 400);

        String[] storedValues = new String[33];
        for (int i = 0; i < storedValues.length; i++) {
            storedValues[i] = "stored" + i;
        }
        assertResponseCode(catchThrowable(() -> ops.ensure(Collections.singletonList("v"),
                new VectorIndexOptions().storedValues(storedValues).params(fixedParams()))), 400);

        assertResponseCode(catchThrowable(() -> ops.ensureOnUnknownCollection(
                new VectorIndexOptions().params(fixedParams()))), 404);
    }

    private static void assertInvalidScaling(final Operations ops, final NLists.ObjectNLists nLists) {
        VectorIndexParams params = new VectorIndexParams()
                .metric(VectorIndexParams.Metric.l2)
                .dimension(DIMENSION)
                .nLists(nLists);
        assertResponseCode(catchThrowable(() -> ops.ensure(Collections.singletonList("v"),
                new VectorIndexOptions().params(params))), 400);
    }

    private static NLists.ObjectNLists validScaling() {
        return NLists.scaling()
                .strategy(NLists.ObjectNLists.Strategy.autoSqrt)
                .multiplier(1)
                .minNLists(2);
    }

    private static VectorIndexParams fixedParams() {
        return new VectorIndexParams()
                .metric(VectorIndexParams.Metric.l2)
                .dimension(DIMENSION)
                .nLists(NLists.fixed(2));
    }

    private static List<BaseDocument> vectorDocuments(final String field, final int count) {
        List<BaseDocument> documents = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            BaseDocument document = new BaseDocument();
            List<Double> vector = new ArrayList<>();
            for (int d = 0; d < DIMENSION; d++) {
                vector.add((double) ((i + 1) * (d + 2) + d * d));
            }
            document.addAttribute(field, vector);
            document.addAttribute("label", "label" + i);
            document.addAttribute("category", i % 2);
            documents.add(document);
        }
        return documents;
    }

    private static IndexEntity waitUntilReady(final Operations ops, final IndexEntity created) {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(30);
        IndexEntity current = created;
        while (current.getTrainingState() != VectorIndexTrainingState.ready && System.nanoTime() < deadline) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new AssertionError("interrupted while waiting for vector-index training", e);
            }
            current = ops.get(created.getId());
        }
        assertThat(current.getTrainingState())
                .withFailMessage("vector index did not become ready: %s", current.getErrorMessage())
                .isEqualTo(VectorIndexTrainingState.ready);
        return current;
    }

    private static void assertVectorIndex(final IndexEntity index, final String field,
                                          final VectorIndexParams params) {
        assertThat(index.getType()).isEqualTo(IndexType.vector);
        assertThat(index.getFields()).containsExactly(field);
        assertThat(index.getParams()).isEqualTo(params);
        assertThat(index.getTrainingState()).isEqualTo(VectorIndexTrainingState.ready);
    }

    private static void assertDefaultScaling(final NLists.ObjectNLists scaling) {
        assertThat(scaling.getStrategy()).isEqualTo(NLists.ObjectNLists.Strategy.autoSqrt);
        assertThat(scaling.getMultiplier()).isEqualTo(4);
        assertThat(scaling.getMinNLists()).isEqualTo(2);
        assertThat(scaling.getTiers()).containsExactly(
                new NLists.ObjectNLists.Tier(1_000_000, 16_384),
                new NLists.ObjectNLists.Tier(10_000_000, 65_536),
                new NLists.ObjectNLists.Tier(300_000_000, 131_072));
    }

    private static void assertReadyShardDetails(final IndexEntity index, final String collectionName,
                                                final int resolvedNLists) {
        assertThat(index.getShards()).isNotEmpty();
        if (BaseJunit5.isSingleServer()) {
            assertThat(index.getShards()).containsKey(collectionName);
        }
        assertThat(index.getShards().values()).allSatisfy(shard -> {
            assertThat(shard.getTrainingState()).isEqualTo(VectorIndexTrainingState.ready);
            assertThat(shard.getError()).isEmpty();
            assertThat(shard.getResolvedNLists()).isEqualTo(resolvedNLists);
        });
    }

    private static void assertUnusable(final IndexEntity index) {
        assertThat(index.getTrainingState()).isEqualTo(VectorIndexTrainingState.unusable);
        assertThat(index.getErrorMessage()).isNotBlank();
    }

    private static IndexEntity findById(final Collection<IndexEntity> indexes, final String id) {
        return indexes.stream()
                .filter(index -> id.equals(index.getId()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("index not listed: " + id));
    }

    private static void assertResponseCode(final Throwable thrown, final int expected) {
        Throwable current = thrown;
        while (current != null && !(current instanceof ArangoDBException)) {
            current = current.getCause();
        }
        assertThat(current).isInstanceOf(ArangoDBException.class);
        assertThat(((ArangoDBException) current).getResponseCode()).isEqualTo(expected);
    }

    private VectorIndexTestSupport() {
    }
}
