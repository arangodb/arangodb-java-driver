/*
 * DISCLAIMER
 *
 * Copyright 2018 ArangoDB GmbH, Cologne, Germany
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

import com.arangodb.model.AqlQueryOptions;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Mark Vollmary
 * @author Michele Rastelli
 */
class ArangoCursorTest extends BaseJunit5 {

    @BeforeAll
    static void init() {
        initDB();
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void firstStream(ArangoDatabase db) {
        final ArangoCursor<JsonNode> cursor = db.query("FOR i IN 0..99 RETURN i", JsonNode.class);
        final Optional<JsonNode> first = cursor.stream().findFirst();
        assertThat(first).isPresent();
        assertThat(first.get().isInt()).isTrue();
        assertThat(first.get().asLong()).isZero();
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void next(ArangoDatabase db) {
        final ArangoCursor<JsonNode> cursor = db.query("FOR i IN 0..99 RETURN i", new AqlQueryOptions().batchSize(5), JsonNode.class);
        while (cursor.hasNext()) {
            cursor.next();
        }
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void mapFilterCountStream(ArangoDatabase db) {
        final ArangoCursor<JsonNode> cursor = db.query("FOR i IN 0..99 RETURN i", JsonNode.class);
        final long count = cursor.stream().map(JsonNode::asLong).filter(t -> t < 50).count();
        assertThat(count).isEqualTo(50L);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void mapFilterCollectIntoSetStream(ArangoDatabase db) {
        final ArangoCursor<JsonNode> cursor = db.query("FOR i IN 0..99 RETURN i", JsonNode.class);
        final Set<Long> target = cursor.stream().map(JsonNode::asLong).filter(t -> t < 50).collect(Collectors.toSet());
        assertThat(target)
                .isNotNull()
                .hasSize(50);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void forEach(ArangoDatabase db) {
        final AtomicLong i = new AtomicLong(0L);
        final ArangoCursor<JsonNode> cursor = db.query("FOR i IN 0..99 RETURN i", JsonNode.class);
        cursor.forEach(t -> assertThat(t.asLong()).isEqualTo(i.getAndIncrement()));
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void mapForeachStream(ArangoDatabase db) {
        final AtomicLong i = new AtomicLong(0L);
        final ArangoCursor<JsonNode> cursor = db.query("FOR i IN 0..99 RETURN i", JsonNode.class);
        cursor.stream().map(JsonNode::asLong).forEach(t -> assertThat(t).isEqualTo(i.getAndIncrement()));
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void mapFilterForEachStream(ArangoDatabase db) {
        final AtomicLong i = new AtomicLong(0L);
        final ArangoCursor<JsonNode> cursor = db.query("FOR i IN 0..99 RETURN i", JsonNode.class);
        cursor.stream().map(JsonNode::asLong).filter(t -> t < 50).forEach(t -> assertThat(t).isEqualTo(i.getAndIncrement()));
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void anyMatchStream(ArangoDatabase db) {
        final ArangoCursor<JsonNode> cursor = db.query("FOR i IN 0..99 RETURN i", JsonNode.class);
        final boolean match = cursor.stream().anyMatch(t -> t.asLong() == 50L);
        assertThat(match).isTrue();
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void noneMatchStream(ArangoDatabase db) {
        final ArangoCursor<JsonNode> cursor = db.query("FOR i IN 0..99 RETURN i", JsonNode.class);
        final boolean match = cursor.stream().noneMatch(t -> t.asLong() == 100L);
        assertThat(match).isTrue();
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void allMatchStream(ArangoDatabase db) {
        final ArangoCursor<JsonNode> cursor = db.query("FOR i IN 0..99 RETURN i", JsonNode.class);
        final boolean match = cursor.stream().allMatch(t -> t.asLong() < 100L);
        assertThat(match).isTrue();
    }

}
