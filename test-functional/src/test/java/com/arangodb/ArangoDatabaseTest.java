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
import com.arangodb.entity.QueryCachePropertiesEntity.CacheMode;
import com.arangodb.model.*;
import com.arangodb.util.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
import static org.assertj.core.api.InstanceOfAssertFactories.*;


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

    @ParameterizedTest
    @MethodSource("dbs")
    void getVersion(ArangoDatabase db) {
        final ArangoDBVersion version = db.getVersion();
        assertThat(version).isNotNull();
        assertThat(version.getServer()).isNotNull();
        assertThat(version.getVersion()).isNotNull();
    }

    @ParameterizedTest
    @MethodSource("dbs")
    void getEngine(ArangoDatabase db) {
        final ArangoDBEngine engine = db.getEngine();
        assertThat(engine).isNotNull();
        assertThat(engine.getName()).isNotNull();
    }

    @ParameterizedTest
    @MethodSource("arangos")
    void exists(ArangoDB arangoDB) {
        assertThat(arangoDB.db(getTestDb()).exists()).isTrue();
        assertThat(arangoDB.db("no").exists()).isFalse();
    }

    @ParameterizedTest
    @MethodSource("dbs")
    void getAccessibleDatabases(ArangoDatabase db) {
        final Collection<String> dbs = db.getAccessibleDatabases();
        assertThat(dbs).contains("_system");
    }

    @ParameterizedTest
    @MethodSource("dbs")
    void createCollection(ArangoDatabase db) {
        String name = rndName();
        final CollectionEntity result = db.createCollection(name, null);
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
    }

    @ParameterizedTest
    @MethodSource("dbs")
    void createCollectionWithNotNormalizedName(ArangoDatabase db) {
        assumeTrue(supportsExtendedNames());
        final String colName = "testCol-\u006E\u0303\u00f1";

        Throwable thrown = catchThrowable(() -> db.createCollection(colName));
        assertThat(thrown)
                .isInstanceOf(ArangoDBException.class)
                .hasMessageContaining("normalized")
                .extracting(it -> ((ArangoDBException) it).getResponseCode()).isEqualTo(400);
    }

    @ParameterizedTest
    @MethodSource("dbs")
    void createCollectionWithReplicationFactor(ArangoDatabase db) {
        assumeTrue(isCluster());
        String name = rndName();
        final CollectionEntity result = db
                .createCollection(name, new CollectionCreateOptions().replicationFactor(2));
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        CollectionPropertiesEntity props = db.collection(name).getProperties();
        assertThat(props.getReplicationFactor().get()).isEqualTo(2);
    }

    @ParameterizedTest
    @MethodSource("dbs")
    void createCollectionWithWriteConcern(ArangoDatabase db) {
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isCluster());

        String name = rndName();
        final CollectionEntity result = db.createCollection(name,
                new CollectionCreateOptions().replicationFactor(2).writeConcern(2));
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        CollectionPropertiesEntity props = db.collection(name).getProperties();
        assertThat(props.getReplicationFactor().get()).isEqualTo(2);
        assertThat(props.getWriteConcern()).isEqualTo(2);
    }

    @ParameterizedTest
    @MethodSource("dbs")
    void createSatelliteCollection(ArangoDatabase db) {
        assumeTrue(isEnterprise());
        assumeTrue(isCluster());

        String name = rndName();
        final CollectionEntity result = db
                .createCollection(name, new CollectionCreateOptions().replicationFactor(ReplicationFactor.ofSatellite()));

        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        CollectionPropertiesEntity props = db.collection(name).getProperties();
        assertThat(props.getReplicationFactor()).isEqualTo(ReplicationFactor.ofSatellite());
    }

    @ParameterizedTest
    @MethodSource("dbs")
    void createCollectionWithNumberOfShards(ArangoDatabase db) {
        assumeTrue(isCluster());
        String name = rndName();
        final CollectionEntity result = db
                .createCollection(name, new CollectionCreateOptions().numberOfShards(2));

        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        CollectionPropertiesEntity props = db.collection(name).getProperties();
        assertThat(props.getNumberOfShards()).isEqualTo(2);
    }

    @ParameterizedTest
    @MethodSource("dbs")
    void createCollectionWithShardingStrategys(ArangoDatabase db) {
        assumeTrue(isAtLeastVersion(3, 4));
        assumeTrue(isCluster());

        String name = rndName();
        final CollectionEntity result = db.createCollection(name, new CollectionCreateOptions()
                .shardingStrategy(ShardingStrategy.COMMUNITY_COMPAT.getInternalName()));

        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        CollectionPropertiesEntity props = db.collection(name).getProperties();
        assertThat(props.getShardingStrategy()).isEqualTo(ShardingStrategy.COMMUNITY_COMPAT.getInternalName());
    }

    @ParameterizedTest
    @MethodSource("dbs")
    void createCollectionWithSmartJoinAttribute(ArangoDatabase db) {
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isEnterprise());
        assumeTrue(isCluster());

        String fooName = rndName();
        db.collection(fooName).create();

        String name = rndName();
        final CollectionEntity result = db.createCollection(name,
                new CollectionCreateOptions().smartJoinAttribute("test123").distributeShardsLike(fooName).shardKeys("_key:"));
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(db.collection(name).getProperties().getSmartJoinAttribute()).isEqualTo("test123");
    }

    @ParameterizedTest
    @MethodSource("dbs")
    void createCollectionWithSmartJoinAttributeWrong(ArangoDatabase db) {
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isEnterprise());
        assumeTrue(isCluster());

        String name = rndName();

        try {
            db.createCollection(name, new CollectionCreateOptions().smartJoinAttribute("test123"));
        } catch (ArangoDBException e) {
            assertThat(e.getErrorNum()).isEqualTo(4006);
            assertThat(e.getResponseCode()).isEqualTo(400);
        }
    }

    @ParameterizedTest
    @MethodSource("dbs")
    void createCollectionWithNumberOfShardsAndShardKey(ArangoDatabase db) {
        assumeTrue(isCluster());

        String name = rndName();
        final CollectionEntity result = db
                .createCollection(name, new CollectionCreateOptions().numberOfShards(2).shardKeys("a"));
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        final CollectionPropertiesEntity properties = db.collection(name).getProperties();
        assertThat(properties.getNumberOfShards()).isEqualTo(2);
        assertThat(properties.getShardKeys()).hasSize(1);
    }

    @ParameterizedTest
    @MethodSource("dbs")
    void createCollectionWithNumberOfShardsAndShardKeys(ArangoDatabase db) {
        assumeTrue(isCluster());
        String name = rndName();
        final CollectionEntity result = db.createCollection(name,
                new CollectionCreateOptions().numberOfShards(2).shardKeys("a", "b"));
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        final CollectionPropertiesEntity properties = db.collection(name).getProperties();
        assertThat(properties.getNumberOfShards()).isEqualTo(2);
        assertThat(properties.getShardKeys()).hasSize(2);
    }

    @ParameterizedTest
    @MethodSource("dbs")
    void createCollectionWithDistributeShardsLike(ArangoDatabase db) {
        assumeTrue(isEnterprise());
        assumeTrue(isCluster());

        final Integer numberOfShards = 3;

        String name1 = rndName();
        String name2 = rndName();
        db.createCollection(name1, new CollectionCreateOptions().numberOfShards(numberOfShards));
        db.createCollection(name2, new CollectionCreateOptions().distributeShardsLike(name1));

        assertThat(db.collection(name1).getProperties().getNumberOfShards()).isEqualTo(numberOfShards);
        assertThat(db.collection(name2).getProperties().getNumberOfShards()).isEqualTo(numberOfShards);
    }

    private void createCollectionWithKeyType(ArangoDatabase db, KeyType keyType) {
        String name = rndName();
        db.createCollection(name, new CollectionCreateOptions().keyOptions(
                false,
                keyType,
                null,
                null
        ));
        assertThat(db.collection(name).getProperties().getKeyOptions().getType()).isEqualTo(keyType);
    }

    @ParameterizedTest
    @MethodSource("dbs")
    void createCollectionWithKeyTypeAutoincrement(ArangoDatabase db) {
        assumeTrue(isSingleServer());
        createCollectionWithKeyType(db, KeyType.autoincrement);
    }

    @ParameterizedTest
    @MethodSource("dbs")
    void createCollectionWithKeyTypePadded(ArangoDatabase db) {
        assumeTrue(isAtLeastVersion(3, 4));
        createCollectionWithKeyType(db, KeyType.padded);
    }

    @ParameterizedTest
    @MethodSource("dbs")
    void createCollectionWithKeyTypeTraditional(ArangoDatabase db) {
        createCollectionWithKeyType(db, KeyType.traditional);
    }

    @ParameterizedTest
    @MethodSource("dbs")
    void createCollectionWithKeyTypeUuid(ArangoDatabase db) {
        assumeTrue(isAtLeastVersion(3, 4));
        createCollectionWithKeyType(db, KeyType.uuid);
    }

    @ParameterizedTest
    @MethodSource("dbs")
    void createCollectionWithJsonSchema(ArangoDatabase db) {
        assumeTrue(isAtLeastVersion(3, 7));
        String name = rndName();
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

        BaseDocument doc = new BaseDocument(UUID.randomUUID().toString());
        doc.addAttribute("number", 33);
        db.collection(name).insertDocument(doc);

        BaseDocument wrongDoc = new BaseDocument(UUID.randomUUID().toString());
        wrongDoc.addAttribute("number", "notANumber");
        Throwable thrown = catchThrowable(() -> db.collection(name).insertDocument(wrongDoc));
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
        ArangoDBException e = (ArangoDBException) thrown;

        assertThat(e).hasMessageContaining(message);
        assertThat(e.getResponseCode()).isEqualTo(400);
        assertThat(e.getErrorNum()).isEqualTo(1620);
    }

    @ParameterizedTest
    @MethodSource("dbs")
    void createCollectionWithComputedFields(ArangoDatabase db) {
        assumeTrue(isAtLeastVersion(3, 10));
        String cName = rndName();
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

    @ParameterizedTest
    @MethodSource("dbs")
    void deleteCollection(ArangoDatabase db) {
        String name = rndName();
        db.createCollection(name, null);
        db.collection(name).drop();
        Throwable thrown = catchThrowable(() -> db.collection(name).getInfo());
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
    }

    @ParameterizedTest
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

    @ParameterizedTest
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

    @ParameterizedTest
    @MethodSource("dbs")
    void getIndex(ArangoDatabase db) {
        final Collection<String> fields = Collections.singletonList("field-" + rnd());
        final IndexEntity createResult = db.collection(CNAME1).ensurePersistentIndex(fields, null);
        final IndexEntity readResult = db.getIndex(createResult.getId());
        assertThat(readResult.getId()).isEqualTo(createResult.getId());
        assertThat(readResult.getType()).isEqualTo(createResult.getType());
    }

    @ParameterizedTest
    @MethodSource("dbs")
    void deleteIndex(ArangoDatabase db) {
        final Collection<String> fields = Collections.singletonList("field-" + rnd());
        final IndexEntity createResult = db.collection(CNAME1).ensurePersistentIndex(fields, null);
        final String id = db.deleteIndex(createResult.getId());
        assertThat(id).isEqualTo(createResult.getId());
        try {
            db.getIndex(id);
            fail();
        } catch (final ArangoDBException e) {
            assertThat(e.getResponseCode()).isEqualTo(404);
        }
    }

    @ParameterizedTest
    @MethodSource("dbs")
    void getCollections(ArangoDatabase db) {
        final Collection<CollectionEntity> collections = db.getCollections(null);
        long count = collections.stream().map(CollectionEntity::getName).filter(it -> it.equals(CNAME1)).count();
        assertThat(count).isEqualTo(1L);
    }

    @ParameterizedTest
    @MethodSource("dbs")
    void getCollectionsExcludeSystem(ArangoDatabase db) {
        final CollectionsReadOptions options = new CollectionsReadOptions().excludeSystem(true);
        final Collection<CollectionEntity> nonSystemCollections = db.getCollections(options);
        final Collection<CollectionEntity> allCollections = db.getCollections(null);
        assertThat(allCollections).hasSizeGreaterThan(nonSystemCollections.size());
    }

    @ParameterizedTest
    @MethodSource("arangos")
    void grantAccess(ArangoDB arangoDB) {
        String user = "user-" + rnd();
        arangoDB.createUser(user, "1234", null);
        arangoDB.db(getTestDb()).grantAccess(user);
    }

    @ParameterizedTest
    @MethodSource("arangos")
    void grantAccessRW(ArangoDB arangoDB) {
        String user = "user-" + rnd();
        arangoDB.createUser(user, "1234", null);
        arangoDB.db(getTestDb()).grantAccess(user, Permissions.RW);
    }

    @ParameterizedTest
    @MethodSource("arangos")
    void grantAccessRO(ArangoDB arangoDB) {
        String user = "user-" + rnd();
        arangoDB.createUser(user, "1234", null);
        arangoDB.db(getTestDb()).grantAccess(user, Permissions.RO);
    }

    @ParameterizedTest
    @MethodSource("arangos")
    void grantAccessNONE(ArangoDB arangoDB) {
        String user = "user-" + rnd();
        arangoDB.createUser(user, "1234", null);
        arangoDB.db(getTestDb()).grantAccess(user, Permissions.NONE);
    }

    @ParameterizedTest
    @MethodSource("dbs")
    void grantAccessUserNotFound(ArangoDatabase db) {
        String user = "user-" + rnd();
        Throwable thrown = catchThrowable(() -> db.grantAccess(user, Permissions.RW));
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
    }

    @ParameterizedTest
    @MethodSource("arangos")
    void revokeAccess(ArangoDB arangoDB) {
        String user = "user-" + rnd();
        arangoDB.createUser(user, "1234", null);
        arangoDB.db(getTestDb()).revokeAccess(user);
    }

    @ParameterizedTest
    @MethodSource("dbs")
    void revokeAccessUserNotFound(ArangoDatabase db) {
        String user = "user-" + rnd();
        Throwable thrown = catchThrowable(() -> db.revokeAccess(user));
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
    }

    @ParameterizedTest
    @MethodSource("arangos")
    void resetAccess(ArangoDB arangoDB) {
        String user = "user-" + rnd();
        arangoDB.createUser(user, "1234", null);
        arangoDB.db(getTestDb()).resetAccess(user);
    }

    @ParameterizedTest
    @MethodSource("dbs")
    void resetAccessUserNotFound(ArangoDatabase db) {
        String user = "user-" + rnd();
        Throwable thrown = catchThrowable(() -> db.resetAccess(user));
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
    }

    @ParameterizedTest
    @MethodSource("arangos")
    void grantDefaultCollectionAccess(ArangoDB arangoDB) {
        String user = "user-" + rnd();
        arangoDB.createUser(user, "1234");
        arangoDB.db(getTestDb()).grantDefaultCollectionAccess(user, Permissions.RW);
    }

    @ParameterizedTest
    @MethodSource("dbs")
    void getPermissions(ArangoDatabase db) {
        assertThat(db.getPermissions("root")).isEqualTo(Permissions.RW);
    }

    @ParameterizedTest
    @MethodSource("dbs")
    void query(ArangoDatabase db) {
        for (int i = 0; i < 10; i++) {
            db.collection(CNAME1).insertDocument(new BaseDocument(), null);
        }
        final ArangoCursor<String> cursor = db.query("for i in " + CNAME1 + " return i._id", String.class);
        assertThat((Object) cursor).isNotNull();
        for (int i = 0; i < 10; i++, cursor.next()) {
            assertThat((Iterator<?>) cursor).hasNext();
        }
    }

    @ParameterizedTest
    @MethodSource("dbs")
    void queryWithNullBindVar(ArangoDatabase db) {
        final ArangoCursor<Object> cursor = db.query("return @foo", Object.class, Collections.singletonMap("foo", null));
        assertThat(cursor.hasNext()).isTrue();
        assertThat(cursor.next()).isNull();
    }

    @ParameterizedTest
    @MethodSource("dbs")
    void queryForEach(ArangoDatabase db) {
        for (int i = 0; i < 10; i++) {
            db.collection(CNAME1).insertDocument(new BaseDocument(), null);
        }
        final ArangoCursor<String> cursor = db.query("for i in " + CNAME1 + " return i._id", String.class);
        assertThat((Object) cursor).isNotNull();

        int i = 0;
        while (cursor.hasNext()) {
            cursor.next();
            i++;
        }
        assertThat(i).isGreaterThanOrEqualTo(10);
    }

    @ParameterizedTest
    @MethodSource("dbs")
    void queryWithCount(ArangoDatabase db) {
        for (int i = 0; i < 10; i++) {
            db.collection(CNAME1).insertDocument(new BaseDocument(), null);
        }

        final ArangoCursor<String> cursor = db
                .query("for i in " + CNAME1 + " Limit 6 return i._id", String.class, new AqlQueryOptions().count(true));
        assertThat((Object) cursor).isNotNull();
        for (int i = 1; i <= 6; i++, cursor.next()) {
            assertThat(cursor.hasNext()).isTrue();
        }
        assertThat(cursor.getCount()).isEqualTo(6);
    }

    @ParameterizedTest
    @MethodSource("dbs")
    void queryWithLimitAndFullCount(ArangoDatabase db) {
        for (int i = 0; i < 10; i++) {
            db.collection(CNAME1).insertDocument(new BaseDocument(), null);
        }

        final ArangoCursor<String> cursor = db
                .query("for i in " + CNAME1 + " Limit 5 return i._id", String.class, new AqlQueryOptions().fullCount(true));
        assertThat((Object) cursor).isNotNull();
        for (int i = 0; i < 5; i++, cursor.next()) {
            assertThat((Iterator<?>) cursor).hasNext();
        }
        assertThat(cursor.getStats()).isNotNull();
        assertThat(cursor.getStats().getExecutionTime()).isPositive();
        assertThat((cursor.getStats().getFullCount())).isGreaterThanOrEqualTo(10);
    }

    @ParameterizedTest
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
        assertThat(cursor.getStats().getCursorsCreated()).isNotNull();
        assertThat(cursor.getStats().getCursorsRearmed()).isNotNull();
        assertThat(cursor.getStats().getCacheHits()).isNotNull();
        assertThat(cursor.getStats().getCacheMisses()).isNotNull();
        assertThat(cursor.getStats().getIntermediateCommits()).isNotNull();
        if (isAtLeastVersion(3, 12)) {
            assertThat(cursor.getStats().getDocumentLookups()).isNotNull();
            assertThat(cursor.getStats().getSeeks()).isNotNull();
        }
    }

    @ParameterizedTest
    @MethodSource("dbs")
    void queryWithBatchSize(ArangoDatabase db) {
        for (int i = 0; i < 10; i++) {
            db.collection(CNAME1).insertDocument(new BaseDocument(), null);
        }

        final ArangoCursor<String> cursor = db
                .query("for i in " + CNAME1 + " return i._id", String.class, new AqlQueryOptions().batchSize(5).count(true));

        assertThat((Object) cursor).isNotNull();
        for (int i = 0; i < 10; i++, cursor.next()) {
            assertThat((Iterator<?>) cursor).hasNext();
        }
    }

    @ParameterizedTest
    @MethodSource("dbs")
    void queryIterateWithBatchSize(ArangoDatabase db) {
        for (int i = 0; i < 10; i++) {
            db.collection(CNAME1).insertDocument(new BaseDocument(), null);
        }

        final ArangoCursor<String> cursor = db
                .query("for i in " + CNAME1 + " return i._id", String.class, new AqlQueryOptions().batchSize(5).count(true));

        assertThat((Object) cursor).isNotNull();
        final AtomicInteger i = new AtomicInteger(0);
        for (; cursor.hasNext(); cursor.next()) {
            i.incrementAndGet();
        }
        assertThat(i.get()).isGreaterThanOrEqualTo(10);
    }

    @SlowTest
    @ParameterizedTest
    @MethodSource("dbs")
    void queryWithTTL(ArangoDatabase db) throws InterruptedException {
        // set TTL to 1 seconds and get the second batch after 2 seconds!
        final int ttl = 1;
        final int wait = 2;
        for (int i = 0; i < 10; i++) {
            db.collection(CNAME1).insertDocument(new BaseDocument(), null);
        }

        final ArangoCursor<String> cursor = db
                .query("for i in " + CNAME1 + " return i._id", String.class, new AqlQueryOptions().batchSize(5).ttl(ttl));

        assertThat((Iterable<String>) cursor).isNotNull();

        try {
            for (int i = 0; i < 10; i++, cursor.next()) {
                assertThat(cursor.hasNext()).isTrue();
                if (i == 1) {
                    Thread.sleep(wait * 1000);
                }
            }
            fail("this should fail");
        } catch (final ArangoDBException ex) {
            assertThat(ex.getMessage()).isEqualTo("Response: 404, Error: 1600 - cursor not found");
        }
    }

    @ParameterizedTest
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

    @ParameterizedTest
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
                .query("FOR t IN " + CNAME1 + " FILTER t.age >= 10 SORT t.age RETURN t._id", String.class,
                        new AqlQueryOptions().cache(true));

        assertThat((Object) cursor).isNotNull();
        assertThat(cursor.isCached()).isFalse();

        final ArangoCursor<String> cachedCursor = db
                .query("FOR t IN " + CNAME1 + " FILTER t.age >= 10 SORT t.age RETURN t._id", String.class,
                        new AqlQueryOptions().cache(true));

        assertThat((Object) cachedCursor).isNotNull();
        assertThat(cachedCursor.isCached()).isTrue();

        final QueryCachePropertiesEntity properties2 = new QueryCachePropertiesEntity();
        properties2.setMode(CacheMode.off);
        db.setQueryCacheProperties(properties2);
    }

    @ParameterizedTest
    @MethodSource("dbs")
    void queryWithMemoryLimit(ArangoDatabase db) {
        Throwable thrown = catchThrowable(() -> db.query("RETURN 1..100000", String.class,
                new AqlQueryOptions().memoryLimit(32 * 1024L)));
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
        assertThat(((ArangoDBException) thrown).getErrorNum()).isEqualTo(32);
    }

    @ParameterizedTest
    @MethodSource("dbs")
    void queryWithFailOnWarningTrue(ArangoDatabase db) {
        Throwable thrown = catchThrowable(() -> db.query("RETURN 1 / 0", String.class,
                new AqlQueryOptions().failOnWarning(true)));
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
    }

    @ParameterizedTest
    @MethodSource("dbs")
    void queryWithFailOnWarningFalse(ArangoDatabase db) {
        final ArangoCursor<String> cursor = db
                .query("RETURN 1 / 0", String.class, new AqlQueryOptions().failOnWarning(false));
        assertThat(cursor.next()).isNull();
    }

    @SlowTest
    @ParameterizedTest
    @MethodSource("dbs")
    void queryWithTimeout(ArangoDatabase db) {
        assumeTrue(isAtLeastVersion(3, 6));
        Throwable thrown = catchThrowable(() -> db.query("RETURN SLEEP(1)", String.class,
                new AqlQueryOptions().maxRuntime(0.1)).next());
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
        assertThat(((ArangoDBException) thrown).getResponseCode()).isEqualTo(410);
    }

    @ParameterizedTest
    @MethodSource("dbs")
    void queryWithMaxWarningCount(ArangoDatabase db) {
        final ArangoCursor<String> cursorWithWarnings = db
                .query("RETURN 1 / 0", String.class, new AqlQueryOptions());
        assertThat(cursorWithWarnings.getWarnings()).hasSize(1);
        final ArangoCursor<String> cursorWithLimitedWarnings = db
                .query("RETURN 1 / 0", String.class, new AqlQueryOptions().maxWarningCount(0L));
        final Collection<CursorWarning> warnings = cursorWithLimitedWarnings.getWarnings();
        assertThat(warnings).isNullOrEmpty();
    }

    @ParameterizedTest
    @MethodSource("dbs")
    void queryCursor(ArangoDatabase db) {
        ArangoCursor<Integer> cursor = db.query("for i in 1..4 return i", Integer.class,
                new AqlQueryOptions().batchSize(1));
        List<Integer> result = new ArrayList<>();
        result.add(cursor.next());
        result.add(cursor.next());
        ArangoCursor<Integer> cursor2 = db.cursor(cursor.getId(), Integer.class);
        result.add(cursor2.next());
        result.add(cursor2.next());
        assertThat(cursor2.hasNext()).isFalse();
        assertThat(result).containsExactly(1, 2, 3, 4);
    }

    @ParameterizedTest
    @MethodSource("dbs")
    void queryCursorInTx(ArangoDatabase db) {
        StreamTransactionEntity tx = db.beginStreamTransaction(new StreamTransactionOptions());
        ArangoCursor<Integer> cursor = db.query("for i in 1..4 return i", Integer.class,
                new AqlQueryOptions().batchSize(1).streamTransactionId(tx.getId()));
        List<Integer> result = new ArrayList<>();
        result.add(cursor.next());
        result.add(cursor.next());
        ArangoCursor<Integer> cursor2 = db.cursor(cursor.getId(), Integer.class,
                new AqlQueryOptions().streamTransactionId(tx.getId())
        );
        result.add(cursor2.next());
        result.add(cursor2.next());
        assertThat(cursor2.hasNext()).isFalse();
        assertThat(result).containsExactly(1, 2, 3, 4);
        db.abortStreamTransaction(tx.getId());
    }

    @ParameterizedTest
    @MethodSource("dbs")
    void queryCursorRetry(ArangoDatabase db) throws IOException {
        assumeTrue(isAtLeastVersion(3, 11));
        ArangoCursor<Integer> cursor = db.query("for i in 1..4 return i", Integer.class,
                new AqlQueryOptions().batchSize(1).allowRetry(true));
        List<Integer> result = new ArrayList<>();
        result.add(cursor.next());
        result.add(cursor.next());
        ArangoCursor<Integer> cursor2 = db.cursor(cursor.getId(), Integer.class, cursor.getNextBatchId());
        result.add(cursor2.next());
        result.add(cursor2.next());
        cursor2.close();
        assertThat(cursor2.hasNext()).isFalse();
        assertThat(result).containsExactly(1, 2, 3, 4);
    }

    @ParameterizedTest
    @MethodSource("dbs")
    void queryCursorRetryInTx(ArangoDatabase db) throws IOException {
        assumeTrue(isAtLeastVersion(3, 11));
        StreamTransactionEntity tx = db.beginStreamTransaction(new StreamTransactionOptions());
        ArangoCursor<Integer> cursor = db.query("for i in 1..4 return i", Integer.class,
                new AqlQueryOptions().batchSize(1).allowRetry(true).streamTransactionId(tx.getId()));
        List<Integer> result = new ArrayList<>();
        result.add(cursor.next());
        result.add(cursor.next());
        ArangoCursor<Integer> cursor2 = db.cursor(cursor.getId(), Integer.class, cursor.getNextBatchId(),
                new AqlQueryOptions().streamTransactionId(tx.getId())
        );
        result.add(cursor2.next());
        result.add(cursor2.next());
        cursor2.close();
        assertThat(cursor2.hasNext()).isFalse();
        assertThat(result).containsExactly(1, 2, 3, 4);
        db.abortStreamTransaction(tx.getId());
    }

    @ParameterizedTest
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

    @ParameterizedTest
    @MethodSource("dbs")
    void queryWithBindVars(ArangoDatabase db) {
        for (int i = 0; i < 10; i++) {
            final BaseDocument baseDocument = new BaseDocument(UUID.randomUUID().toString());
            baseDocument.addAttribute("age", 20 + i);
            db.collection(CNAME1).insertDocument(baseDocument, null);
        }
        final Map<String, Object> bindVars = new HashMap<>();
        bindVars.put("@coll", CNAME1);
        bindVars.put("age", 25);

        final ArangoCursor<String> cursor = db
                .query("FOR t IN @@coll FILTER t.age >= @age SORT t.age RETURN t._id", String.class, bindVars);

        assertThat((Object) cursor).isNotNull();

        for (int i = 0; i < 5; i++, cursor.next()) {
            assertThat((Iterator<?>) cursor).hasNext();
        }
    }

    @ParameterizedTest
    @MethodSource("dbs")
    void queryWithRawBindVars(ArangoDatabase db) {
        final Map<String, Object> bindVars = new HashMap<>();
        bindVars.put("foo", RawJson.of("\"fooValue\""));
        bindVars.put("bar", RawBytes.of(db.getSerde().serializeUserData(11)));

        final JsonNode res = db.query("RETURN {foo: @foo, bar: @bar}", JsonNode.class, bindVars).next();

        assertThat(res.get("foo").textValue()).isEqualTo("fooValue");
        assertThat(res.get("bar").intValue()).isEqualTo(11);
    }

    @ParameterizedTest
    @MethodSource("arangos")
    void queryWithWarning(ArangoDB arangoDB) {
        final ArangoCursor<String> cursor = arangoDB.db().query("return 1/0", String.class);

        assertThat((Object) cursor).isNotNull();
        assertThat(cursor.getWarnings()).isNotNull();
    }

    @ParameterizedTest
    @MethodSource("dbs")
    void queryStream(ArangoDatabase db) {
        final ArangoCursor<Void> cursor = db
                .query("FOR i IN 1..2 RETURN i", Void.class, new AqlQueryOptions().stream(true).count(true));
        assertThat((Object) cursor).isNotNull();
        assertThat(cursor.getCount()).isNull();
    }

    @ParameterizedTest
    @MethodSource("dbs")
    void queryForceOneShardAttributeValue(ArangoDatabase db) {
        assumeTrue(isAtLeastVersion(3, 10));
        assumeTrue(isCluster());
        assumeTrue(isEnterprise());

        String cname = "forceOneShardAttr-" + UUID.randomUUID();
        db.createCollection(cname, new CollectionCreateOptions()
                .shardKeys("foo")
                .numberOfShards(3));
        ArangoCollection col = db.collection(cname);
        BaseDocument doc = new BaseDocument();
        doc.addAttribute("foo", "bar");
        col.insertDocument(doc);

        ArangoCursor<BaseDocument> c1 = db
                .query("FOR d IN @@c RETURN d", BaseDocument.class, Collections.singletonMap("@c", cname),
                        new AqlQueryOptions().forceOneShardAttributeValue("bar"));
        assertThat(c1.hasNext()).isTrue();
        assertThat(c1.next().getAttribute("foo")).isEqualTo("bar");

        ArangoCursor<BaseDocument> c2 = db
                .query("FOR d IN @@c RETURN d", BaseDocument.class, Collections.singletonMap("@c", cname),
                        new AqlQueryOptions().forceOneShardAttributeValue("ooo"));
        assertThat(c2.hasNext()).isFalse();
    }

    @ParameterizedTest
    @MethodSource("arangos")
    void queryClose(ArangoDB arangoDB) throws IOException {
        final ArangoCursor<String> cursor = arangoDB.db()
                .query("for i in 1..2 return i", String.class, new AqlQueryOptions().batchSize(1));
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

    @ParameterizedTest
    @MethodSource("arangos")
    void queryCloseShouldBeIdempotent(ArangoDB arangoDB) throws IOException {
        ArangoCursor<Integer> cursor = arangoDB.db().query("for i in 1..2 return i", Integer.class,
                new AqlQueryOptions().batchSize(1));
        cursor.close();
        cursor.close();
    }

    @ParameterizedTest
    @MethodSource("arangos")
    void queryCloseOnCursorWithoutId(ArangoDB arangoDB) throws IOException {
        ArangoCursor<Integer> cursor = arangoDB.db().query("return 1", Integer.class);
        cursor.close();
        cursor.close();
    }

    @ParameterizedTest
    @MethodSource("dbs")
    void queryNoResults(ArangoDatabase db) throws IOException {
        final ArangoCursor<BaseDocument> cursor = db
                .query("FOR i IN @@col RETURN i", BaseDocument.class, new MapBuilder().put("@col", CNAME1).get());
        cursor.close();
    }

    @ParameterizedTest
    @MethodSource("dbs")
    void queryWithNullBindParam(ArangoDatabase db) throws IOException {
        final ArangoCursor<BaseDocument> cursor = db.query("FOR i IN @@col FILTER i.test == @test RETURN i",
                BaseDocument.class, new MapBuilder().put("@col", CNAME1).put("test", null).get());
        cursor.close();
    }

    @ParameterizedTest
    @MethodSource("dbs")
    void queryAllowDirtyRead(ArangoDatabase db) throws IOException {
        final ArangoCursor<BaseDocument> cursor = db.query("FOR i IN @@col FILTER i.test == @test RETURN i",
                BaseDocument.class, new MapBuilder().put("@col", CNAME1).put("test", null).get(),
                new AqlQueryOptions().allowDirtyRead(true));
        if (isAtLeastVersion(3, 10)) {
            assertThat(cursor.isPotentialDirtyRead()).isTrue();
        }
        cursor.close();
    }

    @ParameterizedTest
    @MethodSource("arangos")
    void queryAllowRetry(ArangoDB arangoDB) throws IOException {
        assumeTrue(isAtLeastVersion(3, 11));
        final ArangoCursor<String> cursor = arangoDB.db()
                .query("for i in 1..2 return i", String.class, new AqlQueryOptions().allowRetry(true).batchSize(1));
        assertThat(cursor.asListRemaining()).containsExactly("1", "2");
    }

    @ParameterizedTest
    @MethodSource("arangos")
    void queryAllowRetryClose(ArangoDB arangoDB) throws IOException {
        assumeTrue(isAtLeastVersion(3, 11));
        final ArangoCursor<String> cursor = arangoDB.db()
                .query("for i in 1..2 return i", String.class, new AqlQueryOptions().allowRetry(true).batchSize(1));
        assertThat(cursor.hasNext()).isTrue();
        assertThat(cursor.next()).isEqualTo("1");
        assertThat(cursor.hasNext()).isTrue();
        assertThat(cursor.next()).isEqualTo("2");
        assertThat(cursor.hasNext()).isFalse();
        cursor.close();
    }

    @ParameterizedTest
    @MethodSource("arangos")
    void queryAllowRetryCloseBeforeLatestBatch(ArangoDB arangoDB) throws IOException {
        assumeTrue(isAtLeastVersion(3, 11));
        final ArangoCursor<String> cursor = arangoDB.db()
                .query("for i in 1..2 return i", String.class, new AqlQueryOptions().allowRetry(true).batchSize(1));
        assertThat(cursor.hasNext()).isTrue();
        assertThat(cursor.next()).isEqualTo("1");
        assertThat(cursor.hasNext()).isTrue();
        cursor.close();
    }

    @ParameterizedTest
    @MethodSource("arangos")
    void queryAllowRetryCloseSingleBatch(ArangoDB arangoDB) throws IOException {
        assumeTrue(isAtLeastVersion(3, 11));
        final ArangoCursor<String> cursor = arangoDB.db()
                .query("for i in 1..2 return i", String.class, new AqlQueryOptions().allowRetry(true));
        assertThat(cursor.hasNext()).isTrue();
        assertThat(cursor.next()).isEqualTo("1");
        assertThat(cursor.hasNext()).isTrue();
        assertThat(cursor.next()).isEqualTo("2");
        assertThat(cursor.hasNext()).isFalse();
        cursor.close();
    }

    @ParameterizedTest
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
        if (isAtLeastVersion(3, 10)) {
            assertThat(explain.getStats().getPeakMemoryUsage()).isNotNull();
            assertThat(explain.getStats().getExecutionTime()).isNotNull();
        }
    }

    @ParameterizedTest
    @MethodSource("dbs")
    void explainQueryWithBindVars(ArangoDatabase db) {
        final AqlExecutionExplainEntity explain = db.explainQuery("for i in 1..1 return @value",
                Collections.singletonMap("value", 11), null);
        assertThat(explain).isNotNull();
        assertThat(explain.getPlan()).isNotNull();
        assertThat(explain.getPlans()).isNull();
        final ExecutionPlan plan = explain.getPlan();
        assertThat(plan.getCollections()).isEmpty();
        assertThat(plan.getEstimatedCost()).isPositive();
        assertThat(plan.getEstimatedNrItems()).isPositive();
        assertThat(plan.getVariables()).hasSize(3);
        assertThat(plan.getNodes()).isNotEmpty();
    }

    @ParameterizedTest
    @MethodSource("dbs")
    void explainQueryWithWarnings(ArangoDatabase db) {
        AqlExecutionExplainEntity explain = db.explainQuery("return 1/0", null, null);
        assertThat(explain.getWarnings())
                .hasSize(1)
                .allSatisfy(w -> {
                    assertThat(w.getCode()).isEqualTo(1562);
                    assertThat(w.getMessage()).isEqualTo("division by zero");
                });
    }

    @ParameterizedTest
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

    private String getExplainQuery(ArangoDatabase db) {
        ArangoCollection character = db.collection("got_characters");
        ArangoCollection actor = db.collection("got_actors");

        if (!character.exists())
            character.create();

        if (!actor.exists())
            actor.create();

        return "FOR `character` IN `got_characters` " +
                " FOR `actor` IN `got_actors` " +
                "   FILTER `actor`.`_id` == @myId" +
                "   FILTER `character`.`actor` == `actor`.`_id` " +
                "   FILTER `character`.`value` != 1/0 " +
                "   RETURN {`character`, `actor`}";
    }

    void checkExecutionPlan(AqlQueryExplainEntity.ExecutionPlan plan) {
        assertThat(plan).isNotNull();
        assertThat(plan.get("estimatedNrItems"))
                .isInstanceOf(Integer.class)
                .asInstanceOf(INTEGER)
                .isNotNull()
                .isNotNegative();
        assertThat(plan.getNodes()).isNotEmpty();

        AqlQueryExplainEntity.ExecutionNode node = plan.getNodes().iterator().next();
        assertThat(node.get("estimatedCost")).isNotNull();

        assertThat(plan.getEstimatedCost()).isNotNull().isNotNegative();
        assertThat(plan.getCollections()).isNotEmpty();

        AqlQueryExplainEntity.ExecutionCollection collection = plan.getCollections().iterator().next();
        assertThat(collection.get("name"))
                .isInstanceOf(String.class)
                .asInstanceOf(STRING)
                .isNotNull()
                .isNotEmpty();

        assertThat(plan.getRules()).isNotEmpty();
        assertThat(plan.getVariables()).isNotEmpty();

        AqlQueryExplainEntity.ExecutionVariable variable = plan.getVariables().iterator().next();
        assertThat(variable.get("name"))
                .isInstanceOf(String.class)
                .asInstanceOf(STRING)
                .isNotNull()
                .isNotEmpty();
    }

    @ParameterizedTest
    @MethodSource("dbs")
    void explainAqlQuery(ArangoDatabase db) {
        AqlQueryExplainEntity explain = db.explainAqlQuery(
                getExplainQuery(db),
                Collections.singletonMap("myId", "123"),
                new AqlQueryExplainOptions());
        assertThat(explain).isNotNull();

        checkExecutionPlan(explain.getPlan());
        assertThat(explain.getPlans()).isNull();
        assertThat(explain.getWarnings()).isNotEmpty();

        CursorWarning warning = explain.getWarnings().iterator().next();
        assertThat(warning).isNotNull();
        assertThat(warning.getCode()).isEqualTo(1562);
        assertThat(warning.getMessage()).contains("division by zero");

        assertThat(explain.getStats()).isNotNull();

        assertThat(explain.getStats().get("executionTime"))
                .isInstanceOf(Double.class)
                .asInstanceOf(DOUBLE)
                .isNotNull()
                .isPositive();

        assertThat(explain.getCacheable()).isFalse();
    }

    @ParameterizedTest
    @MethodSource("dbs")
    void explainAqlQueryAllPlans(ArangoDatabase db) {
        AqlQueryExplainEntity explain = db.explainAqlQuery(
                getExplainQuery(db),
                Collections.singletonMap("myId", "123"),
                new AqlQueryExplainOptions().allPlans(true));
        assertThat(explain).isNotNull();

        assertThat(explain.getPlan()).isNull();
        assertThat(explain.getPlans()).allSatisfy(this::checkExecutionPlan);
        assertThat(explain.getWarnings()).isNotEmpty();

        CursorWarning warning = explain.getWarnings().iterator().next();
        assertThat(warning).isNotNull();
        assertThat(warning.getCode()).isEqualTo(1562);
        assertThat(warning.getMessage()).contains("division by zero");

        assertThat(explain.getStats()).isNotNull();

        assertThat(explain.getStats().get("executionTime"))
                .isInstanceOf(Double.class)
                .asInstanceOf(DOUBLE)
                .isNotNull()
                .isPositive();

        assertThat(explain.getCacheable()).isNull();
    }

    @ParameterizedTest
    @MethodSource("dbs")
    void parseQuery(ArangoDatabase db) {
        final AqlParseEntity parse = db.parseQuery("for i in 1..1 return i");
        assertThat(parse).isNotNull();
        assertThat(parse.getBindVars()).isEmpty();
        assertThat(parse.getCollections()).isEmpty();
        assertThat(parse.getAst()).hasSize(1);
    }

    @SlowTest
    @ParameterizedTest
    @MethodSource("dbs")
    void getCurrentlyRunningQueries(ArangoDatabase db) throws InterruptedException {
        String query = "return sleep(1)";
        Thread t = new Thread(() -> db.query(query, Void.class));
        t.start();
        Thread.sleep(300);
        final Collection<QueryEntity> currentlyRunningQueries = db.getCurrentlyRunningQueries();
        assertThat(currentlyRunningQueries).hasSize(1);
        final QueryEntity queryEntity = currentlyRunningQueries.iterator().next();
        assertThat(queryEntity.getId()).isNotNull();
        assertThat(queryEntity.getDatabase()).isEqualTo(db.name());
        assertThat(queryEntity.getUser()).isEqualTo("root");
        assertThat(queryEntity.getQuery()).isEqualTo(query);
        assertThat(queryEntity.getBindVars()).isEmpty();
        assertThat(queryEntity.getStarted()).isInThePast();
        assertThat(queryEntity.getRunTime()).isPositive();
        if (isAtLeastVersion(3, 11)) {
            assertThat(queryEntity.getPeakMemoryUsage()).isNotNull();
        }
        assertThat(queryEntity.getState()).isEqualTo(QueryExecutionState.EXECUTING);
        assertThat(queryEntity.getStream()).isFalse();
        t.join();
    }

    @SlowTest
    @ParameterizedTest
    @MethodSource("dbs")
    void killQuery(ArangoDatabase db) throws InterruptedException, ExecutionException {
        ExecutorService es = Executors.newSingleThreadExecutor();
        Future<?> future = es.submit(() -> {
            try {
                db.query("return sleep(5)", Void.class);
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

    @SlowTest
    @ParameterizedTest
    @MethodSource("dbs")
    void getAndClearSlowQueries(ArangoDatabase db) {
        db.clearSlowQueries();

        final QueryTrackingPropertiesEntity properties = db.getQueryTrackingProperties();
        final Long slowQueryThreshold = properties.getSlowQueryThreshold();
        properties.setSlowQueryThreshold(1L);
        db.setQueryTrackingProperties(properties);

        String query = "return sleep(1.1)";
        db.query(query, Void.class);
        final Collection<QueryEntity> slowQueries = db.getSlowQueries();
        assertThat(slowQueries).hasSize(1);
        final QueryEntity queryEntity = slowQueries.iterator().next();
        assertThat(queryEntity.getId()).isNotNull();
        assertThat(queryEntity.getDatabase()).isEqualTo(db.name());
        assertThat(queryEntity.getUser()).isEqualTo("root");
        assertThat(queryEntity.getQuery()).isEqualTo(query);
        assertThat(queryEntity.getBindVars()).isEmpty();
        assertThat(queryEntity.getStarted()).isInThePast();
        assertThat(queryEntity.getRunTime()).isPositive();
        if (isAtLeastVersion(3, 11)) {
            assertThat(queryEntity.getPeakMemoryUsage()).isNotNull();
        }
        assertThat(queryEntity.getState()).isEqualTo(QueryExecutionState.FINISHED);
        assertThat(queryEntity.getStream()).isFalse();

        db.clearSlowQueries();
        assertThat(db.getSlowQueries()).isEmpty();
        properties.setSlowQueryThreshold(slowQueryThreshold);
        db.setQueryTrackingProperties(properties);
    }

    @ParameterizedTest
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

    @ParameterizedTest
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

    @ParameterizedTest
    @MethodSource("dbs")
    void createGraph(ArangoDatabase db) {
        String name = "graph-" + rnd();
        final GraphEntity result = db.createGraph(name, null, null);
        assertThat(result.getName()).isEqualTo(name);
    }

    @ParameterizedTest
    @MethodSource("dbs")
    void createGraphSatellite(ArangoDatabase db) {
        assumeTrue(isAtLeastVersion(3, 7));
        assumeTrue(isCluster());
        assumeTrue(isEnterprise());

        String name = "graph-" + rnd();
        final GraphEntity result = db.createGraph(name, null, new GraphCreateOptions().replicationFactor(ReplicationFactor.ofSatellite()));
        assertThat(result.getReplicationFactor()).isEqualTo(ReplicationFactor.ofSatellite());

        GraphEntity info = db.graph(name).getInfo();
        assertThat(info.getReplicationFactor()).isEqualTo(ReplicationFactor.ofSatellite());

        GraphEntity graph = db.getGraphs().stream().filter(g -> name.equals(g.getName())).findFirst().get();
        assertThat(graph.getReplicationFactor()).isEqualTo(ReplicationFactor.ofSatellite());
    }

    @ParameterizedTest
    @MethodSource("dbs")
    void createGraphReplicationFaktor(ArangoDatabase db) {
        assumeTrue(isCluster());
        String name = "graph-" + rnd();
        final String edgeCollection = rndName();
        final String fromCollection = rndName();
        final String toCollection = rndName();
        final Collection<EdgeDefinition> edgeDefinitions =
                Collections.singletonList(new EdgeDefinition().collection(edgeCollection).from(fromCollection).to(toCollection));
        final GraphEntity result = db.createGraph(name, edgeDefinitions, new GraphCreateOptions().replicationFactor(2));
        assertThat(result).isNotNull();
        for (final String collection : Arrays.asList(edgeCollection, fromCollection, toCollection)) {
            final CollectionPropertiesEntity properties = db.collection(collection).getProperties();
            assertThat(properties.getReplicationFactor().get()).isEqualTo(2);
        }
    }

    @ParameterizedTest
    @MethodSource("dbs")
    void createGraphNumberOfShards(ArangoDatabase db) {
        assumeTrue(isCluster());
        String name = "graph-" + rnd();
        final String edgeCollection = rndName();
        final String fromCollection = rndName();
        final String toCollection = rndName();
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

    @ParameterizedTest
    @MethodSource("dbs")
    void getGraphs(ArangoDatabase db) {
        String name = "graph-" + rnd();
        db.createGraph(name, null, null);
        final Collection<GraphEntity> graphs = db.getGraphs();
        assertThat(graphs).hasSizeGreaterThanOrEqualTo(1);
        long count = graphs.stream().map(GraphEntity::getName).filter(name::equals).count();
        assertThat(count).isEqualTo(1L);
    }

    @ParameterizedTest
    @MethodSource("dbs")
    void transactionString(ArangoDatabase db) {
        final TransactionOptions options = new TransactionOptions().params("test");
        final RawJson result = db.transaction("function (params) {return params;}", RawJson.class, options);
        assertThat(result.get()).isEqualTo("\"test\"");
    }

    @ParameterizedTest
    @MethodSource("dbs")
    void transactionNumber(ArangoDatabase db) {
        final TransactionOptions options = new TransactionOptions().params(5);
        final Integer result = db.transaction("function (params) {return params;}", Integer.class, options);
        assertThat(result).isEqualTo(5);
    }

    @ParameterizedTest
    @MethodSource("dbs")
    void transactionJsonNode(ArangoDatabase db) {
        final TransactionOptions options = new TransactionOptions().params(JsonNodeFactory.instance.textNode("test"));
        final JsonNode result = db.transaction("function (params) {return params;}", JsonNode.class, options);
        assertThat(result.isTextual()).isTrue();
        assertThat(result.asText()).isEqualTo("test");
    }

    @ParameterizedTest
    @MethodSource("dbs")
    void transactionJsonObject(ArangoDatabase db) {
        ObjectNode params = JsonNodeFactory.instance.objectNode().put("foo", "hello").put("bar", "world");
        final TransactionOptions options = new TransactionOptions().params(params);
        final RawJson result = db
                .transaction("function (params) { return params['foo'] + ' ' + params['bar'];}", RawJson.class,
                        options);
        assertThat(result.get()).isEqualTo("\"hello world\"");
    }

    @ParameterizedTest
    @MethodSource("dbs")
    void transactionJsonArray(ArangoDatabase db) {
        ArrayNode params = JsonNodeFactory.instance.arrayNode().add("hello").add("world");
        final TransactionOptions options = new TransactionOptions().params(params);
        final RawJson result = db
                .transaction("function (params) { return params[0] + ' ' + params[1];}", RawJson.class, options);
        assertThat(result.get()).isEqualTo("\"hello world\"");
    }

    @ParameterizedTest
    @MethodSource("dbs")
    void transactionMap(ArangoDatabase db) {
        final Map<String, Object> params = new MapBuilder().put("foo", "hello").put("bar", "world").get();
        final TransactionOptions options = new TransactionOptions().params(params);
        final RawJson result = db
                .transaction("function (params) { return params['foo'] + ' ' + params['bar'];}", RawJson.class,
                        options);
        assertThat(result.get()).isEqualTo("\"hello world\"");
    }

    @ParameterizedTest
    @MethodSource("dbs")
    void transactionArray(ArangoDatabase db) {
        final String[] params = new String[]{"hello", "world"};
        final TransactionOptions options = new TransactionOptions().params(params);
        final RawJson result = db
                .transaction("function (params) { return params[0] + ' ' + params[1];}", RawJson.class, options);
        assertThat(result.get()).isEqualTo("\"hello world\"");
    }

    @ParameterizedTest
    @MethodSource("dbs")
    void transactionCollection(ArangoDatabase db) {
        final Collection<String> params = new ArrayList<>();
        params.add("hello");
        params.add("world");
        final TransactionOptions options = new TransactionOptions().params(params);
        final RawJson result = db
                .transaction("function (params) { return params[0] + ' ' + params[1];}", RawJson.class, options);
        assertThat(result.get()).isEqualTo("\"hello world\"");
    }

    @ParameterizedTest
    @MethodSource("dbs")
    void transactionInsertJson(ArangoDatabase db) {
        String key = "key-" + rnd();
        final TransactionOptions options = new TransactionOptions().params("{\"_key\":\"" + key + "\"}")
                .writeCollections(CNAME1);
        db.transaction("function (params) { "
                + "var db = require('internal').db;"
                + "db." + CNAME1 + ".save(JSON.parse(params));"
                + "}", Void.class, options);
        assertThat(db.collection(CNAME1).getDocument(key, RawJson.class)).isNotNull();
    }

    @ParameterizedTest
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
        assertThat(db.collection(CNAME1).getDocument(key, RawJson.class)).isNotNull();
    }

    @ParameterizedTest
    @MethodSource("dbs")
    void transactionEmpty(ArangoDatabase db) {
        db.transaction("function () {}", Void.class, null);
    }

    @ParameterizedTest
    @MethodSource("dbs")
    void transactionAllowImplicit(ArangoDatabase db) {
        final String action = "function (params) {" + "var db = require('internal').db;"
                + "return {'a':db." + CNAME1 + ".all().toArray()[0], 'b':db." + CNAME2 + ".all().toArray()[0]};"
                + "}";
        final TransactionOptions options = new TransactionOptions().readCollections(CNAME1);
        db.transaction(action, JsonNode.class, options);
        options.allowImplicit(false);
        Throwable thrown = catchThrowable(() -> db.transaction(action, JsonNode.class, options));
        assertThat(thrown)
                .isInstanceOf(ArangoDBException.class)
                .extracting(it -> ((ArangoDBException) it).getResponseCode())
                .isEqualTo(400);
    }

    @ParameterizedTest
    @MethodSource("dbs")
    void transactionPojoReturn(ArangoDatabase db) {
        final String action = "function() { return {'value':'hello world'}; }";
        final TransactionTestEntity res = db.transaction(action, TransactionTestEntity.class, new TransactionOptions());
        assertThat(res).isNotNull();
        assertThat(res.value).isEqualTo("hello world");
    }

    @ParameterizedTest
    @MethodSource("dbs")
    void getInfo(ArangoDatabase db) {
        final DatabaseEntity info = db.getInfo();
        assertThat(info).isNotNull();
        assertThat(info.getId()).isNotNull();
        assertThat(info.getName()).isEqualTo(getTestDb());
        assertThat(info.getPath()).isNotNull();
        assertThat(info.getIsSystem()).isFalse();

        if (isAtLeastVersion(3, 6) && isCluster()) {
            assertThat(info.getSharding()).isNotNull();
            assertThat(info.getWriteConcern()).isNotNull();
            assertThat(info.getReplicationFactor()).isNotNull();
        }
    }

    @ParameterizedTest
    @MethodSource("dbs")
    void shouldIncludeExceptionMessage(ArangoDatabase db) {
        assumeTrue(isAtLeastVersion(3, 4));

        final String exceptionMessage = "My error context";
        final String action = "function (params) {" + "throw '" + exceptionMessage + "';" + "}";
        try {
            db.transaction(action, Void.class, null);
            fail();
        } catch (final ArangoDBException e) {
            assertThat(e.getErrorMessage()).isEqualTo(exceptionMessage);
        }
    }

    @ParameterizedTest
    @MethodSource("dbs")
    void reloadRouting(ArangoDatabase db) {
        db.reloadRouting();
    }

    public static class TransactionTestEntity {
        private String value;

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }
}
