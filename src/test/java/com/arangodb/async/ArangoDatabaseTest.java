/*
 * DISCLAIMER
 *
 * Copyright 2016 ArangoDB GmbH, Cologne, Germany
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

package com.arangodb.async;

import com.arangodb.ArangoDBException;
import com.arangodb.DbName;
import com.arangodb.entity.AqlExecutionExplainEntity.ExecutionPlan;
import com.arangodb.entity.*;
import com.arangodb.entity.AqlParseEntity.AstNode;
import com.arangodb.entity.QueryCachePropertiesEntity.CacheMode;
import com.arangodb.model.*;
import com.arangodb.model.TraversalOptions.Direction;
import com.arangodb.velocypack.VPackBuilder;
import com.arangodb.velocypack.VPackSlice;
import com.arangodb.velocypack.exception.VPackException;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.assumeTrue;


/**
 * @author Mark Vollmary
 * @author Michele Rastelli
 */
class ArangoDatabaseTest extends BaseTest {

    private static final String COLLECTION_NAME = "db_test";
    private static final String GRAPH_NAME = "graph_test";

    @Test
    void create() throws InterruptedException, ExecutionException {
        try {
            final Boolean result = arangoDB.db(DbName.of(BaseTest.TEST_DB.get() + "_1")).create().get();
            assertThat(result).isTrue();
        } finally {
            arangoDB.db(DbName.of(BaseTest.TEST_DB.get() + "_1")).drop().get();
        }
    }

    @Test
    void getVersion() throws InterruptedException, ExecutionException {
        db.getVersion()
                .whenComplete((version, ex) -> {
                    assertThat(version).isNotNull();
                    assertThat(version.getServer()).isNotNull();
                    assertThat(version.getVersion()).isNotNull();
                })
                .get();
    }

    @Test
    void getEngine() throws ExecutionException, InterruptedException {
        final ArangoDBEngine engine = db.getEngine().get();
        assertThat(engine).isNotNull();
        assertThat(engine.getName()).isNotNull();
    }

    @Test
    void exists() throws InterruptedException, ExecutionException {
        assertThat(db.exists().get()).isTrue();
        assertThat(arangoDB.db(DbName.of("no")).exists().get()).isFalse();
    }

    @Test
    void getAccessibleDatabases() throws InterruptedException, ExecutionException {
        db.getAccessibleDatabases()
                .whenComplete((dbs, ex) -> {
                    assertThat(dbs).isNotNull();
                    assertThat(dbs.size()).isGreaterThan(0);
                    assertThat(dbs).contains("_system");
                })
                .get();
    }

    @Test
    void createCollection() throws InterruptedException, ExecutionException {
        db.createCollection(COLLECTION_NAME, null)
                .whenComplete((result, ex) -> {
                    assertThat(result).isNotNull();
                    assertThat(result.getId()).isNotNull();
                })
                .get();
        db.collection(COLLECTION_NAME).drop().get();
    }

    @Test
    void createCollectionWithReplicationFactor() throws InterruptedException, ExecutionException {
        assumeTrue(isCluster());
        final CollectionEntity result = db
                .createCollection(COLLECTION_NAME, new CollectionCreateOptions().replicationFactor(2)).get();
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(db.collection(COLLECTION_NAME).getProperties().get().getReplicationFactor()).isEqualTo(2);
        db.collection(COLLECTION_NAME).drop().get();
    }

    @Test
    void createCollectionWithWriteConcern() throws ExecutionException, InterruptedException {
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isCluster());

