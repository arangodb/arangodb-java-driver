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
import com.arangodb.internal.ArangoRequestParam;
import com.arangodb.internal.serde.SerdeUtils;
import com.arangodb.model.*;
import com.arangodb.model.LogOptions.SortOrder;
import com.arangodb.util.RawJson;
import com.arangodb.util.UnicodeUtils;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * @author Mark Vollmary
 * @author Reşat SABIQ
 * @author Michele Rastelli
 */
class ArangoDBAsyncTest extends BaseJunit5 {

    private static final String DB1 = "ArangoDBTest_db1";
    private static final String DB2 = "ArangoDBTest_db2";

    private static final String ROOT = "root";
    private static final String PW = "machts der hund";

    @BeforeAll
    static void initDBs() {
        initDB(DB1);
        initDB(DB2);
    }

    @AfterAll
    static void shutdown() {
        dropDB(DB1);
        dropDB(DB2);
    }

//    @ParameterizedTest(name = "{index}")
//    @MethodSource("arangos")
//    void getVersion(ArangoDB arangoDB) {
//        final ArangoDBVersion version = arangoDB.async().getVersion();
//        assertThat(version.getServer()).isNotNull();
//        assertThat(version.getVersion()).isNotNull();
//    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("arangos")
    void createAndDeleteDatabase(ArangoDB arangoDB) throws ExecutionException, InterruptedException {
        final String dbName = rndDbName();
        final Boolean resultCreate = arangoDB.async().createDatabase(dbName).get();
        assertThat(resultCreate).isTrue();
//        final Boolean resultDelete = arangoDB.async().db(dbName).drop();
//        assertThat(resultDelete).isTrue();
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("arangos")
    void createWithNotNormalizedName(ArangoDB arangoDB) {
        assumeTrue(supportsExtendedDbNames());

        final String dbName = "testDB-\u006E\u0303\u00f1";
        String normalized = UnicodeUtils.normalize(dbName);
        arangoDB.async().createDatabase(normalized);
//        arangoDB.async().db(normalized).drop();

        Throwable thrown = catchThrowable(() -> arangoDB.async().createDatabase(dbName));
        assertThat(thrown)
                .isInstanceOf(ArangoDBException.class)
                .hasMessageContaining("normalized");
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("arangos")
    void createDatabaseWithOptions(ArangoDB arangoDB) throws ExecutionException, InterruptedException {
        assumeTrue(isCluster());
        assumeTrue(isAtLeastVersion(3, 6));
        final String dbName = rndDbName();
        final Boolean resultCreate = arangoDB.async().createDatabase(new DBCreateOptions()
                .name(dbName)
                .options(new DatabaseOptions()
                        .writeConcern(2)
                        .replicationFactor(2)
                        .sharding("")
                )
        ).get();
        assertThat(resultCreate).isTrue();

//        DatabaseEntity info = arangoDB.async().db(dbName).getInfo();
//        assertThat(info.getReplicationFactor().get()).isEqualTo(2);
//        assertThat(info.getWriteConcern()).isEqualTo(2);
//        assertThat(info.getSharding()).isEmpty();
//
//        final Boolean resultDelete = arangoDB.async().db(dbName).drop();
//        assertThat(resultDelete).isTrue();
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("arangos")
    void createDatabaseWithOptionsSatellite(ArangoDB arangoDB) throws ExecutionException, InterruptedException {
        assumeTrue(isCluster());
        assumeTrue(isEnterprise());
        assumeTrue(isAtLeastVersion(3, 6));

        final String dbName = rndDbName();
        final Boolean resultCreate = arangoDB.async().createDatabase(new DBCreateOptions()
                .name(dbName)
                .options(new DatabaseOptions()
                        .writeConcern(2)
                        .replicationFactor(ReplicationFactor.ofSatellite())
                        .sharding("")
                )
        ).get();
        assertThat(resultCreate).isTrue();

//        DatabaseEntity info = arangoDB.async().db(dbName).getInfo();
//        assertThat(info.getReplicationFactor()).isEqualTo(ReplicationFactor.ofSatellite());
//        assertThat(info.getWriteConcern()).isEqualTo(2);
//        assertThat(info.getSharding()).isEmpty();
//
//        final Boolean resultDelete = arangoDB.async().db(dbName).drop();
//        assertThat(resultDelete).isTrue();
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("arangos")
    void createDatabaseWithUsers(ArangoDB arangoDB) throws InterruptedException, ExecutionException {
        final String dbName = rndDbName();
        final Map<String, Object> extra = Collections.singletonMap("key", "value");
        final Boolean resultCreate = arangoDB.async().createDatabase(new DBCreateOptions()
                .name(dbName)
                .users(Collections.singletonList(new DatabaseUsersOptions()
                        .active(true)
                        .username("testUser")
                        .passwd("testPasswd")
                        .extra(extra)
                ))
        ).get();
        assertThat(resultCreate).isTrue();

//        DatabaseEntity info = arangoDB.async().db(dbName).getInfo();
//        assertThat(info.getName()).isEqualTo(dbName);

        Optional<UserEntity> retrievedUserOptional = arangoDB.async().getUsers().get().stream()
                .filter(it -> it.getUser().equals("testUser"))
                .findFirst();
        assertThat(retrievedUserOptional).isPresent();

        UserEntity retrievedUser = retrievedUserOptional.get();
        assertThat(retrievedUser.getActive()).isTrue();
        assertThat(retrievedUser.getExtra()).isEqualTo(extra);

        // needed for active-failover tests only
        Thread.sleep(2_000);

        ArangoDB arangoDBTestUser = new ArangoDB.Builder()
                .loadProperties(config)
                .user("testUser")
                .password("testPasswd")
                .build();

        // check if testUser has been created and can access the created db
//        ArangoCollection collection = arangoDBTestUser.async().db(dbName).collection("col-" + UUID.randomUUID());
//        collection.create();
//        arangoDBTestUser.shutdown();
//
//        final Boolean resultDelete = arangoDB.async().db(dbName).drop();
//        assertThat(resultDelete).isTrue();
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("arangos")
    void getDatabases(ArangoDB arangoDB) throws ExecutionException, InterruptedException {
        Collection<String> dbs = arangoDB.async().getDatabases().get();
        assertThat(dbs).contains("_system", DB1);
    }

//    @ParameterizedTest(name = "{index}")
//    @MethodSource("arangos")
//    void getAccessibleDatabases(ArangoDB arangoDB) {
//        final Collection<String> dbs = arangoDB.async().getAccessibleDatabases();
//        assertThat(dbs).contains("_system");
//    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("arangos")
    void getAccessibleDatabasesFor(ArangoDB arangoDB) throws ExecutionException, InterruptedException {
        final Collection<String> dbs = arangoDB.async().getAccessibleDatabasesFor("root").get();
        assertThat(dbs).contains("_system");
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("arangos")
    void createUser(ArangoDB arangoDB) throws ExecutionException, InterruptedException {
        String username = "user-" + UUID.randomUUID();
        final UserEntity result = arangoDB.async().createUser(username, PW, null).get();
        assertThat(result.getUser()).isEqualTo(username);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("arangos")
    void deleteUser(ArangoDB arangoDB) {
        String username = "user-" + UUID.randomUUID();
        arangoDB.async().createUser(username, PW, null);
        arangoDB.async().deleteUser(username);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("arangos")
    void getUserRoot(ArangoDB arangoDB) throws ExecutionException, InterruptedException {
        final UserEntity user = arangoDB.async().getUser(ROOT).get();
        assertThat(user.getUser()).isEqualTo(ROOT);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("arangos")
    void getUser(ArangoDB arangoDB) throws ExecutionException, InterruptedException {
        String username = "user-" + UUID.randomUUID();
        arangoDB.async().createUser(username, PW, null).get();
        final UserEntity user = arangoDB.async().getUser(username).get();
        assertThat(user.getUser()).isEqualTo(username);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("arangos")
    void getUsersOnlyRoot(ArangoDB arangoDB) throws ExecutionException, InterruptedException {
        final Collection<UserEntity> users = arangoDB.async().getUsers().get();
        assertThat(users).isNotEmpty();
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("arangos")
    void getUsers(ArangoDB arangoDB) throws ExecutionException, InterruptedException {
        String username = "user-" + UUID.randomUUID();
        // Allow & account for pre-existing users other than ROOT:
        final Collection<UserEntity> initialUsers = arangoDB.async().getUsers().get();

        arangoDB.async().createUser(username, PW, null).get();
        final Collection<UserEntity> users = arangoDB.async().getUsers().get();
        assertThat(users).hasSize(initialUsers.size() + 1);

        final List<String> expected = new ArrayList<>(users.size());
        // Add initial users, including root:
        for (final UserEntity userEntity : initialUsers) {
            expected.add(userEntity.getUser());
        }
        // Add username:
        expected.add(username);

        for (final UserEntity user : users) {
            assertThat(user.getUser()).isIn(expected);
        }
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("arangos")
    void updateUserNoOptions(ArangoDB arangoDB) {
        String username = "user-" + UUID.randomUUID();
        arangoDB.async().createUser(username, PW, null);
        arangoDB.async().updateUser(username, null);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("arangos")
    void updateUser(ArangoDB arangoDB) throws ExecutionException, InterruptedException {
        String username = "user-" + UUID.randomUUID();
        final Map<String, Object> extra = new HashMap<>();
        extra.put("hund", false);
        arangoDB.async().createUser(username, PW, new UserCreateOptions().extra(extra));
        extra.put("hund", true);
        extra.put("mund", true);
        final UserEntity user = arangoDB.async().updateUser(username, new UserUpdateOptions().extra(extra)).get();
        assertThat(user.getExtra()).hasSize(2);
        assertThat(user.getExtra()).containsKey("hund");
        assertThat(Boolean.valueOf(String.valueOf(user.getExtra().get("hund")))).isTrue();
        final UserEntity user2 = arangoDB.async().getUser(username).get();
        assertThat(user2.getExtra()).hasSize(2);
        assertThat(user2.getExtra()).containsKey("hund");
        assertThat(Boolean.valueOf(String.valueOf(user2.getExtra().get("hund")))).isTrue();
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("arangos")
    void replaceUser(ArangoDB arangoDB) throws ExecutionException, InterruptedException {
        String username = "user-" + UUID.randomUUID();
        final Map<String, Object> extra = new HashMap<>();
        extra.put("hund", false);
        arangoDB.async().createUser(username, PW, new UserCreateOptions().extra(extra)).get();
        extra.remove("hund");
        extra.put("mund", true);
        final UserEntity user = arangoDB.async().replaceUser(username, new UserUpdateOptions().extra(extra)).get();
        assertThat(user.getExtra()).hasSize(1);
        assertThat(user.getExtra()).containsKey("mund");
        assertThat(Boolean.valueOf(String.valueOf(user.getExtra().get("mund")))).isTrue();
        final UserEntity user2 = arangoDB.async().getUser(username).get();
        assertThat(user2.getExtra()).hasSize(1);
        assertThat(user2.getExtra()).containsKey("mund");
        assertThat(Boolean.valueOf(String.valueOf(user2.getExtra().get("mund")))).isTrue();
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("arangos")
    void updateUserDefaultDatabaseAccess(ArangoDB arangoDB) {
        String username = "user-" + UUID.randomUUID();
        arangoDB.async().createUser(username, PW);
        arangoDB.async().grantDefaultDatabaseAccess(username, Permissions.RW);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("arangos")
    void updateUserDefaultCollectionAccess(ArangoDB arangoDB) {
        String username = "user-" + UUID.randomUUID();
        arangoDB.async().createUser(username, PW);
        arangoDB.async().grantDefaultCollectionAccess(username, Permissions.RW);
    }

//    @Test
//    void authenticationFailPassword() {
//        final ArangoDB arangoDB = new ArangoDB.Builder()
//                .loadProperties(config)
//                .password("no").jwt(null).build();
//        Throwable thrown = catchThrowable(() -> arangoDB.async().getVersion().get());
//        assertThat(thrown).isInstanceOf(ArangoDBException.class);
//        assertThat(((ArangoDBException) thrown).getResponseCode()).isEqualTo(401);
//    }

//    @ParameterizedTest(name = "{index}")
//    @MethodSource("arangos")
//    void authenticationFailUser() {
//        final ArangoDB arangoDB = new ArangoDB.Builder()
//                .loadProperties(config)
//                .user("no").jwt(null).build();
//        Throwable thrown = catchThrowable(() -> arangoDB.async().getVersion().get());
//        assertThat(thrown).isInstanceOf(ArangoDBException.class);
//        assertThat(((ArangoDBException) thrown).getResponseCode()).isEqualTo(401);
//    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("arangos")
    void executeGetVersion(ArangoDB arangoDB) throws ExecutionException, InterruptedException {
        Request<?> request = Request.builder()
                .db(ArangoRequestParam.SYSTEM)
                .method(Request.Method.GET)
                .path("/_api/version")
                .queryParam("details", "true")
                .build();
        final Response<RawJson> response = arangoDB.async().execute(request, RawJson.class).get();
        JsonNode body = SerdeUtils.INSTANCE.parseJson(response.getBody().get());
        assertThat(body.get("version").isTextual()).isTrue();
        assertThat(body.get("details").isObject()).isTrue();
        assertThat(response.getResponseCode()).isEqualTo(200);
        if (isAtLeastVersion(3, 9)) {
            String header = response.getHeaders().get("x-arango-queue-time-seconds");
            assertThat(header).isNotNull();
        }
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("arangos")
    void getLogEntries(ArangoDB arangoDB) throws ExecutionException, InterruptedException {
        assumeTrue(isAtLeastVersion(3, 8));
        final LogEntriesEntity logs = arangoDB.async().getLogEntries(null).get();
        assertThat(logs.getTotal()).isPositive();
        assertThat(logs.getMessages()).hasSize(logs.getTotal().intValue());
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("arangos")
    void getLogEntriesUpto(ArangoDB arangoDB) throws ExecutionException, InterruptedException {
        assumeTrue(isAtLeastVersion(3, 8));
        final LogEntriesEntity logsUpto = arangoDB.async().getLogEntries(new LogOptions().upto(LogLevel.WARNING)).get();
        assertThat(logsUpto.getMessages())
                .map(LogEntriesEntity.Message::getLevel)
                .doesNotContain("INFO");
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("arangos")
    void getLogEntriesLevel(ArangoDB arangoDB) throws ExecutionException, InterruptedException {
        assumeTrue(isAtLeastVersion(3, 8));
        final LogEntriesEntity logsInfo = arangoDB.async().getLogEntries(new LogOptions().level(LogLevel.INFO)).get();
        assertThat(logsInfo.getMessages())
                .map(LogEntriesEntity.Message::getLevel)
                .containsOnly("INFO");
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("arangos")
    void getLogEntriesStart(ArangoDB arangoDB) throws ExecutionException, InterruptedException {
        assumeTrue(isAtLeastVersion(3, 8));
        final LogEntriesEntity logs = arangoDB.async().getLogEntries(null).get();
        final Long firstId = logs.getMessages().get(0).getId();
        final LogEntriesEntity logsStart = arangoDB.async().getLogEntries(new LogOptions().start(firstId + 1)).get();
        assertThat(logsStart.getMessages())
                .map(LogEntriesEntity.Message::getId)
                .doesNotContain(firstId);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("arangos")
    void getLogEntriesSize(ArangoDB arangoDB) throws ExecutionException, InterruptedException {
        assumeTrue(isAtLeastVersion(3, 8));
        final LogEntriesEntity logs = arangoDB.async().getLogEntries(null).get();
        int count = logs.getMessages().size();
        assertThat(count).isPositive();
        final LogEntriesEntity logsSize = arangoDB.async().getLogEntries(new LogOptions().size(count - 1)).get();
        assertThat(logsSize.getMessages()).hasSize(count - 1);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("arangos")
    void getLogEntriesOffset(ArangoDB arangoDB) throws ExecutionException, InterruptedException {
        assumeTrue(isAtLeastVersion(3, 8));
        final LogEntriesEntity logs = arangoDB.async().getLogEntries(null).get();
        assertThat(logs.getTotal()).isPositive();
        Long firstId = logs.getMessages().get(0).getId();
        final LogEntriesEntity logsOffset = arangoDB.async().getLogEntries(new LogOptions().offset(1)).get();
        assertThat(logsOffset.getMessages())
                .map(LogEntriesEntity.Message::getId)
                .doesNotContain(firstId);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("arangos")
    void getLogEntriesSearch(ArangoDB arangoDB) throws ExecutionException, InterruptedException {
        assumeTrue(isAtLeastVersion(3, 8));
        final LogEntriesEntity logs = arangoDB.async().getLogEntries(null).get();
        final LogEntriesEntity logsSearch = arangoDB.async().getLogEntries(new LogOptions().search(TEST_DB)).get();
        assertThat(logs.getTotal()).isGreaterThan(logsSearch.getTotal());
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("arangos")
    void getLogEntriesSortAsc(ArangoDB arangoDB) throws ExecutionException, InterruptedException {
        assumeTrue(isAtLeastVersion(3, 8));
        final LogEntriesEntity logs = arangoDB.async().getLogEntries(new LogOptions().sort(SortOrder.asc)).get();
        long lastId = -1;
        List<Long> ids = logs.getMessages().stream()
                .map(LogEntriesEntity.Message::getId)
                .collect(Collectors.toList());
        for (final Long id : ids) {
            assertThat(id).isGreaterThan(lastId);
            lastId = id;
        }
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("arangos")
    void getLogEntriesSortDesc(ArangoDB arangoDB) throws ExecutionException, InterruptedException {
        assumeTrue(isAtLeastVersion(3, 8));
        final LogEntriesEntity logs = arangoDB.async().getLogEntries(new LogOptions().sort(SortOrder.desc)).get();
        long lastId = Long.MAX_VALUE;
        List<Long> ids = logs.getMessages().stream()
                .map(LogEntriesEntity.Message::getId)
                .collect(Collectors.toList());
        for (final Long id : ids) {
            assertThat(lastId).isGreaterThan(id);
            lastId = id;
        }
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("arangos")
    void getLogLevel(ArangoDB arangoDB) throws ExecutionException, InterruptedException {
        assumeTrue(isAtLeastVersion(3, 7)); // it fails in 3.6 active-failover (BTS-362)
        final LogLevelEntity logLevel = arangoDB.async().getLogLevel().get();
        assertThat(logLevel.getAgency()).isEqualTo(LogLevelEntity.LogLevel.INFO);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("arangos")
    void setLogLevel(ArangoDB arangoDB) throws ExecutionException, InterruptedException {
        assumeTrue(isAtLeastVersion(3, 7)); // it fails in 3.6 active-failover (BTS-362)
        final LogLevelEntity entity = new LogLevelEntity();
        try {
            entity.setAgency(LogLevelEntity.LogLevel.ERROR);
            final LogLevelEntity logLevel = arangoDB.async().setLogLevel(entity).get();
            assertThat(logLevel.getAgency()).isEqualTo(LogLevelEntity.LogLevel.ERROR);
        } finally {
            entity.setAgency(LogLevelEntity.LogLevel.INFO);
            arangoDB.async().setLogLevel(entity);
        }
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("arangos")
    void setAllLogLevel(ArangoDB arangoDB) throws ExecutionException, InterruptedException {
        assumeTrue(isAtLeastVersion(3, 9));
        final LogLevelEntity entity = new LogLevelEntity();
        try {
            entity.setAll(LogLevelEntity.LogLevel.ERROR);
            final LogLevelEntity logLevel = arangoDB.async().setLogLevel(entity).get();
            assertThat(logLevel.getAgency()).isEqualTo(LogLevelEntity.LogLevel.ERROR);
            assertThat(logLevel.getQueries()).isEqualTo(LogLevelEntity.LogLevel.ERROR);
            LogLevelEntity retrievedLevels = arangoDB.async().getLogLevel().get();
            assertThat(retrievedLevels.getAgency()).isEqualTo(LogLevelEntity.LogLevel.ERROR);
        } finally {
            entity.setAll(LogLevelEntity.LogLevel.INFO);
            arangoDB.async().setLogLevel(entity);
        }
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("arangos")
    void logLevelWithServerId(ArangoDB arangoDB) throws ExecutionException, InterruptedException {
        assumeTrue(isAtLeastVersion(3, 10));
        assumeTrue(isCluster());
        String serverId = arangoDB.async().getServerId().get();
        LogLevelOptions options = new LogLevelOptions().serverId(serverId);
        final LogLevelEntity entity = new LogLevelEntity();
        try {
            entity.setGraphs(LogLevelEntity.LogLevel.ERROR);
            final LogLevelEntity logLevel = arangoDB.async().setLogLevel(entity, options).get();
            assertThat(logLevel.getGraphs()).isEqualTo(LogLevelEntity.LogLevel.ERROR);
            assertThat(arangoDB.async().getLogLevel(options).get().getGraphs()).isEqualTo(LogLevelEntity.LogLevel.ERROR);
        } finally {
            entity.setGraphs(LogLevelEntity.LogLevel.INFO);
            arangoDB.async().setLogLevel(entity);
        }
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("arangos")
    void getQueryOptimizerRules(ArangoDB arangoDB) throws ExecutionException, InterruptedException {
        assumeTrue(isAtLeastVersion(3, 10));
        final Collection<QueryOptimizerRule> rules = arangoDB.async().getQueryOptimizerRules().get();
        assertThat(rules).isNotEmpty();
        for (QueryOptimizerRule rule : rules) {
            assertThat(rule).isNotNull();
            assertThat(rule.getName()).isNotNull();
            QueryOptimizerRule.Flags flags = rule.getFlags();
            assertThat(flags.getHidden()).isNotNull();
            assertThat(flags.getClusterOnly()).isNotNull();
            assertThat(flags.getCanBeDisabled()).isNotNull();
            assertThat(flags.getCanCreateAdditionalPlans()).isNotNull();
            assertThat(flags.getDisabledByDefault()).isNotNull();
            assertThat(flags.getEnterpriseOnly()).isNotNull();
        }
    }

//    @ParameterizedTest(name = "{index}")
//    @MethodSource("arangos")
//    void arangoDBException(ArangoDB arangoDB) {
//        Throwable thrown = catchThrowable(() -> arangoDB.async().db("no").getInfo());
//        assertThat(thrown).isInstanceOf(ArangoDBException.class);
//        ArangoDBException e = (ArangoDBException) thrown;
//        assertThat(e.getResponseCode()).isEqualTo(404);
//        assertThat(e.getErrorNum()).isEqualTo(1228);
//    }

//    @ParameterizedTest(name = "{index}")
//    @MethodSource("arangos")
//    void fallbackHost() {
//        final ArangoDB arangoDB = new ArangoDB.Builder()
//                .loadProperties(config)
//                .host("not-accessible", 8529).host("127.0.0.1", 8529).build();
//        final ArangoDBVersion version = arangoDB.async().getVersion();
//        assertThat(version).isNotNull();
//    }

//    @ParameterizedTest(name = "{index}")
//    @MethodSource("arangos")
//    void loadproperties() {
//        Throwable thrown = catchThrowable(() -> new ArangoDB.Builder()
//                .loadProperties(ConfigUtils.loadConfig("arangodb-bad.properties"))
//        );
//        assertThat(thrown).isInstanceOf(IllegalArgumentException.class);
//    }

//    @ParameterizedTest(name = "{index}")
//    @MethodSource("arangos")
//    void loadpropertiesWithPrefix() {
//        ArangoDB adb = new arangoDB.async().Builder()
//                .loadProperties(ConfigUtils.loadConfig("arangodb-with-prefix.properties", "adb"))
//                .build();
//        adb.getVersion();
//        adb.shutdown();
//    }

//    @ParameterizedTest(name = "{index}")
//    @MethodSource("arangos")
//    void accessMultipleDatabases(ArangoDB arangoDB) {
//        final ArangoDBVersion version1 = arangoDB.async().db(DB1).getVersion();
//        assertThat(version1).isNotNull();
//        final ArangoDBVersion version2 = arangoDB.async().db(DB2).getVersion();
//        assertThat(version2).isNotNull();
//    }

//    @ParameterizedTest(name = "{index}")
//    @MethodSource("arangos")
//    @Disabled("Manual execution only")
//    void queueTime(ArangoDB arangoDB) throws InterruptedException, ExecutionException {
//        List<CompletableFuture<Void>> futures = IntStream.range(0, 80)
//                .mapToObj(i -> CompletableFuture.runAsync(
//                        () -> arangoDB.async().db().query("RETURN SLEEP(1)", Void.class),
//                        Executors.newFixedThreadPool(80))
//                )
//                .collect(Collectors.toList());
//        for (CompletableFuture<Void> f : futures) {
//            f.get();
//        }
//
//        QueueTimeMetrics qt = arangoDB.async().metrics().getQueueTime();
//        double avg = qt.getAvg();
//        QueueTimeSample[] values = qt.getValues();
//        if (isAtLeastVersion(3, 9)) {
//            assertThat(values).hasSize(20);
//            for (int i = 0; i < values.length; i++) {
//                assertThat(values[i].value).isNotNegative();
//                if (i > 0) {
//                    assertThat(values[i].timestamp).isGreaterThanOrEqualTo(values[i - 1].timestamp);
//                }
//            }
//
//            if (avg < 0.0) {
//                System.err.println("avg < 0: " + avg);
//                System.err.println("got values:");
//                for (QueueTimeSample v : values) {
//                    System.err.println(v.value);
//                }
//            }
//            assertThat(avg).isNotNegative();
//        } else {
//            assertThat(avg).isEqualTo(0.0);
//            assertThat(values).isEmpty();
//        }
//
//    }
}
