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
import com.arangodb.entity.AqlExecutionExplainEntity.ExecutionPlan;
import com.arangodb.entity.*;
import com.arangodb.entity.AqlParseEntity.AstNode;
import com.arangodb.entity.QueryCachePropertiesEntity.CacheMode;
import com.arangodb.model.*;
import com.arangodb.model.TraversalOptions.Direction;
import com.arangodb.velocypack.VPackBuilder;
import com.arangodb.velocypack.VPackSlice;
import com.arangodb.velocypack.exception.VPackException;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

/**
 * @author Mark Vollmary
 * @author Michele Rastelli
 */
public class ArangoDatabaseTest extends BaseTest {

    private static final String COLLECTION_NAME = "db_test";
    private static final String GRAPH_NAME = "graph_test";

    @Test
    public void create() throws InterruptedException, ExecutionException {
        try {
            final Boolean result = arangoDB.db(BaseTest.TEST_DB + "_1").create().get();
            assertThat(result, is(true));
        } finally {
            arangoDB.db(BaseTest.TEST_DB + "_1").drop().get();
        }
    }

    @Test
    public void getVersion() throws InterruptedException, ExecutionException {
        db.getVersion()
                .whenComplete((version, ex) -> {
                    assertThat(version, is(notNullValue()));
                    assertThat(version.getServer(), is(notNullValue()));
                    assertThat(version.getVersion(), is(notNullValue()));
                })
                .get();
    }

    @Test
    public void getEngine() throws ExecutionException, InterruptedException {
        final ArangoDBEngine engine = db.getEngine().get();
        assertThat(engine, is(notNullValue()));
        assertThat(engine.getName(), is(notNullValue()));
    }

    @Test
    public void exists() throws InterruptedException, ExecutionException {
        assertThat(db.exists().get(), is(true));
        assertThat(arangoDB.db("no").exists().get(), is(false));
    }

    @Test
    public void getAccessibleDatabases() throws InterruptedException, ExecutionException {
        db.getAccessibleDatabases()
                .whenComplete((dbs, ex) -> {
                    assertThat(dbs, is(notNullValue()));
                    assertThat(dbs.size(), greaterThan(0));
                    assertThat(dbs, hasItem("_system"));
                })
                .get();
    }

    @Test
    public void createCollection() throws InterruptedException, ExecutionException {
        db.createCollection(COLLECTION_NAME, null)
                .whenComplete((result, ex) -> {
                    assertThat(result, is(notNullValue()));
                    assertThat(result.getId(), is(notNullValue()));
                })
                .get();
        db.collection(COLLECTION_NAME).drop().get();
    }

    @Test
    public void createCollectionWithReplicationFactor() throws InterruptedException, ExecutionException {
        assumeTrue(isCluster());
        final CollectionEntity result = db
                .createCollection(COLLECTION_NAME, new CollectionCreateOptions().replicationFactor(2)).get();
        assertThat(result, is(notNullValue()));
        assertThat(result.getId(), is(notNullValue()));
        assertThat(db.collection(COLLECTION_NAME).getProperties().get().getReplicationFactor(), is(2));
        db.collection(COLLECTION_NAME).drop().get();
    }

    @Test
    public void createCollectionWithMinReplicationFactor() throws ExecutionException, InterruptedException {
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isCluster());

