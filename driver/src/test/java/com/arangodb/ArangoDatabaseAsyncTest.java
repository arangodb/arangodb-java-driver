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
import com.arangodb.util.MapBuilder;
import com.arangodb.util.RawJson;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.*;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.api.Assumptions.assumeTrue;


/**
 * @author Mark Vollmary
 * @author Michele Rastelli
 */
class ArangoDatabaseAsyncTest extends BaseJunit5 {

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
    @MethodSource("asyncDbs")
    void getVersion(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        final ArangoDBVersion version = db.getVersion().get();
        assertThat(version).isNotNull();
        assertThat(version.getServer()).isNotNull();
        assertThat(version.getVersion()).isNotNull();
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("asyncDbs")
    void getEngine(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        final ArangoDBEngine engine = db.getEngine().get();
        assertThat(engine).isNotNull();
        assertThat(engine.getName()).isNotNull();
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("asyncArangos")
    void exists(ArangoDBAsync arangoDB) throws ExecutionException, InterruptedException {
        assertThat(arangoDB.db(TEST_DB).exists().get()).isTrue();
        assertThat(arangoDB.db("no").exists().get()).isFalse();
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("asyncDbs")
    void getAccessibleDatabases(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        final Collection<String> dbs = db.getAccessibleDatabases().get();
        assertThat(dbs).contains("_system");
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("asyncDbs")
    void createCollection(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        String name = rndName();
        final CollectionEntity result = db.createCollection(name, null).get();
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("asyncDbs")
    void createCollectionWithNotNormalizedName(ArangoDatabaseAsync db) {
        assumeTrue(supportsExtendedNames());
        final String colName = "testCol-\u006E\u0303\u00f1";

        Throwable thrown = catchThrowable(() -> db.createCollection(colName));
        assertThat(thrown)
                .isInstanceOf(ArangoDBException.class)
                .hasMessageContaining("normalized")
                .extracting(it -> ((ArangoDBException) it).getResponseCode()).isEqualTo(400);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("asyncDbs")
    void createCollectionWithReplicationFactor(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        assumeTrue(isCluster());
        String name = rndName();
        final CollectionEntity result = db
                .createCollection(name, new CollectionCreateOptions().replicationFactor(2)).get();
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        CollectionPropertiesEntity props = db.collection(name).getProperties().get();
        assertThat(props.getReplicationFactor().get()).isEqualTo(2);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("asyncDbs")
    void createCollectionWithWriteConcern(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isCluster());

        String name = rndName();
        final CollectionEntity result = db.createCollection(name,
                new CollectionCreateOptions().replicationFactor(2).writeConcern(2)).get();
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        CollectionPropertiesEntity props = db.collection(name).getProperties().get();
        assertThat(props.getReplicationFactor().get()).isEqualTo(2);
        assertThat(props.getWriteConcern()).isEqualTo(2);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("asyncDbs")
    void createSatelliteCollection(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        assumeTrue(isEnterprise());
        assumeTrue(isCluster());

        String name = rndName();
        final CollectionEntity result = db
                .createCollection(name, new CollectionCreateOptions().replicationFactor(ReplicationFactor.ofSatellite())).get();

        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        CollectionPropertiesEntity props = db.collection(name).getProperties().get();
        assertThat(props.getReplicationFactor()).isEqualTo(ReplicationFactor.ofSatellite());
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("asyncDbs")
    void createCollectionWithNumberOfShards(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        assumeTrue(isCluster());
        String name = rndName();
        final CollectionEntity result = db
                .createCollection(name, new CollectionCreateOptions().numberOfShards(2)).get();

        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        CollectionPropertiesEntity props = db.collection(name).getProperties().get();
        assertThat(props.getNumberOfShards()).isEqualTo(2);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("asyncDbs")
    void createCollectionWithShardingStrategys(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        assumeTrue(isAtLeastVersion(3, 4));
        assumeTrue(isCluster());

        String name = rndName();
        final CollectionEntity result = db.createCollection(name, new CollectionCreateOptions()
                .shardingStrategy(ShardingStrategy.COMMUNITY_COMPAT.getInternalName())).get();

        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        CollectionPropertiesEntity props = db.collection(name).getProperties().get();
        assertThat(props.getShardingStrategy()).isEqualTo(ShardingStrategy.COMMUNITY_COMPAT.getInternalName());
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("asyncDbs")
    void createCollectionWithSmartJoinAttribute(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isEnterprise());
        assumeTrue(isCluster());

        String fooName = rndName();
        db.collection(fooName).create();

        String name = rndName();
        final CollectionEntity result = db.createCollection(name,
                new CollectionCreateOptions().smartJoinAttribute("test123").distributeShardsLike(fooName).shardKeys("_key:")).get();
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(db.collection(name).getProperties().get().getSmartJoinAttribute()).isEqualTo("test123");
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("asyncDbs")
    void createCollectionWithSmartJoinAttributeWrong(ArangoDatabaseAsync db) {
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isEnterprise());
        assumeTrue(isCluster());

        String name = rndName();

        Throwable thrown = catchThrowable(() -> db.createCollection(name, new CollectionCreateOptions().smartJoinAttribute("test123")).get()).getCause();
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
        assertThat(((ArangoDBException) thrown).getErrorNum()).isEqualTo(4006);
        assertThat(((ArangoDBException) thrown).getResponseCode()).isEqualTo(400);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("asyncDbs")
    void createCollectionWithNumberOfShardsAndShardKey(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        assumeTrue(isCluster());

        String name = rndName();
        final CollectionEntity result = db
                .createCollection(name, new CollectionCreateOptions().numberOfShards(2).shardKeys("a")).get();
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        final CollectionPropertiesEntity properties = db.collection(name).getProperties().get();
        assertThat(properties.getNumberOfShards()).isEqualTo(2);
        assertThat(properties.getShardKeys()).hasSize(1);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("asyncDbs")
    void createCollectionWithNumberOfShardsAndShardKeys(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        assumeTrue(isCluster());
        String name = rndName();
        final CollectionEntity result = db.createCollection(name,
                new CollectionCreateOptions().numberOfShards(2).shardKeys("a", "b")).get();
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        final CollectionPropertiesEntity properties = db.collection(name).getProperties().get();
        assertThat(properties.getNumberOfShards()).isEqualTo(2);
        assertThat(properties.getShardKeys()).hasSize(2);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("asyncDbs")
    void createCollectionWithDistributeShardsLike(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        assumeTrue(isEnterprise());
        assumeTrue(isCluster());

        final Integer numberOfShards = 3;

        String name1 = rndName();
        String name2 = rndName();
        db.createCollection(name1, new CollectionCreateOptions().numberOfShards(numberOfShards)).get();
        db.createCollection(name2, new CollectionCreateOptions().distributeShardsLike(name1)).get();

        assertThat(db.collection(name1).getProperties().get().getNumberOfShards()).isEqualTo(numberOfShards);
        assertThat(db.collection(name2).getProperties().get().getNumberOfShards()).isEqualTo(numberOfShards);
    }

    private void createCollectionWithKeyType(ArangoDatabaseAsync db, KeyType keyType) throws ExecutionException, InterruptedException {
        String name = rndName();
        db.createCollection(name, new CollectionCreateOptions().keyOptions(
                false,
                keyType,
                null,
                null
        )).get();
        assertThat(db.collection(name).getProperties().get().getKeyOptions().getType()).isEqualTo(keyType);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("asyncDbs")
    void createCollectionWithKeyTypeAutoincrement(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        assumeTrue(isSingleServer());
        createCollectionWithKeyType(db, KeyType.autoincrement);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("asyncDbs")
    void createCollectionWithKeyTypePadded(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        assumeTrue(isAtLeastVersion(3, 4));
        createCollectionWithKeyType(db, KeyType.padded);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("asyncDbs")
    void createCollectionWithKeyTypeTraditional(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        createCollectionWithKeyType(db, KeyType.traditional);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("asyncDbs")
    void createCollectionWithKeyTypeUuid(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        assumeTrue(isAtLeastVersion(3, 4));
        createCollectionWithKeyType(db, KeyType.uuid);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("asyncDbs")
    void createCollectionWithJsonSchema(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
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
                ).get();
        assertThat(result.getSchema().getLevel()).isEqualTo(CollectionSchema.Level.NEW);
        assertThat(result.getSchema().getRule()).isEqualTo(rule);
        assertThat(result.getSchema().getMessage()).isEqualTo(message);

        CollectionPropertiesEntity props = db.collection(name).getProperties().get();
        assertThat(props.getSchema().getLevel()).isEqualTo(CollectionSchema.Level.NEW);
        assertThat(props.getSchema().getRule()).isEqualTo(rule);
        assertThat(props.getSchema().getMessage()).isEqualTo(message);

        BaseDocument doc = new BaseDocument(UUID.randomUUID().toString());
        doc.addAttribute("number", 33);
        db.collection(name).insertDocument(doc);

        BaseDocument wrongDoc = new BaseDocument(UUID.randomUUID().toString());
        wrongDoc.addAttribute("number", "notANumber");
        Throwable thrown = catchThrowable(() -> db.collection(name).insertDocument(wrongDoc).get()).getCause();
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
        ArangoDBException e = (ArangoDBException) thrown;

        assertThat(e).hasMessageContaining(message);
        assertThat(e.getResponseCode()).isEqualTo(400);
        assertThat(e.getErrorNum()).isEqualTo(1620);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("asyncDbs")
    void createCollectionWithComputedFields(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        assumeTrue(isAtLeastVersion(3, 10));
        String cName = rndName();
        ComputedValue cv = new ComputedValue()
                .name("foo")
                .expression("RETURN 11")
                .overwrite(false)
                .computeOn(ComputedValue.ComputeOn.insert)
                .keepNull(false)
                .failOnWarning(true);

        final CollectionEntity result = db.createCollection(cName, new CollectionCreateOptions().computedValues(cv)).get();

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

        db.collection(cName).changeProperties(new CollectionPropertiesOptions().computedValues(cv2)).get();

        CollectionPropertiesEntity props = db.collection(cName).getProperties().get();
        assertThat(props.getComputedValues())
                .hasSize(1)
                .contains(cv2);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("asyncDbs")
    void deleteCollection(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        String name = rndName();
        db.createCollection(name, null).get();
        db.collection(name).drop().get();
        Throwable thrown = catchThrowable(() -> db.collection(name).getInfo().get()).getCause();
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("asyncDbs")
    void deleteSystemCollection(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        final String name = "_system_test";
        db.createCollection(name, new CollectionCreateOptions().isSystem(true)).get();
        db.collection(name).drop(true).get();
        Throwable thrown = catchThrowable(() -> db.collection(name).getInfo().get()).getCause();
        assertThat(thrown)
                .isInstanceOf(ArangoDBException.class)
                .extracting(it -> ((ArangoDBException) it).getResponseCode())
                .isEqualTo(404);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("asyncDbs")
    void deleteSystemCollectionFail(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        final String name = "_system_test";
        ArangoCollectionAsync collection = db.collection(name);
        if (collection.exists().get())
            collection.drop(true).get();

        db.createCollection(name, new CollectionCreateOptions().isSystem(true)).get();
        Throwable thrown = catchThrowable(() -> collection.drop().get()).getCause();
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
        ArangoDBException e = (ArangoDBException) thrown;
        assertThat(e.getResponseCode()).isEqualTo(403);
        collection.drop(true).get();
        assertThat(collection.exists().get()).isFalse();
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("asyncDbs")
    void getIndex(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        final Collection<String> fields = Collections.singletonList("field-" + rnd());
        final IndexEntity createResult = db.collection(CNAME1).ensurePersistentIndex(fields, null).get();
        final IndexEntity readResult = db.getIndex(createResult.getId()).get();
        assertThat(readResult.getId()).isEqualTo(createResult.getId());
        assertThat(readResult.getType()).isEqualTo(createResult.getType());
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("asyncDbs")
    void deleteIndex(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        final Collection<String> fields = Collections.singletonList("field-" + rnd());
        final IndexEntity createResult = db.collection(CNAME1).ensurePersistentIndex(fields, null).get();
        final String id = db.deleteIndex(createResult.getId()).get();
        assertThat(id).isEqualTo(createResult.getId());
        Throwable thrown = catchThrowable(() -> db.getIndex(id).get()).getCause();
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
        ArangoDBException e = (ArangoDBException) thrown;
        assertThat(e.getResponseCode()).isEqualTo(404);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("asyncDbs")
    void getCollections(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        final Collection<CollectionEntity> collections = db.getCollections(null).get();
        long count = collections.stream().map(CollectionEntity::getName).filter(it -> it.equals(CNAME1)).count();
        assertThat(count).isEqualTo(1L);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("asyncDbs")
    void getCollectionsExcludeSystem(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        final CollectionsReadOptions options = new CollectionsReadOptions().excludeSystem(true);
        final Collection<CollectionEntity> nonSystemCollections = db.getCollections(options).get();
        final Collection<CollectionEntity> allCollections = db.getCollections(null).get();
        assertThat(allCollections).hasSizeGreaterThan(nonSystemCollections.size());
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("asyncArangos")
    void grantAccess(ArangoDBAsync arangoDB) throws ExecutionException, InterruptedException {
        String user = "user-" + rnd();
        arangoDB.createUser(user, "1234", null).get();
        arangoDB.db(TEST_DB).grantAccess(user).get();
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("asyncArangos")
    void grantAccessRW(ArangoDBAsync arangoDB) {
        String user = "user-" + rnd();
        arangoDB.createUser(user, "1234", null);
        arangoDB.db(TEST_DB).grantAccess(user, Permissions.RW);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("asyncArangos")
    void grantAccessRO(ArangoDBAsync arangoDB) {
        String user = "user-" + rnd();
        arangoDB.createUser(user, "1234", null);
        arangoDB.db(TEST_DB).grantAccess(user, Permissions.RO);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("asyncArangos")
    void grantAccessNONE(ArangoDBAsync arangoDB) {
        String user = "user-" + rnd();
        arangoDB.createUser(user, "1234", null);
        arangoDB.db(TEST_DB).grantAccess(user, Permissions.NONE);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("asyncDbs")
    void grantAccessUserNotFound(ArangoDatabaseAsync db) {
        String user = "user-" + rnd();
        Throwable thrown = catchThrowable(() -> db.grantAccess(user, Permissions.RW).get()).getCause();
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("asyncArangos")
    void revokeAccess(ArangoDBAsync arangoDB) {
        String user = "user-" + rnd();
        arangoDB.createUser(user, "1234", null);
        arangoDB.db(TEST_DB).revokeAccess(user);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("asyncDbs")
    void revokeAccessUserNotFound(ArangoDatabaseAsync db) {
        String user = "user-" + rnd();
        Throwable thrown = catchThrowable(() -> db.revokeAccess(user).get()).getCause();
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("asyncArangos")
    void resetAccess(ArangoDBAsync arangoDB) {
        String user = "user-" + rnd();
        arangoDB.createUser(user, "1234", null);
        arangoDB.db(TEST_DB).resetAccess(user);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("asyncDbs")
    void resetAccessUserNotFound(ArangoDatabaseAsync db) {
        String user = "user-" + rnd();
        Throwable thrown = catchThrowable(() -> db.resetAccess(user).get()).getCause();
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("asyncArangos")
    void grantDefaultCollectionAccess(ArangoDBAsync arangoDB) {
        String user = "user-" + rnd();
        arangoDB.createUser(user, "1234");
        arangoDB.db(TEST_DB).grantDefaultCollectionAccess(user, Permissions.RW);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("asyncDbs")
    void getPermissions(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        assertThat(db.getPermissions("root").get()).isEqualTo(Permissions.RW);
    }

//    @ParameterizedTest(name = "{index}")
//    @MethodSource("asyncDbs")
//    void query(ArangoDatabaseAsync db) {
//        for (int i = 0; i < 10; i++) {
//            db.collection(CNAME1).insertDocument(new BaseDocument(), null);
//        }
//        final ArangoCursor<String> cursor = db.query("for i in " + CNAME1 + " return i._id", String.class);
//        assertThat((Object) cursor).isNotNull();
//        for (int i = 0; i < 10; i++, cursor.next()) {
//            assertThat((Iterator<?>) cursor).hasNext();
//        }
//    }
//
//    @ParameterizedTest(name = "{index}")
//    @MethodSource("asyncDbs")
//    void queryWithNullBindVar(ArangoDatabaseAsync db) {
//        final ArangoCursor<Object> cursor = db.query("return @foo", Object.class, Collections.singletonMap("foo", null));
//        assertThat(cursor.hasNext()).isTrue();
//        assertThat(cursor.next()).isNull();
//    }
//
//    @ParameterizedTest(name = "{index}")
//    @MethodSource("asyncDbs")
//    void queryForEach(ArangoDatabaseAsync db) {
//        for (int i = 0; i < 10; i++) {
//            db.collection(CNAME1).insertDocument(new BaseDocument(), null);
//        }
//        final ArangoCursor<String> cursor = db.query("for i in " + CNAME1 + " return i._id", String.class);
//        assertThat((Object) cursor).isNotNull();
//
//        int i = 0;
//        while (cursor.hasNext()) {
//            cursor.next();
//            i++;
//        }
//        assertThat(i).isGreaterThanOrEqualTo(10);
//    }
//
//    @ParameterizedTest(name = "{index}")
//    @MethodSource("asyncDbs")
//    void queryWithCount(ArangoDatabaseAsync db) {
//        for (int i = 0; i < 10; i++) {
//            db.collection(CNAME1).insertDocument(new BaseDocument(), null);
//        }
//
//        final ArangoCursor<String> cursor = db
//                .query("for i in " + CNAME1 + " Limit 6 return i._id", String.class, new AqlQueryOptions().count(true));
//        assertThat((Object) cursor).isNotNull();
//        for (int i = 1; i <= 6; i++, cursor.next()) {
//            assertThat(cursor.hasNext()).isTrue();
//        }
//        assertThat(cursor.getCount()).isEqualTo(6);
//    }
//
//    @ParameterizedTest(name = "{index}")
//    @MethodSource("asyncDbs")
//    void queryWithLimitAndFullCount(ArangoDatabaseAsync db) {
//        for (int i = 0; i < 10; i++) {
//            db.collection(CNAME1).insertDocument(new BaseDocument(), null);
//        }
//
//        final ArangoCursor<String> cursor = db
//                .query("for i in " + CNAME1 + " Limit 5 return i._id", String.class, new AqlQueryOptions().fullCount(true));
//        assertThat((Object) cursor).isNotNull();
//        for (int i = 0; i < 5; i++, cursor.next()) {
//            assertThat((Iterator<?>) cursor).hasNext();
//        }
//        assertThat(cursor.getStats()).isNotNull();
//        assertThat(cursor.getStats().getExecutionTime()).isPositive();
//        assertThat((cursor.getStats().getFullCount())).isGreaterThanOrEqualTo(10);
//    }
//
//    @ParameterizedTest(name = "{index}")
//    @MethodSource("asyncDbs")
//    void queryStats(ArangoDatabaseAsync db) {
//        for (int i = 0; i < 10; i++) {
//            db.collection(CNAME1).insertDocument(new BaseDocument(), null);
//        }
//
//        final ArangoCursor<Object> cursor = db.query("for i in " + CNAME1 + " return i", Object.class);
//        assertThat((Object) cursor).isNotNull();
//        for (int i = 0; i < 5; i++, cursor.next()) {
//            assertThat((Iterator<?>) cursor).hasNext();
//        }
//        assertThat(cursor.getStats()).isNotNull();
//        assertThat(cursor.getStats().getWritesExecuted()).isNotNull();
//        assertThat(cursor.getStats().getWritesIgnored()).isNotNull();
//        assertThat(cursor.getStats().getScannedFull()).isNotNull();
//        assertThat(cursor.getStats().getScannedIndex()).isNotNull();
//        assertThat(cursor.getStats().getFiltered()).isNotNull();
//        assertThat(cursor.getStats().getExecutionTime()).isNotNull();
//        assertThat(cursor.getStats().getPeakMemoryUsage()).isNotNull();
//        if (isAtLeastVersion(3, 10)) {
//            assertThat(cursor.getStats().getCursorsCreated()).isNotNull();
//            assertThat(cursor.getStats().getCursorsRearmed()).isNotNull();
//            assertThat(cursor.getStats().getCacheHits()).isNotNull();
//            assertThat(cursor.getStats().getCacheMisses()).isNotNull();
//        }
//    }
//
//    @ParameterizedTest(name = "{index}")
//    @MethodSource("asyncDbs")
//    void queryWithBatchSize(ArangoDatabaseAsync db) {
//        for (int i = 0; i < 10; i++) {
//            db.collection(CNAME1).insertDocument(new BaseDocument(), null);
//        }
//
//        final ArangoCursor<String> cursor = db
//                .query("for i in " + CNAME1 + " return i._id", String.class, new AqlQueryOptions().batchSize(5).count(true));
//
//        assertThat((Object) cursor).isNotNull();
//        for (int i = 0; i < 10; i++, cursor.next()) {
//            assertThat((Iterator<?>) cursor).hasNext();
//        }
//    }
//
//    @ParameterizedTest(name = "{index}")
//    @MethodSource("asyncDbs")
//    void queryIterateWithBatchSize(ArangoDatabaseAsync db) {
//        for (int i = 0; i < 10; i++) {
//            db.collection(CNAME1).insertDocument(new BaseDocument(), null);
//        }
//
//        final ArangoCursor<String> cursor = db
//                .query("for i in " + CNAME1 + " return i._id", String.class, new AqlQueryOptions().batchSize(5).count(true));
//
//        assertThat((Object) cursor).isNotNull();
//        final AtomicInteger i = new AtomicInteger(0);
//        for (; cursor.hasNext(); cursor.next()) {
//            i.incrementAndGet();
//        }
//        assertThat(i.get()).isGreaterThanOrEqualTo(10);
//    }
//
//    @ParameterizedTest(name = "{index}")
//    @MethodSource("asyncDbs")
//    void queryWithTTL(ArangoDatabaseAsync db) throws InterruptedException {
//        // set TTL to 1 seconds and get the second batch after 2 seconds!
//        final int ttl = 1;
//        final int wait = 2;
//        for (int i = 0; i < 10; i++) {
//            db.collection(CNAME1).insertDocument(new BaseDocument(), null);
//        }
//
//        final ArangoCursor<String> cursor = db
//                .query("for i in " + CNAME1 + " return i._id", String.class, new AqlQueryOptions().batchSize(5).ttl(ttl));
//
//        assertThat((Iterable<String>) cursor).isNotNull();
//
//        try {
//            for (int i = 0; i < 10; i++, cursor.next()) {
//                assertThat(cursor.hasNext()).isTrue();
//                if (i == 1) {
//                    Thread.sleep(wait * 1000);
//                }
//            }
//            fail("this should fail");
//        } catch (final ArangoDBException ex) {
//            assertThat(ex.getMessage()).isEqualTo("Response: 404, Error: 1600 - cursor not found");
//        }
//    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("asyncDbs")
    void changeQueryCache(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
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

        final QueryCachePropertiesEntity properties2 = new QueryCachePropertiesEntity();
        properties2.setMode(CacheMode.off);
        db.setQueryCacheProperties(properties2);
    }

//    @ParameterizedTest(name = "{index}")
//    @MethodSource("asyncDbs")
//    void queryWithCache(ArangoDatabaseAsync db) {
//        assumeTrue(isSingleServer());
//        for (int i = 0; i < 10; i++) {
//            db.collection(CNAME1).insertDocument(new BaseDocument(), null);
//        }
//
//        final QueryCachePropertiesEntity properties = new QueryCachePropertiesEntity();
//        properties.setMode(CacheMode.on);
//        db.setQueryCacheProperties(properties);
//
//        final ArangoCursor<String> cursor = db
//                .query("FOR t IN " + CNAME1 + " FILTER t.age >= 10 SORT t.age RETURN t._id", String.class,
//                        new AqlQueryOptions().cache(true));
//
//        assertThat((Object) cursor).isNotNull();
//        assertThat(cursor.isCached()).isFalse();
//
//        final ArangoCursor<String> cachedCursor = db
//                .query("FOR t IN " + CNAME1 + " FILTER t.age >= 10 SORT t.age RETURN t._id", String.class,
//                        new AqlQueryOptions().cache(true));
//
//        assertThat((Object) cachedCursor).isNotNull();
//        assertThat(cachedCursor.isCached()).isTrue();
//
//        final QueryCachePropertiesEntity properties2 = new QueryCachePropertiesEntity();
//        properties2.setMode(CacheMode.off);
//        db.setQueryCacheProperties(properties2);
//    }
//
//    @ParameterizedTest(name = "{index}")
//    @MethodSource("asyncDbs")
//    void queryWithMemoryLimit(ArangoDatabaseAsync db) {
//        Throwable thrown = catchThrowable(() -> db.query("RETURN 1..100000", String.class,
//                new AqlQueryOptions().memoryLimit(32 * 1024L)));
//        assertThat(thrown).isInstanceOf(ArangoDBException.class);
//        assertThat(((ArangoDBException) thrown).getErrorNum()).isEqualTo(32);
//    }
//
//    @ParameterizedTest(name = "{index}")
//    @MethodSource("asyncDbs")
//    void queryWithFailOnWarningTrue(ArangoDatabaseAsync db) {
//        Throwable thrown = catchThrowable(() -> db.query("RETURN 1 / 0", String.class,
//                new AqlQueryOptions().failOnWarning(true)));
//        assertThat(thrown).isInstanceOf(ArangoDBException.class);
//    }
//
//    @ParameterizedTest(name = "{index}")
//    @MethodSource("asyncDbs")
//    void queryWithFailOnWarningFalse(ArangoDatabaseAsync db) {
//        final ArangoCursor<String> cursor = db
//                .query("RETURN 1 / 0", String.class, new AqlQueryOptions().failOnWarning(false));
//        assertThat(cursor.next()).isNull();
//    }
//
//    @ParameterizedTest(name = "{index}")
//    @MethodSource("asyncDbs")
//    void queryWithTimeout(ArangoDatabaseAsync db) {
//        assumeTrue(isAtLeastVersion(3, 6));
//        Throwable thrown = catchThrowable(() -> db.query("RETURN SLEEP(1)", String.class,
//                new AqlQueryOptions().maxRuntime(0.1)).next());
//        assertThat(thrown).isInstanceOf(ArangoDBException.class);
//        assertThat(((ArangoDBException) thrown).getResponseCode()).isEqualTo(410);
//    }
//
//    @ParameterizedTest(name = "{index}")
//    @MethodSource("asyncDbs")
//    void queryWithMaxWarningCount(ArangoDatabaseAsync db) {
//        final ArangoCursor<String> cursorWithWarnings = db
//                .query("RETURN 1 / 0", String.class, new AqlQueryOptions());
//        assertThat(cursorWithWarnings.getWarnings()).hasSize(1);
//        final ArangoCursor<String> cursorWithLimitedWarnings = db
//                .query("RETURN 1 / 0", String.class, new AqlQueryOptions().maxWarningCount(0L));
//        final Collection<CursorWarning> warnings = cursorWithLimitedWarnings.getWarnings();
//        assertThat(warnings).isNullOrEmpty();
//    }
//
//    @ParameterizedTest(name = "{index}")
//    @MethodSource("asyncDbs")
//    void queryCursor(ArangoDatabaseAsync db) {
//        ArangoCursor<Integer> cursor = db.query("for i in 1..4 return i", Integer.class,
//                new AqlQueryOptions().batchSize(1));
//        List<Integer> result = new ArrayList<>();
//        result.add(cursor.next());
//        result.add(cursor.next());
//        ArangoCursor<Integer> cursor2 = db.cursor(cursor.getId(), Integer.class);
//        result.add(cursor2.next());
//        result.add(cursor2.next());
//        assertThat(cursor2.hasNext()).isFalse();
//        assertThat(result).containsExactly(1, 2, 3, 4);
//    }
//
//    @ParameterizedTest(name = "{index}")
//    @MethodSource("asyncDbs")
//    void queryCursorRetry(ArangoDatabaseAsync db) throws IOException {
//        assumeTrue(isAtLeastVersion(3, 11));
//        ArangoCursor<Integer> cursor = db.query("for i in 1..4 return i", Integer.class,
//                new AqlQueryOptions().batchSize(1).allowRetry(true));
//        List<Integer> result = new ArrayList<>();
//        result.add(cursor.next());
//        result.add(cursor.next());
//        ArangoCursor<Integer> cursor2 = db.cursor(cursor.getId(), Integer.class, cursor.getNextBatchId());
//        result.add(cursor2.next());
//        result.add(cursor2.next());
//        cursor2.close();
//        assertThat(cursor2.hasNext()).isFalse();
//        assertThat(result).containsExactly(1, 2, 3, 4);
//    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("asyncDbs")
    void changeQueryTrackingProperties(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
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

//    @ParameterizedTest(name = "{index}")
//    @MethodSource("asyncDbs")
//    void queryWithBindVars(ArangoDatabaseAsync db) {
//        for (int i = 0; i < 10; i++) {
//            final BaseDocument baseDocument = new BaseDocument(UUID.randomUUID().toString());
//            baseDocument.addAttribute("age", 20 + i);
//            db.collection(CNAME1).insertDocument(baseDocument, null);
//        }
//        final Map<String, Object> bindVars = new HashMap<>();
//        bindVars.put("@coll", CNAME1);
//        bindVars.put("age", 25);
//
//        final ArangoCursor<String> cursor = db
//                .query("FOR t IN @@coll FILTER t.age >= @age SORT t.age RETURN t._id", String.class, bindVars);
//
//        assertThat((Object) cursor).isNotNull();
//
//        for (int i = 0; i < 5; i++, cursor.next()) {
//            assertThat((Iterator<?>) cursor).hasNext();
//        }
//    }
//
//    @ParameterizedTest(name = "{index}")
//    @MethodSource("asyncDbs")
//    void queryWithRawBindVars(ArangoDatabaseAsync db) {
//        final Map<String, Object> bindVars = new HashMap<>();
//        bindVars.put("foo", RawJson.of("\"fooValue\""));
//        bindVars.put("bar", RawBytes.of(db.getSerde().serializeUserData(11)));
//
//        final JsonNode res = db.query("RETURN {foo: @foo, bar: @bar}", JsonNode.class, bindVars).next();
//
//        assertThat(res.get("foo").textValue()).isEqualTo("fooValue");
//        assertThat(res.get("bar").intValue()).isEqualTo(11);
//    }
//
//    @ParameterizedTest(name = "{index}")
//    @MethodSource("asyncArangos")
//    void queryWithWarning(ArangoDBAsync arangoDB) {
//        final ArangoCursor<String> cursor = arangoDB.db().query("return 1/0", String.class);
//
//        assertThat((Object) cursor).isNotNull();
//        assertThat(cursor.getWarnings()).isNotNull();
//    }
//
//    @ParameterizedTest(name = "{index}")
//    @MethodSource("asyncDbs")
//    void queryStream(ArangoDatabaseAsync db) {
//        final ArangoCursor<Void> cursor = db
//                .query("FOR i IN 1..2 RETURN i", Void.class, new AqlQueryOptions().stream(true).count(true));
//        assertThat((Object) cursor).isNotNull();
//        assertThat(cursor.getCount()).isNull();
//    }

//    @ParameterizedTest(name = "{index}")
//    @MethodSource("asyncDbs")
//    void queryForceOneShardAttributeValue(ArangoDatabaseAsync db) {
//        assumeTrue(isAtLeastVersion(3, 10));
//        assumeTrue(isCluster());
//        assumeTrue(isEnterprise());
//
//        String cname = "forceOneShardAttr-" + UUID.randomUUID();
//        db.createCollection(cname, new CollectionCreateOptions()
//                .shardKeys("foo")
//                .numberOfShards(3));
//        ArangoCollection col = db.collection(cname);
//        BaseDocument doc = new BaseDocument();
//        doc.addAttribute("foo", "bar");
//        col.insertDocument(doc);
//
//        ArangoCursor<BaseDocument> c1 = db
//                .query("FOR d IN @@c RETURN d", BaseDocument.class, Collections.singletonMap("@c", cname),
//                        new AqlQueryOptions().forceOneShardAttributeValue("bar"));
//        assertThat(c1.hasNext()).isTrue();
//        assertThat(c1.next().getAttribute("foo")).isEqualTo("bar");
//
//        ArangoCursor<BaseDocument> c2 = db
//                .query("FOR d IN @@c RETURN d", BaseDocument.class, Collections.singletonMap("@c", cname),
//                        new AqlQueryOptions().forceOneShardAttributeValue("ooo"));
//        assertThat(c2.hasNext()).isFalse();
//    }
//
//    @ParameterizedTest(name = "{index}")
//    @MethodSource("asyncArangos")
//    void queryClose(ArangoDBAsync arangoDB) throws IOException {
//        final ArangoCursor<String> cursor = arangoDB.db()
//                .query("for i in 1..2 return i", String.class, new AqlQueryOptions().batchSize(1));
//        cursor.close();
//        AtomicInteger count = new AtomicInteger();
//        Throwable thrown = catchThrowable(() -> {
//            while (cursor.hasNext()) {
//                cursor.next();
//                count.incrementAndGet();
//            }
//        });
//
//        assertThat(thrown).isInstanceOf(ArangoDBException.class);
//        assertThat(count).hasValue(1);
//    }
//
//    @ParameterizedTest(name = "{index}")
//    @MethodSource("asyncDbs")
//    void queryNoResults(ArangoDatabaseAsync db) throws IOException {
//        final ArangoCursor<BaseDocument> cursor = db
//                .query("FOR i IN @@col RETURN i", BaseDocument.class, new MapBuilder().put("@col", CNAME1).get());
//        cursor.close();
//    }
//
//    @ParameterizedTest(name = "{index}")
//    @MethodSource("asyncDbs")
//    void queryWithNullBindParam(ArangoDatabaseAsync db) throws IOException {
//        final ArangoCursor<BaseDocument> cursor = db.query("FOR i IN @@col FILTER i.test == @test RETURN i",
//                BaseDocument.class, new MapBuilder().put("@col", CNAME1).put("test", null).get());
//        cursor.close();
//    }
//
//    @ParameterizedTest(name = "{index}")
//    @MethodSource("asyncDbs")
//    void queryAllowDirtyRead(ArangoDatabaseAsync db) throws IOException {
//        final ArangoCursor<BaseDocument> cursor = db.query("FOR i IN @@col FILTER i.test == @test RETURN i",
//                BaseDocument.class, new MapBuilder().put("@col", CNAME1).put("test", null).get(),
//                new AqlQueryOptions().allowDirtyRead(true));
//        if (isAtLeastVersion(3, 10)) {
//            assertThat(cursor.isPotentialDirtyRead()).isTrue();
//        }
//        cursor.close();
//    }
//
//    @ParameterizedTest(name = "{index}")
//    @MethodSource("asyncArangos")
//    void queryAllowRetry(ArangoDBAsync arangoDB) throws IOException {
//        assumeTrue(isAtLeastVersion(3, 11));
//        final ArangoCursor<String> cursor = arangoDB.db()
//                .query("for i in 1..2 return i", String.class, new AqlQueryOptions().allowRetry(true).batchSize(1));
//        assertThat(cursor.asListRemaining()).containsExactly("1", "2");
//    }
//
//    @ParameterizedTest(name = "{index}")
//    @MethodSource("asyncArangos")
//    void queryAllowRetryClose(ArangoDBAsync arangoDB) throws IOException {
//        assumeTrue(isAtLeastVersion(3, 11));
//        final ArangoCursor<String> cursor = arangoDB.db()
//                .query("for i in 1..2 return i", String.class, new AqlQueryOptions().allowRetry(true).batchSize(1));
//        assertThat(cursor.hasNext()).isTrue();
//        assertThat(cursor.next()).isEqualTo("1");
//        assertThat(cursor.hasNext()).isTrue();
//        assertThat(cursor.next()).isEqualTo("2");
//        assertThat(cursor.hasNext()).isFalse();
//        cursor.close();
//    }
//
//    @ParameterizedTest(name = "{index}")
//    @MethodSource("asyncArangos")
//    void queryAllowRetryCloseBeforeLatestBatch(ArangoDBAsync arangoDB) throws IOException {
//        assumeTrue(isAtLeastVersion(3, 11));
//        final ArangoCursor<String> cursor = arangoDB.db()
//                .query("for i in 1..2 return i", String.class, new AqlQueryOptions().allowRetry(true).batchSize(1));
//        assertThat(cursor.hasNext()).isTrue();
//        assertThat(cursor.next()).isEqualTo("1");
//        assertThat(cursor.hasNext()).isTrue();
//        cursor.close();
//    }
//
//    @ParameterizedTest(name = "{index}")
//    @MethodSource("asyncArangos")
//    void queryAllowRetryCloseSingleBatch(ArangoDBAsync arangoDB) throws IOException {
//        assumeTrue(isAtLeastVersion(3, 11));
//        final ArangoCursor<String> cursor = arangoDB.db()
//                .query("for i in 1..2 return i", String.class, new AqlQueryOptions().allowRetry(true));
//        assertThat(cursor.hasNext()).isTrue();
//        assertThat(cursor.next()).isEqualTo("1");
//        assertThat(cursor.hasNext()).isTrue();
//        assertThat(cursor.next()).isEqualTo("2");
//        assertThat(cursor.hasNext()).isFalse();
//        cursor.close();
//    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("asyncDbs")
    void explainQuery(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        final AqlExecutionExplainEntity explain = db.explainQuery("for i in 1..1 return i", null, null).get();
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

    @ParameterizedTest(name = "{index}")
    @MethodSource("asyncDbs")
    void explainQueryWithBindVars(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        final AqlExecutionExplainEntity explain = db.explainQuery("for i in 1..1 return @value",
                Collections.singletonMap("value", 11), null).get();
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

//    @ParameterizedTest(name = "{index}")
//    @MethodSource("asyncDbs")
//    void explainQueryWithIndexNode(ArangoDatabaseAsync db) {
//        ArangoCollection character = db.collection("got_characters");
//        ArangoCollection actor = db.collection("got_actors");
//
//        if (!character.exists())
//            character.create();
//
//        if (!actor.exists())
//            actor.create();
//
//        String query = "" +
//                "FOR `character` IN `got_characters` " +
//                "   FOR `actor` IN `got_actors` " +
//                "       FILTER `character`.`actor` == `actor`.`_id` " +
//                "       RETURN `character`";
//
//        final ExecutionPlan plan = db.explainQuery(query, null, null).getPlan();
//        plan.getNodes().stream()
//                .filter(it -> "IndexNode".equals(it.getType()))
//                .flatMap(it -> it.getIndexes().stream())
//                .forEach(it -> {
//                    assertThat(it.getType()).isEqualTo(IndexType.primary);
//                    assertThat(it.getFields()).contains("_key");
//                });
//    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("asyncDbs")
    void parseQuery(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        final AqlParseEntity parse = db.parseQuery("for i in 1..1 return i").get();
        assertThat(parse).isNotNull();
        assertThat(parse.getBindVars()).isEmpty();
        assertThat(parse.getCollections()).isEmpty();
        assertThat(parse.getAst()).hasSize(1);
    }

//    @ParameterizedTest(name = "{index}")
//    @MethodSource("asyncDbs")
//    void getCurrentlyRunningQueries(ArangoDatabaseAsync db) throws InterruptedException {
//        String query = "return sleep(1)";
//        Thread t = new Thread(() -> db.query(query, Void.class));
//        t.start();
//        Thread.sleep(300);
//        final Collection<QueryEntity> currentlyRunningQueries = db.getCurrentlyRunningQueries();
//        assertThat(currentlyRunningQueries).hasSize(1);
//        final QueryEntity queryEntity = currentlyRunningQueries.iterator().next();
//        assertThat(queryEntity.getId()).isNotNull();
//        assertThat(queryEntity.getDatabase()).isEqualTo(db.name());
//        assertThat(queryEntity.getUser()).isEqualTo("root");
//        assertThat(queryEntity.getQuery()).isEqualTo(query);
//        assertThat(queryEntity.getBindVars()).isEmpty();
//        assertThat(queryEntity.getStarted()).isInThePast();
//        assertThat(queryEntity.getRunTime()).isPositive();
//        if (isAtLeastVersion(3, 11)) {
//            assertThat(queryEntity.getPeakMemoryUsage()).isNotNull();
//        }
//        assertThat(queryEntity.getState()).isEqualTo(QueryExecutionState.EXECUTING);
//        assertThat(queryEntity.getStream()).isFalse();
//        t.join();
//    }
//
//    @ParameterizedTest(name = "{index}")
//    @MethodSource("asyncDbs")
//    void killQuery(ArangoDatabaseAsync db) throws InterruptedException, ExecutionException {
//        ExecutorService es = Executors.newSingleThreadExecutor();
//        Future<?> future = es.submit(() -> {
//            try {
//                db.query("return sleep(5)", Void.class);
//                fail();
//            } catch (ArangoDBException e) {
//                assertThat(e.getResponseCode()).isEqualTo(410);
//                assertThat(e.getErrorNum()).isEqualTo(1500);
//                assertThat(e.getErrorMessage()).contains("query killed");
//            }
//        });
//        Thread.sleep(500);
//
//        Collection<QueryEntity> currentlyRunningQueries = db.getCurrentlyRunningQueries();
//        assertThat(currentlyRunningQueries).hasSize(1);
//        QueryEntity queryEntity = currentlyRunningQueries.iterator().next();
//        assertThat(queryEntity.getState()).isEqualTo(QueryExecutionState.EXECUTING);
//        db.killQuery(queryEntity.getId());
//
//        db.getCurrentlyRunningQueries().forEach(q ->
//                assertThat(q.getState()).isEqualTo(QueryExecutionState.KILLED)
//        );
//
//        future.get();
//        es.shutdown();
//    }
//
//    @ParameterizedTest(name = "{index}")
//    @MethodSource("asyncDbs")
//    void getAndClearSlowQueries(ArangoDatabaseAsync db) {
//        db.clearSlowQueries();
//
//        final QueryTrackingPropertiesEntity properties = db.getQueryTrackingProperties();
//        final Long slowQueryThreshold = properties.getSlowQueryThreshold();
//        properties.setSlowQueryThreshold(1L);
//        db.setQueryTrackingProperties(properties);
//
//        String query = "return sleep(1.1)";
//        db.query(query, Void.class);
//        final Collection<QueryEntity> slowQueries = db.getSlowQueries();
//        assertThat(slowQueries).hasSize(1);
//        final QueryEntity queryEntity = slowQueries.iterator().next();
//        assertThat(queryEntity.getId()).isNotNull();
//        assertThat(queryEntity.getDatabase()).isEqualTo(db.name());
//        assertThat(queryEntity.getUser()).isEqualTo("root");
//        assertThat(queryEntity.getQuery()).isEqualTo(query);
//        assertThat(queryEntity.getBindVars()).isEmpty();
//        assertThat(queryEntity.getStarted()).isInThePast();
//        assertThat(queryEntity.getRunTime()).isPositive();
//        if (isAtLeastVersion(3, 11)) {
//            assertThat(queryEntity.getPeakMemoryUsage()).isNotNull();
//        }
//        assertThat(queryEntity.getState()).isEqualTo(QueryExecutionState.FINISHED);
//        assertThat(queryEntity.getStream()).isFalse();
//
//        db.clearSlowQueries();
//        assertThat(db.getSlowQueries()).isEmpty();
//        properties.setSlowQueryThreshold(slowQueryThreshold);
//        db.setQueryTrackingProperties(properties);
//    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("asyncDbs")
    void createGetDeleteAqlFunction(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        final Collection<AqlFunctionEntity> aqlFunctionsInitial = db.getAqlFunctions(null).get();
        assertThat(aqlFunctionsInitial).isEmpty();
        try {
            db.createAqlFunction("myfunctions::temperature::celsiustofahrenheit",
                    "function (celsius) { return celsius * 1.8 + 32; }", null).get();

            final Collection<AqlFunctionEntity> aqlFunctions = db.getAqlFunctions(null).get();
            assertThat(aqlFunctions).hasSizeGreaterThan(aqlFunctionsInitial.size());
        } finally {
            final Integer deleteCount = db.deleteAqlFunction("myfunctions::temperature::celsiustofahrenheit", null).get();
            // compatibility with ArangoDB < 3.4
            if (isAtLeastVersion(3, 4)) {
                assertThat(deleteCount).isEqualTo(1);
            } else {
                assertThat(deleteCount).isNull();
            }
            final Collection<AqlFunctionEntity> aqlFunctions = db.getAqlFunctions(null).get();
            assertThat(aqlFunctions).hasSize(aqlFunctionsInitial.size());
        }
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("asyncDbs")
    void createGetDeleteAqlFunctionWithNamespace(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        final Collection<AqlFunctionEntity> aqlFunctionsInitial = db.getAqlFunctions(null).get();
        assertThat(aqlFunctionsInitial).isEmpty();
        try {
            db.createAqlFunction("myfunctions::temperature::celsiustofahrenheit1",
                    "function (celsius) { return celsius * 1.8 + 32; }", null).get();
            db.createAqlFunction("myfunctions::temperature::celsiustofahrenheit2",
                    "function (celsius) { return celsius * 1.8 + 32; }", null).get();

        } finally {
            final Integer deleteCount = db
                    .deleteAqlFunction("myfunctions::temperature", new AqlFunctionDeleteOptions().group(true)).get();
            // compatibility with ArangoDB < 3.4
            if (isAtLeastVersion(3, 4)) {
                assertThat(deleteCount).isEqualTo(2);
            } else {
                assertThat(deleteCount).isNull();
            }
            final Collection<AqlFunctionEntity> aqlFunctions = db.getAqlFunctions(null).get();
            assertThat(aqlFunctions).hasSize(aqlFunctionsInitial.size());
        }
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("asyncDbs")
    void createGraph(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        String name = "graph-" + rnd();
        final GraphEntity result = db.createGraph(name, null, null).get();
        assertThat(result.getName()).isEqualTo(name);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("asyncDbs")
    void createGraphSatellite(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        assumeTrue(isAtLeastVersion(3, 7));
        assumeTrue(isCluster());
        assumeTrue(isEnterprise());

        String name = "graph-" + rnd();
        final GraphEntity result = db.createGraph(name, null, new GraphCreateOptions().replicationFactor(ReplicationFactor.ofSatellite())).get();
        assertThat(result.getReplicationFactor()).isEqualTo(ReplicationFactor.ofSatellite());

//        GraphEntity info = db.graph(name).getInfo();
//        assertThat(info.getReplicationFactor()).isEqualTo(ReplicationFactor.ofSatellite());
//
//        GraphEntity graph = db.getGraphs().stream().filter(g -> name.equals(g.getName())).findFirst().get();
//        assertThat(graph.getReplicationFactor()).isEqualTo(ReplicationFactor.ofSatellite());
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("asyncDbs")
    void createGraphReplicationFaktor(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        assumeTrue(isCluster());
        String name = "graph-" + rnd();
        final String edgeCollection = rndName();
        final String fromCollection = rndName();
        final String toCollection = rndName();
        final Collection<EdgeDefinition> edgeDefinitions =
                Collections.singletonList(new EdgeDefinition().collection(edgeCollection).from(fromCollection).to(toCollection));
        final GraphEntity result = db.createGraph(name, edgeDefinitions, new GraphCreateOptions().replicationFactor(2)).get();
        assertThat(result).isNotNull();
        for (final String collection : Arrays.asList(edgeCollection, fromCollection, toCollection)) {
            final CollectionPropertiesEntity properties = db.collection(collection).getProperties().get();
            assertThat(properties.getReplicationFactor().get()).isEqualTo(2);
        }
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("asyncDbs")
    void createGraphNumberOfShards(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        assumeTrue(isCluster());
        String name = "graph-" + rnd();
        final String edgeCollection = rndName();
        final String fromCollection = rndName();
        final String toCollection = rndName();
        final Collection<EdgeDefinition> edgeDefinitions =
                Collections.singletonList(new EdgeDefinition().collection(edgeCollection).from(fromCollection).to(toCollection));
        final GraphEntity result = db
                .createGraph(name, edgeDefinitions, new GraphCreateOptions().numberOfShards(2)).get();
        assertThat(result).isNotNull();
        for (final String collection : Arrays.asList(edgeCollection, fromCollection, toCollection)) {
            final CollectionPropertiesEntity properties = db.collection(collection).getProperties().get();
            assertThat(properties.getNumberOfShards()).isEqualTo(2);
        }
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("asyncDbs")
    void getGraphs(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        String name = "graph-" + rnd();
        db.createGraph(name, null, null).get();
        final Collection<GraphEntity> graphs = db.getGraphs().get();
        assertThat(graphs).hasSizeGreaterThanOrEqualTo(1);
        long count = graphs.stream().map(GraphEntity::getName).filter(name::equals).count();
        assertThat(count).isEqualTo(1L);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("asyncDbs")
    void transactionString(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        final TransactionOptions options = new TransactionOptions().params("test");
        final RawJson result = db.transaction("function (params) {return params;}", RawJson.class, options).get();
        assertThat(result.get()).isEqualTo("\"test\"");
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("asyncDbs")
    void transactionNumber(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        final TransactionOptions options = new TransactionOptions().params(5);
        final Integer result = db.transaction("function (params) {return params;}", Integer.class, options).get();
        assertThat(result).isEqualTo(5);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("asyncDbs")
    void transactionJsonNode(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        final TransactionOptions options = new TransactionOptions().params(JsonNodeFactory.instance.textNode("test"));
        final JsonNode result = db.transaction("function (params) {return params;}", JsonNode.class, options).get();
        assertThat(result.isTextual()).isTrue();
        assertThat(result.asText()).isEqualTo("test");
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("asyncDbs")
    void transactionJsonObject(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        ObjectNode params = JsonNodeFactory.instance.objectNode().put("foo", "hello").put("bar", "world");
        final TransactionOptions options = new TransactionOptions().params(params);
        final RawJson result = db
                .transaction("function (params) { return params['foo'] + ' ' + params['bar'];}", RawJson.class,
                        options).get();
        assertThat(result.get()).isEqualTo("\"hello world\"");
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("asyncDbs")
    void transactionJsonArray(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        ArrayNode params = JsonNodeFactory.instance.arrayNode().add("hello").add("world");
        final TransactionOptions options = new TransactionOptions().params(params);
        final RawJson result = db
                .transaction("function (params) { return params[0] + ' ' + params[1];}", RawJson.class, options).get();
        assertThat(result.get()).isEqualTo("\"hello world\"");
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("asyncDbs")
    void transactionMap(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        final Map<String, Object> params = new MapBuilder().put("foo", "hello").put("bar", "world").get();
        final TransactionOptions options = new TransactionOptions().params(params);
        final RawJson result = db
                .transaction("function (params) { return params['foo'] + ' ' + params['bar'];}", RawJson.class,
                        options).get();
        assertThat(result.get()).isEqualTo("\"hello world\"");
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("asyncDbs")
    void transactionArray(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        final String[] params = new String[]{"hello", "world"};
        final TransactionOptions options = new TransactionOptions().params(params);
        final RawJson result = db
                .transaction("function (params) { return params[0] + ' ' + params[1];}", RawJson.class, options).get();
        assertThat(result.get()).isEqualTo("\"hello world\"");
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("asyncDbs")
    void transactionCollection(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        final Collection<String> params = new ArrayList<>();
        params.add("hello");
        params.add("world");
        final TransactionOptions options = new TransactionOptions().params(params);
        final RawJson result = db
                .transaction("function (params) { return params[0] + ' ' + params[1];}", RawJson.class, options).get();
        assertThat(result.get()).isEqualTo("\"hello world\"");
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("asyncDbs")
    void transactionInsertJson(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        String key = "key-" + rnd();
        final TransactionOptions options = new TransactionOptions().params("{\"_key\":\"" + key + "\"}")
                .writeCollections(CNAME1);
        db.transaction("function (params) { "
                + "var db = require('internal').db;"
                + "db." + CNAME1 + ".save(JSON.parse(params));"
                + "}", Void.class, options).get();
        assertThat(db.collection(CNAME1).getDocument(key, RawJson.class).get()).isNotNull();
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("asyncDbs")
    void transactionExclusiveWrite(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        assumeTrue(isAtLeastVersion(3, 4));
        String key = "key-" + rnd();
        final TransactionOptions options = new TransactionOptions().params("{\"_key\":\"" + key + "\"}")
                .exclusiveCollections(CNAME1);
        db.transaction("function (params) { "
                + "var db = require('internal').db;"
                + "db." + CNAME1 + ".save(JSON.parse(params));"
                + "}", Void.class, options).get();
        assertThat(db.collection(CNAME1).getDocument(key, RawJson.class).get()).isNotNull();
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("asyncDbs")
    void transactionEmpty(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        db.transaction("function () {}", Void.class, null).get();
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("asyncDbs")
    void transactionAllowImplicit(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        final String action = "function (params) {" + "var db = require('internal').db;"
                + "return {'a':db." + CNAME1 + ".all().toArray()[0], 'b':db." + CNAME2 + ".all().toArray()[0]};"
                + "}";
        final TransactionOptions options = new TransactionOptions().readCollections(CNAME1);
        db.transaction(action, JsonNode.class, options).get();
        options.allowImplicit(false);
        Throwable thrown = catchThrowable(() -> db.transaction(action, JsonNode.class, options).get()).getCause();
        assertThat(thrown)
                .isInstanceOf(ArangoDBException.class)
                .extracting(it -> ((ArangoDBException) it).getResponseCode())
                .isEqualTo(400);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("asyncDbs")
    void transactionPojoReturn(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        final String action = "function() { return {'value':'hello world'}; }";
        final TransactionTestEntity res = db.transaction(action, TransactionTestEntity.class, new TransactionOptions()).get();
        assertThat(res).isNotNull();
        assertThat(res.value).isEqualTo("hello world");
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("asyncDbs")
    void getInfo(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        final DatabaseEntity info = db.getInfo().get();
        assertThat(info).isNotNull();
        assertThat(info.getId()).isNotNull();
        assertThat(info.getName()).isEqualTo(TEST_DB);
        assertThat(info.getPath()).isNotNull();
        assertThat(info.getIsSystem()).isFalse();

        if (isAtLeastVersion(3, 6) && isCluster()) {
            assertThat(info.getSharding()).isNotNull();
            assertThat(info.getWriteConcern()).isNotNull();
            assertThat(info.getReplicationFactor()).isNotNull();
        }
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("asyncDbs")
    void shouldIncludeExceptionMessage(ArangoDatabaseAsync db) {
        assumeTrue(isAtLeastVersion(3, 4));

        final String exceptionMessage = "My error context";
        final String action = "function (params) {" + "throw '" + exceptionMessage + "';" + "}";
        Throwable thrown = catchThrowable(() -> db.transaction(action, Void.class, null).get()).getCause();
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
        assertThat(((ArangoDBException) thrown).getErrorMessage()).isEqualTo(exceptionMessage);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("asyncDbs")
    void reloadRouting(ArangoDatabaseAsync db) {
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
