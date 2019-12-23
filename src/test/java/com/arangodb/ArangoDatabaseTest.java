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

package com.arangodb;

import com.arangodb.ArangoDB.Builder;
import com.arangodb.entity.*;
import com.arangodb.entity.AqlExecutionExplainEntity.ExecutionPlan;
import com.arangodb.entity.CursorEntity.Warning;
import com.arangodb.entity.QueryCachePropertiesEntity.CacheMode;
import com.arangodb.model.*;
import com.arangodb.model.TraversalOptions.Direction;
import com.arangodb.util.MapBuilder;
import com.arangodb.velocypack.VPackBuilder;
import com.arangodb.velocypack.VPackSlice;
import com.arangodb.velocypack.ValueType;
import com.arangodb.velocypack.exception.VPackException;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.util.*;
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
@RunWith(Parameterized.class)
public class ArangoDatabaseTest extends BaseTest {

    private static final String COLLECTION_NAME = "db_test";
    private static final String GRAPH_NAME = "graph_test";

    public ArangoDatabaseTest(final Builder builder) {
        super(builder);
    }

    @Before
    public void setUp() {
        db.getCollections().stream()
                .filter(it -> !it.getIsSystem())
                .map(CollectionEntity::getName)
                .map(db::collection)
                .forEach(ArangoCollection::drop);

        db.getGraphs().stream()
                .map(GraphEntity::getName)
                .map(db::graph)
                .forEach(ArangoGraph::drop);
    }

    @Test
    public void create() {
        final Boolean result = arangoDB.db(BaseTest.TEST_DB + "_1").create();
        assertThat(result, is(true));
        arangoDB.db(BaseTest.TEST_DB + "_1").drop();
    }

    @Test
    public void getVersion() {
        final ArangoDBVersion version = db.getVersion();
        assertThat(version, is(notNullValue()));
        assertThat(version.getServer(), is(notNullValue()));
        assertThat(version.getVersion(), is(notNullValue()));
    }

    @Test
    public void getEngine() {
        final ArangoDBEngine engine = db.getEngine();
        assertThat(engine, is(notNullValue()));
        assertThat(engine.getName(), is(notNullValue()));
    }

    @Test
    public void exists() {
        assertThat(db.exists(), is(true));
        assertThat(arangoDB.db("no").exists(), is(false));
    }

    @Test
    public void getAccessibleDatabases() {
        final Collection<String> dbs = db.getAccessibleDatabases();
        assertThat(dbs, is(notNullValue()));
        assertThat(dbs.size(), greaterThan(0));
        assertThat(dbs, hasItem("_system"));
    }

    @Test
    public void createCollection() {
        try {
            final CollectionEntity result = db.createCollection(COLLECTION_NAME, null);
            assertThat(result, is(notNullValue()));
            assertThat(result.getId(), is(notNullValue()));
        } finally {
            db.collection(COLLECTION_NAME).drop();
        }
    }

    @Test
    public void createCollectionWithReplicationFactor() {
        assumeTrue(isCluster());
        final CollectionEntity result = db
                .createCollection(COLLECTION_NAME, new CollectionCreateOptions().replicationFactor(2));
        assertThat(result, is(notNullValue()));
        assertThat(result.getId(), is(notNullValue()));
        assertThat(db.collection(COLLECTION_NAME).getProperties().getReplicationFactor(), is(2));
        assertThat(db.collection(COLLECTION_NAME).getProperties().getSatellite(), is(nullValue()));
        db.collection(COLLECTION_NAME).drop();
    }

    @Test
    public void createCollectionWithMinReplicationFactor() {
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isCluster());

