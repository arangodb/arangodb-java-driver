package com.arangodb;

import com.arangodb.config.ArangoConfigProperties;
import com.arangodb.config.ConfigUtils;
import com.arangodb.entity.*;
import com.arangodb.model.CollectionCreateOptions;
import com.arangodb.model.GraphCreateOptions;
import com.arangodb.util.TestUtils;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.params.provider.Arguments;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.arangodb.util.TestUtils.TEST_DB;

public class BaseJunit5 {
    protected static final ArangoConfigProperties config = ConfigUtils.loadConfig();
    private static final ArangoDB adb = new ArangoDB.Builder()
            .loadProperties(config)
            .protocol(Protocol.HTTP_JSON)
            .build();

    private static final ArangoDBVersion version = adb.getVersion();
    private static final ServerRole role = adb.getRole();

    private static final List<Named<ArangoDB>> adbs = Arrays.stream(Protocol.values())
            .filter(p -> !p.equals(Protocol.VST))
            .map(p -> Named.of(p.toString(), new ArangoDB.Builder()
                    .loadProperties(config)
                    .protocol(p)
                    .build()))
            .collect(Collectors.toList());

    private static Boolean extendedDbNames;
    private static Boolean extendedNames;

    protected static Stream<Named<ArangoDB>> adbsStream() {
        return adbs.stream();
    }

    protected static Stream<Named<ArangoDatabase>> dbsStream() {
        return adbsStream().map(mapNamedPayload(p -> p.db(TEST_DB)));
    }

    protected static Stream<Named<ArangoDBAsync>> asyncAdbsStream() {
        return adbs.stream().map(mapNamedPayload(ArangoDB::async));
    }

    protected static Stream<Named<ArangoDatabaseAsync>> asyncDbsStream() {
        return asyncAdbsStream().map(mapNamedPayload(p -> p.db(TEST_DB)));
    }

    protected static Stream<Arguments> arangos() {
        return adbsStream().map(Arguments::of);
    }

    protected static Stream<Arguments> asyncArangos() {
        return asyncAdbsStream().map(Arguments::of);
    }

    protected static Stream<Arguments> dbs() {
        return dbsStream().map(Arguments::of);
    }

    protected static Stream<Arguments> asyncDbs() {
        return asyncDbsStream().map(Arguments::of);
    }

    protected static <T, U> Function<Named<T>, Named<U>> mapNamedPayload(Function<T, U> mapper) {
        return named -> Named.of(named.getName(), mapper.apply(named.getPayload()));
    }

    protected static String getJwt() {
        Response<ObjectNode> response = adb.execute(Request.builder()
                .method(Request.Method.POST)
                .db("_system")
                .path("/_open/auth")
                .body(JsonNodeFactory.instance.objectNode()
                        .put("username", config.getUser().orElse("root"))
                        .put("password", config.getPassword().orElse(""))
                )
                .build(), ObjectNode.class);

        return response.getBody().get("jwt").textValue();
    }

    static ArangoDatabase initDB(String name) {
        ArangoDatabase database = adb.db(name);
        if (!database.exists())
            database.create();
        return database;
    }

    protected static ArangoDatabase initDB() {
        return initDB(TEST_DB);
    }

    static void dropDB(String name) {
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
    }

    protected String getTestDb() {
        return TEST_DB;
    }

    public static String rnd() {
        return UUID.randomUUID().toString();
    }

    public static synchronized boolean supportsExtendedDbNames() {
        if (extendedDbNames == null) {
            try {
                ArangoDatabase testDb = adb
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

    public static synchronized boolean supportsExtendedNames() {
        if (extendedNames == null) {
            try {
                ArangoCollection testCol = adb.db()
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

    public static String rndDbName() {
        return "testDB-" + TestUtils.generateRandomName(supportsExtendedDbNames(), 20);
    }

    public static String rndName() {
        return "dd-" + TestUtils.generateRandomName(supportsExtendedNames(), 20);
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

    public static boolean isSingleServer() {
        return role == ServerRole.SINGLE;
    }

    public static boolean isCluster() {
        return role == ServerRole.COORDINATOR;
    }

}