        final CollectionEntity result = db.createCollection(COLLECTION_NAME,
                new CollectionCreateOptions().replicationFactor(2).writeConcern(2)).get();
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(db.collection(COLLECTION_NAME).getProperties().get().getReplicationFactor()).isEqualTo(2);
        assertThat(db.collection(COLLECTION_NAME).getProperties().get().getWriteConcern()).isEqualTo(2);
        assertThat(db.collection(COLLECTION_NAME).getProperties().get().getSatellite()).isNull();
        db.collection(COLLECTION_NAME).drop();
    }

    @Test
    void createCollectionWithNumberOfShards() throws InterruptedException, ExecutionException {
        assumeTrue(isCluster());
        final CollectionEntity result = db
                .createCollection(COLLECTION_NAME, new CollectionCreateOptions().numberOfShards(2)).get();
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(db.collection(COLLECTION_NAME).getProperties().get().getNumberOfShards()).isEqualTo(2);
        db.collection(COLLECTION_NAME).drop().get();
    }

    @Test
    void createCollectionWithNumberOfShardsAndShardKey() throws InterruptedException, ExecutionException {
        assumeTrue(isCluster());
        final CollectionEntity result = db
                .createCollection(COLLECTION_NAME, new CollectionCreateOptions().numberOfShards(2).shardKeys("a"))
                .get();
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        final CollectionPropertiesEntity properties = db.collection(COLLECTION_NAME).getProperties().get();
        assertThat(properties.getNumberOfShards()).isEqualTo(2);
        assertThat(properties.getShardKeys()).hasSize(1);
        db.collection(COLLECTION_NAME).drop().get();
    }

    @Test
    void createCollectionWithNumberOfShardsAndShardKeys() throws InterruptedException, ExecutionException {
        assumeTrue(isCluster());
        final CollectionEntity result = db.createCollection(COLLECTION_NAME,
                new CollectionCreateOptions().numberOfShards(2).shardKeys("a", "b")).get();
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        final CollectionPropertiesEntity properties = db.collection(COLLECTION_NAME).getProperties().get();
        assertThat(properties.getNumberOfShards()).isEqualTo(2);
        assertThat(properties.getShardKeys()).hasSize(2);
        db.collection(COLLECTION_NAME).drop().get();
    }

    @Test
    void deleteCollection() throws InterruptedException, ExecutionException {
        db.createCollection(COLLECTION_NAME, null).get();
        db.collection(COLLECTION_NAME).drop().get();
        try {
            db.collection(COLLECTION_NAME).getInfo().get();
            fail();
        } catch (final ExecutionException e) {
            assertThat(e.getCause()).isInstanceOf(ArangoDBException.class);
        }
    }

    @Test
    void deleteSystemCollection() throws InterruptedException, ExecutionException {
        final String name = "_system_test";
        db.createCollection(name, new CollectionCreateOptions().isSystem(true)).get();
        db.collection(name).drop(true).get();
        try {
            db.collection(name).getInfo().get();
            fail();
        } catch (final ExecutionException e) {
            assertThat(e.getCause()).isInstanceOf(ArangoDBException.class);
        }
    }

    @Test
    void deleteSystemCollectionFail() throws InterruptedException, ExecutionException {
        final String name = "_system_test";
        db.createCollection(name, new CollectionCreateOptions().isSystem(true)).get();
        try {
            db.collection(name).drop().get();
            fail();
        } catch (final ExecutionException e) {
            assertThat(e.getCause()).isInstanceOf(ArangoDBException.class);
        }
        db.collection(name).drop(true).get();
        try {
            db.collection(name).getInfo().get();
            fail();
        } catch (final ExecutionException e) {
            assertThat(e.getCause()).isInstanceOf(ArangoDBException.class);
        }
    }

    @Test
    void getIndex() throws InterruptedException, ExecutionException {
        db.createCollection(COLLECTION_NAME, null).get();
        final Collection<String> fields = new ArrayList<>();
        fields.add("a");
        final IndexEntity createResult = db.collection(COLLECTION_NAME).ensureHashIndex(fields, null).get();
        db.getIndex(createResult.getId())
                .whenComplete((readResult, ex) -> {
                    assertThat(readResult.getId()).isEqualTo(createResult.getId());
                    assertThat(readResult.getType()).isEqualTo(createResult.getType());
                })
                .get();
        db.collection(COLLECTION_NAME).drop().get();
    }

    @Test
    void deleteIndex() throws InterruptedException, ExecutionException {
        try {
            db.createCollection(COLLECTION_NAME, null).get();
            final Collection<String> fields = new ArrayList<>();
            fields.add("a");
            final IndexEntity createResult = db.collection(COLLECTION_NAME).ensureHashIndex(fields, null).get();
            db.deleteIndex(createResult.getId())
                    .whenComplete((id, ex) -> {
                        assertThat(id).isEqualTo(createResult.getId());
                        try {
                            db.getIndex(id).get();
                            fail();
                        } catch (InterruptedException e) {
                            fail();
                        } catch (ExecutionException e) {
                            assertThat(e.getCause()).isInstanceOf(ArangoDBException.class);
                        }
                    })
                    .get();
        } finally {
            db.collection(COLLECTION_NAME).drop().get();
        }
    }

    @Test
    void getCollections() throws InterruptedException, ExecutionException {
        try {
            final Collection<CollectionEntity> systemCollections = db.getCollections(null).get();
            db.createCollection(COLLECTION_NAME + "1", null).get();
            db.createCollection(COLLECTION_NAME + "2", null).get();
            db.getCollections(null)
                    .whenComplete((collections, ex) -> {
                        assertThat(collections.size()).isEqualTo(2 + systemCollections.size());
                        assertThat(collections).isNotNull();
                    })
                    .get();
        } finally {
            db.collection(COLLECTION_NAME + "1").drop().get();
            db.collection(COLLECTION_NAME + "2").drop().get();
        }
    }

    @Test
    void getCollectionsExcludeSystem() throws InterruptedException, ExecutionException {
        try {
            final CollectionsReadOptions options = new CollectionsReadOptions().excludeSystem(true);
            final Collection<CollectionEntity> systemCollections = db.getCollections(options).get();
            assertThat(systemCollections).isEmpty();
            db.createCollection(COLLECTION_NAME + "1", null).get();
            db.createCollection(COLLECTION_NAME + "2", null).get();
            db.getCollections(options)
                    .whenComplete((collections, ex) -> {
                        assertThat(collections.size()).isEqualTo(2);
                        assertThat(collections).isNotNull();
                    })
                    .get();
        } finally {
            db.collection(COLLECTION_NAME + "1").drop().get();
            db.collection(COLLECTION_NAME + "2").drop().get();
        }
    }

    @Test
    void grantAccess() throws InterruptedException, ExecutionException {
        try {
            arangoDB.createUser("user1", "1234", null).get();
            db.grantAccess("user1").get();
        } finally {
            arangoDB.deleteUser("user1").get();
        }
    }

    @Test
    void grantAccessRW() throws InterruptedException, ExecutionException {
        try {
            arangoDB.createUser("user1", "1234", null).get();
            db.grantAccess("user1", Permissions.RW).get();
        } finally {
            arangoDB.deleteUser("user1").get();
        }
    }

    @Test
    void grantAccessRO() throws InterruptedException, ExecutionException {
        try {
            arangoDB.createUser("user1", "1234", null).get();
            db.grantAccess("user1", Permissions.RO).get();
        } finally {
            arangoDB.deleteUser("user1").get();
        }
    }

    @Test
    void grantAccessNONE() throws InterruptedException, ExecutionException {
        try {
            arangoDB.createUser("user1", "1234", null).get();
            db.grantAccess("user1", Permissions.NONE).get();
        } finally {
            arangoDB.deleteUser("user1").get();
        }
    }

    @Test
    void grantAccessUserNotFound() {
        Throwable thrown = catchThrowable(() -> db.grantAccess("user1", Permissions.RW).get());
        assertThat(thrown).isInstanceOf(ExecutionException.class);
    }

    @Test
    void revokeAccess() throws InterruptedException, ExecutionException {
        try {
            arangoDB.createUser("user1", "1234", null).get();
            db.revokeAccess("user1").get();
        } finally {
            arangoDB.deleteUser("user1").get();
        }
    }

    @Test
    void revokeAccessUserNotFound() {
        Throwable thrown = catchThrowable(() -> db.revokeAccess("user1").get());
        assertThat(thrown).isInstanceOf(ExecutionException.class);
    }

    @Test
    void resetAccess() throws InterruptedException, ExecutionException {
        try {
            arangoDB.createUser("user1", "1234", null).get();
            db.resetAccess("user1").get();
        } finally {
            arangoDB.deleteUser("user1").get();
        }
    }

    @Test
    void resetAccessUserNotFound() {
        Throwable thrown = catchThrowable(() -> db.resetAccess("user1").get());
        assertThat(thrown).isInstanceOf(ExecutionException.class);
    }

    @Test
    void grantDefaultCollectionAccess() throws InterruptedException, ExecutionException {
        try {
            arangoDB.createUser("user1", "1234").get();
            db.grantDefaultCollectionAccess("user1", Permissions.RW).get();
        } finally {
            arangoDB.deleteUser("user1").get();
        }
    }

    @Test
    void getPermissions() throws InterruptedException, ExecutionException {
        assertThat(db.getPermissions("root").get()).isEqualTo(Permissions.RW);
    }

    @Test
    void query() throws InterruptedException, ExecutionException {
        try {
            db.createCollection(COLLECTION_NAME, null).get();
            for (int i = 0; i < 10; i++) {
                db.collection(COLLECTION_NAME).insertDocument(new BaseDocument(), null).get();
            }
            db.query("for i in db_test return i._id", null, null, String.class)
                    .whenComplete((cursor, ex) -> {
                        for (int i = 0; i < 10; i++, cursor.next()) {
                            assertThat(cursor.hasNext()).isEqualTo(true);
                        }
                    })
                    .get();
        } finally {
            db.collection(COLLECTION_NAME).drop().get();
        }
    }

    @Test
    void queryForEach() throws InterruptedException, ExecutionException {
        try {
            db.createCollection(COLLECTION_NAME, null).get();
            for (int i = 0; i < 10; i++) {
                db.collection(COLLECTION_NAME).insertDocument(new BaseDocument(), null).get();
            }
            db.query("for i in db_test return i._id", null, null, String.class)
                    .whenComplete((cursor, ex) -> {
                        final AtomicInteger i = new AtomicInteger(0);
                        cursor.forEachRemaining(e -> i.incrementAndGet());
                        assertThat(i.get()).isEqualTo(10);
                    })
                    .get();
        } finally {
            db.collection(COLLECTION_NAME).drop().get();
        }
    }

    @Test
    void queryStream() throws InterruptedException, ExecutionException {
        try {
            db.createCollection(COLLECTION_NAME, null).get();
            for (int i = 0; i < 10; i++) {
                db.collection(COLLECTION_NAME).insertDocument(new BaseDocument(), null).get();
            }
            db.query("for i in db_test return i._id", null, null, String.class)
                    .whenComplete((cursor, ex) -> {
                        final AtomicInteger i = new AtomicInteger(0);
                        cursor.forEachRemaining(e -> i.incrementAndGet());
                        assertThat(i.get()).isEqualTo(10);
                    })
                    .get();
        } finally {
            db.collection(COLLECTION_NAME).drop().get();
        }
    }

    @Test
    void queryWithTimeout() throws ExecutionException, InterruptedException {
        assumeTrue(isAtLeastVersion(3, 6));

        try {
            db.query("RETURN SLEEP(1)", null, new AqlQueryOptions().maxRuntime(0.1), String.class).get().next();
            fail();
        } catch (ExecutionException e) {
            assertThat(e.getCause()).isInstanceOf(ArangoDBException.class);
            assertThat(((ArangoDBException) e.getCause()).getResponseCode()).isEqualTo(410);
        }
    }

    @Test
    void queryWithCount() throws InterruptedException, ExecutionException {
        try {
            db.createCollection(COLLECTION_NAME, null).get();
            for (int i = 0; i < 10; i++) {
                db.collection(COLLECTION_NAME).insertDocument(new BaseDocument(), null).get();
            }

            db.query("for i in db_test Limit 6 return i._id", null, new AqlQueryOptions().count(true), String.class)
                    .whenComplete((cursor, ex) -> {
                        for (int i = 0; i < 6; i++, cursor.next()) {
                            assertThat(cursor.hasNext()).isEqualTo(true);
                        }
                        assertThat(cursor.getCount()).isEqualTo(6);
                    })
                    .get();
        } finally {
            db.collection(COLLECTION_NAME).drop().get();
        }
    }

    @Test
    void queryWithLimitAndFullCount() throws InterruptedException, ExecutionException {
        try {
            db.createCollection(COLLECTION_NAME, null).get();
            for (int i = 0; i < 10; i++) {
                db.collection(COLLECTION_NAME).insertDocument(new BaseDocument(), null).get();
            }

            db.query("for i in db_test Limit 5 return i._id", null, new AqlQueryOptions().fullCount(true), String.class)
                    .whenComplete((cursor, ex) -> {
                        for (int i = 0; i < 5; i++, cursor.next()) {
                            assertThat(cursor.hasNext()).isEqualTo(true);
                        }
                        assertThat(cursor.getStats()).isNotNull();
                        assertThat(cursor.getStats().getFullCount()).isEqualTo(10L);
                    })
                    .get();
        } finally {
            db.collection(COLLECTION_NAME).drop().get();
        }
    }

    @Test
    void queryWithBatchSize() throws InterruptedException, ExecutionException {
        try {
            db.createCollection(COLLECTION_NAME, null).get();
            for (int i = 0; i < 10; i++) {
                db.collection(COLLECTION_NAME).insertDocument(new BaseDocument(), null).get();
            }
            final ArangoCursorAsync<String> cursor = db.query("for i in db_test return i._id", null,
                    new AqlQueryOptions().batchSize(5).count(true), String.class).get();
            for (int i = 0; i < 10; i++, cursor.next()) {
                assertThat(cursor.hasNext()).isTrue();
            }
        } finally {
            db.collection(COLLECTION_NAME).drop().get();
        }
    }

    @Test
    void queryStreamWithBatchSize() throws InterruptedException, ExecutionException {
        try {
            db.createCollection(COLLECTION_NAME, null).get();
            for (int i = 0; i < 10; i++) {
                db.collection(COLLECTION_NAME).insertDocument(new BaseDocument(), null).get();
            }
            final ArangoCursorAsync<String> cursor = db.query("for i in db_test return i._id", null,
                    new AqlQueryOptions().batchSize(5).count(true), String.class).get();
            final AtomicInteger i = new AtomicInteger(0);
            cursor.streamRemaining().forEach(e -> i.incrementAndGet());
            assertThat(i.get()).isEqualTo(10);
        } finally {
            db.collection(COLLECTION_NAME).drop().get();
        }
    }

    /**
     * ignored. takes to long
     */
    @Test
    @Disabled
    void queryWithTTL() throws InterruptedException, ExecutionException {
        // set TTL to 1 seconds and get the second batch after 2 seconds!
        final int ttl = 1;
        final int wait = 2;
        try {
            db.createCollection(COLLECTION_NAME, null).get();
            for (int i = 0; i < 10; i++) {
                db.collection(COLLECTION_NAME).insertDocument(new BaseDocument(), null).get();
            }
            final ArangoCursorAsync<String> cursor = db.query("for i in db_test return i._id", null,
                    new AqlQueryOptions().batchSize(5).ttl(ttl), String.class).get();
            for (int i = 0; i < 10; i++, cursor.next()) {
                assertThat(cursor.hasNext()).isTrue();
                if (i == 1) {
                    Thread.sleep(wait * 1000);
                }
            }
            fail();
        } catch (final ArangoDBException ex) {
            assertThat(ex.getResponseCode()).isEqualTo(404);
            assertThat(ex.getErrorNum()).isEqualTo(1600);
        } finally {
            db.collection(COLLECTION_NAME).drop().get();
        }
    }

    @Test
    void changeQueryCache() throws InterruptedException, ExecutionException {
        try {
            QueryCachePropertiesEntity properties = db.getQueryCacheProperties().get();
            assertThat(properties).isNotNull();
            assertThat(properties.getMode()).isEqualTo(CacheMode.off);
            assertThat(properties.getMaxResults()).isPositive();

            properties.setMode(CacheMode.on);
            properties = db.setQueryCacheProperties(properties).get();
            assertThat(properties).isNotNull();
            assertThat(properties.getMode()).isEqualTo(CacheMode.on);

            properties = db.getQueryCacheProperties().get();
            assertThat(properties.getMode()).isEqualTo(CacheMode.on);
        } finally {
            final QueryCachePropertiesEntity properties = new QueryCachePropertiesEntity();
            properties.setMode(CacheMode.off);
            db.setQueryCacheProperties(properties).get();
        }
    }

    @Test
    void queryWithCache() throws InterruptedException, ExecutionException {
        assumeTrue(isSingleServer());
        try {
            db.createCollection(COLLECTION_NAME, null).get();
            for (int i = 0; i < 10; i++) {
                db.collection(COLLECTION_NAME).insertDocument(new BaseDocument(), null).get();
            }

            final QueryCachePropertiesEntity properties = new QueryCachePropertiesEntity();
            properties.setMode(CacheMode.on);
            db.setQueryCacheProperties(properties).get();

            final ArangoCursorAsync<String> cursor = db
                    .query("FOR t IN db_test FILTER t.age >= 10 SORT t.age RETURN t._id", null,
                            new AqlQueryOptions().cache(true), String.class)
                    .get();

            assertThat(cursor.isCached()).isFalse();

            final ArangoCursorAsync<String> cachedCursor = db
                    .query("FOR t IN db_test FILTER t.age >= 10 SORT t.age RETURN t._id", null,
                            new AqlQueryOptions().cache(true), String.class)
                    .get();

            assertThat(cachedCursor.isCached()).isTrue();

        } finally {
            db.collection(COLLECTION_NAME).drop().get();
            final QueryCachePropertiesEntity properties = new QueryCachePropertiesEntity();
            properties.setMode(CacheMode.off);
            db.setQueryCacheProperties(properties).get();
        }
    }

    @Test
    void queryCursor() throws InterruptedException, ExecutionException {
        try {
            db.createCollection(COLLECTION_NAME, null).get();
            final int numbDocs = 10;
            for (int i = 0; i < numbDocs; i++) {
                db.collection(COLLECTION_NAME).insertDocument(new BaseDocument(), null).get();
            }

            final int batchSize = 5;
            final ArangoCursorAsync<String> cursor = db.query("for i in db_test return i._id", null,
                    new AqlQueryOptions().batchSize(batchSize).count(true), String.class).get();
            assertThat(cursor.getCount()).isEqualTo(numbDocs);

            final ArangoCursorAsync<String> cursor2 = db.cursor(cursor.getId(), String.class).get();
            assertThat(cursor2.getCount()).isEqualTo(numbDocs);
            assertThat(cursor2.hasNext()).isTrue();

            for (int i = 0; i < batchSize; i++, cursor.next()) {
                assertThat(cursor.hasNext()).isTrue();
            }
        } finally {
            db.collection(COLLECTION_NAME).drop().get();
        }
    }

    @Test
    void changeQueryTrackingProperties() throws InterruptedException, ExecutionException {
        try {
            QueryTrackingPropertiesEntity properties = db.getQueryTrackingProperties().get();
            assertThat(properties).isNotNull();
            assertThat(properties.getEnabled()).isTrue();
            assertThat(properties.getTrackSlowQueries()).isTrue();
            assertThat(properties.getMaxQueryStringLength()).isPositive();
            assertThat(properties.getMaxSlowQueries()).isPositive();
            assertThat(properties.getSlowQueryThreshold()).isPositive();
            properties.setEnabled(false);
            properties = db.setQueryTrackingProperties(properties).get();
            assertThat(properties).isNotNull();
            assertThat(properties.getEnabled()).isFalse();
            properties = db.getQueryTrackingProperties().get();
            assertThat(properties.getEnabled()).isFalse();
        } finally {
            final QueryTrackingPropertiesEntity properties = new QueryTrackingPropertiesEntity();
            properties.setEnabled(true);
            db.setQueryTrackingProperties(properties).get();
        }
    }

    @Test
    void queryWithBindVars() throws InterruptedException, ExecutionException {
        try {
            db.createCollection(COLLECTION_NAME, null).get();
            for (int i = 0; i < 10; i++) {
                final BaseDocument baseDocument = new BaseDocument();
                baseDocument.addAttribute("age", 20 + i);
                db.collection(COLLECTION_NAME).insertDocument(baseDocument, null).get();
            }
            final Map<String, Object> bindVars = new HashMap<>();
            bindVars.put("@coll", COLLECTION_NAME);
            bindVars.put("age", 25);
            db.query("FOR t IN @@coll FILTER t.age >= @age SORT t.age RETURN t._id", bindVars, null, String.class)
                    .whenComplete((cursor, ex) -> {
                        for (int i = 0; i < 5; i++, cursor.next()) {
                            assertThat(cursor.hasNext()).isEqualTo(true);
                        }
                    })
                    .get();
        } finally {
            db.collection(COLLECTION_NAME).drop().get();
        }
    }

    @Test
    void queryWithWarning() throws InterruptedException, ExecutionException {
        arangoDB.db().query("return 1/0", null, null, String.class)
                .whenComplete((cursor, ex) -> {
                    assertThat(cursor.getWarnings()).isNotNull();
                    assertThat(cursor.getWarnings()).isNotEmpty();
                })
                .get();
    }

    @Test
    void queryClose() throws IOException, InterruptedException, ExecutionException {
        final ArangoCursorAsync<String> cursor = arangoDB.db()
                .query("for i in 1..2 return i", null, new AqlQueryOptions().batchSize(1), String.class).get();
        cursor.close();
        int count = 0;
        try {
            for (; cursor.hasNext(); cursor.next(), count++) {
            }
            fail();
        } catch (final ArangoDBException e) {
            assertThat(count).isEqualTo(1);
        }

    }

    @Test
    void explainQuery() throws InterruptedException, ExecutionException {
        arangoDB.db().explainQuery("for i in 1..1 return i", null, null)
                .whenComplete((explain, ex) -> {
                    assertThat(explain).isNotNull();
                    assertThat(explain.getPlan()).isNotNull();
                    assertThat(explain.getPlans()).isNull();
                    final ExecutionPlan plan = explain.getPlan();
                    assertThat(plan.getCollections().size()).isEqualTo(0);
                    assertThat(plan.getEstimatedCost()).isGreaterThan(0);
                    assertThat(plan.getEstimatedNrItems()).isGreaterThan(0);
                    assertThat(plan.getVariables().size()).isEqualTo(2);
                    assertThat(plan.getNodes().size()).isGreaterThan(0);
                })
                .get();
    }

    @Test
    void parseQuery() throws InterruptedException, ExecutionException {
        assumeTrue(isAtLeastVersion(3, 4));
        arangoDB.db().parseQuery("for i in _apps return i")
                .whenComplete((parse, ex) -> {
                    assertThat(parse).isNotNull();
                    assertThat(parse.getBindVars()).isEmpty();
                    assertThat(parse.getCollections().size()).isEqualTo(1);
                    assertThat(parse.getCollections().iterator().next()).isEqualTo("_apps");
                    assertThat(parse.getAst().size()).isEqualTo(1);
                    final AstNode root = parse.getAst().iterator().next();
                    assertThat(root.getType()).isEqualTo("root");
                    assertThat(root.getName()).isNull();
                    assertThat(root.getSubNodes()).isNotNull();
                    assertThat(root.getSubNodes().size()).isEqualTo(2);
                    final Iterator<AstNode> iterator = root.getSubNodes().iterator();
                    final AstNode for_ = iterator.next();
                    assertThat(for_.getType()).isEqualTo("for");
                    assertThat(for_.getSubNodes()).isNotNull();
                    assertThat(for_.getSubNodes().size()).isEqualTo(3);
                    final Iterator<AstNode> iterator2 = for_.getSubNodes().iterator();
                    final AstNode first = iterator2.next();
                    assertThat(first.getType()).isEqualTo("variable");
                    assertThat(first.getName()).isEqualTo("i");
                    final AstNode second = iterator2.next();
                    assertThat(second.getType()).isEqualTo("collection");
                    assertThat(second.getName()).isEqualTo("_apps");
                    final AstNode return_ = iterator.next();
                    assertThat(return_.getType()).isEqualTo("return");
                    assertThat(return_.getSubNodes()).isNotNull();
                    assertThat(return_.getSubNodes().size()).isEqualTo(1);
                    assertThat(return_.getSubNodes().iterator().next().getType()).isEqualTo("reference");
                    assertThat(return_.getSubNodes().iterator().next().getName()).isEqualTo("i");
                })
                .get();
    }

    @Test
    @Disabled
    void getAndClearSlowQueries() throws InterruptedException, ExecutionException {
        final QueryTrackingPropertiesEntity properties = db.getQueryTrackingProperties().get();
        final Long slowQueryThreshold = properties.getSlowQueryThreshold();
        try {
            properties.setSlowQueryThreshold(1L);
            db.setQueryTrackingProperties(properties);

            db.query("return sleep(1.1)", null, null, Void.class);
            final Collection<QueryEntity> slowQueries = db.getSlowQueries().get();
            assertThat(slowQueries).isNotNull();
            assertThat(slowQueries).hasSize(1);
            final QueryEntity queryEntity = slowQueries.iterator().next();
            assertThat(queryEntity.getQuery()).isEqualTo("return sleep(1.1)");

            db.clearSlowQueries().get();
            assertThat(db.getSlowQueries().get().size()).isZero();
        } finally {
            properties.setSlowQueryThreshold(slowQueryThreshold);
            db.setQueryTrackingProperties(properties);
        }
    }

    @Test
    void createGetDeleteAqlFunction() throws InterruptedException, ExecutionException {
        final Collection<AqlFunctionEntity> aqlFunctionsInitial = db.getAqlFunctions(null).get();
        assertThat(aqlFunctionsInitial).isEmpty();
        try {
            db.createAqlFunction("myfunctions::temperature::celsiustofahrenheit",
                    "function (celsius) { return celsius * 1.8 + 32; }", null).get();

            final Collection<AqlFunctionEntity> aqlFunctions = db.getAqlFunctions(null).get();
            assertThat(aqlFunctions).hasSizeGreaterThan(aqlFunctionsInitial.size());
        } finally {
            final Integer deleteCount = db.deleteAqlFunction("myfunctions::temperature::celsiustofahrenheit", null)
                    .get();
            // compatibility with ArangoDB < 3.4
            if (isAtLeastVersion(3, 4)) {
                assertThat(deleteCount).isEqualTo(1);
            } else {
                assertThat(deleteCount).isNull();
            }

            final Collection<AqlFunctionEntity> aqlFunctions = db.getAqlFunctions(null).get();
            assertThat(aqlFunctions).hasSameSizeAs(aqlFunctionsInitial);
        }
    }

    @Test
    void createGetDeleteAqlFunctionWithNamespace() throws InterruptedException, ExecutionException {
        final Collection<AqlFunctionEntity> aqlFunctionsInitial = db.getAqlFunctions(null).get();
        assertThat(aqlFunctionsInitial).isEmpty();
        try {
            db.createAqlFunction("myfunctions::temperature::celsiustofahrenheit1",
                    "function (celsius) { return celsius * 1.8 + 32; }", null).get();
            db.createAqlFunction("myfunctions::temperature::celsiustofahrenheit2",
                    "function (celsius) { return celsius * 1.8 + 32; }", null).get();

        } finally {
            db.deleteAqlFunction("myfunctions::temperature", new AqlFunctionDeleteOptions().group(true)).get();

            final Collection<AqlFunctionEntity> aqlFunctions = db.getAqlFunctions(null).get();
            assertThat(aqlFunctions).hasSameSizeAs(aqlFunctionsInitial);
        }
    }

    @Test
    void createGraph() throws InterruptedException, ExecutionException {
        try {
            db.createGraph(GRAPH_NAME, null, null)
                    .whenComplete((result, ex) -> {
                        assertThat(result).isNotNull();
                        assertThat(result.getName()).isEqualTo(GRAPH_NAME);
                    })
                    .get();
        } finally {
            db.graph(GRAPH_NAME).drop().get();
        }
    }

    @Test
    void getGraphs() throws InterruptedException, ExecutionException {
        try {
            db.createGraph(GRAPH_NAME, null, null).get();
            db.getGraphs()
                    .whenComplete((graphs, ex) -> {
                        assertThat(graphs).isNotNull();
                        assertThat(graphs.size()).isEqualTo(1);
                    })
                    .get();
        } finally {
            db.graph(GRAPH_NAME).drop().get();
        }
    }

    @Test
    void transactionString() throws InterruptedException, ExecutionException {
        final TransactionOptions options = new TransactionOptions().params("test");
        db.transaction("function (params) {return params;}", String.class, options)
                .whenComplete((result, ex) -> assertThat(result).isEqualTo("test"))
                .get();
    }

    @Test
    void transactionNumber() throws InterruptedException, ExecutionException {
        final TransactionOptions options = new TransactionOptions().params(5);
        db.transaction("function (params) {return params;}", Integer.class, options)
                .whenComplete((result, ex) -> assertThat(result).isEqualTo(5))
                .get();
    }

    @Test
    void transactionVPack() throws VPackException, InterruptedException, ExecutionException {
        final TransactionOptions options = new TransactionOptions().params(new VPackBuilder().add("test").slice());
        db.transaction("function (params) {return params;}", VPackSlice.class, options)
                .whenComplete((result, ex) -> {
                    assertThat(result.isString()).isEqualTo(true);
                    assertThat(result.getAsString()).isEqualTo("test");
                })
                .get();
    }

    @Test
    void transactionEmpty() throws InterruptedException, ExecutionException {
        db.transaction("function () {}", null, null).get();
    }

    @Test
    void transactionallowImplicit() throws InterruptedException, ExecutionException {
        try {
            db.createCollection("someCollection", null).get();
            db.createCollection("someOtherCollection", null).get();
            final String action = "function (params) {" + "var db = require('internal').db;"
                    + "return {'a':db.someCollection.all().toArray()[0], 'b':db.someOtherCollection.all().toArray()[0]};"
                    + "}";
            final TransactionOptions options = new TransactionOptions().readCollections("someCollection");
            db.transaction(action, VPackSlice.class, options).get();
            try {
                options.allowImplicit(false);
                db.transaction(action, VPackSlice.class, options).get();
                fail();
            } catch (final ExecutionException e) {
                assertThat(e.getCause()).isInstanceOf(ArangoDBException.class);
            }
        } finally {
            db.collection("someCollection").drop().get();
            db.collection("someOtherCollection").drop().get();
        }
    }

    @Test
    void transactionPojoReturn() throws ExecutionException, InterruptedException {
        final String action = "function() { return {'value':'hello world'}; }";
        db.transaction(action, TransactionTestEntity.class, new TransactionOptions())
                .whenComplete((res, ex) -> {
                    assertThat(res).isNotNull();
                    assertThat(res.value).isEqualTo("hello world");
                })
                .get();
    }

    @Test
    void getInfo() throws InterruptedException, ExecutionException {
        final CompletableFuture<DatabaseEntity> f = db.getInfo();
        assertThat(f).isNotNull();
        f.whenComplete((info, ex) -> {
            assertThat(info).isNotNull();
            assertThat(info.getId()).isNotNull();
            assertThat(info.getName()).isEqualTo(TEST_DB.get());
            assertThat(info.getPath()).isNotNull();
            assertThat(info.getIsSystem()).isFalse();

            try {
                if (isAtLeastVersion(3, 6) && isCluster()) {
                    assertThat(info.getSharding()).isNotNull();
                    assertThat(info.getWriteConcern()).isNotNull();
                    assertThat(info.getReplicationFactor()).isNotNull();
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
                fail();
            }

        });
        f.get();
    }

    @Test
    void executeTraversal() throws InterruptedException, ExecutionException {
        try {
            db.createCollection("person", null).get();
            db.createCollection("knows", new CollectionCreateOptions().type(CollectionType.EDGES)).get();
            for (final String e : new String[]{"Alice", "Bob", "Charlie", "Dave", "Eve"}) {
                final BaseDocument doc = new BaseDocument();
                doc.setKey(e);
                db.collection("person").insertDocument(doc, null).get();
            }
            for (final String[] e : new String[][]{new String[]{"Alice", "Bob"}, new String[]{"Bob", "Charlie"},
                    new String[]{"Bob", "Dave"}, new String[]{"Eve", "Alice"}, new String[]{"Eve", "Bob"}}) {
                final BaseEdgeDocument edge = new BaseEdgeDocument();
                edge.setKey(e[0] + "_knows_" + e[1]);
                edge.setFrom("person/" + e[0]);
                edge.setTo("person/" + e[1]);
                db.collection("knows").insertDocument(edge, null).get();
            }
            final TraversalOptions options = new TraversalOptions().edgeCollection("knows").startVertex("person/Alice")
                    .direction(Direction.outbound);
            db.executeTraversal(BaseDocument.class, BaseEdgeDocument.class, options)
                    .whenComplete((traversal, ex) -> {
                        assertThat(traversal).isNotNull();

                        final Collection<BaseDocument> vertices = traversal.getVertices();
                        assertThat(vertices).isNotNull();
                        assertThat(vertices.size()).isEqualTo(4);

                        final Iterator<BaseDocument> verticesIterator = vertices.iterator();
                        final Collection<String> v = Arrays.asList("Alice", "Bob", "Charlie", "Dave");
                        while (verticesIterator.hasNext()) {
                            assertThat(v.contains(verticesIterator.next().getKey())).isEqualTo(true);
                        }

                        final Collection<PathEntity<BaseDocument, BaseEdgeDocument>> paths = traversal.getPaths();
                        assertThat(paths).isNotNull();
                        assertThat(paths.size()).isEqualTo(4);

                        assertThat(paths.iterator().hasNext()).isEqualTo(true);
                        final PathEntity<BaseDocument, BaseEdgeDocument> first = paths.iterator().next();
                        assertThat(first).isNotNull();
                        assertThat(first.getEdges().size()).isEqualTo(0);
                        assertThat(first.getVertices().size()).isEqualTo(1);
                        assertThat(first.getVertices().iterator().next().getKey()).isEqualTo("Alice");
                    })
                    .get();
        } finally {
            db.collection("person").drop().get();
            db.collection("knows").drop().get();
        }
    }

    @Test
    void getDocument() throws InterruptedException, ExecutionException {
        String collectionName = COLLECTION_NAME + "getDocument";
        db.createCollection(collectionName).get();
        final BaseDocument value = new BaseDocument();
        value.setKey("123");
        db.collection(collectionName).insertDocument(value).get();
        db.getDocument(collectionName + "/123", BaseDocument.class)
                .whenComplete((document, ex) -> {
                    assertThat(document).isNotNull();
                    assertThat(document.getKey()).isEqualTo("123");
                })
                .get();
        db.collection(collectionName).drop().get();
    }

    @Test
    void shouldIncludeExceptionMessage() throws InterruptedException, ExecutionException {
        final String exceptionMessage = "My error context";
        final String action = "function (params) {" + "throw '" + exceptionMessage + "';" + "}";
        try {
            db.transaction(action, VPackSlice.class, null).get();
            fail();
        } catch (final ExecutionException e) {
            assertThat(e.getCause()).isInstanceOf(ArangoDBException.class);
            ArangoDBException cause = ((ArangoDBException) e.getCause());
            assertThat(cause.getErrorNum()).isEqualTo(1650);
            if (isAtLeastVersion(3, 4)) {
                assertThat(cause.getErrorMessage()).isEqualTo(exceptionMessage);
            }
        }
    }

    @Test
    void getDocumentWrongId() {
        Throwable thrown = catchThrowable(() -> db.getDocument("123", BaseDocument.class).get());
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
    }

    @Test
    void reloadRouting() throws InterruptedException, ExecutionException {
        db.reloadRouting().get();
    }

    @SuppressWarnings({"WeakerAccess", "unused"})
    static class TransactionTestEntity {
        private String value;

        TransactionTestEntity() {
            super();
        }
    }
}
