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
import com.arangodb.entity.QueryCachePropertiesEntity.CacheMode;
import com.arangodb.internal.serde.InternalSerde;
import com.arangodb.model.*;
import com.arangodb.util.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.InstanceOfAssertFactories.*;
import static org.assertj.core.api.InstanceOfAssertFactories.DOUBLE;
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

    @ParameterizedTest
    @MethodSource("asyncDbs")
    void getVersion(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        final ArangoDBVersion version = db.getVersion().get();
        assertThat(version).isNotNull();
        assertThat(version.getServer()).isNotNull();
        assertThat(version.getVersion()).isNotNull();
    }

    @ParameterizedTest
    @MethodSource("asyncDbs")
    void getEngine(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        final ArangoDBEngine engine = db.getEngine().get();
        assertThat(engine).isNotNull();
        assertThat(engine.getName()).isNotNull();
    }

    @ParameterizedTest
    @MethodSource("asyncArangos")
    void exists(ArangoDBAsync arangoDB) throws ExecutionException, InterruptedException {
        assertThat(arangoDB.db(getTestDb()).exists().get()).isTrue();
        assertThat(arangoDB.db("no").exists().get()).isFalse();
    }

    @ParameterizedTest
    @MethodSource("asyncDbs")
    void getAccessibleDatabases(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        final Collection<String> dbs = db.getAccessibleDatabases().get();
        assertThat(dbs).contains("_system");
    }

    @ParameterizedTest
    @MethodSource("asyncDbs")
    void createCollection(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        String name = rndName();
        final CollectionEntity result = db.createCollection(name, null).get();
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
    }

    @ParameterizedTest
    @MethodSource("asyncDbs")
    void createCollectionWithNotNormalizedName(ArangoDatabaseAsync db) {
        assumeTrue(supportsExtendedNames());
        final String colName = "testCol-\u006E\u0303\u00f1";

        Throwable thrown = catchThrowable(() -> db.createCollection(colName).get()).getCause();
        assertThat(thrown)
                .isInstanceOf(ArangoDBException.class)
                .hasMessageContaining("normalized")
                .extracting(it -> ((ArangoDBException) it).getResponseCode()).isEqualTo(400);
    }

    @ParameterizedTest
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

    @ParameterizedTest
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

    @ParameterizedTest
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

    @ParameterizedTest
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

    @ParameterizedTest
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

    @ParameterizedTest
    @MethodSource("asyncDbs")
    void createCollectionWithSmartJoinAttribute(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isEnterprise());
        assumeTrue(isCluster());

        String fooName = rndName();
        db.collection(fooName).create().get();

        String name = rndName();
        final CollectionEntity result = db.createCollection(name,
                new CollectionCreateOptions().smartJoinAttribute("test123").distributeShardsLike(fooName).shardKeys("_key:")).get();
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(db.collection(name).getProperties().get().getSmartJoinAttribute()).isEqualTo("test123");
    }

    @ParameterizedTest
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

    @ParameterizedTest
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

    @ParameterizedTest
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

    @ParameterizedTest
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

    @ParameterizedTest
    @MethodSource("asyncDbs")
    void createCollectionWithKeyTypeAutoincrement(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        assumeTrue(isSingleServer());
        createCollectionWithKeyType(db, KeyType.autoincrement);
    }

    @ParameterizedTest
    @MethodSource("asyncDbs")
    void createCollectionWithKeyTypePadded(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        assumeTrue(isAtLeastVersion(3, 4));
        createCollectionWithKeyType(db, KeyType.padded);
    }

    @ParameterizedTest
    @MethodSource("asyncDbs")
    void createCollectionWithKeyTypeTraditional(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        createCollectionWithKeyType(db, KeyType.traditional);
    }

    @ParameterizedTest
    @MethodSource("asyncDbs")
    void createCollectionWithKeyTypeUuid(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        assumeTrue(isAtLeastVersion(3, 4));
        createCollectionWithKeyType(db, KeyType.uuid);
    }

    @ParameterizedTest
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
        db.collection(name).insertDocument(doc).get();

        BaseDocument wrongDoc = new BaseDocument(UUID.randomUUID().toString());
        wrongDoc.addAttribute("number", "notANumber");
        Throwable thrown = catchThrowable(() -> db.collection(name).insertDocument(wrongDoc).get()).getCause();
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
        ArangoDBException e = (ArangoDBException) thrown;

        assertThat(e).hasMessageContaining(message);
        assertThat(e.getResponseCode()).isEqualTo(400);
        assertThat(e.getErrorNum()).isEqualTo(1620);
    }

    @ParameterizedTest
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

    @ParameterizedTest
    @MethodSource("asyncDbs")
    void deleteCollection(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        String name = rndName();
        db.createCollection(name, null).get();
        db.collection(name).drop().get();
        Throwable thrown = catchThrowable(() -> db.collection(name).getInfo().get()).getCause();
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
    }

    @ParameterizedTest
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

    @ParameterizedTest
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

    @ParameterizedTest
    @MethodSource("asyncDbs")
    void getIndex(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        final Collection<String> fields = Collections.singletonList("field-" + rnd());
        final IndexEntity createResult = db.collection(CNAME1).ensurePersistentIndex(fields, null).get();
        final IndexEntity readResult = db.getIndex(createResult.getId()).get();
        assertThat(readResult.getId()).isEqualTo(createResult.getId());
        assertThat(readResult.getType()).isEqualTo(createResult.getType());
    }

    @ParameterizedTest
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

    @ParameterizedTest
    @MethodSource("asyncDbs")
    void getCollections(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        final Collection<CollectionEntity> collections = db.getCollections(null).get();
        long count = collections.stream().map(CollectionEntity::getName).filter(it -> it.equals(CNAME1)).count();
        assertThat(count).isEqualTo(1L);
    }

    @ParameterizedTest
    @MethodSource("asyncDbs")
    void getCollectionsExcludeSystem(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        final CollectionsReadOptions options = new CollectionsReadOptions().excludeSystem(true);
        final Collection<CollectionEntity> nonSystemCollections = db.getCollections(options).get();
        final Collection<CollectionEntity> allCollections = db.getCollections(null).get();
        assertThat(allCollections).hasSizeGreaterThan(nonSystemCollections.size());
    }

    @ParameterizedTest
    @MethodSource("asyncArangos")
    void grantAccess(ArangoDBAsync arangoDB) throws ExecutionException, InterruptedException {
        String user = "user-" + rnd();
        arangoDB.createUser(user, "1234", null).get();
        try {
            arangoDB.db(getTestDb()).grantAccess(user).get();
        } finally {
            arangoDB.deleteUser(user).get();
        }
    }

    @ParameterizedTest
    @MethodSource("asyncArangos")
    void grantAccessRW(ArangoDBAsync arangoDB) throws ExecutionException, InterruptedException {
        String user = "user-" + rnd();
        arangoDB.createUser(user, "1234", null).get();
        try {
            arangoDB.db(getTestDb()).grantAccess(user, Permissions.RW).get();
        } finally {
            arangoDB.deleteUser(user).get();
        }
    }

    @ParameterizedTest
    @MethodSource("asyncArangos")
    void grantAccessRO(ArangoDBAsync arangoDB) throws ExecutionException, InterruptedException {
        String user = "user-" + rnd();
        arangoDB.createUser(user, "1234", null).get();
        try {
            arangoDB.db(getTestDb()).grantAccess(user, Permissions.RO).get();
        } finally {
            arangoDB.deleteUser(user).get();
        }
    }

    @ParameterizedTest
    @MethodSource("asyncArangos")
    void grantAccessNONE(ArangoDBAsync arangoDB) throws ExecutionException, InterruptedException {
        String user = "user-" + rnd();
        arangoDB.createUser(user, "1234", null).get();
        try {
            arangoDB.db(getTestDb()).grantAccess(user, Permissions.NONE).get();
        } finally {
            arangoDB.deleteUser(user).get();
        }
    }

    @ParameterizedTest
    @MethodSource("asyncDbs")
    void grantAccessUserNotFound(ArangoDatabaseAsync db) {
        String user = "user-" + rnd();
        Throwable thrown = catchThrowable(() -> db.grantAccess(user, Permissions.RW).get()).getCause();
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
    }

    @ParameterizedTest
    @MethodSource("asyncArangos")
    void revokeAccess(ArangoDBAsync arangoDB) throws ExecutionException, InterruptedException {
        String user = "user-" + rnd();
        arangoDB.createUser(user, "1234", null).get();
        try {
            arangoDB.db(getTestDb()).revokeAccess(user).get();
        } finally {
            arangoDB.deleteUser(user).get();
        }
    }

    @ParameterizedTest
    @MethodSource("asyncDbs")
    void revokeAccessUserNotFound(ArangoDatabaseAsync db) {
        String user = "user-" + rnd();
        Throwable thrown = catchThrowable(() -> db.revokeAccess(user).get()).getCause();
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
    }

    @ParameterizedTest
    @MethodSource("asyncArangos")
    void resetAccess(ArangoDBAsync arangoDB) throws ExecutionException, InterruptedException {
        String user = "user-" + rnd();
        arangoDB.createUser(user, "1234", null).get();
        try {
            arangoDB.db(getTestDb()).resetAccess(user).get();
        } finally {
            arangoDB.deleteUser(user).get();
        }
    }

    @ParameterizedTest
    @MethodSource("asyncDbs")
    void resetAccessUserNotFound(ArangoDatabaseAsync db) {
        String user = "user-" + rnd();
        Throwable thrown = catchThrowable(() -> db.resetAccess(user).get()).getCause();
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
    }

    @ParameterizedTest
    @MethodSource("asyncArangos")
    void grantDefaultCollectionAccess(ArangoDBAsync arangoDB) throws ExecutionException, InterruptedException {
        String user = "user-" + rnd();
        arangoDB.createUser(user, "1234").get();
        try {
            arangoDB.db(getTestDb()).grantDefaultCollectionAccess(user, Permissions.RW).get();
        } finally {
            arangoDB.deleteUser(user).get();
        }
    }

    @ParameterizedTest
    @MethodSource("asyncDbs")
    void getPermissions(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        assertThat(db.getPermissions("root").get()).isEqualTo(Permissions.RW);
    }

    @ParameterizedTest
    @MethodSource("asyncDbs")
    void query(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        ArangoCursorAsync<Integer> cursor = db.query("for i in 0..9 return i", Integer.class).get();
        List<Integer> res = cursor.getResult();
        assertThat(res).hasSize(10);
        for (int i = 0; i < 10; i++) {
            assertThat(res.get(i)).isEqualTo(i);
        }
    }

    @ParameterizedTest
    @MethodSource("asyncDbs")
    void queryWithNullBindVar(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        final ArangoCursorAsync<Object> cursor = db.query("return @foo", Object.class, Collections.singletonMap("foo", null)).get();
        assertThat(cursor.getResult()).containsExactly((Object) null);
    }

    @ParameterizedTest
    @MethodSource("asyncDbs")
    void queryForEach(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        for (int i = 0; i < 10; i++) {
            db.collection(CNAME1).insertDocument(new BaseDocument(), null).get();
        }
        final ArangoCursorAsync<String> cursor = db.query("for i in " + CNAME1 + " return i._id", String.class).get();
        assertThat(cursor.getResult()).hasSizeGreaterThanOrEqualTo(10);
    }

    @ParameterizedTest
    @MethodSource("asyncDbs")
    void queryWithCount(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        for (int i = 0; i < 10; i++) {
            db.collection(CNAME1).insertDocument(new BaseDocument(), null).get();
        }

        final ArangoCursorAsync<String> cursor = db
                .query("for i in " + CNAME1 + " Limit 6 return i._id", String.class, new AqlQueryOptions().count(true)).get();
        assertThat(cursor.getCount()).isEqualTo(6);
        assertThat(cursor.getResult()).hasSize(6);
    }

    @ParameterizedTest
    @MethodSource("asyncDbs")
    void queryWithLimitAndFullCount(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        for (int i = 0; i < 10; i++) {
            db.collection(CNAME1).insertDocument(new BaseDocument(), null).get();
        }

        final ArangoCursorAsync<String> cursor = db
                .query("for i in " + CNAME1 + " Limit 5 return i._id", String.class, new AqlQueryOptions().fullCount(true)).get();
        assertThat(cursor.getResult()).hasSize(5);
        assertThat(cursor.getExtra().getStats()).isNotNull();
        assertThat(cursor.getExtra().getStats().getExecutionTime()).isPositive();
        assertThat((cursor.getExtra().getStats().getFullCount())).isGreaterThanOrEqualTo(10);
    }

    @ParameterizedTest
    @MethodSource("asyncDbs")
    void queryStats(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        for (int i = 0; i < 10; i++) {
            db.collection(CNAME1).insertDocument(new BaseDocument(), null).get();
        }

        final ArangoCursorAsync<Object> cursor = db.query("for i in " + CNAME1 + " return i", Object.class).get();
        assertThat(cursor.getResult()).hasSizeGreaterThanOrEqualTo(10);
        assertThat(cursor.getExtra().getStats()).isNotNull();
        assertThat(cursor.getExtra().getStats().getWritesExecuted()).isNotNull();
        assertThat(cursor.getExtra().getStats().getWritesIgnored()).isNotNull();
        assertThat(cursor.getExtra().getStats().getScannedFull()).isNotNull();
        assertThat(cursor.getExtra().getStats().getScannedIndex()).isNotNull();
        assertThat(cursor.getExtra().getStats().getFiltered()).isNotNull();
        assertThat(cursor.getExtra().getStats().getExecutionTime()).isNotNull();
        assertThat(cursor.getExtra().getStats().getPeakMemoryUsage()).isNotNull();
        assertThat(cursor.getExtra().getStats().getIntermediateCommits()).isNotNull();
        if (isAtLeastVersion(3, 12)) {
            assertThat(cursor.getExtra().getStats().getDocumentLookups()).isNotNull();
            assertThat(cursor.getExtra().getStats().getSeeks()).isNotNull();
        }
    }

    @ParameterizedTest
    @MethodSource("asyncDbs")
    void queryWithBatchSize(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        final ArangoCursorAsync<Integer> cursor = db
                .query("for i in 1..10 return i", Integer.class, new AqlQueryOptions().batchSize(5)).get();

        assertThat(cursor.getResult()).hasSize(5);
        assertThat(cursor.hasMore()).isTrue();

        ArangoCursorAsync<Integer> c2 = cursor.nextBatch().get();
        assertThat(c2.getResult()).hasSize(5);
        assertThat(c2.hasMore()).isFalse();
    }

    @SlowTest
    @ParameterizedTest
    @MethodSource("asyncDbs")
    void queryWithTTL(ArangoDatabaseAsync db) throws InterruptedException, ExecutionException {
        final ArangoCursorAsync<Integer> cursor = db
                .query("for i in 1..10 return i", Integer.class, new AqlQueryOptions().batchSize(5).ttl(1)).get();
        assertThat(cursor.getResult()).hasSize(5);
        assertThat(cursor.hasMore()).isTrue();
        Thread.sleep(1_000);
        Throwable thrown = catchThrowable(() -> cursor.nextBatch().get()).getCause();
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
        ArangoDBException ex = (ArangoDBException) thrown;
        assertThat(ex.getMessage()).isEqualTo("Response: 404, Error: 1600 - cursor not found");
    }

    @ParameterizedTest
    @MethodSource("asyncDbs")
    void queryRawBytes(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        InternalSerde serde = db.getSerde();
        RawBytes doc = RawBytes.of(serde.serialize(Collections.singletonMap("value", 1)));
        RawBytes res = db.query("RETURN @doc", RawBytes.class, Collections.singletonMap("doc", doc)).get()
                .getResult().get(0);
        JsonNode data = serde.deserialize(res.get(), JsonNode.class);
        assertThat(data.isObject()).isTrue();
        assertThat(data.get("value").isNumber()).isTrue();
        assertThat(data.get("value").numberValue()).isEqualTo(1);
    }

    @ParameterizedTest
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
        db.setQueryCacheProperties(properties2).get();
    }

    @ParameterizedTest
    @MethodSource("asyncDbs")
    void queryWithCache(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        assumeTrue(isSingleServer());
        for (int i = 0; i < 10; i++) {
            db.collection(CNAME1).insertDocument(new BaseDocument(), null).get();
        }

        final QueryCachePropertiesEntity properties = new QueryCachePropertiesEntity();
        properties.setMode(CacheMode.on);
        db.setQueryCacheProperties(properties).get();

        final ArangoCursorAsync<String> cursor = db
                .query("FOR t IN " + CNAME1 + " FILTER t.age >= 10 SORT t.age RETURN t._id", String.class,
                        new AqlQueryOptions().cache(true)).get();

        assertThat((Object) cursor).isNotNull();
        assertThat(cursor.isCached()).isFalse();

        final ArangoCursorAsync<String> cachedCursor = db
                .query("FOR t IN " + CNAME1 + " FILTER t.age >= 10 SORT t.age RETURN t._id", String.class,
                        new AqlQueryOptions().cache(true)).get();

        assertThat((Object) cachedCursor).isNotNull();
        assertThat(cachedCursor.isCached()).isTrue();

        final QueryCachePropertiesEntity properties2 = new QueryCachePropertiesEntity();
        properties2.setMode(CacheMode.off);
        db.setQueryCacheProperties(properties2).get();
    }

    @ParameterizedTest
    @MethodSource("asyncDbs")
    void queryWithMemoryLimit(ArangoDatabaseAsync db) {
        Throwable thrown = catchThrowable(() -> db.query("RETURN 1..100000", String.class,
                new AqlQueryOptions().memoryLimit(32 * 1024L)).get()).getCause();
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
        assertThat(((ArangoDBException) thrown).getErrorNum()).isEqualTo(32);
    }

    @ParameterizedTest
    @MethodSource("asyncDbs")
    void queryWithFailOnWarningTrue(ArangoDatabaseAsync db) {
        Throwable thrown = catchThrowable(() -> db.query("RETURN 1 / 0", String.class,
                new AqlQueryOptions().failOnWarning(true)).get()).getCause();
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
    }

    @ParameterizedTest
    @MethodSource("asyncDbs")
    void queryWithFailOnWarningFalse(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        final ArangoCursorAsync<String> cursor = db
                .query("RETURN 1 / 0", String.class, new AqlQueryOptions().failOnWarning(false)).get();
        assertThat(cursor.getResult()).containsExactly((String) null);
    }

    @SlowTest
    @ParameterizedTest
    @MethodSource("asyncDbs")
    void queryWithTimeout(ArangoDatabaseAsync db) {
        assumeTrue(isAtLeastVersion(3, 6));
        Throwable thrown = catchThrowable(() -> db.query("RETURN SLEEP(1)", String.class,
                new AqlQueryOptions().maxRuntime(0.1)).get()).getCause();
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
        assertThat(((ArangoDBException) thrown).getResponseCode()).isEqualTo(410);
    }

    @ParameterizedTest
    @MethodSource("asyncDbs")
    void queryWithMaxWarningCount(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        final ArangoCursorAsync<String> cursorWithWarnings = db
                .query("RETURN 1 / 0", String.class, new AqlQueryOptions()).get();
        assertThat(cursorWithWarnings.getExtra().getWarnings()).hasSize(1);
        final ArangoCursorAsync<String> cursorWithLimitedWarnings = db
                .query("RETURN 1 / 0", String.class, new AqlQueryOptions().maxWarningCount(0L)).get();
        final Collection<CursorWarning> warnings = cursorWithLimitedWarnings.getExtra().getWarnings();
        assertThat(warnings).isNullOrEmpty();
    }

    @ParameterizedTest
    @MethodSource("asyncDbs")
    void queryCursor(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        ArangoCursorAsync<Integer> c1 = db.query("for i in 1..4 return i", Integer.class,
                new AqlQueryOptions().batchSize(1)).get();
        List<Integer> result = new ArrayList<>();
        result.addAll(c1.getResult());
        ArangoCursorAsync<Integer> c2 = c1.nextBatch().get();
        result.addAll(c2.getResult());
        ArangoCursorAsync<Integer> c3 = db.cursor(c2.getId(), Integer.class).get();
        result.addAll(c3.getResult());
        ArangoCursorAsync<Integer> c4 = c3.nextBatch().get();
        result.addAll(c4.getResult());
        assertThat(c4.hasMore()).isFalse();
        assertThat(result).containsExactly(1, 2, 3, 4);
    }

    @ParameterizedTest
    @MethodSource("asyncDbs")
    void queryCursorInTx(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        StreamTransactionEntity tx = db.beginStreamTransaction(new StreamTransactionOptions()).get();
        ArangoCursorAsync<Integer> c1 = db.query("for i in 1..4 return i", Integer.class,
                new AqlQueryOptions().batchSize(1).streamTransactionId(tx.getId())).get();
        List<Integer> result = new ArrayList<>();
        result.addAll(c1.getResult());
        ArangoCursorAsync<Integer> c2 = c1.nextBatch().get();
        result.addAll(c2.getResult());
        ArangoCursorAsync<Integer> c3 = db.cursor(c2.getId(), Integer.class,
                new AqlQueryOptions().streamTransactionId(tx.getId())).get();
        result.addAll(c3.getResult());
        ArangoCursorAsync<Integer> c4 = c3.nextBatch().get();
        result.addAll(c4.getResult());
        assertThat(c4.hasMore()).isFalse();
        assertThat(result).containsExactly(1, 2, 3, 4);
        db.abortStreamTransaction(tx.getId()).get();
    }

    @ParameterizedTest
    @MethodSource("asyncDbs")
    void queryCursorRetry(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        assumeTrue(isAtLeastVersion(3, 11));
        ArangoCursorAsync<Integer> c1 = db.query("for i in 1..4 return i", Integer.class,
                new AqlQueryOptions().batchSize(1).allowRetry(true)).get();
        List<Integer> result = new ArrayList<>();
        result.addAll(c1.getResult());
        ArangoCursorAsync<Integer> c2 = c1.nextBatch().get();
        result.addAll(c2.getResult());
        ArangoCursorAsync<Integer> c3 = db.cursor(c2.getId(), Integer.class, c2.getNextBatchId()).get();
        result.addAll(c3.getResult());
        ArangoCursorAsync<Integer> c4 = c3.nextBatch().get();
        result.addAll(c4.getResult());
        c4.close();
        assertThat(c4.hasMore()).isFalse();
        assertThat(result).containsExactly(1, 2, 3, 4);
    }

    @ParameterizedTest
    @MethodSource("asyncDbs")
    void queryCursorRetryInTx(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        assumeTrue(isAtLeastVersion(3, 11));
        StreamTransactionEntity tx = db.beginStreamTransaction(new StreamTransactionOptions()).get();
        ArangoCursorAsync<Integer> c1 = db.query("for i in 1..4 return i", Integer.class,
                new AqlQueryOptions().batchSize(1).allowRetry(true).streamTransactionId(tx.getId())).get();
        List<Integer> result = new ArrayList<>();
        result.addAll(c1.getResult());
        ArangoCursorAsync<Integer> c2 = c1.nextBatch().get();
        result.addAll(c2.getResult());
        ArangoCursorAsync<Integer> c3 = db.cursor(c2.getId(), Integer.class, c2.getNextBatchId(),
                new AqlQueryOptions().streamTransactionId(tx.getId())).get();
        result.addAll(c3.getResult());
        ArangoCursorAsync<Integer> c4 = c3.nextBatch().get();
        result.addAll(c4.getResult());
        c4.close();
        assertThat(c4.hasMore()).isFalse();
        assertThat(result).containsExactly(1, 2, 3, 4);
        db.abortStreamTransaction(tx.getId()).get();
    }

    @ParameterizedTest
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

    @ParameterizedTest
    @MethodSource("asyncDbs")
    void queryWithBindVars(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        for (int i = 0; i < 10; i++) {
            final BaseDocument baseDocument = new BaseDocument(UUID.randomUUID().toString());
            baseDocument.addAttribute("age", 20 + i);
            db.collection(CNAME1).insertDocument(baseDocument, null).get();
        }
        final Map<String, Object> bindVars = new HashMap<>();
        bindVars.put("@coll", CNAME1);
        bindVars.put("age", 25);

        final ArangoCursorAsync<String> cursor = db
                .query("FOR t IN @@coll FILTER t.age >= @age SORT t.age RETURN t._id", String.class, bindVars).get();

        assertThat(cursor.getResult()).hasSizeGreaterThanOrEqualTo(5);
    }

    @ParameterizedTest
    @MethodSource("asyncDbs")
    void queryWithRawBindVars(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        final Map<String, RawData> bindVars = new HashMap<>();
        bindVars.put("foo", RawJson.of("\"fooValue\""));
        bindVars.put("bar", RawBytes.of(db.getSerde().serializeUserData(11)));

        final JsonNode res = db.query("RETURN {foo: @foo, bar: @bar}", JsonNode.class, bindVars).get()
                .getResult().get(0);

        assertThat(res.get("foo").textValue()).isEqualTo("fooValue");
        assertThat(res.get("bar").intValue()).isEqualTo(11);
    }

    @ParameterizedTest
    @MethodSource("asyncArangos")
    void queryWithWarning(ArangoDBAsync arangoDB) throws ExecutionException, InterruptedException {
        final ArangoCursorAsync<String> cursor = arangoDB.db().query("return 1/0", String.class).get();
        assertThat(cursor.getExtra().getWarnings())
                .isNotNull()
                .hasSize(1)
                .allSatisfy(w -> assertThat(w.getMessage()).contains("division by zero"));
    }

    @ParameterizedTest
    @MethodSource("asyncDbs")
    void queryStream(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        final ArangoCursorAsync<Void> cursor = db
                .query("FOR i IN 1..2 RETURN i", Void.class, new AqlQueryOptions().stream(true).count(true)).get();
        assertThat((Object) cursor).isNotNull();
        assertThat(cursor.getCount()).isNull();
    }

    @ParameterizedTest
    @MethodSource("asyncDbs")
    void queryForceOneShardAttributeValue(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        assumeTrue(isAtLeastVersion(3, 10));
        assumeTrue(isCluster());
        assumeTrue(isEnterprise());

        String cname = "forceOneShardAttr-" + UUID.randomUUID();
        db.createCollection(cname, new CollectionCreateOptions()
                .shardKeys("foo")
                .numberOfShards(3)).get();
        ArangoCollectionAsync col = db.collection(cname);
        BaseDocument doc = new BaseDocument();
        doc.addAttribute("foo", "bar");
        col.insertDocument(doc).get();

        Iterator<BaseDocument> c1 = db
                .query("FOR d IN @@c RETURN d", BaseDocument.class, Collections.singletonMap("@c", cname),
                        new AqlQueryOptions().forceOneShardAttributeValue("bar")).get().getResult().iterator();
        assertThat(c1.hasNext()).isTrue();
        assertThat(c1.next().getAttribute("foo")).isEqualTo("bar");

        Iterator<BaseDocument> c2 = db
                .query("FOR d IN @@c RETURN d", BaseDocument.class, Collections.singletonMap("@c", cname),
                        new AqlQueryOptions().forceOneShardAttributeValue("ooo")).get().getResult().iterator();
        assertThat(c2.hasNext()).isFalse();
    }

    @ParameterizedTest
    @MethodSource("asyncArangos")
    void queryClose(ArangoDBAsync arangoDB) throws ExecutionException, InterruptedException {
        final ArangoCursorAsync<String> cursor = arangoDB.db()
                .query("for i in 1..2 return i", String.class, new AqlQueryOptions().batchSize(1)).get();
        cursor.close().get();
        assertThat(cursor.getResult()).hasSize(1);
        Throwable thrown = catchThrowable(() -> cursor.nextBatch().get()).getCause();
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
        ArangoDBException ex = (ArangoDBException) thrown;
        assertThat(ex.getResponseCode()).isEqualTo(404);
        assertThat(ex.getMessage()).contains("cursor not found");
    }

    @ParameterizedTest
    @MethodSource("asyncArangos")
    void queryCloseShouldBeIdempotent(ArangoDBAsync arangoDB) throws ExecutionException, InterruptedException {
        ArangoCursorAsync<Integer> cursor = arangoDB.db().query("for i in 1..2 return i", Integer.class,
                new AqlQueryOptions().batchSize(1)).get();
        cursor.close().get();
        cursor.close().get();
    }

    @ParameterizedTest
    @MethodSource("asyncArangos")
    void queryCloseOnCursorWithoutId(ArangoDBAsync arangoDB) throws ExecutionException, InterruptedException {
        ArangoCursorAsync<Integer> cursor = arangoDB.db().query("return 1", Integer.class).get();
        cursor.close().get();
        cursor.close().get();
    }

    @ParameterizedTest
    @MethodSource("asyncDbs")
    void queryNoResults(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        db.query("FOR i IN @@col RETURN i", BaseDocument.class, new MapBuilder().put("@col", CNAME1).get()).get();
    }

    @ParameterizedTest
    @MethodSource("asyncDbs")
    void queryWithNullBindParam(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        db.query("FOR i IN @@col FILTER i.test == @test RETURN i", BaseDocument.class,
                new MapBuilder().put("@col", CNAME1).put("test", null).get()).get();
    }

    @ParameterizedTest
    @MethodSource("asyncDbs")
    void queryAllowDirtyRead(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        final ArangoCursorAsync<BaseDocument> cursor = db.query("FOR i IN @@col FILTER i.test == @test RETURN i",
                BaseDocument.class, new MapBuilder().put("@col", CNAME1).put("test", null).get(),
                new AqlQueryOptions().allowDirtyRead(true)).get();
        assertThat(cursor.isPotentialDirtyRead()).isTrue();
    }

    @ParameterizedTest
    @MethodSource("asyncArangos")
    void queryAllowRetry(ArangoDBAsync arangoDB) throws ExecutionException, InterruptedException {
        assumeTrue(isAtLeastVersion(3, 11));
        final ArangoCursorAsync<String> cursor = arangoDB.db()
                .query("for i in 1..2 return i", String.class, new AqlQueryOptions().allowRetry(true).batchSize(1)).get();
        assertThat(cursor.getResult()).containsExactly("1");
        assertThat(cursor.hasMore()).isTrue();
        cursor.nextBatch().get();
        cursor.nextBatch().get();

        ArangoCursorAsync<String> c2 = cursor.nextBatch().get();
        assertThat(c2.getResult()).containsExactly("2");
        assertThat(c2.hasMore()).isFalse();

        cursor.close().get();
    }

    @ParameterizedTest
    @MethodSource("asyncArangos")
    void queryAllowRetryClose(ArangoDBAsync arangoDB) throws ExecutionException, InterruptedException {
        assumeTrue(isAtLeastVersion(3, 11));
        final ArangoCursorAsync<String> cursor = arangoDB.db()
                .query("for i in 1..2 return i", String.class, new AqlQueryOptions().allowRetry(true).batchSize(1)).get();
        assertThat(cursor.getResult()).containsExactly("1");
        assertThat(cursor.hasMore()).isTrue();
        ArangoCursorAsync<String> c2 = cursor.nextBatch().get();
        assertThat(c2.getResult()).containsExactly("2");
        assertThat(c2.hasMore()).isFalse();
        c2.close().get();
    }

    @ParameterizedTest
    @MethodSource("asyncArangos")
    void queryAllowRetryCloseBeforeLatestBatch(ArangoDBAsync arangoDB) throws ExecutionException, InterruptedException {
        assumeTrue(isAtLeastVersion(3, 11));
        final ArangoCursorAsync<String> cursor = arangoDB.db()
                .query("for i in 1..2 return i", String.class, new AqlQueryOptions().allowRetry(true).batchSize(1)).get();
        assertThat(cursor.getResult()).containsExactly("1");
        assertThat(cursor.hasMore()).isTrue();
        cursor.close().get();
    }

    @ParameterizedTest
    @MethodSource("asyncArangos")
    void queryAllowRetryCloseSingleBatch(ArangoDBAsync arangoDB) throws ExecutionException, InterruptedException {
        assumeTrue(isAtLeastVersion(3, 11));
        final ArangoCursorAsync<String> cursor = arangoDB.db()
                .query("for i in 1..2 return i", String.class, new AqlQueryOptions().allowRetry(true)).get();
        assertThat(cursor.getResult()).containsExactly("1", "2");
        assertThat(cursor.hasMore()).isFalse();
        cursor.close().get();
    }

    private String getExplainQuery(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        ArangoCollectionAsync character = db.collection("got_characters");
        ArangoCollectionAsync actor = db.collection("got_actors");

        if (!character.exists().get())
            character.create().get();

        if (!actor.exists().get())
            actor.create().get();

        return "FOR `character` IN `got_characters` " +
                " FOR `actor` IN `got_actors` " +
                "   FILTER `actor`.`_id` == @myId" +
                "   FILTER `character`.`actor` == `actor`.`_id` " +
                "   FILTER `character`.`value` != 1/0 " +
                "   RETURN {`character`, `actor`}";
    }

    void checkExecutionPlan(AqlExecutionExplainEntity.ExecutionPlan plan) {
        assertThat(plan).isNotNull();
        assertThat(plan.getEstimatedNrItems())
                .isNotNull()
                .isNotNegative();
        assertThat(plan.getNodes()).isNotEmpty();

        AqlExecutionExplainEntity.ExecutionNode node = plan.getNodes().iterator().next();
        assertThat(node.getEstimatedCost()).isNotNull();

        assertThat(plan.getEstimatedCost()).isNotNull().isNotNegative();
        assertThat(plan.getCollections()).isNotEmpty();

        AqlExecutionExplainEntity.ExecutionCollection collection = plan.getCollections().iterator().next();
        assertThat(collection.getName())
                .isNotNull()
                .isNotEmpty();

        assertThat(plan.getRules()).isNotEmpty();
        assertThat(plan.getVariables()).isNotEmpty();

        AqlExecutionExplainEntity.ExecutionVariable variable = plan.getVariables().iterator().next();
        assertThat(variable.getName())
                .isNotNull()
                .isNotEmpty();
    }

    @SuppressWarnings("deprecation")
    @ParameterizedTest
    @MethodSource("asyncDbs")
    void explainQuery(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        AqlExecutionExplainEntity explain = db.explainQuery(
                getExplainQuery(db),
                Collections.singletonMap("myId", "123"),
                new AqlQueryExplainOptions()).get();
        assertThat(explain).isNotNull();

        checkExecutionPlan(explain.getPlan());
        assertThat(explain.getPlans()).isNull();
        assertThat(explain.getWarnings()).isNotEmpty();

        CursorWarning warning = explain.getWarnings().iterator().next();
        assertThat(warning).isNotNull();
        assertThat(warning.getCode()).isEqualTo(1562);
        assertThat(warning.getMessage()).contains("division by zero");

        assertThat(explain.getStats()).isNotNull();

        assertThat(explain.getStats().getExecutionTime())
                .isNotNull()
                .isPositive();

        assertThat(explain.getCacheable()).isFalse();
    }

    @SuppressWarnings("deprecation")
    @ParameterizedTest
    @MethodSource("asyncDbs")
    void explainQueryAllPlans(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        AqlExecutionExplainEntity explain = db.explainQuery(
                getExplainQuery(db),
                Collections.singletonMap("myId", "123"),
                new AqlQueryExplainOptions().allPlans(true)).get();
        assertThat(explain).isNotNull();

        assertThat(explain.getPlan()).isNull();
        assertThat(explain.getPlans()).allSatisfy(this::checkExecutionPlan);
        assertThat(explain.getWarnings()).isNotEmpty();

        CursorWarning warning = explain.getWarnings().iterator().next();
        assertThat(warning).isNotNull();
        assertThat(warning.getCode()).isEqualTo(1562);
        assertThat(warning.getMessage()).contains("division by zero");

        assertThat(explain.getStats()).isNotNull();

        assertThat(explain.getStats().getExecutionTime())
                .isNotNull()
                .isPositive();

        assertThat(explain.getCacheable()).isNull();
    }

    void checkUntypedExecutionPlan(AqlQueryExplainEntity.ExecutionPlan plan) {
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
    @MethodSource("asyncDbs")
    void explainAqlQuery(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        AqlQueryExplainEntity explain = db.explainAqlQuery(
                getExplainQuery(db),
                Collections.singletonMap("myId", "123"),
                new ExplainAqlQueryOptions()).get();
        assertThat(explain).isNotNull();

        checkUntypedExecutionPlan(explain.getPlan());
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
    @MethodSource("asyncDbs")
    void explainAqlQueryAllPlans(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        AqlQueryExplainEntity explain = db.explainAqlQuery(
                getExplainQuery(db),
                Collections.singletonMap("myId", "123"),
                new ExplainAqlQueryOptions().allPlans(true)).get();
        assertThat(explain).isNotNull();

        assertThat(explain.getPlan()).isNull();
        assertThat(explain.getPlans()).allSatisfy(this::checkUntypedExecutionPlan);
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
    @MethodSource("asyncDbs")
    void explainAqlQueryAllPlansCustomOption(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        AqlQueryExplainEntity explain = db.explainAqlQuery(
                getExplainQuery(db),
                Collections.singletonMap("myId", "123"),
                new ExplainAqlQueryOptions().customOption("allPlans", true)).get();
        assertThat(explain).isNotNull();

        assertThat(explain.getPlan()).isNull();
        assertThat(explain.getPlans()).allSatisfy(this::checkUntypedExecutionPlan);
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
    @MethodSource("asyncDbs")
    void parseQuery(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        final AqlParseEntity parse = db.parseQuery("for i in 1..1 return i").get();
        assertThat(parse).isNotNull();
        assertThat(parse.getBindVars()).isEmpty();
        assertThat(parse.getCollections()).isEmpty();
        assertThat(parse.getAst()).hasSize(1);
    }

    @SlowTest
    @ParameterizedTest
    @MethodSource("asyncDbs")
    void getCurrentlyRunningQueries(ArangoDatabaseAsync db) throws InterruptedException, ExecutionException {
        String query = "return sleep(1)";
        CompletableFuture<ArangoCursorAsync<Void>> q = db.query(query, Void.class);
        Thread.sleep(300);
        final Collection<QueryEntity> currentlyRunningQueries = db.getCurrentlyRunningQueries().get();
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
        q.get();
    }

    @SlowTest
    @ParameterizedTest
    @MethodSource("asyncDbs")
    void killQuery(ArangoDatabaseAsync db) throws InterruptedException, ExecutionException {
        CompletableFuture<ArangoCursorAsync<Void>> c = db.query("return sleep(5)", Void.class);
        Thread.sleep(500);

        Collection<QueryEntity> currentlyRunningQueries = db.getCurrentlyRunningQueries().get();
        assertThat(currentlyRunningQueries).hasSize(1);
        QueryEntity queryEntity = currentlyRunningQueries.iterator().next();
        assertThat(queryEntity.getState()).isEqualTo(QueryExecutionState.EXECUTING);
        db.killQuery(queryEntity.getId()).get();

        db.getCurrentlyRunningQueries().get().forEach(q ->
                assertThat(q.getState()).isEqualTo(QueryExecutionState.KILLED)
        );

        Throwable thrown = catchThrowable(c::get).getCause();
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
        ArangoDBException e = (ArangoDBException) thrown;
        assertThat(e.getResponseCode()).isEqualTo(410);
        assertThat(e.getErrorNum()).isEqualTo(1500);
        assertThat(e.getErrorMessage()).contains("query killed");
    }

    @SlowTest
    @ParameterizedTest
    @MethodSource("asyncDbs")
    void getAndClearSlowQueries(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        db.clearSlowQueries().get();

        final QueryTrackingPropertiesEntity properties = db.getQueryTrackingProperties().get();
        final Long slowQueryThreshold = properties.getSlowQueryThreshold();
        properties.setSlowQueryThreshold(1L);
        db.setQueryTrackingProperties(properties).get();

        String query = "return sleep(1.1)";
        db.query(query, Void.class).get();
        final Collection<QueryEntity> slowQueries = db.getSlowQueries().get();
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

        db.clearSlowQueries().get();
        assertThat(db.getSlowQueries().get()).isEmpty();
        properties.setSlowQueryThreshold(slowQueryThreshold);
        db.setQueryTrackingProperties(properties).get();
    }

    @ParameterizedTest
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

    @ParameterizedTest
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

    @ParameterizedTest
    @MethodSource("asyncDbs")
    void createGraph(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        String name = "graph-" + rnd();
        final GraphEntity result = db.createGraph(name, null, null).get();
        assertThat(result.getName()).isEqualTo(name);
    }

    @ParameterizedTest
    @MethodSource("asyncDbs")
    void createGraphSatellite(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        assumeTrue(isAtLeastVersion(3, 7));
        assumeTrue(isCluster());
        assumeTrue(isEnterprise());

        String name = "graph-" + rnd();
        final GraphEntity result = db.createGraph(name, null, new GraphCreateOptions().replicationFactor(ReplicationFactor.ofSatellite())).get();
        assertThat(result.getReplicationFactor()).isEqualTo(ReplicationFactor.ofSatellite());

        GraphEntity info = db.graph(name).getInfo().get();
        assertThat(info.getReplicationFactor()).isEqualTo(ReplicationFactor.ofSatellite());

        GraphEntity graph = db.getGraphs().get().stream().filter(g -> name.equals(g.getName())).findFirst().get();
        assertThat(graph.getReplicationFactor()).isEqualTo(ReplicationFactor.ofSatellite());
    }

    @ParameterizedTest
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

    @ParameterizedTest
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

    @ParameterizedTest
    @MethodSource("asyncDbs")
    void getGraphs(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        String name = "graph-" + rnd();
        db.createGraph(name, null, null).get();
        final Collection<GraphEntity> graphs = db.getGraphs().get();
        assertThat(graphs).hasSizeGreaterThanOrEqualTo(1);
        long count = graphs.stream().map(GraphEntity::getName).filter(name::equals).count();
        assertThat(count).isEqualTo(1L);
    }

    @ParameterizedTest
    @MethodSource("asyncDbs")
    void transactionString(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        final TransactionOptions options = new TransactionOptions().params("test");
        final RawJson result = db.transaction("function (params) {return params;}", RawJson.class, options).get();
        assertThat(result.get()).isEqualTo("\"test\"");
    }

    @ParameterizedTest
    @MethodSource("asyncDbs")
    void transactionNumber(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        final TransactionOptions options = new TransactionOptions().params(5);
        final Integer result = db.transaction("function (params) {return params;}", Integer.class, options).get();
        assertThat(result).isEqualTo(5);
    }

    @ParameterizedTest
    @MethodSource("asyncDbs")
    void transactionJsonNode(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        final TransactionOptions options = new TransactionOptions().params(JsonNodeFactory.instance.textNode("test"));
        final JsonNode result = db.transaction("function (params) {return params;}", JsonNode.class, options).get();
        assertThat(result.isTextual()).isTrue();
        assertThat(result.asText()).isEqualTo("test");
    }

    @ParameterizedTest
    @MethodSource("asyncDbs")
    void transactionJsonObject(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        ObjectNode params = JsonNodeFactory.instance.objectNode().put("foo", "hello").put("bar", "world");
        final TransactionOptions options = new TransactionOptions().params(params);
        final RawJson result = db
                .transaction("function (params) { return params['foo'] + ' ' + params['bar'];}", RawJson.class,
                        options).get();
        assertThat(result.get()).isEqualTo("\"hello world\"");
    }

    @ParameterizedTest
    @MethodSource("asyncDbs")
    void transactionJsonArray(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        ArrayNode params = JsonNodeFactory.instance.arrayNode().add("hello").add("world");
        final TransactionOptions options = new TransactionOptions().params(params);
        final RawJson result = db
                .transaction("function (params) { return params[0] + ' ' + params[1];}", RawJson.class, options).get();
        assertThat(result.get()).isEqualTo("\"hello world\"");
    }

    @ParameterizedTest
    @MethodSource("asyncDbs")
    void transactionMap(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        final Map<String, Object> params = new MapBuilder().put("foo", "hello").put("bar", "world").get();
        final TransactionOptions options = new TransactionOptions().params(params);
        final RawJson result = db
                .transaction("function (params) { return params['foo'] + ' ' + params['bar'];}", RawJson.class,
                        options).get();
        assertThat(result.get()).isEqualTo("\"hello world\"");
    }

    @ParameterizedTest
    @MethodSource("asyncDbs")
    void transactionArray(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        final String[] params = new String[]{"hello", "world"};
        final TransactionOptions options = new TransactionOptions().params(params);
        final RawJson result = db
                .transaction("function (params) { return params[0] + ' ' + params[1];}", RawJson.class, options).get();
        assertThat(result.get()).isEqualTo("\"hello world\"");
    }

    @ParameterizedTest
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

    @ParameterizedTest
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

    @ParameterizedTest
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

    @ParameterizedTest
    @MethodSource("asyncDbs")
    void transactionEmpty(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        db.transaction("function () {}", Void.class, null).get();
    }

    @ParameterizedTest
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

    @ParameterizedTest
    @MethodSource("asyncDbs")
    void transactionPojoReturn(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        final String action = "function() { return {'value':'hello world'}; }";
        final TransactionTestEntity res = db.transaction(action, TransactionTestEntity.class, new TransactionOptions()).get();
        assertThat(res).isNotNull();
        assertThat(res.value).isEqualTo("hello world");
    }

    @ParameterizedTest
    @MethodSource("asyncDbs")
    void getInfo(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        final DatabaseEntity info = db.getInfo().get();
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
    @MethodSource("asyncDbs")
    void shouldIncludeExceptionMessage(ArangoDatabaseAsync db) {
        assumeTrue(isAtLeastVersion(3, 4));

        final String exceptionMessage = "My error context";
        final String action = "function (params) {" + "throw '" + exceptionMessage + "';" + "}";
        Throwable thrown = catchThrowable(() -> db.transaction(action, Void.class, null).get()).getCause();
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
        assertThat(((ArangoDBException) thrown).getErrorMessage()).isEqualTo(exceptionMessage);
    }

    @ParameterizedTest
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
