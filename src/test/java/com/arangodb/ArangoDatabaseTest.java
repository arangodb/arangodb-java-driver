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
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.assumeTrue;


/**
 * @author Mark Vollmary
 * @author Michele Rastelli
 */
class ArangoDatabaseTest extends BaseJunit5 {

    private static final String CNAME1 = "ArangoDatabaseTest_collection_1";
    private static final String CNAME2 = "ArangoDatabaseTest_collection_2";
    private static final String ENAMES = "ArangoDatabaseTest_edge_collection";

    @BeforeAll
    static void init() {
        BaseJunit5.initDB();
        BaseJunit5.initCollections(CNAME1, CNAME2);
        BaseJunit5.initEdgeCollections(ENAMES);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void getVersion(ArangoDatabase db) {
        final ArangoDBVersion version = db.getVersion();
        assertThat(version).isNotNull();
        assertThat(version.getServer()).isNotNull();
        assertThat(version.getVersion()).isNotNull();
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void getEngine(ArangoDatabase db) {
        final ArangoDBEngine engine = db.getEngine();
        assertThat(engine).isNotNull();
        assertThat(engine.getName()).isNotNull();
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("arangos")
    void exists(ArangoDB arangoDB) {
        assertThat(arangoDB.db(TEST_DB).exists()).isTrue();
        assertThat(arangoDB.db(DbName.of("no")).exists()).isFalse();
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void getAccessibleDatabases(ArangoDatabase db) {
        final Collection<String> dbs = db.getAccessibleDatabases();
        assertThat(dbs).contains("_system");
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void createCollection(ArangoDatabase db) {
        String name = "collection-" + rnd();
        final CollectionEntity result = db.createCollection(name, null);
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void createCollectionWithReplicationFactor(ArangoDatabase db) {
        assumeTrue(isCluster());
        String name = "collection-" + rnd();
        final CollectionEntity result = db
                .createCollection(name, new CollectionCreateOptions().replicationFactor(2));
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        CollectionPropertiesEntity props = db.collection(name).getProperties();
        assertThat(props.getReplicationFactor()).isEqualTo(2);
        assertThat(props.getSatellite()).isNull();
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void createCollectionWithWriteConcern(ArangoDatabase db) {
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isCluster());

        String name = "collection-" + rnd();
        final CollectionEntity result = db.createCollection(name,
                new CollectionCreateOptions().replicationFactor(2).writeConcern(2));
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        CollectionPropertiesEntity props = db.collection(name).getProperties();
        assertThat(props.getReplicationFactor()).isEqualTo(2);
        assertThat(props.getWriteConcern()).isEqualTo(2);
        assertThat(props.getSatellite()).isNull();
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void createSatelliteCollection(ArangoDatabase db) {
        assumeTrue(isEnterprise());
        assumeTrue(isCluster());

        String name = "collection-" + rnd();
        final CollectionEntity result = db
                .createCollection(name, new CollectionCreateOptions().satellite(true));

        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        CollectionPropertiesEntity props = db.collection(name).getProperties();
        assertThat(props.getReplicationFactor()).isNull();
        assertThat(props.getSatellite()).isTrue();
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void createCollectionWithNumberOfShards(ArangoDatabase db) {
        assumeTrue(isCluster());
        String name = "collection-" + rnd();
        final CollectionEntity result = db
                .createCollection(name, new CollectionCreateOptions().numberOfShards(2));

        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        CollectionPropertiesEntity props = db.collection(name).getProperties();
        assertThat(props.getNumberOfShards()).isEqualTo(2);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void createCollectionWithShardingStrategys(ArangoDatabase db) {
        assumeTrue(isAtLeastVersion(3, 4));
        assumeTrue(isCluster());

        String name = "collection-" + rnd();
        final CollectionEntity result = db.createCollection(name, new CollectionCreateOptions()
                .shardingStrategy(ShardingStrategy.COMMUNITY_COMPAT.getInternalName()));

        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        CollectionPropertiesEntity props = db.collection(name).getProperties();
        assertThat(props.getShardingStrategy()).isEqualTo(ShardingStrategy.COMMUNITY_COMPAT.getInternalName());
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void createCollectionWithSmartJoinAttribute(ArangoDatabase db) {
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isEnterprise());
        assumeTrue(isCluster());

        String name = "collection-" + rnd();
        final CollectionEntity result = db.createCollection(name,
                new CollectionCreateOptions().smartJoinAttribute("test123").shardKeys("_key:"));
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(db.collection(name).getProperties().getSmartJoinAttribute()).isEqualTo("test123");
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void createCollectionWithSmartJoinAttributeWrong(ArangoDatabase db) {
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isEnterprise());
        assumeTrue(isCluster());

        String name = "collection-" + rnd();

        try {
            db.createCollection(name, new CollectionCreateOptions().smartJoinAttribute("test123"));
        } catch (ArangoDBException e) {
            assertThat(e.getErrorNum()).isEqualTo(4006);
            // TODO:
            //  	at the moment older server versions reply with response code 500, which is a misbehavior
            //		when the fix has been backported to all the supported db versions uncomment the following:
            //		assertThat(e.getResponseCode()).isEqualTo(400));
        }
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void createCollectionWithNumberOfShardsAndShardKey(ArangoDatabase db) {
        assumeTrue(isCluster());

        String name = "collection-" + rnd();
        final CollectionEntity result = db
                .createCollection(name, new CollectionCreateOptions().numberOfShards(2).shardKeys("a"));
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        final CollectionPropertiesEntity properties = db.collection(name).getProperties();
        assertThat(properties.getNumberOfShards()).isEqualTo(2);
        assertThat(properties.getShardKeys()).hasSize(1);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void createCollectionWithNumberOfShardsAndShardKeys(ArangoDatabase db) {
        assumeTrue(isCluster());
        String name = "collection-" + rnd();
        final CollectionEntity result = db.createCollection(name,
                new CollectionCreateOptions().numberOfShards(2).shardKeys("a", "b"));
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        final CollectionPropertiesEntity properties = db.collection(name).getProperties();
        assertThat(properties.getNumberOfShards()).isEqualTo(2);
        assertThat(properties.getShardKeys()).hasSize(2);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void createCollectionWithDistributeShardsLike(ArangoDatabase db) {
        assumeTrue(isEnterprise());
        assumeTrue(isCluster());

        final Integer numberOfShards = 3;

        String name1 = "collection-" + rnd();
        String name2 = "collection-" + rnd();
        db.createCollection(name1, new CollectionCreateOptions().numberOfShards(numberOfShards));
        db.createCollection(name2, new CollectionCreateOptions().distributeShardsLike(name1));

        assertThat(db.collection(name1).getProperties().getNumberOfShards()).isEqualTo(numberOfShards);
        assertThat(db.collection(name2).getProperties().getNumberOfShards()).isEqualTo(numberOfShards);
    }

    private void createCollectionWithKeyType(ArangoDatabase db, KeyType keyType) {
        String name = "collection-" + rnd();
        db.createCollection(name, new CollectionCreateOptions().keyOptions(
                false,
                keyType,
                null,
                null
        ));
        assertThat(db.collection(name).getProperties().getKeyOptions().getType()).isEqualTo(keyType);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void createCollectionWithKeyTypeAutoincrement(ArangoDatabase db) {
        assumeTrue(isSingleServer());
        createCollectionWithKeyType(db, KeyType.autoincrement);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void createCollectionWithKeyTypePadded(ArangoDatabase db) {
        assumeTrue(isAtLeastVersion(3, 4));
        createCollectionWithKeyType(db, KeyType.padded);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void createCollectionWithKeyTypeTraditional(ArangoDatabase db) {
        createCollectionWithKeyType(db, KeyType.traditional);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void createCollectionWithKeyTypeUuid(ArangoDatabase db) {
        assumeTrue(isAtLeastVersion(3, 4));
        createCollectionWithKeyType(db, KeyType.uuid);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void createCollectionWithJsonSchema(ArangoDatabase db) {
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
        assertThat(result.getSchema().getLevel()).isEqualTo(CollectionSchema.Level.NEW);
        assertThat(result.getSchema().getRule()).isEqualTo(rule);
        assertThat(result.getSchema().getMessage()).isEqualTo(message);

        CollectionPropertiesEntity props = db.collection(name).getProperties();
        assertThat(props.getSchema().getLevel()).isEqualTo(CollectionSchema.Level.NEW);
        assertThat(props.getSchema().getRule()).isEqualTo(rule);
        assertThat(props.getSchema().getMessage()).isEqualTo(message);

        BaseDocument doc = new BaseDocument();
        doc.addAttribute("number", 33);
        db.collection(name).insertDocument(doc);

        BaseDocument wrongDoc = new BaseDocument();
        wrongDoc.addAttribute("number", "notANumber");
        Throwable thrown = catchThrowable(() -> db.collection(name).insertDocument(wrongDoc));
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
        ArangoDBException e = (ArangoDBException) thrown;

        assertThat(e).hasMessageContaining(message);
        assertThat(e.getResponseCode()).isEqualTo(400);
        assertThat(e.getErrorNum()).isEqualTo(1620);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void createCollectionWithComputedFields(ArangoDatabase db) {
        assumeTrue(isAtLeastVersion(3, 10));
        String cName = "collection-" + rnd();
        ComputedValue cv = new ComputedValue()
                .name("foo")
                .expression("RETURN 11")
                .overwrite(false)
                .computeOn(ComputedValue.ComputeOn.insert)
                .keepNull(false)
                .failOnWarning(true);

        final CollectionEntity result = db.createCollection(cName, new CollectionCreateOptions().computedValues(cv));

        assertThat(result).isNotNull();
        assertThat(result.getComputedValues())
                .hasSize(1)
                .contains(cv);

        ComputedValue cv2 = new ComputedValue()
                .name("bar")
                .expression("RETURN 22")
                .overwrite(true)
                .computeOn(ComputedValue.ComputeOn.update, ComputedValue.ComputeOn.replace)
                .keepNull(true)
                .failOnWarning(false);

        db.collection(cName).changeProperties(new CollectionPropertiesOptions().computedValues(cv2));

        CollectionPropertiesEntity props = db.collection(cName).getProperties();
        assertThat(props.getComputedValues())
                .hasSize(1)
                .contains(cv2);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void deleteCollection(ArangoDatabase db) {
        String name = "collection-" + rnd();
        db.createCollection(name, null);
        db.collection(name).drop();
        Throwable thrown = catchThrowable(() -> db.collection(name).getInfo());
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void deleteSystemCollection(ArangoDatabase db) {
        final String name = "_system_test";
        db.createCollection(name, new CollectionCreateOptions().isSystem(true));
        db.collection(name).drop(true);
        Throwable thrown = catchThrowable(() -> db.collection(name).getInfo());
        assertThat(thrown)
                .isInstanceOf(ArangoDBException.class)
                .extracting(it -> ((ArangoDBException) it).getResponseCode())
                .isEqualTo(404);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void deleteSystemCollectionFail(ArangoDatabase db) {
        final String name = "_system_test";
        ArangoCollection collection = db.collection(name);
        if (collection.exists())
            collection.drop(true);

        db.createCollection(name, new CollectionCreateOptions().isSystem(true));
        try {
            collection.drop();
            fail();
        } catch (final ArangoDBException e) {
            assertThat(e.getResponseCode()).isEqualTo(403);
        }
        collection.drop(true);
        try {
            collection.getInfo();
            fail();
        } catch (final ArangoDBException e) {
            assertThat(e.getResponseCode()).isEqualTo(404);
        }
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void getIndex(ArangoDatabase db) {
        final Collection<String> fields = Collections.singletonList("field-" + rnd());
        final IndexEntity createResult = db.collection(CNAME1).ensureHashIndex(fields, null);
        final IndexEntity readResult = db.getIndex(createResult.getId());
        assertThat(readResult.getId()).isEqualTo(createResult.getId());
        assertThat(readResult.getType()).isEqualTo(createResult.getType());
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void deleteIndex(ArangoDatabase db) {
        final Collection<String> fields = Collections.singletonList("field-" + rnd());
        final IndexEntity createResult = db.collection(CNAME1).ensureHashIndex(fields, null);
        final String id = db.deleteIndex(createResult.getId());
        assertThat(id).isEqualTo(createResult.getId());
        try {
            db.getIndex(id);
            fail();
        } catch (final ArangoDBException e) {
            assertThat(e.getResponseCode()).isEqualTo(404);
        }
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void getCollections(ArangoDatabase db) {
        final Collection<CollectionEntity> collections = db.getCollections(null);
        long count = collections.stream().map(CollectionEntity::getName).filter(it -> it.equals(CNAME1)).count();
        assertThat(count).isEqualTo(1L);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void getCollectionsExcludeSystem(ArangoDatabase db) {
        final CollectionsReadOptions options = new CollectionsReadOptions().excludeSystem(true);
        final Collection<CollectionEntity> nonSystemCollections = db.getCollections(options);
        final Collection<CollectionEntity> allCollections = db.getCollections(null);
        assertThat(allCollections).hasSizeGreaterThan(nonSystemCollections.size());
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("arangos")
    void grantAccess(ArangoDB arangoDB) {
        String user = "user-" + rnd();
        arangoDB.createUser(user, "1234", null);
        arangoDB.db(TEST_DB).grantAccess(user);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("arangos")
    void grantAccessRW(ArangoDB arangoDB) {
        String user = "user-" + rnd();
        arangoDB.createUser(user, "1234", null);
        arangoDB.db(TEST_DB).grantAccess(user, Permissions.RW);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("arangos")
    void grantAccessRO(ArangoDB arangoDB) {
        String user = "user-" + rnd();
        arangoDB.createUser(user, "1234", null);
        arangoDB.db(TEST_DB).grantAccess(user, Permissions.RO);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("arangos")
    void grantAccessNONE(ArangoDB arangoDB) {
        String user = "user-" + rnd();
        arangoDB.createUser(user, "1234", null);
        arangoDB.db(TEST_DB).grantAccess(user, Permissions.NONE);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void grantAccessUserNotFound(ArangoDatabase db) {
        String user = "user-" + rnd();
        Throwable thrown = catchThrowable(() -> db.grantAccess(user, Permissions.RW));
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("arangos")
    void revokeAccess(ArangoDB arangoDB) {
        String user = "user-" + rnd();
        arangoDB.createUser(user, "1234", null);
        arangoDB.db(TEST_DB).revokeAccess(user);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void revokeAccessUserNotFound(ArangoDatabase db) {
        String user = "user-" + rnd();
        Throwable thrown = catchThrowable(() -> db.revokeAccess(user));
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("arangos")
    void resetAccess(ArangoDB arangoDB) {
        String user = "user-" + rnd();
        arangoDB.createUser(user, "1234", null);
        arangoDB.db(TEST_DB).resetAccess(user);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void resetAccessUserNotFound(ArangoDatabase db) {
        String user = "user-" + rnd();
        Throwable thrown = catchThrowable(() -> db.resetAccess(user));
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("arangos")
    void grantDefaultCollectionAccess(ArangoDB arangoDB) {
        String user = "user-" + rnd();
        arangoDB.createUser(user, "1234");
        arangoDB.db(TEST_DB).grantDefaultCollectionAccess(user, Permissions.RW);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void getPermissions(ArangoDatabase db) {
        assertThat(db.getPermissions("root")).isEqualTo(Permissions.RW);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void query(ArangoDatabase db) {
        for (int i = 0; i < 10; i++) {
            db.collection(CNAME1).insertDocument(new BaseDocument(), null);
        }
        final ArangoCursor<String> cursor = db.query("for i in " + CNAME1 + " return i._id", null, null, String.class);
        assertThat((Object) cursor).isNotNull();
        for (int i = 0; i < 10; i++, cursor.next()) {
            assertThat((Iterator<?>) cursor).hasNext();
        }
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void queryForEach(ArangoDatabase db) {
        for (int i = 0; i < 10; i++) {
            db.collection(CNAME1).insertDocument(new BaseDocument(), null);
        }
        final ArangoCursor<String> cursor = db.query("for i in " + CNAME1 + " return i._id", null, null, String.class);
        assertThat((Object) cursor).isNotNull();

        int i = 0;
        while (cursor.hasNext()) {
            cursor.next();
            i++;
        }
        assertThat(i).isGreaterThanOrEqualTo(10);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void queryWithCount(ArangoDatabase db) {
        for (int i = 0; i < 10; i++) {
            db.collection(CNAME1).insertDocument(new BaseDocument(), null);
        }

        final ArangoCursor<String> cursor = db
                .query("for i in " + CNAME1 + " Limit 6 return i._id", null, new AqlQueryOptions().count(true),
                        String.class);
        assertThat((Object) cursor).isNotNull();
        for (int i = 1; i <= 6; i++, cursor.next()) {
            assertThat(cursor.hasNext()).isTrue();
        }
        assertThat(cursor.getCount()).isEqualTo(6);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void queryWithLimitAndFullCount(ArangoDatabase db) {
        for (int i = 0; i < 10; i++) {
            db.collection(CNAME1).insertDocument(new BaseDocument(), null);
        }

        final ArangoCursor<String> cursor = db
                .query("for i in " + CNAME1 + " Limit 5 return i._id", null, new AqlQueryOptions().fullCount(true),
                        String.class);
        assertThat((Object) cursor).isNotNull();
        for (int i = 0; i < 5; i++, cursor.next()) {
            assertThat((Iterator<?>) cursor).hasNext();
        }
        assertThat(cursor.getStats()).isNotNull();
        assertThat(cursor.getStats().getFullCount()).isGreaterThanOrEqualTo(10L);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void queryStats(ArangoDatabase db) {
        for (int i = 0; i < 10; i++) {
            db.collection(CNAME1).insertDocument(new BaseDocument(), null);
        }

        final ArangoCursor<Object> cursor = db.query("for i in " + CNAME1 + " return i", Object.class);
        assertThat((Object) cursor).isNotNull();
        for (int i = 0; i < 5; i++, cursor.next()) {
            assertThat((Iterator<?>) cursor).hasNext();
        }
        assertThat(cursor.getStats()).isNotNull();
        assertThat(cursor.getStats().getWritesExecuted()).isNotNull();
        assertThat(cursor.getStats().getWritesIgnored()).isNotNull();
        assertThat(cursor.getStats().getScannedFull()).isNotNull();
        assertThat(cursor.getStats().getScannedIndex()).isNotNull();
        assertThat(cursor.getStats().getFiltered()).isNotNull();
        assertThat(cursor.getStats().getExecutionTime()).isNotNull();
        assertThat(cursor.getStats().getPeakMemoryUsage()).isNotNull();
        if (isAtLeastVersion(3, 10)) {
            assertThat(cursor.getStats().getCursorsCreated()).isNotNull();
            assertThat(cursor.getStats().getCursorsRearmed()).isNotNull();
            assertThat(cursor.getStats().getCacheHits()).isNotNull();
            assertThat(cursor.getStats().getCacheMisses()).isNotNull();
        }
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void queryWithBatchSize(ArangoDatabase db) {
        for (int i = 0; i < 10; i++) {
            db.collection(CNAME1).insertDocument(new BaseDocument(), null);
        }

        final ArangoCursor<String> cursor = db
                .query("for i in " + CNAME1 + " return i._id", null, new AqlQueryOptions().batchSize(5).count(true),
                        String.class);

        assertThat((Object) cursor).isNotNull();
        for (int i = 0; i < 10; i++, cursor.next()) {
            assertThat((Iterator<?>) cursor).hasNext();
        }
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void queryIterateWithBatchSize(ArangoDatabase db) {
        for (int i = 0; i < 10; i++) {
            db.collection(CNAME1).insertDocument(new BaseDocument(), null);
        }

        final ArangoCursor<String> cursor = db
                .query("for i in " + CNAME1 + " return i._id", null, new AqlQueryOptions().batchSize(5).count(true),
                        String.class);

        assertThat((Object) cursor).isNotNull();
        final AtomicInteger i = new AtomicInteger(0);
        for (; cursor.hasNext(); cursor.next()) {
            i.incrementAndGet();
        }
        assertThat(i.get()).isGreaterThanOrEqualTo(10);
    }

    // FIXME
//    /**
//     * ignored. takes to long
//     */
//    @Test
//    @Ignore
//     void queryWithTTL() throws InterruptedException {
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
//            assertThat(cursor).isNotNull();
//
//            for (int i = 0; i < 10; i++, cursor.next()) {
//                assertThat(cursor.hasNext()).isEqualTo(true));
//                if (i == 1) {
//                    Thread.sleep(wait * 1000);
//                }
//            }
//            fail("this should fail");
//        } catch (final ArangoDBException ex) {
//            assertThat(ex.getMessage()).isEqualTo("Response: 404, Error: 1600 - cursor not found"));
//        } finally {
//            db.collection(COLLECTION_NAME).drop();
//        }
//    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void changeQueryCache(ArangoDatabase db) {
        QueryCachePropertiesEntity properties = db.getQueryCacheProperties();
        assertThat(properties).isNotNull();
        assertThat(properties.getMode()).isEqualTo(CacheMode.off);
        assertThat(properties.getMaxResults()).isPositive();

        properties.setMode(CacheMode.on);
        properties = db.setQueryCacheProperties(properties);
        assertThat(properties).isNotNull();
        assertThat(properties.getMode()).isEqualTo(CacheMode.on);

        properties = db.getQueryCacheProperties();
        assertThat(properties.getMode()).isEqualTo(CacheMode.on);

        final QueryCachePropertiesEntity properties2 = new QueryCachePropertiesEntity();
        properties2.setMode(CacheMode.off);
        db.setQueryCacheProperties(properties2);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void queryWithCache(ArangoDatabase db) {
        assumeTrue(isSingleServer());
        for (int i = 0; i < 10; i++) {
            db.collection(CNAME1).insertDocument(new BaseDocument(), null);
        }

        final QueryCachePropertiesEntity properties = new QueryCachePropertiesEntity();
        properties.setMode(CacheMode.on);
        db.setQueryCacheProperties(properties);

        final ArangoCursor<String> cursor = db
                .query("FOR t IN " + CNAME1 + " FILTER t.age >= 10 SORT t.age RETURN t._id", null,
                        new AqlQueryOptions().cache(true), String.class);

        assertThat((Object) cursor).isNotNull();
        assertThat(cursor.isCached()).isFalse();

        final ArangoCursor<String> cachedCursor = db
                .query("FOR t IN " + CNAME1 + " FILTER t.age >= 10 SORT t.age RETURN t._id", null,
                        new AqlQueryOptions().cache(true), String.class);

        assertThat((Object) cachedCursor).isNotNull();
        assertThat(cachedCursor.isCached()).isTrue();

        final QueryCachePropertiesEntity properties2 = new QueryCachePropertiesEntity();
        properties2.setMode(CacheMode.off);
        db.setQueryCacheProperties(properties2);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void queryWithMemoryLimit(ArangoDatabase db) {
        Throwable thrown = catchThrowable(() -> db.query("RETURN 1..100000", null,
                new AqlQueryOptions().memoryLimit(32 * 1024L), String.class));
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
        assertThat(((ArangoDBException) thrown).getErrorNum()).isEqualTo(32);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void queryWithFailOnWarningTrue(ArangoDatabase db) {
        Throwable thrown = catchThrowable(() -> db.query("RETURN 1 / 0", null,
                new AqlQueryOptions().failOnWarning(true), String.class));
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void queryWithFailOnWarningFalse(ArangoDatabase db) {
        final ArangoCursor<String> cursor = db
                .query("RETURN 1 / 0", null, new AqlQueryOptions().failOnWarning(false), String.class);
        assertThat(cursor.next()).isNull();
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void queryWithTimeout(ArangoDatabase db) {
        assumeTrue(isAtLeastVersion(3, 6));
        Throwable thrown = catchThrowable(() -> db.query("RETURN SLEEP(1)", null,
                new AqlQueryOptions().maxRuntime(0.1), String.class).next());
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
        assertThat(((ArangoDBException) thrown).getResponseCode()).isEqualTo(410);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void queryWithMaxWarningCount(ArangoDatabase db) {
        final ArangoCursor<String> cursorWithWarnings = db
                .query("RETURN 1 / 0", null, new AqlQueryOptions(), String.class);
        assertThat(cursorWithWarnings.getWarnings()).hasSize(1);
        final ArangoCursor<String> cursorWithLimitedWarnings = db
                .query("RETURN 1 / 0", null, new AqlQueryOptions().maxWarningCount(0L), String.class);
        final Collection<Warning> warnings = cursorWithLimitedWarnings.getWarnings();
        assertThat(warnings).isNullOrEmpty();
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void queryCursor(ArangoDatabase db) {
        final int numbDocs = 10;
        for (int i = 0; i < numbDocs; i++) {
            db.collection(CNAME1).insertDocument(new BaseDocument(), null);
        }

        final int batchSize = 5;
        final ArangoCursor<String> cursor = db.query("for i in " + CNAME1 + " return i._id", null,
                new AqlQueryOptions().batchSize(batchSize).count(true), String.class);
        assertThat((Object) cursor).isNotNull();
        assertThat(cursor.getCount()).isGreaterThanOrEqualTo(numbDocs);

        final ArangoCursor<String> cursor2 = db.cursor(cursor.getId(), String.class);
        assertThat((Object) cursor2).isNotNull();
        assertThat(cursor2.getCount()).isGreaterThanOrEqualTo(numbDocs);
        assertThat((Iterator<?>) cursor2).hasNext();

        for (int i = 0; i < batchSize; i++, cursor.next()) {
            assertThat((Iterator<?>) cursor).hasNext();
        }
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void changeQueryTrackingProperties(ArangoDatabase db) {
        try {
            QueryTrackingPropertiesEntity properties = db.getQueryTrackingProperties();
            assertThat(properties).isNotNull();
            assertThat(properties.getEnabled()).isTrue();
            assertThat(properties.getTrackSlowQueries()).isTrue();
            assertThat(properties.getMaxQueryStringLength()).isPositive();
            assertThat(properties.getMaxSlowQueries()).isPositive();
            assertThat(properties.getSlowQueryThreshold()).isPositive();
            properties.setEnabled(false);
            properties = db.setQueryTrackingProperties(properties);
            assertThat(properties).isNotNull();
            assertThat(properties.getEnabled()).isFalse();
            properties = db.getQueryTrackingProperties();
            assertThat(properties.getEnabled()).isFalse();
        } finally {
            final QueryTrackingPropertiesEntity properties = new QueryTrackingPropertiesEntity();
            properties.setEnabled(true);
            db.setQueryTrackingProperties(properties);
        }
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void queryWithBindVars(ArangoDatabase db) {
        for (int i = 0; i < 10; i++) {
            final BaseDocument baseDocument = new BaseDocument();
            baseDocument.addAttribute("age", 20 + i);
            db.collection(CNAME1).insertDocument(baseDocument, null);
        }
        final Map<String, Object> bindVars = new HashMap<>();
        bindVars.put("@coll", CNAME1);
        bindVars.put("age", 25);

        final ArangoCursor<String> cursor = db
                .query("FOR t IN @@coll FILTER t.age >= @age SORT t.age RETURN t._id", bindVars, null,
                        String.class);

        assertThat((Object) cursor).isNotNull();

        for (int i = 0; i < 5; i++, cursor.next()) {
            assertThat((Iterator<?>) cursor).hasNext();
        }
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("arangos")
    void queryWithWarning(ArangoDB arangoDB) {
        final ArangoCursor<String> cursor = arangoDB.db().query("return 1/0", null, null, String.class);

        assertThat((Object) cursor).isNotNull();
        assertThat(cursor.getWarnings()).isNotNull();
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void queryStream(ArangoDatabase db) {
        if (isAtLeastVersion(3, 4)) {
            final ArangoCursor<VPackSlice> cursor = db
                    .query("FOR i IN 1..2 RETURN i", null, new AqlQueryOptions().stream(true).count(true),
                            VPackSlice.class);
            assertThat((Object) cursor).isNotNull();
            assertThat(cursor.getCount()).isNull();
        }
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("arangos")
    void queryClose(ArangoDB arangoDB) throws IOException {
        final ArangoCursor<String> cursor = arangoDB.db()
                .query("for i in 1..2 return i", null, new AqlQueryOptions().batchSize(1), String.class);
        cursor.close();
        AtomicInteger count = new AtomicInteger();
        Throwable thrown = catchThrowable(() -> {
            while (cursor.hasNext()) {
                cursor.next();
                count.incrementAndGet();
            }
        });

        assertThat(thrown).isInstanceOf(ArangoDBException.class);
        assertThat(count).hasValue(1);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void queryNoResults(ArangoDatabase db) throws IOException {
        final ArangoCursor<BaseDocument> cursor = db
                .query("FOR i IN @@col RETURN i", new MapBuilder().put("@col", CNAME1).get(), null,
                        BaseDocument.class);
        cursor.close();
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void queryWithNullBindParam(ArangoDatabase db) throws IOException {
        final ArangoCursor<BaseDocument> cursor = db.query("FOR i IN @@col FILTER i.test == @test RETURN i",
                new MapBuilder().put("@col", CNAME1).put("test", null).get(), null, BaseDocument.class);
        cursor.close();
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void queryAllowDirtyRead(ArangoDatabase db) throws IOException {
        final ArangoCursor<BaseDocument> cursor = db.query("FOR i IN @@col FILTER i.test == @test RETURN i",
                new MapBuilder().put("@col", CNAME1).put("test", null).get(),
                new AqlQueryOptions().allowDirtyRead(true), BaseDocument.class);
        if (isAtLeastVersion(3, 10)) {
            assertThat(cursor.isPotentialDirtyRead()).isTrue();
        }
        cursor.close();
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void explainQuery(ArangoDatabase db) {
        final AqlExecutionExplainEntity explain = db.explainQuery("for i in 1..1 return i", null, null);
        assertThat(explain).isNotNull();
        assertThat(explain.getPlan()).isNotNull();
        assertThat(explain.getPlans()).isNull();
        final ExecutionPlan plan = explain.getPlan();
        assertThat(plan.getCollections()).isEmpty();
        assertThat(plan.getEstimatedCost()).isPositive();
        assertThat(plan.getEstimatedNrItems()).isPositive();
        assertThat(plan.getVariables()).hasSize(2);
        assertThat(plan.getNodes()).isNotEmpty();
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void explainQueryWithIndexNode(ArangoDatabase db) {
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
                    assertThat(it.getType()).isEqualTo(IndexType.primary);
                    assertThat(it.getFields()).contains("_key");
                });
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void parseQuery(ArangoDatabase db) {
        final AqlParseEntity parse = db.parseQuery("for i in 1..1 return i");
        assertThat(parse).isNotNull();
        assertThat(parse.getBindVars()).isEmpty();
        assertThat(parse.getCollections()).isEmpty();
        assertThat(parse.getAst()).hasSize(1);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void getCurrentlyRunningQueries(ArangoDatabase db) throws InterruptedException {
        String query = "return sleep(1)";
        Thread t = new Thread(() -> db.query(query, null, null, Void.class));
        t.start();
        Thread.sleep(300);
        final Collection<QueryEntity> currentlyRunningQueries = db.getCurrentlyRunningQueries();
        assertThat(currentlyRunningQueries).hasSize(1);
        final QueryEntity queryEntity = currentlyRunningQueries.iterator().next();
        assertThat(queryEntity.getQuery()).isEqualTo(query);
        assertThat(queryEntity.getState()).isEqualTo(QueryExecutionState.EXECUTING);
        t.join();
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void killQuery(ArangoDatabase db) throws InterruptedException, ExecutionException {
        ExecutorService es = Executors.newSingleThreadExecutor();
        Future<?> future = es.submit(() -> {
            try {
                db.query("return sleep(5)", null, null, Void.class);
                fail();
            } catch (ArangoDBException e) {
                assertThat(e.getResponseCode()).isEqualTo(410);
                assertThat(e.getErrorNum()).isEqualTo(1500);
                assertThat(e.getErrorMessage()).contains("query killed");
            }
        });
        Thread.sleep(500);

        Collection<QueryEntity> currentlyRunningQueries = db.getCurrentlyRunningQueries();
        assertThat(currentlyRunningQueries).hasSize(1);
        QueryEntity queryEntity = currentlyRunningQueries.iterator().next();
        assertThat(queryEntity.getState()).isEqualTo(QueryExecutionState.EXECUTING);
        db.killQuery(queryEntity.getId());

        db.getCurrentlyRunningQueries().forEach(q ->
                assertThat(q.getState()).isEqualTo(QueryExecutionState.KILLED)
        );

        future.get();
        es.shutdown();
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void getAndClearSlowQueries(ArangoDatabase db) {
        db.clearSlowQueries();

        final QueryTrackingPropertiesEntity properties = db.getQueryTrackingProperties();
        final Long slowQueryThreshold = properties.getSlowQueryThreshold();
        properties.setSlowQueryThreshold(1L);
        db.setQueryTrackingProperties(properties);

        db.query("return sleep(1.1)", null, null, Void.class);
        final Collection<QueryEntity> slowQueries = db.getSlowQueries();
        assertThat(slowQueries).hasSize(1);
        final QueryEntity queryEntity = slowQueries.iterator().next();
        assertThat(queryEntity.getQuery()).isEqualTo("return sleep(1.1)");

        db.clearSlowQueries();
        assertThat(db.getSlowQueries()).isEmpty();
        properties.setSlowQueryThreshold(slowQueryThreshold);
        db.setQueryTrackingProperties(properties);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void createGetDeleteAqlFunction(ArangoDatabase db) {
        final Collection<AqlFunctionEntity> aqlFunctionsInitial = db.getAqlFunctions(null);
        assertThat(aqlFunctionsInitial).isEmpty();
        try {
            db.createAqlFunction("myfunctions::temperature::celsiustofahrenheit",
                    "function (celsius) { return celsius * 1.8 + 32; }", null);

            final Collection<AqlFunctionEntity> aqlFunctions = db.getAqlFunctions(null);
            assertThat(aqlFunctions).hasSizeGreaterThan(aqlFunctionsInitial.size());
        } finally {
            final Integer deleteCount = db.deleteAqlFunction("myfunctions::temperature::celsiustofahrenheit", null);
            // compatibility with ArangoDB < 3.4
            if (isAtLeastVersion(3, 4)) {
                assertThat(deleteCount).isEqualTo(1);
            } else {
                assertThat(deleteCount).isNull();
            }
            final Collection<AqlFunctionEntity> aqlFunctions = db.getAqlFunctions(null);
            assertThat(aqlFunctions).hasSize(aqlFunctionsInitial.size());
        }
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void createGetDeleteAqlFunctionWithNamespace(ArangoDatabase db) {
        final Collection<AqlFunctionEntity> aqlFunctionsInitial = db.getAqlFunctions(null);
        assertThat(aqlFunctionsInitial).isEmpty();
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
                assertThat(deleteCount).isEqualTo(2);
            } else {
                assertThat(deleteCount).isNull();
            }
            final Collection<AqlFunctionEntity> aqlFunctions = db.getAqlFunctions(null);
            assertThat(aqlFunctions).hasSize(aqlFunctionsInitial.size());
        }
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void createGraph(ArangoDatabase db) {
        String name = "graph-" + rnd();
        final GraphEntity result = db.createGraph(name, null, null);
        assertThat(result.getName()).isEqualTo(name);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void createGraphSatellite(ArangoDatabase db) {
        assumeTrue(isAtLeastVersion(3, 7));
        assumeTrue(isCluster());
        assumeTrue(isEnterprise());

        String name = "graph-" + rnd();
        final GraphEntity result = db.createGraph(name, null, new GraphCreateOptions().satellite(true));
        assertThat(result.getSatellite()).isTrue();

        GraphEntity info = db.graph(name).getInfo();
        assertThat(info.getSatellite()).isTrue();

        GraphEntity graph = db.getGraphs().stream().filter(g -> name.equals(g.getName())).findFirst().get();
        assertThat(graph.getSatellite()).isTrue();
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void createGraphReplicationFaktor(ArangoDatabase db) {
        assumeTrue(isCluster());
        String name = "graph-" + rnd();
        final String edgeCollection = "edge-" + rnd();
        final String fromCollection = "from-" + rnd();
        final String toCollection = "to-" + rnd();
        final Collection<EdgeDefinition> edgeDefinitions =
                Collections.singletonList(new EdgeDefinition().collection(edgeCollection).from(fromCollection).to(toCollection));
        final GraphEntity result = db.createGraph(name, edgeDefinitions, new GraphCreateOptions().replicationFactor(2));
        assertThat(result).isNotNull();
        for (final String collection : Arrays.asList(edgeCollection, fromCollection, toCollection)) {
            final CollectionPropertiesEntity properties = db.collection(collection).getProperties();
            assertThat(properties.getReplicationFactor()).isEqualTo(2);
        }
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void createGraphNumberOfShards(ArangoDatabase db) {
        assumeTrue(isCluster());
        String name = "graph-" + rnd();
        final String edgeCollection = "edge-" + rnd();
        final String fromCollection = "from-" + rnd();
        final String toCollection = "to-" + rnd();
        final Collection<EdgeDefinition> edgeDefinitions =
                Collections.singletonList(new EdgeDefinition().collection(edgeCollection).from(fromCollection).to(toCollection));
        final GraphEntity result = db
                .createGraph(name, edgeDefinitions, new GraphCreateOptions().numberOfShards(2));
        assertThat(result).isNotNull();
        for (final String collection : Arrays.asList(edgeCollection, fromCollection, toCollection)) {
            final CollectionPropertiesEntity properties = db.collection(collection).getProperties();
            assertThat(properties.getNumberOfShards()).isEqualTo(2);
        }
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void getGraphs(ArangoDatabase db) {
        String name = "graph-" + rnd();
        db.createGraph(name, null, null);
        final Collection<GraphEntity> graphs = db.getGraphs();
        assertThat(graphs).hasSizeGreaterThanOrEqualTo(1);
        long count = graphs.stream().map(GraphEntity::getName).filter(name::equals).count();
        assertThat(count).isEqualTo(1L);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void transactionString(ArangoDatabase db) {
        final TransactionOptions options = new TransactionOptions().params("test");
        final String result = db.transaction("function (params) {return params;}", String.class, options);
        assertThat(result).isEqualTo("test");
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void transactionNumber(ArangoDatabase db) {
        final TransactionOptions options = new TransactionOptions().params(5);
        final Integer result = db.transaction("function (params) {return params;}", Integer.class, options);
        assertThat(result).isEqualTo(5);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void transactionVPack(ArangoDatabase db) throws VPackException {
        final TransactionOptions options = new TransactionOptions().params(new VPackBuilder().add("test").slice());
        final VPackSlice result = db.transaction("function (params) {return params;}", VPackSlice.class, options);
        assertThat(result.isString()).isTrue();
        assertThat(result.getAsString()).isEqualTo("test");
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void transactionVPackObject(ArangoDatabase db) throws VPackException {
        final VPackSlice params = new VPackBuilder().add(ValueType.OBJECT).add("foo", "hello").add("bar", "world")
                .close().slice();
        final TransactionOptions options = new TransactionOptions().params(params);
        final String result = db
                .transaction("function (params) { return params['foo'] + ' ' + params['bar'];}", String.class, options);
        assertThat(result).isEqualTo("hello world");
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void transactionVPackArray(ArangoDatabase db) throws VPackException {
        final VPackSlice params = new VPackBuilder().add(ValueType.ARRAY).add("hello").add("world").close().slice();
        final TransactionOptions options = new TransactionOptions().params(params);
        final String result = db
                .transaction("function (params) { return params[0] + ' ' + params[1];}", String.class, options);
        assertThat(result).isEqualTo("hello world");
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void transactionMap(ArangoDatabase db) {
        final Map<String, Object> params = new MapBuilder().put("foo", "hello").put("bar", "world").get();
        final TransactionOptions options = new TransactionOptions().params(params);
        final String result = db
                .transaction("function (params) { return params['foo'] + ' ' + params['bar'];}", String.class, options);
        assertThat(result).isEqualTo("hello world");
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void transactionArray(ArangoDatabase db) {
        final String[] params = new String[]{"hello", "world"};
        final TransactionOptions options = new TransactionOptions().params(params);
        final String result = db
                .transaction("function (params) { return params[0] + ' ' + params[1];}", String.class, options);
        assertThat(result).isEqualTo("hello world");
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void transactionCollection(ArangoDatabase db) {
        final Collection<String> params = new ArrayList<>();
        params.add("hello");
        params.add("world");
        final TransactionOptions options = new TransactionOptions().params(params);
        final String result = db
                .transaction("function (params) { return params[0] + ' ' + params[1];}", String.class, options);
        assertThat(result).isEqualTo("hello world");
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void transactionInsertJson(ArangoDatabase db) {
        String key = "key-" + rnd();
        final TransactionOptions options = new TransactionOptions().params("{\"_key\":\"" + key + "\"}")
                .writeCollections(CNAME1);
        db.transaction("function (params) { "
                + "var db = require('internal').db;"
                + "db." + CNAME1 + ".save(JSON.parse(params));"
                + "}", Void.class, options);
        assertThat(db.collection(CNAME1).getDocument(key, String.class)).isNotNull();
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void transactionExclusiveWrite(ArangoDatabase db) {
        assumeTrue(isAtLeastVersion(3, 4));
        String key = "key-" + rnd();
        final TransactionOptions options = new TransactionOptions().params("{\"_key\":\"" + key + "\"}")
                .exclusiveCollections(CNAME1);
        db.transaction("function (params) { "
                + "var db = require('internal').db;"
                + "db." + CNAME1 + ".save(JSON.parse(params));"
                + "}", Void.class, options);
        assertThat(db.collection(CNAME1).getDocument(key, String.class)).isNotNull();
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void transactionEmpty(ArangoDatabase db) {
        db.transaction("function () {}", null, null);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void transactionAllowImplicit(ArangoDatabase db) {
        final String action = "function (params) {" + "var db = require('internal').db;"
                + "return {'a':db." + CNAME1 + ".all().toArray()[0], 'b':db." + CNAME2 + ".all().toArray()[0]};"
                + "}";
        final TransactionOptions options = new TransactionOptions().readCollections(CNAME1);
        db.transaction(action, VPackSlice.class, options);
        options.allowImplicit(false);
        Throwable thrown = catchThrowable(() -> db.transaction(action, VPackSlice.class, options));
        assertThat(thrown)
                .isInstanceOf(ArangoDBException.class)
                .extracting(it -> ((ArangoDBException) it).getResponseCode())
                .isEqualTo(400);
    }

    static class TransactionTestEntity {
        private String value;

        TransactionTestEntity() {
            super();
        }
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void transactionPojoReturn(ArangoDatabase db) {
        final String action = "function() { return {'value':'hello world'}; }";
        final TransactionTestEntity res = db.transaction(action, TransactionTestEntity.class, new TransactionOptions());
        assertThat(res).isNotNull();
        assertThat(res.value).isEqualTo("hello world");
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void getInfo(ArangoDatabase db) {
        final DatabaseEntity info = db.getInfo();
        assertThat(info).isNotNull();
        assertThat(info.getId()).isNotNull();
        assertThat(info.getName()).isEqualTo(TEST_DB.get());
        assertThat(info.getPath()).isNotNull();
        assertThat(info.getIsSystem()).isFalse();

        if (isAtLeastVersion(3, 6) && isCluster()) {
            assertThat(info.getSharding()).isNotNull();
            assertThat(info.getWriteConcern()).isNotNull();
            assertThat(info.getReplicationFactor()).isNotNull();
        }
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void executeTraversal(ArangoDatabase db) {
        String k1 = "key-" + rnd();
        String k2 = "key-" + rnd();
        String k3 = "key-" + rnd();
        String k4 = "key-" + rnd();
        String k5 = "key-" + rnd();

        for (final String e : new String[]{
                k1, k2, k3, k4, k5
        }) {
            db.collection(CNAME1).insertDocument(new BaseDocument(e), null);
        }
        for (final String[] e : new String[][]{
                new String[]{k1, k2}, new String[]{k2, k3},
                new String[]{k2, k4}, new String[]{k5, k1}, new String[]{k5, k2}
        }) {
            final BaseEdgeDocument edge = new BaseEdgeDocument();
            edge.setKey(e[0] + "_knows_" + e[1]);
            edge.setFrom(CNAME1 + "/" + e[0]);
            edge.setTo(CNAME1 + "/" + e[1]);
            db.collection(ENAMES).insertDocument(edge, null);
        }

        final TraversalOptions options =
                new TraversalOptions().edgeCollection(ENAMES).startVertex(CNAME1 + "/" + k1).direction(Direction.outbound);
        final TraversalEntity<BaseDocument, BaseEdgeDocument> traversal = db.executeTraversal(BaseDocument.class,
                BaseEdgeDocument.class, options);
        assertThat(traversal).isNotNull();

        final Collection<BaseDocument> vertices = traversal.getVertices();
        assertThat(vertices).hasSize(4);

        final Iterator<BaseDocument> verticesIterator = vertices.iterator();
        final Collection<String> v = Arrays.asList(k1, k2, k3, k4);
        while (verticesIterator.hasNext()) {
            assertThat(v).contains(verticesIterator.next().getKey());
        }

        final Collection<PathEntity<BaseDocument, BaseEdgeDocument>> paths = traversal.getPaths();
        assertThat(paths).hasSize(4);
        final PathEntity<BaseDocument, BaseEdgeDocument> first = paths.iterator().next();
        assertThat(first.getEdges()).isEmpty();
        assertThat(first.getVertices()).hasSize(1);
        assertThat(first.getVertices().iterator().next().getKey()).isEqualTo(k1);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void getDocument(ArangoDatabase db) {
        String key = "key-" + rnd();
        final BaseDocument value = new BaseDocument(key);
        db.collection(CNAME1).insertDocument(value);
        final BaseDocument document = db.getDocument(CNAME1 + "/" + key, BaseDocument.class);
        assertThat(document).isNotNull();
        assertThat(document.getKey()).isEqualTo(key);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void shouldIncludeExceptionMessage(ArangoDatabase db) {
        assumeTrue(isAtLeastVersion(3, 4));

        final String exceptionMessage = "My error context";
        final String action = "function (params) {" + "throw '" + exceptionMessage + "';" + "}";
        try {
            db.transaction(action, VPackSlice.class, null);
            fail();
        } catch (final ArangoDBException e) {
            assertThat(e.getErrorMessage()).isEqualTo(exceptionMessage);
        }
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void getDocumentWrongId(ArangoDatabase db) {
        Throwable thrown = catchThrowable(() -> db.getDocument("123", BaseDocument.class));
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void reloadRouting(ArangoDatabase db) {
        db.reloadRouting();
    }
}
