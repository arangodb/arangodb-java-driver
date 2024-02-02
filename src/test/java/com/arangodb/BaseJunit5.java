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

public class BaseJunit5 {
    protected static final DbName TEST_DB = DbName.of("java_driver_test_db");

    private static final ArangoDB adb = new ArangoDB.Builder()
            .useProtocol(Protocol.HTTP_JSON)
            .serializer(new ArangoJack())
            .build();

    private static final ArangoDBVersion version = adb.getVersion();

    private static final List<ArangoDB> adbs = Arrays.stream(Protocol.values())
            .filter(p -> !p.equals(Protocol.VST) || isLessThanVersion(3, 12))
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
        ArangoDatabase database = adb.db(name);
        if (!database.exists())
            database.create();
        return database;
    }

    static ArangoDatabase initDB() {
        return initDB(TEST_DB);
    }

    static void dropDB(DbName name) {
        ArangoDatabase database = adb.db(name);
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
        adb.shutdown();
    }

    static String rnd() {
        return UUID.randomUUID().toString();
    }

    public static boolean isAtLeastVersion(final int major, final int minor) {
        return isAtLeastVersion(major, minor, 0);
    }

    public static boolean isAtLeastVersion(final int major, final int minor, final int patch) {
        return TestUtils.isAtLeastVersion(version.getVersion(), major, minor, patch);
    }

    public static boolean isLessThanVersion(final int major, final int minor) {
        return isLessThanVersion(major, minor, 0);
    }

    public static boolean isLessThanVersion(final int major, final int minor, final int patch) {
        return TestUtils.isLessThanVersion(version.getVersion(), major, minor, patch);
    }

    public static boolean isStorageEngine(ArangoDBEngine.StorageEngineName name) {
        return name.equals(adb.getEngine().getName());
    }

    public static boolean isSingleServer() {
        return adb.getRole() == ServerRole.SINGLE;
    }

    public static boolean isCluster() {
        return adb.getRole() == ServerRole.COORDINATOR;
    }

    public static boolean isEnterprise() {
        return adb.getVersion().getLicense() == License.ENTERPRISE;
    }

}
