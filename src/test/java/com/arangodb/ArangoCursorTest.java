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
import com.arangodb.velocypack.VPackSlice;
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
    void first(ArangoDatabase db) {
        final ArangoCursor<VPackSlice> cursor = db.query("FOR i IN 0..99 RETURN i", VPackSlice.class);
        final VPackSlice first = cursor.first();
        assertThat(first).isNotNull();
        assertThat(first.isInteger()).isTrue();
        assertThat(first.getAsLong()).isZero();
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void firstStream(ArangoDatabase db) {
        final ArangoCursor<VPackSlice> cursor = db.query("FOR i IN 0..99 RETURN i", VPackSlice.class);
        final Optional<VPackSlice> first = cursor.stream().findFirst();
        assertThat(first.isPresent()).isTrue();
        assertThat(first.get().isInteger()).isTrue();
        assertThat(first.get().getAsLong()).isZero();
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void next(ArangoDatabase db) {

        final ArangoCursor<VPackSlice> cursor = db.query("FOR i IN 0..99 RETURN i", new AqlQueryOptions().batchSize(5), VPackSlice.class);

        while (cursor.hasNext()) {
            cursor.next();
        }

    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void mapFilterCount(ArangoDatabase db) {
        final ArangoCursor<VPackSlice> cursor = db.query("FOR i IN 0..99 RETURN i", VPackSlice.class);
        final long count = cursor.map(VPackSlice::getAsLong).filter(t -> t < 50).count();
        assertThat(count).isEqualTo(50L);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void mapFilterCountStream(ArangoDatabase db) {
        final ArangoCursor<VPackSlice> cursor = db.query("FOR i IN 0..99 RETURN i", VPackSlice.class);
        final long count = cursor.stream().map(VPackSlice::getAsLong).filter(t -> t < 50).count();
        assertThat(count).isEqualTo(50L);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void mapMapFilterCount(ArangoDatabase db) {
        final ArangoCursor<VPackSlice> cursor = db.query("FOR i IN 0..99 RETURN i", VPackSlice.class);
        final long count = cursor.map(VPackSlice::getAsLong).map(t -> t * 10).filter(t -> t < 500).count();
        assertThat(count).isEqualTo(50L);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void mapMapFilterFilterCount(ArangoDatabase db) {
        final ArangoCursor<VPackSlice> cursor = db.query("FOR i IN 0..99 RETURN i", VPackSlice.class);
        final long count = cursor.map(VPackSlice::getAsLong).map(t -> t * 10).filter(t -> t < 500).filter(t -> t < 250).count();
        assertThat(count).isEqualTo(25L);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void mapFilterNext(ArangoDatabase db) {
        final ArangoCursor<VPackSlice> cursor = db.query("FOR i IN 0..99 RETURN i", VPackSlice.class);
        final long count = cursor.map(VPackSlice::getAsLong).filter(t -> t < 50).iterator().next();
        assertThat(count).isZero();
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void mapFilterFirst(ArangoDatabase db) {
        final ArangoCursor<VPackSlice> cursor = db.query("FOR i IN 0..99 RETURN i", VPackSlice.class);
        final long count = cursor.map(VPackSlice::getAsLong).filter(t -> t < 50).first();
        assertThat(count).isZero();
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void mapFilterCollectIntoList(ArangoDatabase db) {
        final ArangoCursor<VPackSlice> cursor = db.query("FOR i IN 0..99 RETURN i", VPackSlice.class);
        final List<Long> target = cursor.map(VPackSlice::getAsLong).filter(t -> t < 50).collectInto(new ArrayList<>());
        assertThat(target).isNotNull();
        assertThat(target).hasSize(50);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void mapFilterCollectIntoSet(ArangoDatabase db) {
        final ArangoCursor<VPackSlice> cursor = db.query("FOR i IN 0..99 RETURN i", VPackSlice.class);
        final Set<Long> target = cursor.map(VPackSlice::getAsLong).filter(t -> t < 50).collectInto(new HashSet<>());
        assertThat(target).isNotNull();
        assertThat(target).hasSize(50);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void mapFilterCollectIntoSetStream(ArangoDatabase db) {
        final ArangoCursor<VPackSlice> cursor = db.query("FOR i IN 0..99 RETURN i", VPackSlice.class);
        final Set<Long> target = cursor.stream().map(VPackSlice::getAsLong).filter(t -> t < 50).collect(Collectors.toSet());
        assertThat(target).isNotNull();
        assertThat(target).hasSize(50);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void foreach(ArangoDatabase db) {
        final AtomicLong i = new AtomicLong(0L);
        final ArangoCursor<VPackSlice> cursor = db.query("FOR i IN 0..99 RETURN i", VPackSlice.class);
        cursor.foreach(t -> assertThat(t.getAsLong()).isEqualTo(i.getAndIncrement()));
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void forEach(ArangoDatabase db) {
        final AtomicLong i = new AtomicLong(0L);
        final ArangoCursor<VPackSlice> cursor = db.query("FOR i IN 0..99 RETURN i", VPackSlice.class);
        cursor.forEach(t -> assertThat(t.getAsLong()).isEqualTo(i.getAndIncrement()));
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void mapForeach(ArangoDatabase db) {
        final AtomicLong i = new AtomicLong(0L);
        final ArangoCursor<VPackSlice> cursor = db.query("FOR i IN 0..99 RETURN i", VPackSlice.class);
        cursor.map(VPackSlice::getAsLong).foreach(t -> assertThat(t).isEqualTo(i.getAndIncrement()));
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void mapForeachStream(ArangoDatabase db) {
        final AtomicLong i = new AtomicLong(0L);
        final ArangoCursor<VPackSlice> cursor = db.query("FOR i IN 0..99 RETURN i", VPackSlice.class);
        cursor.stream().map(VPackSlice::getAsLong).forEach(t -> assertThat(t).isEqualTo(i.getAndIncrement()));
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void mapFilterForeach(ArangoDatabase db) {
        final AtomicLong i = new AtomicLong(0L);
        final ArangoCursor<VPackSlice> cursor = db.query("FOR i IN 0..99 RETURN i", VPackSlice.class);
        cursor.map(VPackSlice::getAsLong).filter(t -> t < 50).foreach(t -> assertThat(t).isEqualTo(i.getAndIncrement()));
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void mapFilterForEachStream(ArangoDatabase db) {
        final AtomicLong i = new AtomicLong(0L);
        final ArangoCursor<VPackSlice> cursor = db.query("FOR i IN 0..99 RETURN i", VPackSlice.class);
        cursor.stream().map(VPackSlice::getAsLong).filter(t -> t < 50).forEach(t -> assertThat(t).isEqualTo(i.getAndIncrement()));
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void anyMatch(ArangoDatabase db) {
        final ArangoCursor<VPackSlice> cursor = db.query("FOR i IN 0..99 RETURN i", VPackSlice.class);
        final boolean match = cursor.anyMatch(t -> t.getAsLong() == 50L);
        assertThat(match).isTrue();
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void anyMatchStream(ArangoDatabase db) {
        final ArangoCursor<VPackSlice> cursor = db.query("FOR i IN 0..99 RETURN i", VPackSlice.class);
        final boolean match = cursor.stream().anyMatch(t -> t.getAsLong() == 50L);
        assertThat(match).isTrue();
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void mapAnyMatch(ArangoDatabase db) {
        final ArangoCursor<VPackSlice> cursor = db.query("FOR i IN 0..99 RETURN i", VPackSlice.class);
        final boolean match = cursor.map(VPackSlice::getAsLong).anyMatch(t -> t == 50L);
        assertThat(match).isTrue();
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void mapFilterAnyMatch(ArangoDatabase db) {
        final ArangoCursor<VPackSlice> cursor = db.query("FOR i IN 0..99 RETURN i", VPackSlice.class);
        final boolean match = cursor.map(VPackSlice::getAsLong).filter(t -> t < 50).anyMatch(t -> t == 25L);
        assertThat(match).isTrue();
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void noneMatch(ArangoDatabase db) {
        final ArangoCursor<VPackSlice> cursor = db.query("FOR i IN 0..99 RETURN i", VPackSlice.class);
        final boolean match = cursor.noneMatch(t -> t.getAsLong() == 100L);
        assertThat(match).isTrue();
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void noneMatchStream(ArangoDatabase db) {
        final ArangoCursor<VPackSlice> cursor = db.query("FOR i IN 0..99 RETURN i", VPackSlice.class);
        final boolean match = cursor.stream().noneMatch(t -> t.getAsLong() == 100L);
        assertThat(match).isTrue();
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void mapNoneMatch(ArangoDatabase db) {
        final ArangoCursor<VPackSlice> cursor = db.query("FOR i IN 0..99 RETURN i", VPackSlice.class);
        final boolean match = cursor.map(VPackSlice::getAsLong).noneMatch(t -> t == 100L);
        assertThat(match).isTrue();
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void mapFilterNoneMatch(ArangoDatabase db) {
        final ArangoCursor<VPackSlice> cursor = db.query("FOR i IN 0..99 RETURN i", VPackSlice.class);
        final boolean match = cursor.map(VPackSlice::getAsLong).filter(t -> t < 50).noneMatch(t -> t == 50L);
        assertThat(match).isTrue();
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void allMatch(ArangoDatabase db) {
        final ArangoCursor<VPackSlice> cursor = db.query("FOR i IN 0..99 RETURN i", VPackSlice.class);
        final boolean match = cursor.allMatch(t -> t.getAsLong() < 100L);
        assertThat(match).isTrue();
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void allMatchStream(ArangoDatabase db) {
        final ArangoCursor<VPackSlice> cursor = db.query("FOR i IN 0..99 RETURN i", VPackSlice.class);
        final boolean match = cursor.stream().allMatch(t -> t.getAsLong() < 100L);
        assertThat(match).isTrue();
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void mapAllMatch(ArangoDatabase db) {
        final ArangoCursor<VPackSlice> cursor = db.query("FOR i IN 0..99 RETURN i", VPackSlice.class);
        final boolean match = cursor.map(VPackSlice::getAsLong).allMatch(t -> t < 100);
        assertThat(match).isTrue();
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void mapFilterAllMatch(ArangoDatabase db) {
        final ArangoCursor<VPackSlice> cursor = db.query("FOR i IN 0..99 RETURN i", VPackSlice.class);
        final boolean match = cursor.map(VPackSlice::getAsLong).filter(t -> t < 50).allMatch(t -> t < 50);
        assertThat(match).isTrue();
    }
}
