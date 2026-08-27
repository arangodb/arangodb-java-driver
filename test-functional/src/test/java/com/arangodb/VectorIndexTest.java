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
import com.arangodb.entity.VectorIndexParams;
import com.arangodb.model.IndexListOptions;
import com.arangodb.model.VectorIndexOptions;
import com.arangodb.util.RawJson;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

class VectorIndexTest extends BaseJunit5 {

    private static final String COLLECTION_NAME = "VectorIndexTest_collection";

    @BeforeAll
    static void init() {
        assumeTrue(isAtLeastVersion(3, 12, 10));
        initCollections(COLLECTION_NAME);
    }

    private static Stream<Arguments> collections() {
        return dbsStream().map(mapNamedPayload(db -> db.collection(COLLECTION_NAME))).map(Arguments::of);
    }

    private static Stream<Arguments> collectionsAndMetrics() {
        return dbsStream().flatMap(db -> Stream.of(VectorIndexParams.Metric.values())
                .map(metric -> Arguments.of(db.getPayload().collection(COLLECTION_NAME), metric)));
    }

    @ParameterizedTest
    @MethodSource("collectionsAndMetrics")
    void fixedModeEveryMetric(ArangoCollection collection, VectorIndexParams.Metric metric) {
        VectorIndexTestSupport.fixedModeEveryMetric(new SyncOperations(collection), metric);
    }

    @ParameterizedTest
    @MethodSource("collections")
    void minimalModeAndServerDefaults(ArangoCollection collection) {
        VectorIndexTestSupport.minimalModeAndServerDefaults(new SyncOperations(collection));
    }

    @ParameterizedTest
    @MethodSource("collections")
    void explicitScalingModeAndShardDetails(ArangoCollection collection) {
        VectorIndexTestSupport.explicitScalingModeAndShardDetails(new SyncOperations(collection));
    }

    @ParameterizedTest
    @MethodSource("collections")
    void creationBeforeDataAndAutomaticSparseTraining(ArangoCollection collection) {
        VectorIndexTestSupport.creationBeforeDataAndAutomaticSparseTraining(new SyncOperations(collection));
    }

    @ParameterizedTest
    @MethodSource("collections")
    void foregroundAndBackgroundUnusableResponses(ArangoCollection collection) {
        VectorIndexTestSupport.foregroundAndBackgroundUnusableResponses(new SyncOperations(collection));
    }

    @ParameterizedTest
    @MethodSource("collections")
    void serverValidationErrors(ArangoCollection collection) {
        VectorIndexTestSupport.serverValidationErrors(new SyncOperations(collection));
    }

    private static final class SyncOperations implements VectorIndexTestSupport.Operations {
        private final ArangoCollection collection;

        private SyncOperations(final ArangoCollection collection) {
            this.collection = collection;
        }

        @Override
        public String collectionName() {
            return collection.name();
        }

        @Override
        public void clean() {
            collection.getIndexes(new IndexListOptions().withHidden(true)).stream()
                    .filter(index -> index.getType() != IndexType.primary)
                    .map(IndexEntity::getId)
                    .forEach(collection::deleteIndex);
            collection.truncate();
        }

        @Override
        public void insert(final List<BaseDocument> documents) {
            collection.insertDocuments(documents);
        }

        @Override
        public IndexEntity ensure(final Iterable<String> fields, final VectorIndexOptions options) {
            return collection.ensureVectorIndex(fields, options);
        }

        @Override
        public IndexEntity get(final String id) {
            return collection.getIndex(id);
        }

        @Override
        public Collection<IndexEntity> indexes(final IndexListOptions options) {
            return collection.getIndexes(options);
        }

        @Override
        public String delete(final String id) {
            return collection.deleteIndex(id);
        }

        @Override
        public void malformedParamsRequest() {
            collection.db().arango().execute(Request.<RawJson>builder()
                    .db(collection.db().name())
                    .method(Request.Method.POST)
                    .path("/_api/index")
                    .queryParam("collection", collection.name())
                    .body(RawJson.of("{\"type\":\"vector\",\"fields\":[\"v\"],\"params\":\"invalid\"}"))
                    .build(), ObjectNode.class);
        }

        @Override
        public void ensureOnUnknownCollection(final VectorIndexOptions options) {
            collection.db().collection("unknown_vector_collection_" + rnd())
                    .ensureVectorIndex(java.util.Collections.singletonList("v"), options);
        }
    }
}
