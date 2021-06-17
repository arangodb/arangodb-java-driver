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
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

/**
 * @author Mark Vollmary
 * @author Michele Rastelli
 */
@RunWith(Parameterized.class)
public class ArangoDatabaseTest extends BaseTest {

    private static final String CNAME1 = "ArangoDatabaseTest_collection_1";
    private static final String CNAME2 = "ArangoDatabaseTest_collection_2";
    private static final String ENAMES = "ArangoDatabaseTest_edge_collection";

    private final ArangoCollection collection1;
    private final ArangoCollection edges;

    @BeforeClass
    public static void init() {
        BaseTest.initDB();
        BaseTest.initCollections(CNAME1, CNAME2);
        BaseTest.initEdgeCollections(ENAMES);
    }

    public ArangoDatabaseTest(final ArangoDB arangoDB) {
        super(arangoDB);
        collection1 = db.collection(CNAME1);
        edges = db.collection(ENAMES);
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
        String name = "collection-" + rnd();
        final CollectionEntity result = db.createCollection(name, null);
        assertThat(result, is(notNullValue()));
        assertThat(result.getId(), is(notNullValue()));
    }

    @Test
    public void createCollectionWithReplicationFactor() {
        assumeTrue(isCluster());
        String name = "collection-" + rnd();
        final CollectionEntity result = db
                .createCollection(name, new CollectionCreateOptions().replicationFactor(2));
        assertThat(result, is(notNullValue()));
        assertThat(result.getId(), is(notNullValue()));
        CollectionPropertiesEntity props = db.collection(name).getProperties();
        assertThat(props.getReplicationFactor(), is(2));
        assertThat(props.getSatellite(), is(nullValue()));
    }

    @Test
    public void createCollectionWithMinReplicationFactor() {
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isCluster());

