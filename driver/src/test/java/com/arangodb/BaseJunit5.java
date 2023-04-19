package com.arangodb;

import com.arangodb.config.ArangoConfigProperties;
import com.arangodb.config.ConfigUtils;
import com.arangodb.entity.*;
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
import java.util.stream.Stream;

class BaseJunit5 {
    protected static final String TEST_DB = "java_driver_test_db";
    protected static final ArangoConfigProperties config = ConfigUtils.loadConfig();
    private static final List<ArangoDB> adbs = Arrays.asList(
            new ArangoDB.Builder().loadProperties(config).protocol(Protocol.VST).build(),
            new ArangoDB.Builder().loadProperties(config).protocol(Protocol.HTTP_VPACK).build(),
            new ArangoDB.Builder().loadProperties(config).protocol(Protocol.HTTP_JSON).build(),
            new ArangoDB.Builder().loadProperties(config).protocol(Protocol.HTTP2_VPACK).build(),
            new ArangoDB.Builder().loadProperties(config).protocol(Protocol.HTTP2_JSON).build()
    );

    private static Boolean extendedDbNames;
    private static Boolean extendedNames;

    protected static Stream<ArangoDatabase> dbsStream() {
        return adbs.stream().map(adb -> adb.db(TEST_DB));
    }

    protected static Stream<Arguments> arangos() {
        return adbs.stream().map(Arguments::of);
    }

    protected static Stream<Arguments> dbs() {
        return dbsStream().map(Arguments::of);
    }

    static ArangoDatabase initDB(String name) {
        ArangoDatabase database = adbs.get(0).db(name);
        if (!database.exists())
            database.create();
        return database;
    }

    static ArangoDatabase initDB() {
        return initDB(TEST_DB);
    }

    static void dropDB(String name) {
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
    }

    static String rnd() {
        return UUID.randomUUID().toString();
    }

    static synchronized boolean supportsExtendedDbNames() {
        if (extendedDbNames == null) {
            try {
                ArangoDatabase testDb = adbs.get(0)
                        .db("test-" + TestUtils.generateRandomName(true, 20));
                testDb.create();
                extendedDbNames = true;
                testDb.drop();
            } catch (ArangoDBException e) {
                extendedDbNames = false;
            }
        }
        return extendedDbNames;
    }

    static synchronized boolean supportsExtendedNames() {
        if (extendedNames == null) {
            try {
                ArangoCollection testCol = adbs.get(0).db()
                        .collection("test-" + TestUtils.generateRandomName(true, 20));
                testCol.create();
                extendedNames = true;
                testCol.drop();
            } catch (ArangoDBException e) {
                extendedNames = false;
            }
        }
        return extendedNames;
    }

    static String rndDbName() {
        return "testDB-" + TestUtils.generateRandomName(supportsExtendedDbNames(), 20);
    }

    static String rndName() {
        return "dd-" + TestUtils.generateRandomName(supportsExtendedNames(), 20);
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
