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

import com.arangodb.config.ConfigUtils;
import com.arangodb.entity.*;
import com.arangodb.internal.ArangoRequestParam;
import com.arangodb.internal.serde.SerdeUtils;
import com.arangodb.model.*;
import com.arangodb.model.LogOptions.SortOrder;
import com.arangodb.util.RawJson;
import com.arangodb.util.SlowTest;
import com.arangodb.util.UnicodeUtils;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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

    @ParameterizedTest
    @MethodSource("asyncArangos")
    void getVersion(ArangoDBAsync arangoDB) throws ExecutionException, InterruptedException {
        final ArangoDBVersion version = arangoDB.getVersion().get();
        assertThat(version.getServer()).isNotNull();
        assertThat(version.getVersion()).isNotNull();
    }

    @SlowTest
    @ParameterizedTest
    @MethodSource("asyncArangos")
    void createAndDeleteDatabase(ArangoDBAsync arangoDB) throws ExecutionException, InterruptedException {
        final String dbName = rndDbName();
        final Boolean resultCreate = arangoDB.createDatabase(dbName).get();
        assertThat(resultCreate).isTrue();
        final Boolean resultDelete = arangoDB.db(dbName).drop().get();
        assertThat(resultDelete).isTrue();
    }

    @SlowTest
    @ParameterizedTest
    @MethodSource("asyncArangos")
    void createWithNotNormalizedName(ArangoDBAsync arangoDB) throws ExecutionException, InterruptedException {
        assumeTrue(supportsExtendedDbNames());

        final String dbName = "testDB-\u006E\u0303\u00f1";
        String normalized = UnicodeUtils.normalize(dbName);
        arangoDB.createDatabase(normalized).get();
        arangoDB.db(normalized).drop().get();

        Throwable thrown = catchThrowable(() -> arangoDB.createDatabase(dbName).get()).getCause();
        assertThat(thrown)
                .isInstanceOf(ArangoDBException.class)
                .hasMessageContaining("normalized");
    }

    @SlowTest
    @ParameterizedTest
    @MethodSource("asyncArangos")
    void createDatabaseWithOptions(ArangoDBAsync arangoDB) throws ExecutionException, InterruptedException {
        assumeTrue(isCluster());
        assumeTrue(isAtLeastVersion(3, 6));
        final String dbName = rndDbName();
        final Boolean resultCreate = arangoDB.createDatabase(new DBCreateOptions()
                .name(dbName)
                .options(new DatabaseOptions()
                        .writeConcern(2)
                        .replicationFactor(2)
                        .sharding("")
                )
        ).get();
        assertThat(resultCreate).isTrue();

        DatabaseEntity info = arangoDB.db(dbName).getInfo().get();
        assertThat(info.getReplicationFactor().get()).isEqualTo(2);
        assertThat(info.getWriteConcern()).isEqualTo(2);
        assertThat(info.getSharding()).isEmpty();

        final Boolean resultDelete = arangoDB.db(dbName).drop().get();
        assertThat(resultDelete).isTrue();
    }

    @SlowTest
    @ParameterizedTest
    @MethodSource("asyncArangos")
    void createDatabaseWithOptionsSatellite(ArangoDBAsync arangoDB) throws ExecutionException, InterruptedException {
        assumeTrue(isCluster());
        assumeTrue(isEnterprise());
        assumeTrue(isAtLeastVersion(3, 6));

        final String dbName = rndDbName();
        final Boolean resultCreate = arangoDB.createDatabase(new DBCreateOptions()
                .name(dbName)
                .options(new DatabaseOptions()
                        .writeConcern(2)
                        .replicationFactor(ReplicationFactor.ofSatellite())
                        .sharding("")
                )
        ).get();
        assertThat(resultCreate).isTrue();

        DatabaseEntity info = arangoDB.db(dbName).getInfo().get();
        assertThat(info.getReplicationFactor()).isEqualTo(ReplicationFactor.ofSatellite());
        assertThat(info.getWriteConcern()).isEqualTo(2);
        assertThat(info.getSharding()).isEmpty();

        final Boolean resultDelete = arangoDB.db(dbName).drop().get();
        assertThat(resultDelete).isTrue();
    }

    @SlowTest
    @ParameterizedTest
    @MethodSource("asyncArangos")
    void createDatabaseWithUsers(ArangoDBAsync arangoDB) throws InterruptedException, ExecutionException {
        final String dbName = rndDbName();
        final Map<String, Object> extra = Collections.singletonMap("key", "value");
        final Boolean resultCreate = arangoDB.createDatabase(new DBCreateOptions()
                .name(dbName)
                .users(Collections.singletonList(new DatabaseUsersOptions()
                        .active(true)
                        .username("testUser")
                        .passwd("testPasswd")
                        .extra(extra)
                ))
        ).get();
        assertThat(resultCreate).isTrue();

        DatabaseEntity info = arangoDB.db(dbName).getInfo().get();
        assertThat(info.getName()).isEqualTo(dbName);

        Optional<UserEntity> retrievedUserOptional = arangoDB.getUsers().get().stream()
                .filter(it -> it.getUser().equals("testUser"))
                .findFirst();
        assertThat(retrievedUserOptional).isPresent();

        UserEntity retrievedUser = retrievedUserOptional.get();
        assertThat(retrievedUser.getActive()).isTrue();
        assertThat(retrievedUser.getExtra()).isEqualTo(extra);

        // needed for active-failover tests only
        Thread.sleep(2_000);

        ArangoDBAsync arangoDBTestUser = new ArangoDB.Builder()
                .loadProperties(config)
                .user("testUser")
                .password("testPasswd")
                .build()
                .async();

        // check if testUser has been created and can access the created db
        ArangoCollectionAsync collection = arangoDBTestUser.db(dbName).collection("col-" + UUID.randomUUID());
        collection.create().get();
        arangoDBTestUser.shutdown();

        final Boolean resultDelete = arangoDB.db(dbName).drop().get();
        assertThat(resultDelete).isTrue();
    }

    @ParameterizedTest
    @MethodSource("asyncArangos")
    void getDatabases(ArangoDBAsync arangoDB) throws ExecutionException, InterruptedException {
        Collection<String> dbs = arangoDB.getDatabases().get();
        assertThat(dbs).contains("_system", DB1);
    }

    @ParameterizedTest
    @MethodSource("asyncArangos")
    void getAccessibleDatabases(ArangoDBAsync arangoDB) throws ExecutionException, InterruptedException {
        final Collection<String> dbs = arangoDB.getAccessibleDatabases().get();
        assertThat(dbs).contains("_system");
    }

    @ParameterizedTest
    @MethodSource("asyncArangos")
    void getAccessibleDatabasesFor(ArangoDBAsync arangoDB) throws ExecutionException, InterruptedException {
        final Collection<String> dbs = arangoDB.getAccessibleDatabasesFor("root").get();
        assertThat(dbs).contains("_system");
    }

    @ParameterizedTest
    @MethodSource("asyncArangos")
    void createUser(ArangoDBAsync arangoDB) throws ExecutionException, InterruptedException {
        assumeTrue(isSingleServer());
        String username = "user-" + UUID.randomUUID();
        final UserEntity result = arangoDB.createUser(username, PW, null).get();
        assertThat(result.getUser()).isEqualTo(username);
    }

    @ParameterizedTest
    @MethodSource("asyncArangos")
    void deleteUser(ArangoDBAsync arangoDB) {
        String username = "user-" + UUID.randomUUID();
        arangoDB.createUser(username, PW, null);
        arangoDB.deleteUser(username);
    }

    @ParameterizedTest
    @MethodSource("asyncArangos")
    void getUserRoot(ArangoDBAsync arangoDB) throws ExecutionException, InterruptedException {
        final UserEntity user = arangoDB.getUser(ROOT).get();
        assertThat(user.getUser()).isEqualTo(ROOT);
    }

    @ParameterizedTest
    @MethodSource("asyncArangos")
    void getUser(ArangoDBAsync arangoDB) throws ExecutionException, InterruptedException {
        String username = "user-" + UUID.randomUUID();
        arangoDB.createUser(username, PW, null).get();
        final UserEntity user = arangoDB.getUser(username).get();
        assertThat(user.getUser()).isEqualTo(username);
    }

    @ParameterizedTest
    @MethodSource("asyncArangos")
    void getUsersOnlyRoot(ArangoDBAsync arangoDB) throws ExecutionException, InterruptedException {
        final Collection<UserEntity> users = arangoDB.getUsers().get();
        assertThat(users).isNotEmpty();
    }

    @ParameterizedTest
    @MethodSource("asyncArangos")
    void getUsers(ArangoDBAsync arangoDB) throws ExecutionException, InterruptedException {
        String username = "user-" + UUID.randomUUID();
        // Allow & account for pre-existing users other than ROOT:
        final Collection<UserEntity> initialUsers = arangoDB.getUsers().get();

        arangoDB.createUser(username, PW, null).get();
        final Collection<UserEntity> users = arangoDB.getUsers().get();
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

    @ParameterizedTest
    @MethodSource("asyncArangos")
    void updateUserNoOptions(ArangoDBAsync arangoDB) {
        String username = "user-" + UUID.randomUUID();
        arangoDB.createUser(username, PW, null);
        arangoDB.updateUser(username, null);
    }

    @ParameterizedTest
    @MethodSource("asyncArangos")
    void updateUser(ArangoDBAsync arangoDB) throws ExecutionException, InterruptedException {
        String username = "user-" + UUID.randomUUID();
        final Map<String, Object> extra = new HashMap<>();
        extra.put("hund", false);
        arangoDB.createUser(username, PW, new UserCreateOptions().extra(extra)).get();
        extra.put("hund", true);
        extra.put("mund", true);
        final UserEntity user = arangoDB.updateUser(username, new UserUpdateOptions().extra(extra)).get();
        assertThat(user.getExtra()).hasSize(2);
        assertThat(user.getExtra()).containsKey("hund");
        assertThat(Boolean.valueOf(String.valueOf(user.getExtra().get("hund")))).isTrue();
        final UserEntity user2 = arangoDB.getUser(username).get();
        assertThat(user2.getExtra()).hasSize(2);
        assertThat(user2.getExtra()).containsKey("hund");
        assertThat(Boolean.valueOf(String.valueOf(user2.getExtra().get("hund")))).isTrue();
    }

    @ParameterizedTest
    @MethodSource("asyncArangos")
    void replaceUser(ArangoDBAsync arangoDB) throws ExecutionException, InterruptedException {
        String username = "user-" + UUID.randomUUID();
        final Map<String, Object> extra = new HashMap<>();
        extra.put("hund", false);
        arangoDB.createUser(username, PW, new UserCreateOptions().extra(extra)).get();
        extra.remove("hund");
        extra.put("mund", true);
        final UserEntity user = arangoDB.replaceUser(username, new UserUpdateOptions().extra(extra)).get();
        assertThat(user.getExtra()).hasSize(1);
        assertThat(user.getExtra()).containsKey("mund");
        assertThat(Boolean.valueOf(String.valueOf(user.getExtra().get("mund")))).isTrue();
        final UserEntity user2 = arangoDB.getUser(username).get();
        assertThat(user2.getExtra()).hasSize(1);
        assertThat(user2.getExtra()).containsKey("mund");
        assertThat(Boolean.valueOf(String.valueOf(user2.getExtra().get("mund")))).isTrue();
    }

    @ParameterizedTest
    @MethodSource("asyncArangos")
    void updateUserDefaultDatabaseAccess(ArangoDBAsync arangoDB) {
        String username = "user-" + UUID.randomUUID();
        arangoDB.createUser(username, PW);
        arangoDB.grantDefaultDatabaseAccess(username, Permissions.RW);
    }

    @ParameterizedTest
    @MethodSource("asyncArangos")
    void updateUserDefaultCollectionAccess(ArangoDBAsync arangoDB) {
        String username = "user-" + UUID.randomUUID();
        arangoDB.createUser(username, PW);
        arangoDB.grantDefaultCollectionAccess(username, Permissions.RW);
    }

    @ParameterizedTest
    @EnumSource(Protocol.class)
    void authenticationFailPassword(Protocol protocol) {
        assumeTrue(!protocol.equals(Protocol.VST) || BaseJunit5.isLessThanVersion(3, 12));

        final ArangoDBAsync arangoDB = new ArangoDB.Builder()
                .loadProperties(config)
                .protocol(protocol)
                .acquireHostList(false)
                .password("no").jwt(null)
                .build()
                .async();
        Throwable thrown = catchThrowable(() -> arangoDB.getVersion().get()).getCause();
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
        assertThat(((ArangoDBException) thrown).getResponseCode()).isEqualTo(401);
    }

    @ParameterizedTest
    @EnumSource(Protocol.class)
    void authenticationFailUser(Protocol protocol) {
        assumeTrue(!protocol.equals(Protocol.VST) || BaseJunit5.isLessThanVersion(3, 12));

        final ArangoDBAsync arangoDB = new ArangoDB.Builder()
                .loadProperties(config)
                .protocol(protocol)
                .acquireHostList(false)
                .user("no").jwt(null)
                .build()
                .async();
        Throwable thrown = catchThrowable(() -> arangoDB.getVersion().get()).getCause();
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
        assertThat(((ArangoDBException) thrown).getResponseCode()).isEqualTo(401);
    }

    @ParameterizedTest
    @MethodSource("asyncArangos")
    void executeGetVersion(ArangoDBAsync arangoDB) throws ExecutionException, InterruptedException {
        Request<?> request = Request.builder()
                .db(ArangoRequestParam.SYSTEM)
                .method(Request.Method.GET)
                .path("/_api/version")
                .queryParam("details", "true")
                .build();
        final Response<RawJson> response = arangoDB.execute(request, RawJson.class).get();
        JsonNode body = SerdeUtils.INSTANCE.parseJson(response.getBody().get());
        assertThat(body.get("version").isTextual()).isTrue();
        assertThat(body.get("details").isObject()).isTrue();
        assertThat(response.getResponseCode()).isEqualTo(200);
        if (isAtLeastVersion(3, 9)) {
            String header = response.getHeaders().get("x-arango-queue-time-seconds");
            assertThat(header).isNotNull();
        }
    }

    @ParameterizedTest
    @MethodSource("asyncArangos")
    void getLogEntries(ArangoDBAsync arangoDB) throws ExecutionException, InterruptedException {
        assumeTrue(isAtLeastVersion(3, 8));
        final LogEntriesEntity logs = arangoDB.getLogEntries(null).get();
        assertThat(logs.getTotal()).isPositive();
        assertThat(logs.getMessages()).hasSize(logs.getTotal().intValue());
    }

    @ParameterizedTest
    @MethodSource("asyncArangos")
    void getLogEntriesUpto(ArangoDBAsync arangoDB) throws ExecutionException, InterruptedException {
        assumeTrue(isAtLeastVersion(3, 8));
        final LogEntriesEntity logsUpto = arangoDB.getLogEntries(new LogOptions().upto(LogLevel.WARNING)).get();
        assertThat(logsUpto.getMessages())
                .map(LogEntriesEntity.Message::getLevel)
                .doesNotContain("INFO");
    }

    @ParameterizedTest
    @MethodSource("asyncArangos")
    void getLogEntriesLevel(ArangoDBAsync arangoDB) throws ExecutionException, InterruptedException {
        assumeTrue(isAtLeastVersion(3, 8));
        final LogEntriesEntity logsInfo = arangoDB.getLogEntries(new LogOptions().level(LogLevel.INFO)).get();
        assertThat(logsInfo.getMessages())
                .map(LogEntriesEntity.Message::getLevel)
                .containsOnly("INFO");
    }

    @ParameterizedTest
    @MethodSource("asyncArangos")
    void getLogEntriesStart(ArangoDBAsync arangoDB) throws ExecutionException, InterruptedException {
        assumeTrue(isAtLeastVersion(3, 8));
        final LogEntriesEntity logs = arangoDB.getLogEntries(null).get();
        final Long firstId = logs.getMessages().get(0).getId();
        final LogEntriesEntity logsStart = arangoDB.getLogEntries(new LogOptions().start(firstId + 1)).get();
        assertThat(logsStart.getMessages())
                .map(LogEntriesEntity.Message::getId)
                .doesNotContain(firstId);
    }

    @ParameterizedTest
    @MethodSource("asyncArangos")
    void getLogEntriesSize(ArangoDBAsync arangoDB) throws ExecutionException, InterruptedException {
        assumeTrue(isAtLeastVersion(3, 8));
        final LogEntriesEntity logs = arangoDB.getLogEntries(null).get();
        int count = logs.getMessages().size();
        assertThat(count).isPositive();
        final LogEntriesEntity logsSize = arangoDB.getLogEntries(new LogOptions().size(count - 1)).get();
        assertThat(logsSize.getMessages()).hasSize(count - 1);
    }

    @ParameterizedTest
    @MethodSource("asyncArangos")
    void getLogEntriesOffset(ArangoDBAsync arangoDB) throws ExecutionException, InterruptedException {
        assumeTrue(isAtLeastVersion(3, 8));
        final LogEntriesEntity logs = arangoDB.getLogEntries(null).get();
        assertThat(logs.getTotal()).isPositive();
        Long firstId = logs.getMessages().get(0).getId();
        final LogEntriesEntity logsOffset = arangoDB.getLogEntries(new LogOptions().offset(1)).get();
        assertThat(logsOffset.getMessages())
                .map(LogEntriesEntity.Message::getId)
                .doesNotContain(firstId);
    }

    @ParameterizedTest
    @MethodSource("asyncArangos")
    void getLogEntriesSearch(ArangoDBAsync arangoDB) throws ExecutionException, InterruptedException {
        assumeTrue(isAtLeastVersion(3, 8));
        final LogEntriesEntity logs = arangoDB.getLogEntries(null).get();
        final LogEntriesEntity logsSearch = arangoDB.getLogEntries(new LogOptions().search(getTestDb())).get();
        assertThat(logs.getTotal()).isGreaterThan(logsSearch.getTotal());
    }

    @ParameterizedTest
    @MethodSource("asyncArangos")
    void getLogEntriesSortAsc(ArangoDBAsync arangoDB) throws ExecutionException, InterruptedException {
        assumeTrue(isAtLeastVersion(3, 8));
        final LogEntriesEntity logs = arangoDB.getLogEntries(new LogOptions().sort(SortOrder.asc)).get();
        long lastId = -1;
        List<Long> ids = logs.getMessages().stream()
                .map(LogEntriesEntity.Message::getId)
                .collect(Collectors.toList());
        for (final Long id : ids) {
            assertThat(id).isGreaterThan(lastId);
            lastId = id;
        }
    }

    @ParameterizedTest
    @MethodSource("asyncArangos")
    void getLogEntriesSortDesc(ArangoDBAsync arangoDB) throws ExecutionException, InterruptedException {
        assumeTrue(isAtLeastVersion(3, 8));
        final LogEntriesEntity logs = arangoDB.getLogEntries(new LogOptions().sort(SortOrder.desc)).get();
        long lastId = Long.MAX_VALUE;
        List<Long> ids = logs.getMessages().stream()
                .map(LogEntriesEntity.Message::getId)
                .collect(Collectors.toList());
        for (final Long id : ids) {
            assertThat(lastId).isGreaterThan(id);
            lastId = id;
        }
    }

    @ParameterizedTest
    @MethodSource("asyncArangos")
    void getLogLevel(ArangoDBAsync arangoDB) throws ExecutionException, InterruptedException {
        assumeTrue(isAtLeastVersion(3, 7)); // it fails in 3.6 active-failover (BTS-362)
        final LogLevelEntity logLevel = arangoDB.getLogLevel().get();
        assertThat(logLevel.getAgency()).isEqualTo(LogLevelEntity.LogLevel.INFO);
    }

    @ParameterizedTest
    @MethodSource("asyncArangos")
    void setLogLevel(ArangoDBAsync arangoDB) throws ExecutionException, InterruptedException {
        assumeTrue(isAtLeastVersion(3, 7)); // it fails in 3.6 active-failover (BTS-362)
        final LogLevelEntity entity = new LogLevelEntity();
        try {
            entity.setAgency(LogLevelEntity.LogLevel.ERROR);
            final LogLevelEntity logLevel = arangoDB.setLogLevel(entity).get();
            assertThat(logLevel.getAgency()).isEqualTo(LogLevelEntity.LogLevel.ERROR);
        } finally {
            entity.setAgency(LogLevelEntity.LogLevel.INFO);
            arangoDB.setLogLevel(entity).get();
        }
    }

    @ParameterizedTest
    @MethodSource("asyncArangos")
    void setAllLogLevel(ArangoDBAsync arangoDB) throws ExecutionException, InterruptedException {
        assumeTrue(isAtLeastVersion(3, 9));
        final LogLevelEntity entity = new LogLevelEntity();
        try {
            entity.setAll(LogLevelEntity.LogLevel.ERROR);
            final LogLevelEntity logLevel = arangoDB.setLogLevel(entity).get();
            assertThat(logLevel.getAgency()).isEqualTo(LogLevelEntity.LogLevel.ERROR);
            assertThat(logLevel.getQueries()).isEqualTo(LogLevelEntity.LogLevel.ERROR);
            LogLevelEntity retrievedLevels = arangoDB.getLogLevel().get();
            assertThat(retrievedLevels.getAgency()).isEqualTo(LogLevelEntity.LogLevel.ERROR);
        } finally {
            entity.setAll(LogLevelEntity.LogLevel.INFO);
            arangoDB.setLogLevel(entity).get();
        }
    }

    @ParameterizedTest
    @MethodSource("asyncArangos")
    void logLevelWithServerId(ArangoDBAsync arangoDB) throws ExecutionException, InterruptedException {
        assumeTrue(isAtLeastVersion(3, 10));
        assumeTrue(isCluster());
        String serverId = arangoDB.getServerId().get();
        LogLevelOptions options = new LogLevelOptions().serverId(serverId);
        final LogLevelEntity entity = new LogLevelEntity();
        try {
            entity.setGraphs(LogLevelEntity.LogLevel.ERROR);
            final LogLevelEntity logLevel = arangoDB.setLogLevel(entity, options).get();
            assertThat(logLevel.getGraphs()).isEqualTo(LogLevelEntity.LogLevel.ERROR);
            assertThat(arangoDB.getLogLevel(options).get().getGraphs()).isEqualTo(LogLevelEntity.LogLevel.ERROR);
        } finally {
            entity.setGraphs(LogLevelEntity.LogLevel.INFO);
            arangoDB.setLogLevel(entity).get();
        }
    }

    @ParameterizedTest
    @MethodSource("asyncArangos")
    void getQueryOptimizerRules(ArangoDBAsync arangoDB) throws ExecutionException, InterruptedException {
        assumeTrue(isAtLeastVersion(3, 10));
        final Collection<QueryOptimizerRule> rules = arangoDB.getQueryOptimizerRules().get();
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

    @ParameterizedTest
    @MethodSource("asyncArangos")
    void arangoDBException(ArangoDBAsync arangoDB) {
        Throwable thrown = catchThrowable(() -> arangoDB.db("no").getInfo().get()).getCause();
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
        ArangoDBException e = (ArangoDBException) thrown;
        assertThat(e.getResponseCode()).isEqualTo(404);
        assertThat(e.getErrorNum()).isEqualTo(1228);
    }

    @ParameterizedTest
    @MethodSource("asyncArangos")
    void fallbackHost() throws ExecutionException, InterruptedException {
        final ArangoDBAsync arangoDB = new ArangoDB.Builder()
                .loadProperties(config)
                .host("not-accessible", 8529).host("172.28.0.1", 8529)
                .build()
                .async();
        final ArangoDBVersion version = arangoDB.getVersion().get();
        assertThat(version).isNotNull();
    }

    @ParameterizedTest
    @MethodSource("asyncArangos")
    void loadpropertiesWithPrefix() throws ExecutionException, InterruptedException {
        ArangoDBAsync adb = new ArangoDB.Builder()
                .loadProperties(ConfigUtils.loadConfig("arangodb-with-prefix.properties", "adb"))
                .build()
                .async();
        adb.getVersion().get();
        adb.shutdown();
    }

    @ParameterizedTest
    @MethodSource("asyncArangos")
    void accessMultipleDatabases(ArangoDBAsync arangoDB) throws ExecutionException, InterruptedException {
        final ArangoDBVersion version1 = arangoDB.db(DB1).getVersion().get();
        assertThat(version1).isNotNull();
        final ArangoDBVersion version2 = arangoDB.db(DB2).getVersion().get();
        assertThat(version2).isNotNull();
    }

    @ParameterizedTest
    @MethodSource("asyncArangos")
    @Disabled("Manual execution only")
    void queueTime(ArangoDBAsync arangoDB) throws InterruptedException, ExecutionException {
        List<CompletableFuture<?>> futures = IntStream.range(0, 80)
                .mapToObj(i -> arangoDB.db().query("RETURN SLEEP(1)", Void.class))
                .collect(Collectors.toList());
        for (CompletableFuture<?> f : futures) {
            f.get();
        }

        QueueTimeMetrics qt = arangoDB.metrics().getQueueTime();
        double avg = qt.getAvg();
        QueueTimeSample[] values = qt.getValues();
        if (isAtLeastVersion(3, 9)) {
            assertThat(values).hasSize(20);
            for (int i = 0; i < values.length; i++) {
                assertThat(values[i].value).isNotNegative();
                if (i > 0) {
                    assertThat(values[i].timestamp).isGreaterThanOrEqualTo(values[i - 1].timestamp);
                }
            }

            if (avg < 0.0) {
                System.err.println("avg < 0: " + avg);
                System.err.println("got values:");
                for (QueueTimeSample v : values) {
                    System.err.println(v.value);
                }
            }
            assertThat(avg).isNotNegative();
        } else {
            assertThat(avg).isEqualTo(0.0);
            assertThat(values).isEmpty();
        }

    }
}