        String name = "collection-" + rnd();
        final CollectionEntity result = db.createCollection(name,
                new CollectionCreateOptions().replicationFactor(2).minReplicationFactor(2));
        assertThat(result, is(notNullValue()));
        assertThat(result.getId(), is(notNullValue()));
        CollectionPropertiesEntity props = db.collection(name).getProperties();
        assertThat(props.getReplicationFactor(), is(2));
        assertThat(props.getMinReplicationFactor(), is(2));
        assertThat(props.getSatellite(), is(nullValue()));
    }

    @Test
    public void createSatelliteCollection() {
        assumeTrue(isEnterprise());
        assumeTrue(isCluster());

        String name = "collection-" + rnd();
        final CollectionEntity result = db
                .createCollection(name, new CollectionCreateOptions().satellite(true));

        assertThat(result, is(notNullValue()));
        assertThat(result.getId(), is(notNullValue()));
        CollectionPropertiesEntity props = db.collection(name).getProperties();
        assertThat(props.getReplicationFactor(), is(nullValue()));
        assertThat(props.getSatellite(), is(true));
    }

    @Test
    public void createCollectionWithNumberOfShards() {
        assumeTrue(isCluster());
        String name = "collection-" + rnd();
        final CollectionEntity result = db
                .createCollection(name, new CollectionCreateOptions().numberOfShards(2));

        assertThat(result, is(notNullValue()));
        assertThat(result.getId(), is(notNullValue()));
        CollectionPropertiesEntity props = db.collection(name).getProperties();
        assertThat(props.getNumberOfShards(), is(2));
    }

    @Test
    public void createCollectionWithShardingStrategys() {
        assumeTrue(isAtLeastVersion(3, 4));
        assumeTrue(isCluster());

        String name = "collection-" + rnd();
        final CollectionEntity result = db.createCollection(name, new CollectionCreateOptions()
                .shardingStrategy(ShardingStrategy.COMMUNITY_COMPAT.getInternalName()));

        assertThat(result, is(notNullValue()));
        assertThat(result.getId(), is(notNullValue()));
        CollectionPropertiesEntity props = db.collection(name).getProperties();
        assertThat(props.getShardingStrategy(), is(ShardingStrategy.COMMUNITY_COMPAT.getInternalName()));
    }

    @Test
    public void createCollectionWithSmartJoinAttribute() {
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isEnterprise());
        assumeTrue(isCluster());

        String name = "collection-" + rnd();
        final CollectionEntity result = db.createCollection(name,
                new CollectionCreateOptions().smartJoinAttribute("test123").shardKeys("_key:"));
        assertThat(result, is(notNullValue()));
        assertThat(result.getId(), is(notNullValue()));
        assertThat(db.collection(name).getProperties().getSmartJoinAttribute(), is("test123"));
    }

    @Test
    public void createCollectionWithSmartJoinAttributeWrong() {
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isEnterprise());
        assumeTrue(isCluster());

        String name = "collection-" + rnd();

        try {
            db.createCollection(name, new CollectionCreateOptions().smartJoinAttribute("test123"));
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

        String name = "collection-" + rnd();
        final CollectionEntity result = db
                .createCollection(name, new CollectionCreateOptions().numberOfShards(2).shardKeys("a"));
        assertThat(result, is(notNullValue()));
        assertThat(result.getId(), is(notNullValue()));
        final CollectionPropertiesEntity properties = db.collection(name).getProperties();
        assertThat(properties.getNumberOfShards(), is(2));
        assertThat(properties.getShardKeys().size(), is(1));
    }

    @Test
    public void createCollectionWithNumberOfShardsAndShardKeys() {
        assumeTrue(isCluster());
        String name = "collection-" + rnd();
        final CollectionEntity result = db.createCollection(name,
                new CollectionCreateOptions().numberOfShards(2).shardKeys("a", "b"));
        assertThat(result, is(notNullValue()));
        assertThat(result.getId(), is(notNullValue()));
        final CollectionPropertiesEntity properties = db.collection(name).getProperties();
        assertThat(properties.getNumberOfShards(), is(2));
        assertThat(properties.getShardKeys().size(), is(2));
    }

    @Test
    public void createCollectionWithDistributeShardsLike() {
        assumeTrue(isEnterprise());
        assumeTrue(isCluster());

        final Integer numberOfShards = 3;

        String name1 = "collection-" + rnd();
        String name2 = "collection-" + rnd();
        db.createCollection(name1, new CollectionCreateOptions().numberOfShards(numberOfShards));
        db.createCollection(name2, new CollectionCreateOptions().distributeShardsLike(name1));

        assertThat(db.collection(name1).getProperties().getNumberOfShards(), is(numberOfShards));
        assertThat(db.collection(name2).getProperties().getNumberOfShards(), is(numberOfShards));
    }

    private void createCollectionWithKeyType(KeyType keyType) {
        String name = "collection-" + rnd();
        db.createCollection(name, new CollectionCreateOptions().keyOptions(
                false,
                keyType,
                null,
                null
        ));
        assertThat(db.collection(name).getProperties().getKeyOptions().getType(), is(keyType));
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

    @Test
    public void createCollectionWithJsonSchema() {
        assumeTrue(isAtLeastVersion(3, 7));
        String name = "collection-" + rnd();
        String rule = ("{  " +
                "           \"properties\": {" +
                "               \"number\": {" +
                "                   \"type\": \"number\"" +
                "               }" +
                "           }" +
                "       }")
                .replaceAll("\\s", "");
        String message = "The document has problems!";

        final CollectionEntity result = db
                .createCollection(name, new CollectionCreateOptions()
                        .schema(
                                new CollectionSchema()
                                        .setLevel(CollectionSchema.Level.NEW)
                                        .setMessage(message)
                                        .setRule(rule)
                        )
                );
        assertThat(result.getSchema().getLevel(), is(CollectionSchema.Level.NEW));
        assertThat(result.getSchema().getRule(), is(rule));
        assertThat(result.getSchema().getMessage(), is(message));

        CollectionPropertiesEntity props = db.collection(name).getProperties();
        assertThat(props.getSchema().getLevel(), is(CollectionSchema.Level.NEW));
        assertThat(props.getSchema().getRule(), is(rule));
        assertThat(props.getSchema().getMessage(), is(message));

        BaseDocument doc = new BaseDocument();
        doc.addAttribute("number", 33);
        db.collection(name).insertDocument(doc);

        try {
            BaseDocument wrongDoc = new BaseDocument();
            doc.addAttribute("number", "notANumber");
            db.collection(name).insertDocument(doc);
            fail();
        } catch (ArangoDBException e) {
            assertThat(e.getMessage(), containsString(message));
            assertThat(e.getResponseCode(), is(400));
            assertThat(e.getErrorNum(), is(1620));
        }
    }

    @Test(expected = ArangoDBException.class)
    public void deleteCollection() {
        String name = "collection-" + rnd();
        db.createCollection(name, null);
        db.collection(name).drop();
        db.collection(name).getInfo();
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
        final Collection<String> fields = Collections.singletonList("field-" + rnd());
        final IndexEntity createResult = collection1.ensureHashIndex(fields, null);
        final IndexEntity readResult = db.getIndex(createResult.getId());
        assertThat(readResult.getId(), is(createResult.getId()));
        assertThat(readResult.getType(), is(createResult.getType()));
    }

    @Test
    public void deleteIndex() {
        final Collection<String> fields = Collections.singletonList("field-" + rnd());
        final IndexEntity createResult = collection1.ensureHashIndex(fields, null);
        final String id = db.deleteIndex(createResult.getId());
        assertThat(id, is(createResult.getId()));
        try {
            db.getIndex(id);
            fail();
        } catch (final ArangoDBException e) {
            assertThat(e.getResponseCode(), is(404));
        }
    }

    @Test
    public void getCollections() {
        final Collection<CollectionEntity> collections = db.getCollections(null);
        long count = collections.stream().map(CollectionEntity::getName).filter(it -> it.equals(CNAME1)).count();
        assertThat(count, is(1L));
    }

    @Test
    public void getCollectionsExcludeSystem() {
        final CollectionsReadOptions options = new CollectionsReadOptions().excludeSystem(true);
        final Collection<CollectionEntity> nonSystemCollections = db.getCollections(options);
        final Collection<CollectionEntity> allCollections = db.getCollections(null);
        assertThat(allCollections.size(), is(greaterThan(nonSystemCollections.size())));
    }

    @Test
    public void grantAccess() {
        String user = "user-" + rnd();
        arangoDB.createUser(user, "1234", null);
        db.grantAccess(user);
    }

    @Test
    public void grantAccessRW() {
        String user = "user-" + rnd();
        arangoDB.createUser(user, "1234", null);
        db.grantAccess(user, Permissions.RW);
    }

    @Test
    public void grantAccessRO() {
        String user = "user-" + rnd();
        arangoDB.createUser(user, "1234", null);
        db.grantAccess(user, Permissions.RO);
    }

    @Test
    public void grantAccessNONE() {
        String user = "user-" + rnd();
        arangoDB.createUser(user, "1234", null);
        db.grantAccess(user, Permissions.NONE);
    }

    @Test(expected = ArangoDBException.class)
    public void grantAccessUserNotFound() {
        String user = "user-" + rnd();
        db.grantAccess(user, Permissions.RW);
    }

    @Test
    public void revokeAccess() {
        String user = "user-" + rnd();
        arangoDB.createUser(user, "1234", null);
        db.revokeAccess(user);
    }

    @Test(expected = ArangoDBException.class)
    public void revokeAccessUserNotFound() {
        String user = "user-" + rnd();
        db.revokeAccess(user);
    }

    @Test
    public void resetAccess() {
        String user = "user-" + rnd();
        arangoDB.createUser(user, "1234", null);
        db.resetAccess(user);
    }

    @Test(expected = ArangoDBException.class)
    public void resetAccessUserNotFound() {
        String user = "user-" + rnd();
        db.resetAccess(user);
    }

    @Test
    public void grantDefaultCollectionAccess() {
        String user = "user-" + rnd();
        arangoDB.createUser(user, "1234");
        db.grantDefaultCollectionAccess(user, Permissions.RW);
    }

    @Test
    public void getPermissions() {
        assertThat(Permissions.RW, is(db.getPermissions("root")));
    }

    @Test
    public void query() {
        for (int i = 0; i < 10; i++) {
            collection1.insertDocument(new BaseDocument(), null);
        }
        final ArangoCursor<String> cursor = db.query("for i in " + CNAME1 + " return i._id", null, null, String.class);
        assertThat(cursor, is(notNullValue()));
        for (int i = 0; i < 10; i++, cursor.next()) {
            assertThat(cursor.hasNext(), is(true));
        }
    }

    @Test
    public void queryForEach() {
        for (int i = 0; i < 10; i++) {
            collection1.insertDocument(new BaseDocument(), null);
        }
        final ArangoCursor<String> cursor = db.query("for i in " + CNAME1 + " return i._id", null, null, String.class);
        assertThat(cursor, is(notNullValue()));

        int i = 0;
        while (cursor.hasNext()) {
            cursor.next();
            i++;
        }
        assertThat(i, is(greaterThanOrEqualTo(10)));
    }

    @Test
    public void queryWithCount() {
        for (int i = 0; i < 10; i++) {
            collection1.insertDocument(new BaseDocument(), null);
        }

        final ArangoCursor<String> cursor = db
                .query("for i in " + CNAME1 + " Limit 6 return i._id", null, new AqlQueryOptions().count(true),
                        String.class);
        assertThat(cursor, is(notNullValue()));
        for (int i = 1; i <= 6; i++, cursor.next()) {
            assertThat(cursor.hasNext(), is(true));
        }
        assertThat(cursor.getCount(), is(6));
    }

    @Test
    public void queryWithLimitAndFullCount() {
        for (int i = 0; i < 10; i++) {
            collection1.insertDocument(new BaseDocument(), null);
        }

        final ArangoCursor<String> cursor = db
                .query("for i in " + CNAME1 + " Limit 5 return i._id", null, new AqlQueryOptions().fullCount(true),
                        String.class);
        assertThat(cursor, is(notNullValue()));
        for (int i = 0; i < 5; i++, cursor.next()) {
            assertThat(cursor.hasNext(), is(true));
        }
        assertThat(cursor.getStats(), is(notNullValue()));
        assertThat(cursor.getStats().getFullCount(), is(greaterThanOrEqualTo(10L)));
    }

    @Test
    public void queryWithBatchSize() {
        for (int i = 0; i < 10; i++) {
            collection1.insertDocument(new BaseDocument(), null);
        }

        final ArangoCursor<String> cursor = db
                .query("for i in " + CNAME1 + " return i._id", null, new AqlQueryOptions().batchSize(5).count(true),
                        String.class);

        assertThat(cursor, is(notNullValue()));
        for (int i = 0; i < 10; i++, cursor.next()) {
            assertThat(cursor.hasNext(), is(true));
        }
    }

    @Test
    public void queryIterateWithBatchSize() {
        for (int i = 0; i < 10; i++) {
            collection1.insertDocument(new BaseDocument(), null);
        }

        final ArangoCursor<String> cursor = db
                .query("for i in " + CNAME1 + " return i._id", null, new AqlQueryOptions().batchSize(5).count(true),
                        String.class);

        assertThat(cursor, is(notNullValue()));
        final AtomicInteger i = new AtomicInteger(0);
        for (; cursor.hasNext(); cursor.next()) {
            i.incrementAndGet();
        }
        assertThat(i.get(), is(greaterThanOrEqualTo(10)));
    }

// FIXME
//    /**
//     * ignored. takes to long
//     */
//    @Test
//    @Ignore
//    public void queryWithTTL() throws InterruptedException {
//        // set TTL to 1 seconds and get the second batch after 2 seconds!
//        final int ttl = 1;
//        final int wait = 2;
//        try {
//            db.createCollection(COLLECTION_NAME, null);
//            for (int i = 0; i < 10; i++) {
//                db.collection(COLLECTION_NAME).insertDocument(new BaseDocument(), null);
//            }
//
//            final ArangoCursor<String> cursor = db
//                    .query("for i in db_test return i._id", null, new AqlQueryOptions().batchSize(5).ttl(ttl),
//                            String.class);
//
//            assertThat(cursor, is(notNullValue()));
//
//            for (int i = 0; i < 10; i++, cursor.next()) {
//                assertThat(cursor.hasNext(), is(true));
//                if (i == 1) {
//                    Thread.sleep(wait * 1000);
//                }
//            }
//            fail("this should fail");
//        } catch (final ArangoDBException ex) {
//            assertThat(ex.getMessage(), is("Response: 404, Error: 1600 - cursor not found"));
//        } finally {
//            db.collection(COLLECTION_NAME).drop();
//        }
//    }

    @Test
    public void changeQueryCache() {
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

        final QueryCachePropertiesEntity properties2 = new QueryCachePropertiesEntity();
        properties2.setMode(CacheMode.off);
        db.setQueryCacheProperties(properties2);
    }

    @Test
    public void queryWithCache() {
        assumeTrue(isSingleServer());
        for (int i = 0; i < 10; i++) {
            collection1.insertDocument(new BaseDocument(), null);
        }

        final QueryCachePropertiesEntity properties = new QueryCachePropertiesEntity();
        properties.setMode(CacheMode.on);
        db.setQueryCacheProperties(properties);

        final ArangoCursor<String> cursor = db
                .query("FOR t IN " + CNAME1 + " FILTER t.age >= 10 SORT t.age RETURN t._id", null,
                        new AqlQueryOptions().cache(true), String.class);

        assertThat(cursor, is(notNullValue()));
        assertThat(cursor.isCached(), is(false));

        final ArangoCursor<String> cachedCursor = db
                .query("FOR t IN " + CNAME1 + " FILTER t.age >= 10 SORT t.age RETURN t._id", null,
                        new AqlQueryOptions().cache(true), String.class);

        assertThat(cachedCursor, is(notNullValue()));
        assertThat(cachedCursor.isCached(), is(true));

        final QueryCachePropertiesEntity properties2 = new QueryCachePropertiesEntity();
        properties2.setMode(CacheMode.off);
        db.setQueryCacheProperties(properties2);
    }

    @Test
    public void queryWithMemoryLimit() {
        try {
            db.query("RETURN 1..10000", null, new AqlQueryOptions().memoryLimit(32 * 1024L), String.class);
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
        final int numbDocs = 10;
        for (int i = 0; i < numbDocs; i++) {
            collection1.insertDocument(new BaseDocument(), null);
        }

        final int batchSize = 5;
        final ArangoCursor<String> cursor = db.query("for i in " + CNAME1 + " return i._id", null,
                new AqlQueryOptions().batchSize(batchSize).count(true), String.class);
        assertThat(cursor, is(notNullValue()));
        assertThat(cursor.getCount(), is(greaterThanOrEqualTo(numbDocs)));

        final ArangoCursor<String> cursor2 = db.cursor(cursor.getId(), String.class);
        assertThat(cursor2, is(notNullValue()));
        assertThat(cursor2.getCount(), is(greaterThanOrEqualTo(numbDocs)));
        assertThat(cursor2.hasNext(), is(true));

        for (int i = 0; i < batchSize; i++, cursor.next()) {
            assertThat(cursor.hasNext(), is(true));
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
        for (int i = 0; i < 10; i++) {
            final BaseDocument baseDocument = new BaseDocument();
            baseDocument.addAttribute("age", 20 + i);
            collection1.insertDocument(baseDocument, null);
        }
        final Map<String, Object> bindVars = new HashMap<>();
        bindVars.put("@coll", CNAME1);
        bindVars.put("age", 25);

        final ArangoCursor<String> cursor = db
                .query("FOR t IN @@coll FILTER t.age >= @age SORT t.age RETURN t._id", bindVars, null,
                        String.class);

        assertThat(cursor, is(notNullValue()));

        for (int i = 0; i < 5; i++, cursor.next()) {
            assertThat(cursor.hasNext(), is(true));
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
            while (cursor.hasNext()) {
                cursor.next();
                count++;
            }
            fail();
        } catch (final ArangoDBException e) {
            assertThat(count, is(1));
        }

    }

    @Test
    public void queryNoResults() throws IOException {
        final ArangoCursor<BaseDocument> cursor = db
                .query("FOR i IN @@col RETURN i", new MapBuilder().put("@col", CNAME1).get(), null,
                        BaseDocument.class);
        cursor.close();
    }

    @Test
    public void queryWithNullBindParam() throws IOException {
        final ArangoCursor<BaseDocument> cursor = db.query("FOR i IN @@col FILTER i.test == @test RETURN i",
                new MapBuilder().put("@col", CNAME1).put("test", null).get(), null, BaseDocument.class);
        cursor.close();
    }

    @Test
    public void queryAllowDirtyRead() throws IOException {
        final ArangoCursor<BaseDocument> cursor = db.query("FOR i IN @@col FILTER i.test == @test RETURN i",
                new MapBuilder().put("@col", CNAME1).put("test", null).get(),
                new AqlQueryOptions().allowDirtyRead(true), BaseDocument.class);
        cursor.close();
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
    public void getCurrentlyRunningQueries() throws InterruptedException {
        String query = "return sleep(1)";
        Thread t = new Thread(() -> db.query(query, null, null, Void.class));
        t.start();
        Thread.sleep(300);
        final Collection<QueryEntity> currentlyRunningQueries = db.getCurrentlyRunningQueries();
        assertThat(currentlyRunningQueries, is(notNullValue()));
        assertThat(currentlyRunningQueries.size(), is(1));
        final QueryEntity queryEntity = currentlyRunningQueries.iterator().next();
        assertThat(queryEntity.getQuery(), is(query));
        assertThat(queryEntity.getState(), is(QueryExecutionState.EXECUTING));
        t.join();
    }

    @Test
    public void killQuery() throws InterruptedException, ExecutionException {
        ExecutorService es = Executors.newSingleThreadExecutor();
        Future<?> future = es.submit(() -> {
            try {
                db.query("return sleep(5)", null, null, Void.class);
                fail();
            } catch (ArangoDBException e) {
                assertThat(e.getResponseCode(), is(410));
                assertThat(e.getErrorNum(), is(1500));
                assertThat(e.getErrorMessage(), containsString("query killed"));
            }
        });
        Thread.sleep(500);

        Collection<QueryEntity> currentlyRunningQueries = db.getCurrentlyRunningQueries();
        assertThat(currentlyRunningQueries.size(), is(1));
        QueryEntity queryEntity = currentlyRunningQueries.iterator().next();
        assertThat(queryEntity.getState(), is(QueryExecutionState.EXECUTING));
        db.killQuery(queryEntity.getId());

        db.getCurrentlyRunningQueries().forEach(q ->
                assertThat(q.getState(), is(QueryExecutionState.KILLED))
        );

        future.get();
        es.shutdown();
    }

    @Test
    public void getAndClearSlowQueries() {
        db.clearSlowQueries();

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
        String name = "graph-" + rnd();
        final GraphEntity result = db.createGraph(name, null, null);
        assertThat(result.getName(), is(name));
    }

    @Test
    public void createGraphSatellite() {
        assumeTrue(isAtLeastVersion(3, 7));
        assumeTrue(isCluster());
        assumeTrue(isEnterprise());

        String name = "graph-" + rnd();
        final GraphEntity result = db.createGraph(name, null, new GraphCreateOptions().satellite(true));
        assertThat(result.getSatellite(), is(true));

        GraphEntity info = db.graph(name).getInfo();
        assertThat(info.getSatellite(), is(true));

        GraphEntity graph = db.getGraphs().stream().filter(g -> name.equals(g.getName())).findFirst().get();
        assertThat(graph.getSatellite(), is(true));
    }

    @Test
    public void createGraphReplicationFaktor() {
        assumeTrue(isCluster());
        String name = "graph-" + rnd();
        final String edgeCollection = "edge-" + rnd();
        final String fromCollection = "from-" + rnd();
        final String toCollection = "to-" + rnd();
        final Collection<EdgeDefinition> edgeDefinitions = Collections.singletonList(new EdgeDefinition().collection(edgeCollection).from(fromCollection).to(toCollection));
        final GraphEntity result = db.createGraph(name, edgeDefinitions, new GraphCreateOptions().replicationFactor(2));
        assertThat(result, is(notNullValue()));
        for (final String collection : Arrays.asList(edgeCollection, fromCollection, toCollection)) {
            final CollectionPropertiesEntity properties = db.collection(collection).getProperties();
            assertThat(properties.getReplicationFactor(), is(2));
        }
    }

    @Test
    public void createGraphNumberOfShards() {
        assumeTrue(isCluster());
        String name = "graph-" + rnd();
        final String edgeCollection = "edge-" + rnd();
        final String fromCollection = "from-" + rnd();
        final String toCollection = "to-" + rnd();
        final Collection<EdgeDefinition> edgeDefinitions = Collections.singletonList(new EdgeDefinition().collection(edgeCollection).from(fromCollection).to(toCollection));
        final GraphEntity result = db
                .createGraph(name, edgeDefinitions, new GraphCreateOptions().numberOfShards(2));
        assertThat(result, is(notNullValue()));
        for (final String collection : Arrays.asList(edgeCollection, fromCollection, toCollection)) {
            final CollectionPropertiesEntity properties = db.collection(collection).getProperties();
            assertThat(properties.getNumberOfShards(), is(2));
        }
    }

    @Test
    public void getGraphs() {
        String name = "graph-" + rnd();
        db.createGraph(name, null, null);
        final Collection<GraphEntity> graphs = db.getGraphs();
        assertThat(graphs, is(notNullValue()));
        assertThat(graphs.size(), is(greaterThanOrEqualTo(1)));
        long count = graphs.stream().map(GraphEntity::getName).filter(name::equals).count();
        assertThat(count, is(1L));
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
        String key = "key-" + rnd();
        final TransactionOptions options = new TransactionOptions().params("{\"_key\":\"" + key + "\"}")
                .writeCollections(CNAME1);
        db.transaction("function (params) { "
                + "var db = require('internal').db;"
                + "db." + CNAME1 + ".save(JSON.parse(params));"
                + "}", Void.class, options);
        assertThat(db.collection(CNAME1).getDocument(key, String.class), is(notNullValue()));
    }

    @Test
    public void transactionExclusiveWrite() {
        assumeTrue(isAtLeastVersion(3, 4));
        String key = "key-" + rnd();
        final TransactionOptions options = new TransactionOptions().params("{\"_key\":\"" + key + "\"}")
                .exclusiveCollections(CNAME1);
        db.transaction("function (params) { "
                + "var db = require('internal').db;"
                + "db." + CNAME1 + ".save(JSON.parse(params));"
                + "}", Void.class, options);
        assertThat(db.collection(CNAME1).getDocument(key, String.class), is(notNullValue()));
    }

    @Test
    public void transactionEmpty() {
        db.transaction("function () {}", null, null);
    }

    @Test
    public void transactionAllowImplicit() {
        final String action = "function (params) {" + "var db = require('internal').db;"
                + "return {'a':db." + CNAME1 + ".all().toArray()[0], 'b':db." + CNAME2 + ".all().toArray()[0]};"
                + "}";
        final TransactionOptions options = new TransactionOptions().readCollections(CNAME1);
        db.transaction(action, VPackSlice.class, options);
        try {
            options.allowImplicit(false);
            db.transaction(action, VPackSlice.class, options);
            fail();
        } catch (final ArangoDBException e) {
            assertThat(e.getResponseCode(), is(400));
        }
    }

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
        assertThat(info.getName(), is(BaseTest.TEST_DB));
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
        String k1 = "key-" + rnd();
        String k2 = "key-" + rnd();
        String k3 = "key-" + rnd();
        String k4 = "key-" + rnd();
        String k5 = "key-" + rnd();

        for (final String e : new String[]{
                k1, k2, k3, k4, k5
        }) {
            collection1.insertDocument(new BaseDocument(e), null);
        }
        for (final String[] e : new String[][]{
                new String[]{k1, k2}, new String[]{k2, k3},
                new String[]{k2, k4}, new String[]{k5, k1}, new String[]{k5, k2}
        }) {
            final BaseEdgeDocument edge = new BaseEdgeDocument();
            edge.setKey(e[0] + "_knows_" + e[1]);
            edge.setFrom(CNAME1 + "/" + e[0]);
            edge.setTo(CNAME1 + "/" + e[1]);
            edges.insertDocument(edge, null);
        }

        final TraversalOptions options = new TraversalOptions().edgeCollection(ENAMES).startVertex(CNAME1 + "/" + k1).direction(Direction.outbound);
        final TraversalEntity<BaseDocument, BaseEdgeDocument> traversal = db.executeTraversal(BaseDocument.class, BaseEdgeDocument.class, options);
        assertThat(traversal, is(notNullValue()));

        final Collection<BaseDocument> vertices = traversal.getVertices();
        assertThat(vertices, is(notNullValue()));
        assertThat(vertices.size(), is(4));

        final Iterator<BaseDocument> verticesIterator = vertices.iterator();
        final Collection<String> v = Arrays.asList(k1, k2, k3, k4);
        while (verticesIterator.hasNext()) {
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
        assertThat(first.getVertices().iterator().next().getKey(), is(k1));
    }

    @Test
    public void getDocument() {
        String key = "key-" + rnd();
        final BaseDocument value = new BaseDocument(key);
        collection1.insertDocument(value);
        final BaseDocument document = db.getDocument(CNAME1 + "/" + key, BaseDocument.class);
        assertThat(document, is(notNullValue()));
        assertThat(document.getKey(), is(key));
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
