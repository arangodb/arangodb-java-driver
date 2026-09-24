package com.arangodb;

import com.arangodb.model.AqlQueryOptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class ArangoCursorProfileTest extends BaseJunit5 {

    private static final String QUERY = "FOR i IN 1..5 RETURN i";

    @BeforeAll
    static void init() {
        initDB();
    }

    private static Stream<Arguments> profileDbs() {
        return dbsStream().flatMap(db -> Stream.of(false, true).flatMap(stream ->
                Stream.of(null, false, true).flatMap(profile -> Stream.of(2, 10)
                        .map(batchSize -> Arguments.of(db, stream, profile, batchSize)))));
    }

    private static Stream<Arguments> asyncProfileDbs() {
        return asyncDbsStream().flatMap(db -> Stream.of(false, true).flatMap(stream ->
                Stream.of(null, false, true).flatMap(profile -> Stream.of(2, 10)
                        .map(batchSize -> Arguments.of(db, stream, profile, batchSize)))));
    }

    private static AqlQueryOptions options(boolean stream, Boolean profile, int batchSize) {
        return new AqlQueryOptions().profile(profile).stream(stream).batchSize(batchSize).cache(false);
    }

    private static void assertProfile(Map<String, Double> profile, boolean expected) {
        if (expected) {
            assertThat(profile).containsKeys("initializing", "executing", "finalizing");
            profile.forEach((phase, duration) -> assertThat(duration).as(phase).isNotNull().isGreaterThanOrEqualTo(0.0));
        } else {
            assertThat(profile).isNull();
        }
    }

    @ParameterizedTest
    @MethodSource("profileDbs")
    void profile(ArangoDatabase db, boolean stream, Boolean profile, int batchSize) throws IOException {
        boolean enabled = Boolean.TRUE.equals(profile);
        try (ArangoCursor<Integer> cursor = db.query(QUERY, Integer.class, options(stream, profile, batchSize))) {
            assertThat(cursor.isCached()).isFalse();
            assertProfile(cursor.getProfile(), enabled && (!stream || batchSize >= 5));
            assertThat(cursor.asListRemaining()).containsExactly(1, 2, 3, 4, 5);
            assertProfile(cursor.getProfile(), enabled);
        }
    }

    @ParameterizedTest
    @MethodSource("asyncProfileDbs")
    void profileAsync(ArangoDatabaseAsync db, boolean stream, Boolean profile, int batchSize) {
        boolean enabled = Boolean.TRUE.equals(profile);
        ArangoCursorAsync<Integer> cursor = db.query(QUERY, Integer.class, options(stream, profile, batchSize)).join();
        try {
            List<Integer> results = new ArrayList<>();
            while (true) {
                assertThat(cursor.isCached()).isFalse();
                assertProfile(cursor.getExtra().getProfile(), enabled && (!stream || !cursor.hasMore()));
                results.addAll(cursor.getResult());
                if (!cursor.hasMore()) {
                    break;
                }
                cursor = cursor.nextBatch().join();
            }
            assertThat(results).containsExactly(1, 2, 3, 4, 5);
            assertProfile(cursor.getExtra().getProfile(), enabled);
        } finally {
            cursor.close().join();
        }
    }
}
