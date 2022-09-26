package com.arangodb;

import com.arangodb.entity.*;
import com.arangodb.mapping.ArangoJack;
import com.arangodb.model.CollectionCreateOptions;
import com.arangodb.model.GraphCreateOptions;
import com.arangodb.util.TestUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.provider.Arguments;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class BaseJunit5 {
    protected static final DbName TEST_DB = DbName.of("java_driver_test_db");

    private static final List<ArangoDB> adbs = Arrays.stream(Protocol.values())
            .map(p -> new ArangoDB.Builder()
                    .useProtocol(p)
                    .serializer(new ArangoJack())
                    .build())
            .collect(Collectors.toList());

    protected static Stream<ArangoDatabase> dbsStream() {
        return adbs.stream().map(adb -> adb.db(TEST_DB));
    }

    protected static Stream<Arguments> arangos() {
        return adbs.stream().map(Arguments::of);
    }

    protected static Stream<Arguments> dbs() {
        return dbsStream().map(Arguments::of);
    }

    static ArangoDatabase initDB(DbName name) {
        ArangoDatabase database = adbs.get(0).db(name);
        if (!database.exists())
            database.create();
        return database;
    }

    static ArangoDatabase initDB() {
        return initDB(TEST_DB);
    }

    static void dropDB(DbName name) {
        ArangoDatabase database = adbs.get(0).db(name);
        if (database.exists())
            database.drop();
    }

    static void initGraph(String name, Collection<EdgeDefinition> edgeDefinitions, GraphCreateOptions options) {
        ArangoDatabase db = initDB();
        db.createGraph(name, edgeDefinitions, options);
    }

    static void initCollections(String... collections) {
        ArangoDatabase db = initDB();
        for (String collection : collections) {
            if (db.collection(collection).exists())
                db.collection(collection).drop();
            db.createCollection(collection, null);
        }
    }

    static void initEdgeCollections(String... collections) {
        ArangoDatabase db = initDB();
        for (String collection : collections) {
            if (db.collection(collection).exists())
                db.collection(collection).drop();
            db.createCollection(collection, new CollectionCreateOptions().type(CollectionType.EDGES));
        }
    }

    @BeforeAll
    static void init() {
        dropDB(TEST_DB);
    }

    @AfterAll
    static void shutdown() {
        dropDB(TEST_DB);
        adbs.forEach(ArangoDB::shutdown);
    }

    static String rnd() {
        return UUID.randomUUID().toString();
    }

    boolean isAtLeastVersion(final int major, final int minor) {
        return isAtLeastVersion(major, minor, 0);
    }

    boolean isAtLeastVersion(final int major, final int minor, final int patch) {
        return TestUtils.isAtLeastVersion(adbs.get(0).getVersion().getVersion(), major, minor, patch);
    }

    boolean isLessThanVersion(final int major, final int minor) {
        return isLessThanVersion(major, minor, 0);
    }

    boolean isLessThanVersion(final int major, final int minor, final int patch) {
        return TestUtils.isLessThanVersion(adbs.get(0).getVersion().getVersion(), major, minor, patch);
    }

    boolean isStorageEngine(ArangoDBEngine.StorageEngineName name) {
        return name.equals(adbs.get(0).getEngine().getName());
    }

    boolean isSingleServer() {
        return adbs.get(0).getRole() == ServerRole.SINGLE;
    }

    boolean isCluster() {
        return adbs.get(0).getRole() == ServerRole.COORDINATOR;
    }

    boolean isEnterprise() {
        return adbs.get(0).getVersion().getLicense() == License.ENTERPRISE;
    }


}