        final CollectionEntity result = db.createCollection(COLLECTION_NAME,
                new CollectionCreateOptions().replicationFactor(2).minReplicationFactor(2));
        assertThat(result, is(notNullValue()));
        assertThat(result.getId(), is(notNullValue()));
        assertThat(db.collection(COLLECTION_NAME).getProperties().getReplicationFactor(), is(2));
        assertThat(db.collection(COLLECTION_NAME).getProperties().getMinReplicationFactor(), is(2));
        assertThat(db.collection(COLLECTION_NAME).getProperties().getSatellite(), is(nullValue()));
        db.collection(COLLECTION_NAME).drop();
    }

    @Test
    public void createSatelliteCollection() {
        assumeTrue(isEnterprise());
        assumeTrue(isCluster());

        try {
            final CollectionEntity result = db
                    .createCollection(COLLECTION_NAME, new CollectionCreateOptions().satellite(true));

            assertThat(result, is(notNullValue()));
            assertThat(result.getId(), is(notNullValue()));
            assertThat(db.collection(COLLECTION_NAME).getProperties().getReplicationFactor(), is(nullValue()));
            assertThat(db.collection(COLLECTION_NAME).getProperties().getSatellite(), is(true));
        } finally {
            db.collection(COLLECTION_NAME).drop();
        }
    }

    @Test
    public void createCollectionWithNumberOfShards() {
        assumeTrue(isCluster());
        try {
            final CollectionEntity result = db
                    .createCollection(COLLECTION_NAME, new CollectionCreateOptions().numberOfShards(2));

            assertThat(result, is(notNullValue()));
            assertThat(result.getId(), is(notNullValue()));
            assertThat(db.collection(COLLECTION_NAME).getProperties().getNumberOfShards(), is(2));
        } finally {
            db.collection(COLLECTION_NAME).drop();
        }
    }

    @Test
    public void createCollectionWithShardingStrategys() {
        assumeTrue(isAtLeastVersion(3, 4));
        assumeTrue(isCluster());

        final CollectionEntity result = db.createCollection(COLLECTION_NAME, new CollectionCreateOptions()
                .shardingStrategy(ShardingStrategy.COMMUNITY_COMPAT.getInternalName()));

        assertThat(result, is(notNullValue()));
        assertThat(result.getId(), is(notNullValue()));
        assertThat(db.collection(COLLECTION_NAME).getProperties().getShardingStrategy(),
                is(ShardingStrategy.COMMUNITY_COMPAT.getInternalName()));
        db.collection(COLLECTION_NAME).drop();
    }

    @Test
    public void createCollectionWithSmartJoinAttribute() {
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isEnterprise());
        assumeTrue(isCluster());

        final CollectionEntity result = db.createCollection(COLLECTION_NAME,
                new CollectionCreateOptions().smartJoinAttribute("test123").shardKeys("_key:"));
        assertThat(result, is(notNullValue()));
        assertThat(result.getId(), is(notNullValue()));
        assertThat(db.collection(COLLECTION_NAME).getProperties().getSmartJoinAttribute(), is("test123"));
        db.collection(COLLECTION_NAME).drop();
    }

    @Test
    public void createCollectionWithSmartJoinAttributeWrong() {
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isEnterprise());
        assumeTrue(isCluster());

        try {
            db.createCollection(COLLECTION_NAME, new CollectionCreateOptions().smartJoinAttribute("test123"));
        } catch (ArangoDBException e) {
            assertThat(e.getErrorNum(), is(4006));
            // TODO:
            //  	at the moment older server versions reply with response code 500, which is a misbehavior
            //		when the fix has been backported to all the supported db versions uncomment the following:
            //		assertThat(e.getResponseCode(), is(400));
        }
    }

    @Test
    public void createCollectionWithNumberOfShardsAndShardKey() {
        assumeTrue(isCluster());

        try {
            final CollectionEntity result = db
                    .createCollection(COLLECTION_NAME, new CollectionCreateOptions().numberOfShards(2).shardKeys("a"));
            assertThat(result, is(notNullValue()));
            assertThat(result.getId(), is(notNullValue()));
            final CollectionPropertiesEntity properties = db.collection(COLLECTION_NAME).getProperties();
            assertThat(properties.getNumberOfShards(), is(2));
            assertThat(properties.getShardKeys().size(), is(1));
        } finally {
            db.collection(COLLECTION_NAME).drop();
        }
    }

    @Test
    public void createCollectionWithNumberOfShardsAndShardKeys() {
        assumeTrue(isCluster());
        try {
            final CollectionEntity result = db.createCollection(COLLECTION_NAME,
                    new CollectionCreateOptions().numberOfShards(2).shardKeys("a", "b"));
            assertThat(result, is(notNullValue()));
            assertThat(result.getId(), is(notNullValue()));
            final CollectionPropertiesEntity properties = db.collection(COLLECTION_NAME).getProperties();
            assertThat(properties.getNumberOfShards(), is(2));
            assertThat(properties.getShardKeys().size(), is(2));
        } finally {
            db.collection(COLLECTION_NAME).drop();
        }
    }

    @Test
    public void createCollectionWithDistributeShardsLike() {
        assumeTrue(isEnterprise());
        assumeTrue(isCluster());

        final Integer numberOfShards = 3;

        db.createCollection(COLLECTION_NAME, new CollectionCreateOptions().numberOfShards(numberOfShards));
        db.createCollection(COLLECTION_NAME + "2",
                new CollectionCreateOptions().distributeShardsLike(COLLECTION_NAME));

        assertThat(db.collection(COLLECTION_NAME).getProperties().getNumberOfShards(), is(numberOfShards));
        assertThat(db.collection(COLLECTION_NAME + "2").getProperties().getNumberOfShards(), is(numberOfShards));

        db.collection(COLLECTION_NAME + "2").drop();
    }

    private void createCollectionWithKeyType(KeyType keyType) {
        try {
            final CollectionEntity result = db.createCollection(COLLECTION_NAME, new CollectionCreateOptions().keyOptions(
                    false,
                    keyType,
                    null,
                    null
            ));
            assertThat(db.collection(COLLECTION_NAME).getProperties().getKeyOptions().getType(), is(keyType));
        } finally {
            db.collection(COLLECTION_NAME).drop();
        }
    }

    @Test
    public void createCollectionWithKeyTypeAutoincrement() {
        assumeTrue(isSingleServer());
        createCollectionWithKeyType(KeyType.autoincrement);
    }

    @Test
    public void createCollectionWithKeyTypePadded() {
        assumeTrue(isAtLeastVersion(3, 4));
        createCollectionWithKeyType(KeyType.padded);
    }

    @Test
    public void createCollectionWithKeyTypeTraditional() {
        createCollectionWithKeyType(KeyType.traditional);
    }

    @Test
    public void createCollectionWithKeyTypeUuid() {
        assumeTrue(isAtLeastVersion(3, 4));
        createCollectionWithKeyType(KeyType.uuid);
    }

    @Test(expected = ArangoDBException.class)
    public void deleteCollection() {
        db.createCollection(COLLECTION_NAME, null);
        db.collection(COLLECTION_NAME).drop();
        db.collection(COLLECTION_NAME).getInfo();
    }

    @Test
    public void deleteSystemCollection() {
        final String name = "_system_test";
        db.createCollection(name, new CollectionCreateOptions().isSystem(true));
        db.collection(name).drop(true);
        try {
            db.collection(name).getInfo();
            fail();
        } catch (final ArangoDBException e) {
            assertThat(e.getResponseCode(), is(404));
        }
    }

    @Test
    public void deleteSystemCollectionFail() {
        final String name = "_system_test";
        ArangoCollection collection = db.collection(name);
        if (collection.exists())
            collection.drop(true);

        db.createCollection(name, new CollectionCreateOptions().isSystem(true));
        try {
            collection.drop();
            fail();
        } catch (final ArangoDBException e) {
            assertThat(e.getResponseCode(), is(403));
        }
        collection.drop(true);
        try {
            collection.getInfo();
            fail();
        } catch (final ArangoDBException e) {
            assertThat(e.getResponseCode(), is(404));
        }
    }

    @Test
    public void getIndex() {
        try {
            db.createCollection(COLLECTION_NAME, null);
            final Collection<String> fields = new ArrayList<>();
            fields.add("a");
            final IndexEntity createResult = db.collection(COLLECTION_NAME).ensureHashIndex(fields, null);
            final IndexEntity readResult = db.getIndex(createResult.getId());
            assertThat(readResult.getId(), is(createResult.getId()));
            assertThat(readResult.getType(), is(createResult.getType()));
        } finally {
            db.collection(COLLECTION_NAME).drop();
        }
    }

    @Test
    public void deleteIndex() {
        db.createCollection(COLLECTION_NAME, null);
        final Collection<String> fields = new ArrayList<>();
        fields.add("a");
        final IndexEntity createResult = db.collection(COLLECTION_NAME).ensureHashIndex(fields, null);
        final String id = db.deleteIndex(createResult.getId());
        assertThat(id, is(createResult.getId()));
        try {
            db.getIndex(id);
            fail();
        } catch (final ArangoDBException e) {
            assertThat(e.getResponseCode(), is(404));
        }
        db.collection(COLLECTION_NAME).drop();
    }

    @Test
    public void getCollections() {
        try {
            final Collection<CollectionEntity> systemCollections = db.getCollections(null);
            db.createCollection(COLLECTION_NAME + "1", null);
            db.createCollection(COLLECTION_NAME + "2", null);
            final Collection<CollectionEntity> collections = db.getCollections(null);
            assertThat(collections.size(), is(2 + systemCollections.size()));
            assertThat(collections, is(notNullValue()));
        } finally {
            db.collection(COLLECTION_NAME + "1").drop();
            db.collection(COLLECTION_NAME + "2").drop();
        }
    }

    @Test
    public void getCollectionsExcludeSystem() {
        final CollectionsReadOptions options = new CollectionsReadOptions().excludeSystem(true);
        final Collection<CollectionEntity> nonSystemCollections = db.getCollections(options);

        int initialSize = nonSystemCollections.size();
        db.createCollection(COLLECTION_NAME + "1", null);
        db.createCollection(COLLECTION_NAME + "2", null);
        final Collection<CollectionEntity> newCollections = db.getCollections(options);
        assertThat(newCollections.size(), is(initialSize + 2));
        assertThat(newCollections, is(notNullValue()));

        db.collection(COLLECTION_NAME + "1").drop();
        db.collection(COLLECTION_NAME + "2").drop();
    }

    @Test
    public void grantAccess() {
        try {
            arangoDB.createUser("user1", "1234", null);
            db.grantAccess("user1");
        } finally {
            arangoDB.deleteUser("user1");
        }
    }

    @Test
    public void grantAccessRW() {
        try {
            arangoDB.createUser("user1", "1234", null);
            db.grantAccess("user1", Permissions.RW);
        } finally {
            arangoDB.deleteUser("user1");
        }
    }

    @Test
    public void grantAccessRO() {
        try {
            arangoDB.createUser("user1", "1234", null);
            db.grantAccess("user1", Permissions.RO);
        } finally {
            arangoDB.deleteUser("user1");
        }
    }

    @Test
    public void grantAccessNONE() {
        try {
            arangoDB.createUser("user1", "1234", null);
            db.grantAccess("user1", Permissions.NONE);
        } finally {
            arangoDB.deleteUser("user1");
        }
    }

    @Test(expected = ArangoDBException.class)
    public void grantAccessUserNotFound() {
        db.grantAccess("user1", Permissions.RW);
    }

    @Test
    public void revokeAccess() {
        try {
            arangoDB.createUser("user1", "1234", null);
            db.revokeAccess("user1");
        } finally {
            arangoDB.deleteUser("user1");
        }
    }

    @Test(expected = ArangoDBException.class)
    public void revokeAccessUserNotFound() {
        db.revokeAccess("user1");
    }

    @Test
    public void resetAccess() {
        try {
            arangoDB.createUser("user1", "1234", null);
            db.resetAccess("user1");
        } finally {
            arangoDB.deleteUser("user1");
        }
    }

    @Test(expected = ArangoDBException.class)
    public void resetAccessUserNotFound() {
        db.resetAccess("user1");
    }

    @Test
    public void grantDefaultCollectionAccess() {
        try {
            arangoDB.createUser("user1", "1234");
            db.grantDefaultCollectionAccess("user1", Permissions.RW);
        } finally {
            arangoDB.deleteUser("user1");
        }
    }

    @Test
    public void getPermissions() {
        assertThat(Permissions.RW, is(db.getPermissions("root")));
    }

    @Test
    public void query() {
        try {
            db.createCollection(COLLECTION_NAME, null);
            for (int i = 0; i < 10; i++) {
                db.collection(COLLECTION_NAME).insertDocument(new BaseDocument(), null);
            }
            final ArangoCursor<String> cursor = db.query("for i in db_test return i._id", null, null, String.class);
            assertThat(cursor, is(notNullValue()));
            for (int i = 0; i < 10; i++, cursor.next()) {
                assertThat(cursor.hasNext(), is(true));
            }
        } finally {
            db.collection(COLLECTION_NAME).drop();
        }
    }

    @Test
    public void queryForEach() {
        try {
            db.createCollection(COLLECTION_NAME, null);
            for (int i = 0; i < 10; i++) {
                db.collection(COLLECTION_NAME).insertDocument(new BaseDocument(), null);
            }
            final ArangoCursor<String> cursor = db.query("for i in db_test return i._id", null, null, String.class);
            assertThat(cursor, is(notNullValue()));
            final AtomicInteger i = new AtomicInteger(0);
            for (; cursor.hasNext(); cursor.next()) {
                i.incrementAndGet();
            }
            assertThat(i.get(), is(10));
        } finally {
            db.collection(COLLECTION_NAME).drop();
        }
    }

    @Test
    public void queryIterate() {
        try {
            db.createCollection(COLLECTION_NAME, null);
            for (int i = 0; i < 10; i++) {
                db.collection(COLLECTION_NAME).insertDocument(new BaseDocument(), null);
            }
            final ArangoCursor<String> cursor = db.query("for i in db_test return i._id", null, null, String.class);
            assertThat(cursor, is(notNullValue()));
            final AtomicInteger i = new AtomicInteger(0);
            for (; cursor.hasNext(); cursor.next()) {
                i.incrementAndGet();
            }
            assertThat(i.get(), is(10));
        } finally {
            db.collection(COLLECTION_NAME).drop();
        }
    }

    @Test
    public void queryWithCount() {
        try {
            db.createCollection(COLLECTION_NAME, null);
            for (int i = 0; i < 10; i++) {
                db.collection(COLLECTION_NAME).insertDocument(new BaseDocument(), null);
            }

            final ArangoCursor<String> cursor = db
                    .query("for i in db_test Limit 6 return i._id", null, new AqlQueryOptions().count(true),
                            String.class);
            assertThat(cursor, is(notNullValue()));
            for (int i = 0; i < 6; i++, cursor.next()) {
                assertThat(cursor.hasNext(), is(true));
            }
            assertThat(cursor.getCount(), is(6));

        } finally {
            db.collection(COLLECTION_NAME).drop();
        }
    }

    @Test
    public void queryWithLimitAndFullCount() {
        try {
            db.createCollection(COLLECTION_NAME, null);
            for (int i = 0; i < 10; i++) {
                db.collection(COLLECTION_NAME).insertDocument(new BaseDocument(), null);
            }

            final ArangoCursor<String> cursor = db
                    .query("for i in db_test Limit 5 return i._id", null, new AqlQueryOptions().fullCount(true),
                            String.class);
            assertThat(cursor, is(notNullValue()));
            for (int i = 0; i < 5; i++, cursor.next()) {
                assertThat(cursor.hasNext(), is(true));
            }
            assertThat(cursor.getStats(), is(notNullValue()));
            assertThat(cursor.getStats().getFullCount(), is(10L));

        } finally {
            db.collection(COLLECTION_NAME).drop();
        }
    }

    @Test
    public void queryWithBatchSize() {
        try {
            db.createCollection(COLLECTION_NAME, null);
            for (int i = 0; i < 10; i++) {
                db.collection(COLLECTION_NAME).insertDocument(new BaseDocument(), null);
            }

            final ArangoCursor<String> cursor = db
                    .query("for i in db_test return i._id", null, new AqlQueryOptions().batchSize(5).count(true),
                            String.class);

            assertThat(cursor, is(notNullValue()));
            for (int i = 0; i < 10; i++, cursor.next()) {
                assertThat(cursor.hasNext(), is(true));
            }
        } catch (final ArangoDBException e) {
            System.out.println(e.getErrorMessage());
            System.out.println(e.getErrorNum());
        } finally {
            db.collection(COLLECTION_NAME).drop();
        }
    }

    @Test
    public void queryIterateWithBatchSize() {
        try {
            db.createCollection(COLLECTION_NAME, null);
            for (int i = 0; i < 10; i++) {
                db.collection(COLLECTION_NAME).insertDocument(new BaseDocument(), null);
            }

            final ArangoCursor<String> cursor = db
                    .query("for i in db_test return i._id", null, new AqlQueryOptions().batchSize(5).count(true),
                            String.class);

            assertThat(cursor, is(notNullValue()));
            final AtomicInteger i = new AtomicInteger(0);
            for (; cursor.hasNext(); cursor.next()) {
                i.incrementAndGet();
            }
            assertThat(i.get(), is(10));
        } finally {
            db.collection(COLLECTION_NAME).drop();
        }
    }

    /**
     * ignored. takes to long
     */
    @Test
    @Ignore
    public void queryWithTTL() throws InterruptedException {
        // set TTL to 1 seconds and get the second batch after 2 seconds!
        final int ttl = 1;
        final int wait = 2;
        try {
            db.createCollection(COLLECTION_NAME, null);
            for (int i = 0; i < 10; i++) {
                db.collection(COLLECTION_NAME).insertDocument(new BaseDocument(), null);
            }

            final ArangoCursor<String> cursor = db
                    .query("for i in db_test return i._id", null, new AqlQueryOptions().batchSize(5).ttl(ttl),
                            String.class);

            assertThat(cursor, is(notNullValue()));

            for (int i = 0; i < 10; i++, cursor.next()) {
                assertThat(cursor.hasNext(), is(true));
                if (i == 1) {
                    Thread.sleep(wait * 1000);
                }
            }
            fail("this should fail");
        } catch (final ArangoDBException ex) {
            assertThat(ex.getMessage(), is("Response: 404, Error: 1600 - cursor not found"));
        } finally {
            db.collection(COLLECTION_NAME).drop();
        }
    }

    @Test
    public void changeQueryCache() {
        try {
            QueryCachePropertiesEntity properties = db.getQueryCacheProperties();
            assertThat(properties, is(notNullValue()));
            assertThat(properties.getMode(), is(CacheMode.off));
            assertThat(properties.getMaxResults(), greaterThan(0L));

            properties.setMode(CacheMode.on);
            properties = db.setQueryCacheProperties(properties);
            assertThat(properties, is(notNullValue()));
            assertThat(properties.getMode(), is(CacheMode.on));

            properties = db.getQueryCacheProperties();
            assertThat(properties.getMode(), is(CacheMode.on));
        } finally {
            final QueryCachePropertiesEntity properties = new QueryCachePropertiesEntity();
            properties.setMode(CacheMode.off);
            db.setQueryCacheProperties(properties);
        }
    }

    @Test
    public void queryWithCache() {
        assumeTrue(isSingleServer());
        try {
            db.createCollection(COLLECTION_NAME, null);
            for (int i = 0; i < 10; i++) {
                db.collection(COLLECTION_NAME).insertDocument(new BaseDocument(), null);
            }

            final QueryCachePropertiesEntity properties = new QueryCachePropertiesEntity();
            properties.setMode(CacheMode.on);
            db.setQueryCacheProperties(properties);

            final ArangoCursor<String> cursor = db
                    .query("FOR t IN db_test FILTER t.age >= 10 SORT t.age RETURN t._id", null,
                            new AqlQueryOptions().cache(true), String.class);

            assertThat(cursor, is(notNullValue()));
            assertThat(cursor.isCached(), is(false));

            final ArangoCursor<String> cachedCursor = db
                    .query("FOR t IN db_test FILTER t.age >= 10 SORT t.age RETURN t._id", null,
                            new AqlQueryOptions().cache(true), String.class);

            assertThat(cachedCursor, is(notNullValue()));
            assertThat(cachedCursor.isCached(), is(true));

        } finally {
            db.collection(COLLECTION_NAME).drop();
            final QueryCachePropertiesEntity properties = new QueryCachePropertiesEntity();
            properties.setMode(CacheMode.off);
            db.setQueryCacheProperties(properties);
        }
    }

    @Test
    public void queryWithMemoryLimit() {
        try {
            db.query("RETURN 'bla'", null, new AqlQueryOptions().memoryLimit(1L), String.class);
            fail();
        } catch (final ArangoDBException e) {
            assertThat(e.getErrorNum(), is(32));
        }
    }

    @Test(expected = ArangoDBException.class)
    public void queryWithFailOnWarningTrue() {
        db.query("RETURN 1 / 0", null, new AqlQueryOptions().failOnWarning(true), String.class);
    }

    @Test
    public void queryWithFailOnWarningFalse() {
        final ArangoCursor<String> cursor = db
                .query("RETURN 1 / 0", null, new AqlQueryOptions().failOnWarning(false), String.class);
        assertThat(cursor.next(), is("null"));
    }

    @Test
    public void queryWithTimeout() {
        assumeTrue(isAtLeastVersion(3, 6));
        try {
            db.query("RETURN SLEEP(1)", null, new AqlQueryOptions().maxRuntime(0.1), String.class).next();
            fail();
        } catch (ArangoDBException e) {
            assertThat(e.getResponseCode(), is(410));
        }
    }

    @Test
    public void queryWithMaxWarningCount() {
        final ArangoCursor<String> cursorWithWarnings = db
                .query("RETURN 1 / 0", null, new AqlQueryOptions(), String.class);
        assertThat(cursorWithWarnings.getWarnings().size(), is(1));
        final ArangoCursor<String> cursorWithLimitedWarnings = db
                .query("RETURN 1 / 0", null, new AqlQueryOptions().maxWarningCount(0L), String.class);
        final Collection<Warning> warnings = cursorWithLimitedWarnings.getWarnings();
        if (warnings != null) {
            assertThat(warnings.size(), is(0));
        }
    }

    @Test
    public void queryCursor() {
        try {
            db.createCollection(COLLECTION_NAME, null);
            final int numbDocs = 10;
            for (int i = 0; i < numbDocs; i++) {
                db.collection(COLLECTION_NAME).insertDocument(new BaseDocument(), null);
            }

            final int batchSize = 5;
            final ArangoCursor<String> cursor = db.query("for i in db_test return i._id", null,
                    new AqlQueryOptions().batchSize(batchSize).count(true), String.class);
            assertThat(cursor, is(notNullValue()));
            assertThat(cursor.getCount(), is(numbDocs));

            final ArangoCursor<String> cursor2 = db.cursor(cursor.getId(), String.class);
            assertThat(cursor2, is(notNullValue()));
            assertThat(cursor2.getCount(), is(numbDocs));
            assertThat(cursor2.hasNext(), is(true));

            for (int i = 0; i < batchSize; i++, cursor.next()) {
                assertThat(cursor.hasNext(), is(true));
            }
        } finally {
            db.collection(COLLECTION_NAME).drop();
        }
    }

    @Test
    public void changeQueryTrackingProperties() {
        try {
            QueryTrackingPropertiesEntity properties = db.getQueryTrackingProperties();
            assertThat(properties, is(notNullValue()));
            assertThat(properties.getEnabled(), is(true));
            assertThat(properties.getTrackSlowQueries(), is(true));
            assertThat(properties.getMaxQueryStringLength(), greaterThan(0L));
            assertThat(properties.getMaxSlowQueries(), greaterThan(0L));
            assertThat(properties.getSlowQueryThreshold(), greaterThan(0L));
            properties.setEnabled(false);
            properties = db.setQueryTrackingProperties(properties);
            assertThat(properties, is(notNullValue()));
            assertThat(properties.getEnabled(), is(false));
            properties = db.getQueryTrackingProperties();
            assertThat(properties.getEnabled(), is(false));
        } finally {
            final QueryTrackingPropertiesEntity properties = new QueryTrackingPropertiesEntity();
            properties.setEnabled(true);
            db.setQueryTrackingProperties(properties);
        }
    }

    @Test
    public void queryWithBindVars() {
        try {
            db.createCollection(COLLECTION_NAME, null);
            for (int i = 0; i < 10; i++) {
                final BaseDocument baseDocument = new BaseDocument();
                baseDocument.addAttribute("age", 20 + i);
                db.collection(COLLECTION_NAME).insertDocument(baseDocument, null);
            }
            final Map<String, Object> bindVars = new HashMap<>();
            bindVars.put("@coll", COLLECTION_NAME);
            bindVars.put("age", 25);

            final ArangoCursor<String> cursor = db
                    .query("FOR t IN @@coll FILTER t.age >= @age SORT t.age RETURN t._id", bindVars, null,
                            String.class);

            assertThat(cursor, is(notNullValue()));

            for (int i = 0; i < 5; i++, cursor.next()) {
                assertThat(cursor.hasNext(), is(true));
            }

        } finally {
            db.collection(COLLECTION_NAME).drop();
        }
    }

    @Test
    public void queryWithWarning() {
        final ArangoCursor<String> cursor = arangoDB.db().query("return 1/0", null, null, String.class);

        assertThat(cursor, is(notNullValue()));
        assertThat(cursor.getWarnings(), is(notNullValue()));
    }

    @Test
    public void queryStream() {
        if (isAtLeastVersion(3, 4)) {
            final ArangoCursor<VPackSlice> cursor = db
                    .query("FOR i IN 1..2 RETURN i", null, new AqlQueryOptions().stream(true).count(true),
                            VPackSlice.class);
            assertThat(cursor, is(notNullValue()));
            assertThat(cursor.getCount(), is(nullValue()));
        }
    }

    @Test
    public void queryClose() throws IOException {
        final ArangoCursor<String> cursor = arangoDB.db()
                .query("for i in 1..2 return i", null, new AqlQueryOptions().batchSize(1), String.class);
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
    public void queryNoResults() throws IOException {
        try {
            db.createCollection(COLLECTION_NAME);
            final ArangoCursor<BaseDocument> cursor = db
                    .query("FOR i IN @@col RETURN i", new MapBuilder().put("@col", COLLECTION_NAME).get(), null,
                            BaseDocument.class);
            cursor.close();
        } finally {
            db.collection(COLLECTION_NAME).drop();
        }
    }

    @Test
    public void queryWithNullBindParam() throws IOException {
        try {
            db.createCollection(COLLECTION_NAME);
            final ArangoCursor<BaseDocument> cursor = db.query("FOR i IN @@col FILTER i.test == @test RETURN i",
                    new MapBuilder().put("@col", COLLECTION_NAME).put("test", null).get(), null, BaseDocument.class);
            cursor.close();
        } finally {
            db.collection(COLLECTION_NAME).drop();
        }
    }

    @Test
    public void queryAllowDirtyRead() throws IOException {
        db.createCollection(COLLECTION_NAME);
        final ArangoCursor<BaseDocument> cursor = db.query("FOR i IN @@col FILTER i.test == @test RETURN i",
                new MapBuilder().put("@col", COLLECTION_NAME).put("test", null).get(),
                new AqlQueryOptions().allowDirtyRead(true), BaseDocument.class);
        cursor.close();
        db.collection(COLLECTION_NAME).drop();
    }

    @Test
    public void explainQuery() {
        final AqlExecutionExplainEntity explain = arangoDB.db().explainQuery("for i in 1..1 return i", null, null);
        assertThat(explain, is(notNullValue()));
        assertThat(explain.getPlan(), is(notNullValue()));
        assertThat(explain.getPlans(), is(nullValue()));
        final ExecutionPlan plan = explain.getPlan();
        assertThat(plan.getCollections().size(), is(0));
        assertThat(plan.getEstimatedCost(), greaterThan(0));
        assertThat(plan.getEstimatedNrItems(), greaterThan(0));
        assertThat(plan.getVariables().size(), is(2));
        assertThat(plan.getNodes().size(), is(greaterThan(0)));
    }

    @Test
    public void explainQueryWithIndexNode() {
        ArangoCollection character = db.collection("got_characters");
        ArangoCollection actor = db.collection("got_actors");

        if (!character.exists())
            character.create();

        if (!actor.exists())
            actor.create();

        String query = "" +
                "FOR `character` IN `got_characters` " +
                "   FOR `actor` IN `got_actors` " +
                "       FILTER `character`.`actor` == `actor`.`_id` " +
                "       RETURN `character`";

        final ExecutionPlan plan = db.explainQuery(query, null, null).getPlan();
        plan.getNodes().stream()
                .filter(it -> "IndexNode".equals(it.getType()))
                .flatMap(it -> it.getIndexes().stream())
                .forEach(it -> {
                    assertThat(it.getType(), is(IndexType.primary));
                    assertThat(it.getFields(), contains("_key"));
                });
    }

    @Test
    public void parseQuery() {
        final AqlParseEntity parse = arangoDB.db().parseQuery("for i in 1..1 return i");
        assertThat(parse, is(notNullValue()));
        assertThat(parse.getBindVars(), is(empty()));
        assertThat(parse.getCollections().size(), is(0));
        assertThat(parse.getAst().size(), is(1));
    }

    @Test
    @Ignore
    public void getCurrentlyRunningQueries() throws InterruptedException {
        final Thread t = new Thread() {
            @Override
            public void run() {
                super.run();
                db.query("return sleep(0.2)", null, null, Void.class);
            }
        };
        t.start();
        Thread.sleep(100);
        try {
            final Collection<QueryEntity> currentlyRunningQueries = db.getCurrentlyRunningQueries();
            assertThat(currentlyRunningQueries, is(notNullValue()));
            assertThat(currentlyRunningQueries.size(), is(1));
            final QueryEntity queryEntity = currentlyRunningQueries.iterator().next();
            assertThat(queryEntity.getQuery(), is("return sleep(0.2)"));
            assertThat(queryEntity.getState(), is(QueryExecutionState.EXECUTING));
        } finally {
            t.join();
        }
    }

    @Test
    public void getAndClearSlowQueries() {
        final QueryTrackingPropertiesEntity properties = db.getQueryTrackingProperties();
        final Long slowQueryThreshold = properties.getSlowQueryThreshold();
        properties.setSlowQueryThreshold(1L);
        db.setQueryTrackingProperties(properties);

        db.query("return sleep(1.1)", null, null, Void.class);
        final Collection<QueryEntity> slowQueries = db.getSlowQueries();
        assertThat(slowQueries, is(notNullValue()));
        assertThat(slowQueries.size(), is(1));
        final QueryEntity queryEntity = slowQueries.iterator().next();
        assertThat(queryEntity.getQuery(), is("return sleep(1.1)"));

        db.clearSlowQueries();
        assertThat(db.getSlowQueries().size(), is(0));
        properties.setSlowQueryThreshold(slowQueryThreshold);
        db.setQueryTrackingProperties(properties);
    }

    @Test
    @Ignore
    public void killQuery() throws InterruptedException {
        final Thread t = new Thread() {
            @Override
            public void run() {
                super.run();
                db.query("return sleep(0.2)", null, null, Void.class);
                fail();
            }
        };
        t.start();
        Thread.sleep(100);

        final Collection<QueryEntity> currentlyRunningQueries = db.getCurrentlyRunningQueries();
        assertThat(currentlyRunningQueries.size(), is(1));

        final QueryEntity queryEntity = currentlyRunningQueries.iterator().next();
        db.killQuery(queryEntity.getId());
    }

    @Test
    public void createGetDeleteAqlFunction() {
        final Collection<AqlFunctionEntity> aqlFunctionsInitial = db.getAqlFunctions(null);
        assertThat(aqlFunctionsInitial, is(empty()));
        try {
            db.createAqlFunction("myfunctions::temperature::celsiustofahrenheit",
                    "function (celsius) { return celsius * 1.8 + 32; }", null);

            final Collection<AqlFunctionEntity> aqlFunctions = db.getAqlFunctions(null);
            assertThat(aqlFunctions.size(), is(greaterThan(aqlFunctionsInitial.size())));
        } finally {
            final Integer deleteCount = db.deleteAqlFunction("myfunctions::temperature::celsiustofahrenheit", null);
            // compatibility with ArangoDB < 3.4
            if (isAtLeastVersion(3, 4)) {
                assertThat(deleteCount, is(1));
            } else {
                assertThat(deleteCount, is(nullValue()));
            }
            final Collection<AqlFunctionEntity> aqlFunctions = db.getAqlFunctions(null);
            assertThat(aqlFunctions.size(), is(aqlFunctionsInitial.size()));
        }
    }

    @Test
    public void createGetDeleteAqlFunctionWithNamespace() {
        final Collection<AqlFunctionEntity> aqlFunctionsInitial = db.getAqlFunctions(null);
        assertThat(aqlFunctionsInitial, is(empty()));
        try {
            db.createAqlFunction("myfunctions::temperature::celsiustofahrenheit1",
                    "function (celsius) { return celsius * 1.8 + 32; }", null);
            db.createAqlFunction("myfunctions::temperature::celsiustofahrenheit2",
                    "function (celsius) { return celsius * 1.8 + 32; }", null);

        } finally {
            final Integer deleteCount = db
                    .deleteAqlFunction("myfunctions::temperature", new AqlFunctionDeleteOptions().group(true));
            // compatibility with ArangoDB < 3.4
            if (isAtLeastVersion(3, 4)) {
                assertThat(deleteCount, is(2));
            } else {
                assertThat(deleteCount, is(nullValue()));
            }
            final Collection<AqlFunctionEntity> aqlFunctions = db.getAqlFunctions(null);
            assertThat(aqlFunctions.size(), is(aqlFunctionsInitial.size()));
        }
    }

    @Test
    public void createGraph() {
        try {
            final GraphEntity result = db.createGraph(GRAPH_NAME, null, null);
            assertThat(result, is(notNullValue()));
            assertThat(result.getName(), is(GRAPH_NAME));
        } finally {
            db.graph(GRAPH_NAME).drop();
        }
    }

    @Test
    public void createGraphReplicationFaktor() {
        assumeTrue(isCluster());
        try {
            final String edgeCollection = COLLECTION_NAME + "edge";
            final String fromCollection = COLLECTION_NAME + "from";
            final String toCollection = COLLECTION_NAME + "to";
            final Collection<EdgeDefinition> edgeDefinitions = Collections.singletonList(new EdgeDefinition().collection(edgeCollection).from(fromCollection).to(toCollection));
            final GraphEntity result = db
                    .createGraph(GRAPH_NAME, edgeDefinitions, new GraphCreateOptions().replicationFactor(2));
            assertThat(result, is(notNullValue()));
            for (final String collection : Arrays.asList(edgeCollection, fromCollection, toCollection)) {
                final CollectionPropertiesEntity properties = db.collection(collection).getProperties();
                assertThat(properties.getReplicationFactor(), is(2));
            }
        } finally {
            db.graph(GRAPH_NAME).drop();
        }
    }

    @Test
    public void createGraphNumberOfShards() {
        assumeTrue(isCluster());
        try {
            final String edgeCollection = COLLECTION_NAME + "edge";
            final String fromCollection = COLLECTION_NAME + "from";
            final String toCollection = COLLECTION_NAME + "to";
            final Collection<EdgeDefinition> edgeDefinitions = Collections.singletonList(new EdgeDefinition().collection(edgeCollection).from(fromCollection).to(toCollection));
            final GraphEntity result = db
                    .createGraph(GRAPH_NAME, edgeDefinitions, new GraphCreateOptions().numberOfShards(2));
            assertThat(result, is(notNullValue()));
            for (final String collection : Arrays.asList(edgeCollection, fromCollection, toCollection)) {
                final CollectionPropertiesEntity properties = db.collection(collection).getProperties();
                assertThat(properties.getNumberOfShards(), is(2));
            }
        } finally {
            db.graph(GRAPH_NAME).drop();
        }
    }

    @Test
    public void getGraphs() {
        try {
            db.createGraph(GRAPH_NAME, null, null);
            final Collection<GraphEntity> graphs = db.getGraphs();
            assertThat(graphs, is(notNullValue()));
            assertThat(graphs.size(), is(1));
            assertThat(graphs.iterator().next().getName(), is(GRAPH_NAME));
        } finally {
            db.graph(GRAPH_NAME).drop();
        }
    }

    @Test
    public void transactionString() {
        final TransactionOptions options = new TransactionOptions().params("test");
        final String result = db.transaction("function (params) {return params;}", String.class, options);
        assertThat(result, is("test"));
    }

    @Test
    public void transactionNumber() {
        final TransactionOptions options = new TransactionOptions().params(5);
        final Integer result = db.transaction("function (params) {return params;}", Integer.class, options);
        assertThat(result, is(5));
    }

    @Test
    public void transactionVPack() throws VPackException {
        final TransactionOptions options = new TransactionOptions().params(new VPackBuilder().add("test").slice());
        final VPackSlice result = db.transaction("function (params) {return params;}", VPackSlice.class, options);
        assertThat(result.isString(), is(true));
        assertThat(result.getAsString(), is("test"));
    }

    @Test
    public void transactionVPackObject() throws VPackException {
        final VPackSlice params = new VPackBuilder().add(ValueType.OBJECT).add("foo", "hello").add("bar", "world")
                .close().slice();
        final TransactionOptions options = new TransactionOptions().params(params);
        final String result = db
                .transaction("function (params) { return params['foo'] + ' ' + params['bar'];}", String.class, options);
        assertThat(result, is("hello world"));
    }

    @Test
    public void transactionVPackArray() throws VPackException {
        final VPackSlice params = new VPackBuilder().add(ValueType.ARRAY).add("hello").add("world").close().slice();
        final TransactionOptions options = new TransactionOptions().params(params);
        final String result = db
                .transaction("function (params) { return params[0] + ' ' + params[1];}", String.class, options);
        assertThat(result, is("hello world"));
    }

    @Test
    public void transactionMap() {
        final Map<String, Object> params = new MapBuilder().put("foo", "hello").put("bar", "world").get();
        final TransactionOptions options = new TransactionOptions().params(params);
        final String result = db
                .transaction("function (params) { return params['foo'] + ' ' + params['bar'];}", String.class, options);
        assertThat(result, is("hello world"));
    }

    @Test
    public void transactionArray() {
        final String[] params = new String[]{"hello", "world"};
        final TransactionOptions options = new TransactionOptions().params(params);
        final String result = db
                .transaction("function (params) { return params[0] + ' ' + params[1];}", String.class, options);
        assertThat(result, is("hello world"));
    }

    @Test
    public void transactionCollection() {
        final Collection<String> params = new ArrayList<>();
        params.add("hello");
        params.add("world");
        final TransactionOptions options = new TransactionOptions().params(params);
        final String result = db
                .transaction("function (params) { return params[0] + ' ' + params[1];}", String.class, options);
        assertThat(result, is("hello world"));
    }

    @Test
    public void transactionInsertJson() {
        try {
            db.createCollection(COLLECTION_NAME);
            final TransactionOptions options = new TransactionOptions().params("{\"_key\":\"0\"}")
                    .writeCollections(COLLECTION_NAME);
            //@formatter:off
            db.transaction("function (params) { "
                    + "var db = require('internal').db;"
                    + "db." + COLLECTION_NAME + ".save(JSON.parse(params));"
                    + "}", Void.class, options);
            //@formatter:on
            assertThat(db.collection(COLLECTION_NAME).count().getCount(), is(1L));
            assertThat(db.collection(COLLECTION_NAME).getDocument("0", String.class), is(notNullValue()));
        } finally {
            db.collection(COLLECTION_NAME).drop();
        }
    }

    @Test
    public void transactionExclusiveWrite() {
        assumeTrue(isAtLeastVersion(3, 4));
        try {
            db.createCollection(COLLECTION_NAME);
            final TransactionOptions options = new TransactionOptions().params("{\"_key\":\"0\"}")
                    .exclusiveCollections(COLLECTION_NAME);
            //@formatter:off
            db.transaction("function (params) { "
                    + "var db = require('internal').db;"
                    + "db." + COLLECTION_NAME + ".save(JSON.parse(params));"
                    + "}", Void.class, options);
            //@formatter:on
            assertThat(db.collection(COLLECTION_NAME).count().getCount(), is(1L));
            assertThat(db.collection(COLLECTION_NAME).getDocument("0", String.class), is(notNullValue()));
        } finally {
            db.collection(COLLECTION_NAME).drop();
        }
    }

    @Test
    public void transactionEmpty() {
        db.transaction("function () {}", null, null);
    }

    @Test
    public void transactionallowImplicit() {
        db.createCollection("someCollection", null);
        db.createCollection("someOtherCollection", null);
        final String action = "function (params) {" + "var db = require('internal').db;"
                + "return {'a':db.someCollection.all().toArray()[0], 'b':db.someOtherCollection.all().toArray()[0]};"
                + "}";
        final TransactionOptions options = new TransactionOptions().readCollections("someCollection");
        db.transaction(action, VPackSlice.class, options);
        try {
            options.allowImplicit(false);
            db.transaction(action, VPackSlice.class, options);
            fail();
        } catch (final ArangoDBException e) {
            assertThat(e.getResponseCode(), is(400));
        }
        db.collection("someCollection").drop();
        db.collection("someOtherCollection").drop();
    }

    @SuppressWarnings({"WeakerAccess", "unused"})
    protected static class TransactionTestEntity {
        private String value;

        public TransactionTestEntity() {
            super();
        }
    }

    @Test
    public void transactionPojoReturn() {
        final String action = "function() { return {'value':'hello world'}; }";
        final TransactionTestEntity res = db.transaction(action, TransactionTestEntity.class, new TransactionOptions());
        assertThat(res, is(notNullValue()));
        assertThat(res.value, is("hello world"));
    }

    @Test
    public void getInfo() {
        final DatabaseEntity info = db.getInfo();
        assertThat(info, is(notNullValue()));
        assertThat(info.getId(), is(notNullValue()));
        assertThat(info.getName(), is(TEST_DB));
        assertThat(info.getPath(), is(notNullValue()));
        assertThat(info.getIsSystem(), is(false));

        if (isAtLeastVersion(3, 6) && isCluster()) {
            assertThat(info.getSharding(), notNullValue());
            assertThat(info.getWriteConcern(), notNullValue());
            assertThat(info.getReplicationFactor(), notNullValue());
        }
    }

    @Test
    public void executeTraversal() {
        try {
            db.createCollection("person", null);
            db.createCollection("knows", new CollectionCreateOptions().type(CollectionType.EDGES));
            for (final String e : new String[]{"Alice", "Bob", "Charlie", "Dave", "Eve"}) {
                final BaseDocument doc = new BaseDocument();
                doc.setKey(e);
                db.collection("person").insertDocument(doc, null);
            }
            for (final String[] e : new String[][]{new String[]{"Alice", "Bob"}, new String[]{"Bob", "Charlie"},
                    new String[]{"Bob", "Dave"}, new String[]{"Eve", "Alice"}, new String[]{"Eve", "Bob"}}) {
                final BaseEdgeDocument edge = new BaseEdgeDocument();
                edge.setKey(e[0] + "_knows_" + e[1]);
                edge.setFrom("person/" + e[0]);
                edge.setTo("person/" + e[1]);
                db.collection("knows").insertDocument(edge, null);
            }
            final TraversalOptions options = new TraversalOptions().edgeCollection("knows").startVertex("person/Alice")
                    .direction(Direction.outbound);
            final TraversalEntity<BaseDocument, BaseEdgeDocument> traversal = db
                    .executeTraversal(BaseDocument.class, BaseEdgeDocument.class, options);

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
        } finally {
            db.collection("person").drop();
            db.collection("knows").drop();
        }
    }

    @Test
    public void getDocument() {
        try {
            db.createCollection(COLLECTION_NAME);
            final BaseDocument value = new BaseDocument();
            value.setKey("123");
            db.collection(COLLECTION_NAME).insertDocument(value);
            final BaseDocument document = db.getDocument(COLLECTION_NAME + "/123", BaseDocument.class);
            assertThat(document, is(notNullValue()));
            assertThat(document.getKey(), is("123"));
        } finally {
            db.collection(COLLECTION_NAME).drop();
        }
    }

    @Test
    public void shouldIncludeExceptionMessage() {
        assumeTrue(isAtLeastVersion(3, 4));

        final String exceptionMessage = "My error context";
        final String action = "function (params) {" + "throw '" + exceptionMessage + "';" + "}";
        try {
            db.transaction(action, VPackSlice.class, null);
            fail();
        } catch (final ArangoDBException e) {
            assertThat(e.getErrorMessage(), is(exceptionMessage));
        }
    }

    @Test(expected = ArangoDBException.class)
    public void getDocumentWrongId() {
        db.getDocument("123", BaseDocument.class);
    }

    @Test
    public void reloadRouting() {
        db.reloadRouting();
    }
}
