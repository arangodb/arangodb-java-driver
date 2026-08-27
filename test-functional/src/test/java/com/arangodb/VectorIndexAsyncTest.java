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

class VectorIndexAsyncTest extends BaseJunit5 {

    private static final String COLLECTION_NAME = "VectorIndexAsyncTest_collection";

    @BeforeAll
    static void init() {
        assumeTrue(isAtLeastVersion(3, 12, 10));
        initCollections(COLLECTION_NAME);
    }

    private static Stream<Arguments> collections() {
        return asyncDbsStream().map(mapNamedPayload(db -> db.collection(COLLECTION_NAME))).map(Arguments::of);
    }

    private static Stream<Arguments> collectionsAndMetrics() {
        return asyncDbsStream().flatMap(db -> Stream.of(VectorIndexParams.Metric.values())
                .map(metric -> Arguments.of(db.getPayload().collection(COLLECTION_NAME), metric)));
    }

    @ParameterizedTest
    @MethodSource("collectionsAndMetrics")
    void fixedModeEveryMetric(ArangoCollectionAsync collection, VectorIndexParams.Metric metric) {
        VectorIndexTestSupport.fixedModeEveryMetric(new AsyncOperations(collection), metric);
    }

    @ParameterizedTest
    @MethodSource("collections")
    void minimalModeAndServerDefaults(ArangoCollectionAsync collection) {
        VectorIndexTestSupport.minimalModeAndServerDefaults(new AsyncOperations(collection));
    }

    @ParameterizedTest
    @MethodSource("collections")
    void explicitScalingModeAndShardDetails(ArangoCollectionAsync collection) {
        VectorIndexTestSupport.explicitScalingModeAndShardDetails(new AsyncOperations(collection));
    }

    @ParameterizedTest
    @MethodSource("collections")
    void creationBeforeDataAndAutomaticSparseTraining(ArangoCollectionAsync collection) {
        VectorIndexTestSupport.creationBeforeDataAndAutomaticSparseTraining(new AsyncOperations(collection));
    }

    @ParameterizedTest
    @MethodSource("collections")
    void foregroundAndBackgroundUnusableResponses(ArangoCollectionAsync collection) {
        VectorIndexTestSupport.foregroundAndBackgroundUnusableResponses(new AsyncOperations(collection));
    }

    @ParameterizedTest
    @MethodSource("collections")
    void serverValidationErrors(ArangoCollectionAsync collection) {
        VectorIndexTestSupport.serverValidationErrors(new AsyncOperations(collection));
    }

    private static final class AsyncOperations implements VectorIndexTestSupport.Operations {
        private final ArangoCollectionAsync collection;

        private AsyncOperations(final ArangoCollectionAsync collection) {
            this.collection = collection;
        }

        @Override
        public String collectionName() {
            return collection.name();
        }

        @Override
        public void clean() {
            collection.getIndexes(new IndexListOptions().withHidden(true)).join().stream()
                    .filter(index -> index.getType() != IndexType.primary)
                    .map(IndexEntity::getId)
                    .forEach(id -> collection.deleteIndex(id).join());
            collection.truncate().join();
        }

        @Override
        public void insert(final List<BaseDocument> documents) {
            collection.insertDocuments(documents).join();
        }

        @Override
        public IndexEntity ensure(final Iterable<String> fields, final VectorIndexOptions options) {
            return collection.ensureVectorIndex(fields, options).join();
        }

        @Override
        public IndexEntity get(final String id) {
            return collection.getIndex(id).join();
        }

        @Override
        public Collection<IndexEntity> indexes(final IndexListOptions options) {
            return collection.getIndexes(options).join();
        }

        @Override
        public String delete(final String id) {
            return collection.deleteIndex(id).join();
        }

        @Override
        public void malformedParamsRequest() {
            collection.db().arango().execute(Request.<RawJson>builder()
                    .db(collection.db().name())
                    .method(Request.Method.POST)
                    .path("/_api/index")
                    .queryParam("collection", collection.name())
                    .body(RawJson.of("{\"type\":\"vector\",\"fields\":[\"v\"],\"params\":\"invalid\"}"))
                    .build(), ObjectNode.class).join();
        }

        @Override
        public void ensureOnUnknownCollection(final VectorIndexOptions options) {
            collection.db().collection("unknown_vector_collection_" + rnd())
                    .ensureVectorIndex(java.util.Collections.singletonList("v"), options).join();
        }
    }
}