        final CollectionEntity result = db.createCollection(COLLECTION_NAME,
                new CollectionCreateOptions().replicationFactor(2).minReplicationFactor(2)).get();
        assertThat(result, is(notNullValue()));
        assertThat(result.getId(), is(notNullValue()));
        assertThat(db.collection(COLLECTION_NAME).getProperties().get().getReplicationFactor(), is(2));
        assertThat(db.collection(COLLECTION_NAME).getProperties().get().getMinReplicationFactor(), is(2));
        assertThat(db.collection(COLLECTION_NAME).getProperties().get().getSatellite(), is(nullValue()));
        db.collection(COLLECTION_NAME).drop();
    }

    @Test
    public void createCollectionWithNumberOfShards() throws InterruptedException, ExecutionException {
        assumeTrue(isCluster());
        final CollectionEntity result = db
                .createCollection(COLLECTION_NAME, new CollectionCreateOptions().numberOfShards(2)).get();
        assertThat(result, is(notNullValue()));
        assertThat(result.getId(), is(notNullValue()));
        assertThat(db.collection(COLLECTION_NAME).getProperties().get().getNumberOfShards(), is(2));
        db.collection(COLLECTION_NAME).drop().get();
    }

    @Test
    public void createCollectionWithNumberOfShardsAndShardKey() throws InterruptedException, ExecutionException {
        assumeTrue(isCluster());
        final CollectionEntity result = db
                .createCollection(COLLECTION_NAME, new CollectionCreateOptions().numberOfShards(2).shardKeys("a"))
                .get();
        assertThat(result, is(notNullValue()));
        assertThat(result.getId(), is(notNullValue()));
        final CollectionPropertiesEntity properties = db.collection(COLLECTION_NAME).getProperties().get();
        assertThat(properties.getNumberOfShards(), is(2));
        assertThat(properties.getShardKeys().size(), is(1));
        db.collection(COLLECTION_NAME).drop().get();
    }

    @Test
    public void createCollectionWithNumberOfShardsAndShardKeys() throws InterruptedException, ExecutionException {
        assumeTrue(isCluster());
        final CollectionEntity result = db.createCollection(COLLECTION_NAME,
                new CollectionCreateOptions().numberOfShards(2).shardKeys("a", "b")).get();
        assertThat(result, is(notNullValue()));
        assertThat(result.getId(), is(notNullValue()));
        final CollectionPropertiesEntity properties = db.collection(COLLECTION_NAME).getProperties().get();
        assertThat(properties.getNumberOfShards(), is(2));
        assertThat(properties.getShardKeys().size(), is(2));
        db.collection(COLLECTION_NAME).drop().get();
    }

    @Test
    public void deleteCollection() throws InterruptedException, ExecutionException {
        db.createCollection(COLLECTION_NAME, null).get();
        db.collection(COLLECTION_NAME).drop().get();
        try {
            db.collection(COLLECTION_NAME).getInfo().get();
            fail();
        } catch (final ExecutionException e) {
            assertThat(e.getCause(), instanceOf(ArangoDBException.class));
        }
    }

    @Test
    public void deleteSystemCollection() throws InterruptedException, ExecutionException {
        final String name = "_system_test";
        db.createCollection(name, new CollectionCreateOptions().isSystem(true)).get();
        db.collection(name).drop(true).get();
        try {
            db.collection(name).getInfo().get();
            fail();
        } catch (final ExecutionException e) {
            assertThat(e.getCause(), instanceOf(ArangoDBException.class));
        }
    }

    @Test
    public void deleteSystemCollectionFail() throws InterruptedException, ExecutionException {
        final String name = "_system_test";
        db.createCollection(name, new CollectionCreateOptions().isSystem(true)).get();
        try {
            db.collection(name).drop().get();
            fail();
        } catch (final ExecutionException e) {
            assertThat(e.getCause(), instanceOf(ArangoDBException.class));
        }
        db.collection(name).drop(true).get();
        try {
            db.collection(name).getInfo().get();
            fail();
        } catch (final ExecutionException e) {
            assertThat(e.getCause(), instanceOf(ArangoDBException.class));
        }
    }

    @Test
    public void getIndex() throws InterruptedException, ExecutionException {
        db.createCollection(COLLECTION_NAME, null).get();
        final Collection<String> fields = new ArrayList<>();
        fields.add("a");
        final IndexEntity createResult = db.collection(COLLECTION_NAME).ensureHashIndex(fields, null).get();
        db.getIndex(createResult.getId())
                .whenComplete((readResult, ex) -> {
                    assertThat(readResult.getId(), is(createResult.getId()));
                    assertThat(readResult.getType(), is(createResult.getType()));
                })
                .get();
        db.collection(COLLECTION_NAME).drop().get();
    }

    @Test
    public void deleteIndex() throws InterruptedException, ExecutionException {
        try {
            db.createCollection(COLLECTION_NAME, null).get();
            final Collection<String> fields = new ArrayList<>();
            fields.add("a");
            final IndexEntity createResult = db.collection(COLLECTION_NAME).ensureHashIndex(fields, null).get();
            db.deleteIndex(createResult.getId())
                    .whenComplete((id, ex) -> {
                        assertThat(id, is(createResult.getId()));
                        try {
                            db.getIndex(id).get();
                            fail();
                        } catch (InterruptedException e) {
                            fail();
                        } catch (ExecutionException e) {
                            assertThat(e.getCause(), instanceOf(ArangoDBException.class));
                        }
                    })
                    .get();
        } finally {
            db.collection(COLLECTION_NAME).drop().get();
        }
    }

    @Test
    public void getCollections() throws InterruptedException, ExecutionException {
        try {
            final Collection<CollectionEntity> systemCollections = db.getCollections(null).get();
            db.createCollection(COLLECTION_NAME + "1", null).get();
            db.createCollection(COLLECTION_NAME + "2", null).get();
            db.getCollections(null)
                    .whenComplete((collections, ex) -> {
                        assertThat(collections.size(), is(2 + systemCollections.size()));
                        assertThat(collections, is(notNullValue()));
                    })
                    .get();
        } finally {
            db.collection(COLLECTION_NAME + "1").drop().get();
            db.collection(COLLECTION_NAME + "2").drop().get();
        }
    }

    @Test
    public void getCollectionsExcludeSystem() throws InterruptedException, ExecutionException {
        try {
            final CollectionsReadOptions options = new CollectionsReadOptions().excludeSystem(true);
            final Collection<CollectionEntity> systemCollections = db.getCollections(options).get();
            assertThat(systemCollections.size(), is(0));
            db.createCollection(COLLECTION_NAME + "1", null).get();
            db.createCollection(COLLECTION_NAME + "2", null).get();
            db.getCollections(options)
                    .whenComplete((collections, ex) -> {
                        assertThat(collections.size(), is(2));
                        assertThat(collections, is(notNullValue()));
                    })
                    .get();
        } finally {
            db.collection(COLLECTION_NAME + "1").drop().get();
            db.collection(COLLECTION_NAME + "2").drop().get();
        }
    }

    @Test
    public void grantAccess() throws InterruptedException, ExecutionException {
        try {
            arangoDB.createUser("user1", "1234", null).get();
            db.grantAccess("user1").get();
        } finally {
            arangoDB.deleteUser("user1").get();
        }
    }

    @Test
    public void grantAccessRW() throws InterruptedException, ExecutionException {
        try {
            arangoDB.createUser("user1", "1234", null).get();
            db.grantAccess("user1", Permissions.RW).get();
        } finally {
            arangoDB.deleteUser("user1").get();
        }
    }

    @Test
    public void grantAccessRO() throws InterruptedException, ExecutionException {
        try {
            arangoDB.createUser("user1", "1234", null).get();
            db.grantAccess("user1", Permissions.RO).get();
        } finally {
            arangoDB.deleteUser("user1").get();
        }
    }

    @Test
    public void grantAccessNONE() throws InterruptedException, ExecutionException {
        try {
            arangoDB.createUser("user1", "1234", null).get();
            db.grantAccess("user1", Permissions.NONE).get();
        } finally {
            arangoDB.deleteUser("user1").get();
        }
    }

    @Test(expected = ExecutionException.class)
    public void grantAccessUserNotFound() throws InterruptedException, ExecutionException {
        db.grantAccess("user1", Permissions.RW).get();
    }

    @Test
    public void revokeAccess() throws InterruptedException, ExecutionException {
        try {
            arangoDB.createUser("user1", "1234", null).get();
            db.revokeAccess("user1").get();
        } finally {
            arangoDB.deleteUser("user1").get();
        }
    }

    @Test(expected = ExecutionException.class)
    public void revokeAccessUserNotFound() throws InterruptedException, ExecutionException {
        db.revokeAccess("user1").get();
    }

    @Test
    public void resetAccess() throws InterruptedException, ExecutionException {
        try {
            arangoDB.createUser("user1", "1234", null).get();
            db.resetAccess("user1").get();
        } finally {
            arangoDB.deleteUser("user1").get();
        }
    }

    @Test(expected = ExecutionException.class)
    public void resetAccessUserNotFound() throws InterruptedException, ExecutionException {
        db.resetAccess("user1").get();
    }

    @Test
    public void grantDefaultCollectionAccess() throws InterruptedException, ExecutionException {
        try {
            arangoDB.createUser("user1", "1234").get();
            db.grantDefaultCollectionAccess("user1", Permissions.RW).get();
        } finally {
            arangoDB.deleteUser("user1").get();
        }
    }

    @Test
    public void getPermissions() throws InterruptedException, ExecutionException {
        assertThat(Permissions.RW, is(db.getPermissions("root").get()));
    }

    @Test
    public void query() throws InterruptedException, ExecutionException {
        try {
            db.createCollection(COLLECTION_NAME, null).get();
            for (int i = 0; i < 10; i++) {
                db.collection(COLLECTION_NAME).insertDocument(new BaseDocument(), null).get();
            }
            db.query("for i in db_test return i._id", null, null, String.class)
                    .whenComplete((cursor, ex) -> {
                        assertThat(cursor, is(notNullValue()));
                        for (int i = 0; i < 10; i++, cursor.next()) {
                            assertThat(cursor.hasNext(), is(true));
                        }
                    })
                    .get();
        } finally {
            db.collection(COLLECTION_NAME).drop().get();
        }
    }

    @Test
    public void queryForEach() throws InterruptedException, ExecutionException {
        try {
            db.createCollection(COLLECTION_NAME, null).get();
            for (int i = 0; i < 10; i++) {
                db.collection(COLLECTION_NAME).insertDocument(new BaseDocument(), null).get();
            }
            db.query("for i in db_test return i._id", null, null, String.class)
                    .whenComplete((cursor, ex) -> {
                        assertThat(cursor, is(notNullValue()));
                        final AtomicInteger i = new AtomicInteger(0);
                        cursor.forEachRemaining(e -> i.incrementAndGet());
                        assertThat(i.get(), is(10));
                    })
                    .get();
        } finally {
            db.collection(COLLECTION_NAME).drop().get();
        }
    }

    @Test
    public void queryStream() throws InterruptedException, ExecutionException {
        try {
            db.createCollection(COLLECTION_NAME, null).get();
            for (int i = 0; i < 10; i++) {
                db.collection(COLLECTION_NAME).insertDocument(new BaseDocument(), null).get();
            }
            db.query("for i in db_test return i._id", null, null, String.class)
                    .whenComplete((cursor, ex) -> {
                        assertThat(cursor, is(notNullValue()));
                        final AtomicInteger i = new AtomicInteger(0);
                        cursor.forEachRemaining(e -> i.incrementAndGet());
                        assertThat(i.get(), is(10));
                    })
                    .get();
        } finally {
            db.collection(COLLECTION_NAME).drop().get();
        }
    }

    @Test
    public void queryWithTimeout() throws ExecutionException, InterruptedException {
        assumeTrue(isAtLeastVersion(3, 6));

        try {
            db.query("RETURN SLEEP(1)", null, new AqlQueryOptions().maxRuntime(0.1), String.class).get().next();
            fail();
        } catch (ExecutionException e) {
            assertThat(e.getCause(), instanceOf(ArangoDBException.class));
            assertThat(((ArangoDBException) e.getCause()).getResponseCode(), is(410));
        }
    }

    @Test
    public void queryWithCount() throws InterruptedException, ExecutionException {
        try {
            db.createCollection(COLLECTION_NAME, null).get();
            for (int i = 0; i < 10; i++) {
                db.collection(COLLECTION_NAME).insertDocument(new BaseDocument(), null).get();
            }

            db.query("for i in db_test Limit 6 return i._id", null, new AqlQueryOptions().count(true), String.class)
                    .whenComplete((cursor, ex) -> {
                        assertThat(cursor, is(notNullValue()));
                        for (int i = 0; i < 6; i++, cursor.next()) {
                            assertThat(cursor.hasNext(), is(true));
                        }
                        assertThat(cursor.getCount(), is(6));
                    })
                    .get();
        } finally {
            db.collection(COLLECTION_NAME).drop().get();
        }
    }

    @Test
    public void queryWithLimitAndFullCount() throws InterruptedException, ExecutionException {
        try {
            db.createCollection(COLLECTION_NAME, null).get();
            for (int i = 0; i < 10; i++) {
                db.collection(COLLECTION_NAME).insertDocument(new BaseDocument(), null).get();
            }

            db.query("for i in db_test Limit 5 return i._id", null, new AqlQueryOptions().fullCount(true), String.class)
                    .whenComplete((cursor, ex) -> {
                        assertThat(cursor, is(notNullValue()));
                        for (int i = 0; i < 5; i++, cursor.next()) {
                            assertThat(cursor.hasNext(), is(true));
                        }
                        assertThat(cursor.getStats(), is(notNullValue()));
                        assertThat(cursor.getStats().getFullCount(), is(10L));
                    })
                    .get();
        } finally {
            db.collection(COLLECTION_NAME).drop().get();
        }
    }

    @Test
    public void queryWithBatchSize() throws InterruptedException, ExecutionException {
        try {
            db.createCollection(COLLECTION_NAME, null).get();
            for (int i = 0; i < 10; i++) {
                db.collection(COLLECTION_NAME).insertDocument(new BaseDocument(), null).get();
            }
            final ArangoCursorAsync<String> cursor = db.query("for i in db_test return i._id", null,
                    new AqlQueryOptions().batchSize(5).count(true), String.class).get();
            assertThat(cursor, is(notNullValue()));
            for (int i = 0; i < 10; i++, cursor.next()) {
                assertThat(cursor.hasNext(), is(true));
            }
        } finally {
            db.collection(COLLECTION_NAME).drop().get();
        }
    }

    @Test
    public void queryStreamWithBatchSize() throws InterruptedException, ExecutionException {
        try {
            db.createCollection(COLLECTION_NAME, null).get();
            for (int i = 0; i < 10; i++) {
                db.collection(COLLECTION_NAME).insertDocument(new BaseDocument(), null).get();
            }
            final ArangoCursorAsync<String> cursor = db.query("for i in db_test return i._id", null,
                    new AqlQueryOptions().batchSize(5).count(true), String.class).get();
            assertThat(cursor, is(notNullValue()));
            final AtomicInteger i = new AtomicInteger(0);
            cursor.streamRemaining().forEach(e -> i.incrementAndGet());
            assertThat(i.get(), is(10));
        } finally {
            db.collection(COLLECTION_NAME).drop().get();
        }
    }

    /**
     * ignored. takes to long
     */
    @Test
    @Ignore
    public void queryWithTTL() throws InterruptedException, ExecutionException {
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
            assertThat(cursor, is(notNullValue()));
            for (int i = 0; i < 10; i++, cursor.next()) {
                assertThat(cursor.hasNext(), is(true));
                if (i == 1) {
                    Thread.sleep(wait * 1000);
                }
            }
            fail();
        } catch (final ArangoDBException ex) {
            assertThat(ex.getResponseCode(), is(404));
            assertThat(ex.getErrorNum(), is(1600));
        } finally {
            db.collection(COLLECTION_NAME).drop().get();
        }
    }

    @Test
    public void changeQueryCache() throws InterruptedException, ExecutionException {
        try {
            QueryCachePropertiesEntity properties = db.getQueryCacheProperties().get();
            assertThat(properties, is(notNullValue()));
            assertThat(properties.getMode(), is(CacheMode.off));
            assertThat(properties.getMaxResults(), greaterThan(0L));

            properties.setMode(CacheMode.on);
            properties = db.setQueryCacheProperties(properties).get();
            assertThat(properties, is(notNullValue()));
            assertThat(properties.getMode(), is(CacheMode.on));

            properties = db.getQueryCacheProperties().get();
            assertThat(properties.getMode(), is(CacheMode.on));
        } finally {
            final QueryCachePropertiesEntity properties = new QueryCachePropertiesEntity();
            properties.setMode(CacheMode.off);
            db.setQueryCacheProperties(properties).get();
        }
    }

    @Test
    public void queryWithCache() throws InterruptedException, ExecutionException {
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

            assertThat(cursor, is(notNullValue()));
            assertThat(cursor.isCached(), is(false));

            final ArangoCursorAsync<String> cachedCursor = db
                    .query("FOR t IN db_test FILTER t.age >= 10 SORT t.age RETURN t._id", null,
                            new AqlQueryOptions().cache(true), String.class)
                    .get();

            assertThat(cachedCursor, is(notNullValue()));
            assertThat(cachedCursor.isCached(), is(true));

        } finally {
            db.collection(COLLECTION_NAME).drop().get();
            final QueryCachePropertiesEntity properties = new QueryCachePropertiesEntity();
            properties.setMode(CacheMode.off);
            db.setQueryCacheProperties(properties).get();
        }
    }

    @Test
    public void queryCursor() throws InterruptedException, ExecutionException {
        try {
            db.createCollection(COLLECTION_NAME, null).get();
            final int numbDocs = 10;
            for (int i = 0; i < numbDocs; i++) {
                db.collection(COLLECTION_NAME).insertDocument(new BaseDocument(), null).get();
            }

            final int batchSize = 5;
            final ArangoCursorAsync<String> cursor = db.query("for i in db_test return i._id", null,
                    new AqlQueryOptions().batchSize(batchSize).count(true), String.class).get();
            assertThat(cursor, is(notNullValue()));
            assertThat(cursor.getCount(), is(numbDocs));

            final ArangoCursorAsync<String> cursor2 = db.cursor(cursor.getId(), String.class).get();
            assertThat(cursor2, is(notNullValue()));
            assertThat(cursor2.getCount(), is(numbDocs));
            assertThat(cursor2.hasNext(), is(true));

            for (int i = 0; i < batchSize; i++, cursor.next()) {
                assertThat(cursor.hasNext(), is(true));
            }
        } finally {
            db.collection(COLLECTION_NAME).drop().get();
        }
    }

    @Test
    public void changeQueryTrackingProperties() throws InterruptedException, ExecutionException {
        try {
            QueryTrackingPropertiesEntity properties = db.getQueryTrackingProperties().get();
            assertThat(properties, is(notNullValue()));
            assertThat(properties.getEnabled(), is(true));
            assertThat(properties.getTrackSlowQueries(), is(true));
            assertThat(properties.getMaxQueryStringLength(), greaterThan(0L));
            assertThat(properties.getMaxSlowQueries(), greaterThan(0L));
            assertThat(properties.getSlowQueryThreshold(), greaterThan(0L));
            properties.setEnabled(false);
            properties = db.setQueryTrackingProperties(properties).get();
            assertThat(properties, is(notNullValue()));
            assertThat(properties.getEnabled(), is(false));
            properties = db.getQueryTrackingProperties().get();
            assertThat(properties.getEnabled(), is(false));
        } finally {
            final QueryTrackingPropertiesEntity properties = new QueryTrackingPropertiesEntity();
            properties.setEnabled(true);
            db.setQueryTrackingProperties(properties).get();
        }
    }

    @Test
    public void queryWithBindVars() throws InterruptedException, ExecutionException {
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
                        assertThat(cursor, is(notNullValue()));
                        for (int i = 0; i < 5; i++, cursor.next()) {
                            assertThat(cursor.hasNext(), is(true));
                        }
                    })
                    .get();
        } finally {
            db.collection(COLLECTION_NAME).drop().get();
        }
    }

    @Test
    public void queryWithWarning() throws InterruptedException, ExecutionException {
        arangoDB.db().query("return 1/0", null, null, String.class)
                .whenComplete((cursor, ex) -> {
                    assertThat(cursor, is(notNullValue()));
                    assertThat(cursor.getWarnings(), is(notNullValue()));
                    assertThat(cursor.getWarnings(), is(not(empty())));
                })
                .get();
    }

    @Test
    public void queryClose() throws IOException, InterruptedException, ExecutionException {
        final ArangoCursorAsync<String> cursor = arangoDB.db()
                .query("for i in 1..2 return i", null, new AqlQueryOptions().batchSize(1), String.class).get();
        cursor.close();
        int count = 0;
        try {
            for (; cursor.hasNext(); cursor.next(), count++) {
            }
            fail();
        } catch (final ArangoDBException e) {
            assertThat(count, is(1));
        }

    }

    @Test
    public void explainQuery() throws InterruptedException, ExecutionException {
        arangoDB.db().explainQuery("for i in 1..1 return i", null, null)
                .whenComplete((explain, ex) -> {
                    assertThat(explain, is(notNullValue()));
                    assertThat(explain.getPlan(), is(notNullValue()));
                    assertThat(explain.getPlans(), is(nullValue()));
                    final ExecutionPlan plan = explain.getPlan();
                    assertThat(plan.getCollections().size(), is(0));
                    assertThat(plan.getEstimatedCost(), greaterThan(0));
                    assertThat(plan.getEstimatedNrItems(), greaterThan(0));
                    assertThat(plan.getVariables().size(), is(2));
                    assertThat(plan.getNodes().size(), is(greaterThan(0)));
                })
                .get();
    }

    @Test
    public void parseQuery() throws InterruptedException, ExecutionException {
        assumeTrue(isAtLeastVersion(3, 4));
        arangoDB.db().parseQuery("for i in _apps return i")
                .whenComplete((parse, ex) -> {
                    assertThat(parse, is(notNullValue()));
                    assertThat(parse.getBindVars(), is(empty()));
                    assertThat(parse.getCollections().size(), is(1));
                    assertThat(parse.getCollections().iterator().next(), is("_apps"));
                    assertThat(parse.getAst().size(), is(1));
                    final AstNode root = parse.getAst().iterator().next();
                    assertThat(root.getType(), is("root"));
                    assertThat(root.getName(), is(nullValue()));
                    assertThat(root.getSubNodes(), is(notNullValue()));
                    assertThat(root.getSubNodes().size(), is(2));
                    final Iterator<AstNode> iterator = root.getSubNodes().iterator();
                    final AstNode for_ = iterator.next();
                    assertThat(for_.getType(), is("for"));
                    assertThat(for_.getSubNodes(), is(notNullValue()));
                    assertThat(for_.getSubNodes().size(), is(3));
                    final Iterator<AstNode> iterator2 = for_.getSubNodes().iterator();
                    final AstNode first = iterator2.next();
                    assertThat(first.getType(), is("variable"));
                    assertThat(first.getName(), is("i"));
                    final AstNode second = iterator2.next();
                    assertThat(second.getType(), is("collection"));
                    assertThat(second.getName(), is("_apps"));
                    final AstNode return_ = iterator.next();
                    assertThat(return_.getType(), is("return"));
                    assertThat(return_.getSubNodes(), is(notNullValue()));
                    assertThat(return_.getSubNodes().size(), is(1));
                    assertThat(return_.getSubNodes().iterator().next().getType(), is("reference"));
                    assertThat(return_.getSubNodes().iterator().next().getName(), is("i"));
                })
                .get();
    }

    @Test
    @Ignore
    public void getAndClearSlowQueries() throws InterruptedException, ExecutionException {
        final QueryTrackingPropertiesEntity properties = db.getQueryTrackingProperties().get();
        final Long slowQueryThreshold = properties.getSlowQueryThreshold();
        try {
            properties.setSlowQueryThreshold(1L);
            db.setQueryTrackingProperties(properties);

            db.query("return sleep(1.1)", null, null, Void.class);
            final Collection<QueryEntity> slowQueries = db.getSlowQueries().get();
            assertThat(slowQueries, is(notNullValue()));
            assertThat(slowQueries.size(), is(1));
            final QueryEntity queryEntity = slowQueries.iterator().next();
            assertThat(queryEntity.getQuery(), is("return sleep(1.1)"));

            db.clearSlowQueries().get();
            assertThat(db.getSlowQueries().get().size(), is(0));
        } finally {
            properties.setSlowQueryThreshold(slowQueryThreshold);
            db.setQueryTrackingProperties(properties);
        }
    }

    @Test
    public void createGetDeleteAqlFunction() throws InterruptedException, ExecutionException {
        final Collection<AqlFunctionEntity> aqlFunctionsInitial = db.getAqlFunctions(null).get();
        assertThat(aqlFunctionsInitial, is(empty()));
        try {
            db.createAqlFunction("myfunctions::temperature::celsiustofahrenheit",
                    "function (celsius) { return celsius * 1.8 + 32; }", null).get();

            final Collection<AqlFunctionEntity> aqlFunctions = db.getAqlFunctions(null).get();
            assertThat(aqlFunctions.size(), is(greaterThan(aqlFunctionsInitial.size())));
        } finally {
            final Integer deleteCount = db.deleteAqlFunction("myfunctions::temperature::celsiustofahrenheit", null)
                    .get();
            // compatibility with ArangoDB < 3.4
            if (isAtLeastVersion(3, 4)) {
                assertThat(deleteCount, is(1));
            } else {
                assertThat(deleteCount, is(nullValue()));
            }

            final Collection<AqlFunctionEntity> aqlFunctions = db.getAqlFunctions(null).get();
            assertThat(aqlFunctions.size(), is(aqlFunctionsInitial.size()));
        }
    }

    @Test
    public void createGetDeleteAqlFunctionWithNamespace() throws InterruptedException, ExecutionException {
        final Collection<AqlFunctionEntity> aqlFunctionsInitial = db.getAqlFunctions(null).get();
        assertThat(aqlFunctionsInitial, is(empty()));
        try {
            db.createAqlFunction("myfunctions::temperature::celsiustofahrenheit1",
                    "function (celsius) { return celsius * 1.8 + 32; }", null).get();
            db.createAqlFunction("myfunctions::temperature::celsiustofahrenheit2",
                    "function (celsius) { return celsius * 1.8 + 32; }", null).get();

        } finally {
            db.deleteAqlFunction("myfunctions::temperature", new AqlFunctionDeleteOptions().group(true)).get();

            final Collection<AqlFunctionEntity> aqlFunctions = db.getAqlFunctions(null).get();
            assertThat(aqlFunctions.size(), is(aqlFunctionsInitial.size()));
        }
    }

    @Test
    public void createGraph() throws InterruptedException, ExecutionException {
        try {
            db.createGraph(GRAPH_NAME, null, null)
                    .whenComplete((result, ex) -> {
                        assertThat(result, is(notNullValue()));
                        assertThat(result.getName(), is(GRAPH_NAME));
                    })
                    .get();
        } finally {
            db.graph(GRAPH_NAME).drop().get();
        }
    }

    @Test
    public void getGraphs() throws InterruptedException, ExecutionException {
        try {
            db.createGraph(GRAPH_NAME, null, null).get();
            db.getGraphs()
                    .whenComplete((graphs, ex) -> {
                        assertThat(graphs, is(notNullValue()));
                        assertThat(graphs.size(), is(1));
                    })
                    .get();
        } finally {
            db.graph(GRAPH_NAME).drop().get();
        }
    }

    @Test
    public void transactionString() throws InterruptedException, ExecutionException {
        final TransactionOptions options = new TransactionOptions().params("test");
        db.transaction("function (params) {return params;}", String.class, options)
                .whenComplete((result, ex) -> assertThat(result, is("test")))
                .get();
    }

    @Test
    public void transactionNumber() throws InterruptedException, ExecutionException {
        final TransactionOptions options = new TransactionOptions().params(5);
        db.transaction("function (params) {return params;}", Integer.class, options)
                .whenComplete((result, ex) -> assertThat(result, is(5)))
                .get();
    }

    @Test
    public void transactionVPack() throws VPackException, InterruptedException, ExecutionException {
        final TransactionOptions options = new TransactionOptions().params(new VPackBuilder().add("test").slice());
        db.transaction("function (params) {return params;}", VPackSlice.class, options)
                .whenComplete((result, ex) -> {
                    assertThat(result.isString(), is(true));
                    assertThat(result.getAsString(), is("test"));
                })
                .get();
    }

    @Test
    public void transactionEmpty() throws InterruptedException, ExecutionException {
        db.transaction("function () {}", null, null).get();
    }

    @Test
    public void transactionallowImplicit() throws InterruptedException, ExecutionException {
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
                assertThat(e.getCause(), instanceOf(ArangoDBException.class));
            }
        } finally {
            db.collection("someCollection").drop().get();
            db.collection("someOtherCollection").drop().get();
        }
    }

    @Test
    public void transactionPojoReturn() throws ExecutionException, InterruptedException {
        final String action = "function() { return {'value':'hello world'}; }";
        db.transaction(action, TransactionTestEntity.class, new TransactionOptions())
                .whenComplete((res, ex) -> {
                    assertThat(res, is(notNullValue()));
                    assertThat(res.value, is("hello world"));
                })
                .get();
    }

    @Test
    public void getInfo() throws InterruptedException, ExecutionException {
        final CompletableFuture<DatabaseEntity> f = db.getInfo();
        assertThat(f, is(notNullValue()));
        f.whenComplete((info, ex) -> {
            assertThat(info, is(notNullValue()));
            assertThat(info.getId(), is(notNullValue()));
            assertThat(info.getName(), is(TEST_DB));
            assertThat(info.getPath(), is(notNullValue()));
            assertThat(info.getIsSystem(), is(false));

            try {
                if (isAtLeastVersion(3, 6) && isCluster()) {
                    assertThat(info.getSharding(), notNullValue());
                    assertThat(info.getWriteConcern(), notNullValue());
                    assertThat(info.getReplicationFactor(), notNullValue());
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
                fail();
            }

        });
        f.get();
    }

    @Test
    public void executeTraversal() throws InterruptedException, ExecutionException {
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
                        assertThat(traversal, is(notNullValue()));

                        final Collection<BaseDocument> vertices = traversal.getVertices();
                        assertThat(vertices, is(notNullValue()));
                        assertThat(vertices.size(), is(4));

                        final Iterator<BaseDocument> verticesIterator = vertices.iterator();
                        final Collection<String> v = Arrays.asList("Alice", "Bob", "Charlie", "Dave");
                        for (; verticesIterator.hasNext(); ) {
                            assertThat(v.contains(verticesIterator.next().getKey()), is(true));
                        }

                        final Collection<PathEntity<BaseDocument, BaseEdgeDocument>> paths = traversal.getPaths();
                        assertThat(paths, is(notNullValue()));
                        assertThat(paths.size(), is(4));

                        assertThat(paths.iterator().hasNext(), is(true));
                        final PathEntity<BaseDocument, BaseEdgeDocument> first = paths.iterator().next();
                        assertThat(first, is(notNullValue()));
                        assertThat(first.getEdges().size(), is(0));
                        assertThat(first.getVertices().size(), is(1));
                        assertThat(first.getVertices().iterator().next().getKey(), is("Alice"));
                    })
                    .get();
        } finally {
            db.collection("person").drop().get();
            db.collection("knows").drop().get();
        }
    }

    @Test
    public void getDocument() throws InterruptedException, ExecutionException {
        String collectionName = COLLECTION_NAME + "getDocument";
        db.createCollection(collectionName).get();
        final BaseDocument value = new BaseDocument();
        value.setKey("123");
        db.collection(collectionName).insertDocument(value).get();
        db.getDocument(collectionName + "/123", BaseDocument.class)
                .whenComplete((document, ex) -> {
                    assertThat(document, is(notNullValue()));
                    assertThat(document.getKey(), is("123"));
                })
                .get();
        db.collection(collectionName).drop().get();
    }

    @Test
    public void shouldIncludeExceptionMessage() throws InterruptedException, ExecutionException {
        final String exceptionMessage = "My error context";
        final String action = "function (params) {" + "throw '" + exceptionMessage + "';" + "}";
        try {
            db.transaction(action, VPackSlice.class, null).get();
            fail();
        } catch (final ExecutionException e) {
            assertThat(e.getCause(), instanceOf(ArangoDBException.class));
            ArangoDBException cause = ((ArangoDBException) e.getCause());
            assertThat(cause.getErrorNum(), is(1650));
            if (isAtLeastVersion(3, 4)) {
                assertThat(cause.getErrorMessage(), is(exceptionMessage));
            }
        }
    }

    @Test(expected = ArangoDBException.class)
    public void getDocumentWrongId() throws InterruptedException, ExecutionException {
        db.getDocument("123", BaseDocument.class).get();
    }

    @Test
    public void reloadRouting() throws InterruptedException, ExecutionException {
        db.reloadRouting().get();
    }

    @SuppressWarnings({"WeakerAccess", "unused"})
    protected static class TransactionTestEntity {
        private String value;

        public TransactionTestEntity() {
            super();
        }
    }
}
