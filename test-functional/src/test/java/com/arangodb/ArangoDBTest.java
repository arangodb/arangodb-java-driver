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
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
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
class ArangoDBTest extends BaseJunit5 {

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
    @MethodSource("arangos")
    void getVersion(ArangoDB arangoDB) {
        final ArangoDBVersion version = arangoDB.getVersion();
        assertThat(version.getServer()).isNotNull();
        assertThat(version.getVersion()).isNotNull();
    }

    @SlowTest
    @ParameterizedTest
    @MethodSource("arangos")
    void createAndDeleteDatabase(ArangoDB arangoDB) {
        final String dbName = rndDbName();
        final Boolean resultCreate;
        resultCreate = arangoDB.createDatabase(dbName);
        assertThat(resultCreate).isTrue();
        final Boolean resultDelete = arangoDB.db(dbName).drop();
        assertThat(resultDelete).isTrue();
    }

    @SlowTest
    @ParameterizedTest
    @MethodSource("arangos")
    void createWithNotNormalizedName(ArangoDB arangoDB) {
        assumeTrue(supportsExtendedDbNames());

        final String dbName = "testDB-\u006E\u0303\u00f1";
        String normalized = UnicodeUtils.normalize(dbName);
        arangoDB.createDatabase(normalized);
        arangoDB.db(normalized).drop();

        Throwable thrown = catchThrowable(() -> arangoDB.createDatabase(dbName));
        assertThat(thrown)
                .isInstanceOf(ArangoDBException.class)
                .hasMessageContaining("normalized");
    }

    @SlowTest
    @ParameterizedTest
    @MethodSource("arangos")
    void createDatabaseWithOptions(ArangoDB arangoDB) {
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
        );
        assertThat(resultCreate).isTrue();

        DatabaseEntity info = arangoDB.db(dbName).getInfo();
        assertThat(info.getReplicationFactor().get()).isEqualTo(2);
        assertThat(info.getWriteConcern()).isEqualTo(2);
        assertThat(info.getSharding()).isEmpty();

        final Boolean resultDelete = arangoDB.db(dbName).drop();
        assertThat(resultDelete).isTrue();
    }

    @SlowTest
    @ParameterizedTest
    @MethodSource("arangos")
    void createDatabaseWithOptionsSatellite(ArangoDB arangoDB) {
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
        );
        assertThat(resultCreate).isTrue();

        DatabaseEntity info = arangoDB.db(dbName).getInfo();
        assertThat(info.getReplicationFactor()).isEqualTo(ReplicationFactor.ofSatellite());
        assertThat(info.getWriteConcern()).isEqualTo(2);
        assertThat(info.getSharding()).isEmpty();

        final Boolean resultDelete = arangoDB.db(dbName).drop();
        assertThat(resultDelete).isTrue();
    }

    @SlowTest
    @ParameterizedTest
    @MethodSource("arangos")
    void createDatabaseWithUsers(ArangoDB arangoDB) throws InterruptedException {
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
        );
        assertThat(resultCreate).isTrue();

        DatabaseEntity info = arangoDB.db(dbName).getInfo();
        assertThat(info.getName()).isEqualTo(dbName);

        Optional<UserEntity> retrievedUserOptional = arangoDB.getUsers().stream()
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
        ArangoCollection collection = arangoDBTestUser.db(dbName).collection("col-" + UUID.randomUUID());
        collection.create();
        arangoDBTestUser.shutdown();

        final Boolean resultDelete = arangoDB.db(dbName).drop();
        assertThat(resultDelete).isTrue();
    }

    @ParameterizedTest
    @MethodSource("arangos")
    void getDatabases(ArangoDB arangoDB) {
        Collection<String> dbs = arangoDB.getDatabases();
        assertThat(dbs).contains("_system", DB1);
    }

    @ParameterizedTest
    @MethodSource("arangos")
    void getAccessibleDatabases(ArangoDB arangoDB) {
        final Collection<String> dbs = arangoDB.getAccessibleDatabases();
        assertThat(dbs).contains("_system");
    }

    @ParameterizedTest
    @MethodSource("arangos")
    void getAccessibleDatabasesFor(ArangoDB arangoDB) {
        final Collection<String> dbs = arangoDB.getAccessibleDatabasesFor("root");
        assertThat(dbs).contains("_system");
    }

    @ParameterizedTest
    @MethodSource("arangos")
    void createUser(ArangoDB arangoDB) {
        String username = "user-" + UUID.randomUUID();
        final UserEntity result = arangoDB.createUser(username, PW, null);
        try {
            assertThat(result.getUser()).isEqualTo(username);
        } finally {
            arangoDB.deleteUser(username);
        }
    }

    @ParameterizedTest
    @MethodSource("arangos")
    void deleteUser(ArangoDB arangoDB) {
        String username = "user-" + UUID.randomUUID();
        arangoDB.createUser(username, PW, null);
        arangoDB.deleteUser(username);
    }

    @ParameterizedTest
    @MethodSource("arangos")
    void getUserRoot(ArangoDB arangoDB) {
        final UserEntity user = arangoDB.getUser(ROOT);
        assertThat(user.getUser()).isEqualTo(ROOT);
    }

    @ParameterizedTest
    @MethodSource("arangos")
    void getUser(ArangoDB arangoDB) {
        String username = "user-" + UUID.randomUUID();
        arangoDB.createUser(username, PW, null);
        final UserEntity user = arangoDB.getUser(username);
        assertThat(user.getUser()).isEqualTo(username);
    }

    @ParameterizedTest
    @MethodSource("arangos")
    void getUsersOnlyRoot(ArangoDB arangoDB) {
        final Collection<UserEntity> users = arangoDB.getUsers();
        assertThat(users).isNotEmpty();
    }

    @ParameterizedTest
    @MethodSource("arangos")
    void getUsers(ArangoDB arangoDB) {
        String username = "user-" + UUID.randomUUID();
        // Allow & account for pre-existing users other than ROOT:
        final Collection<UserEntity> initialUsers = arangoDB.getUsers();

        arangoDB.createUser(username, PW, null);
        try {
            final Collection<UserEntity> users = arangoDB.getUsers();
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
        } finally {
            arangoDB.deleteUser(username);
        }
    }

    @ParameterizedTest
    @MethodSource("arangos")
    void updateUserNoOptions(ArangoDB arangoDB) {
        String username = "user-" + UUID.randomUUID();
        arangoDB.createUser(username, PW, null);
        try {
            arangoDB.updateUser(username, null);
        } finally {
            arangoDB.deleteUser(username);
        }
    }

    @ParameterizedTest
    @MethodSource("arangos")
    void updateUser(ArangoDB arangoDB) {
        String username = "user-" + UUID.randomUUID();
        final Map<String, Object> extra = new HashMap<>();
        extra.put("hund", false);
        arangoDB.createUser(username, PW, new UserCreateOptions().extra(extra));
        try {
            extra.put("hund", true);
            extra.put("mund", true);
            final UserEntity user = arangoDB.updateUser(username, new UserUpdateOptions().extra(extra));
            assertThat(user.getExtra()).hasSize(2);
            assertThat(user.getExtra()).containsKey("hund");
            assertThat(Boolean.valueOf(String.valueOf(user.getExtra().get("hund")))).isTrue();
            final UserEntity user2 = arangoDB.getUser(username);
            assertThat(user2.getExtra()).hasSize(2);
            assertThat(user2.getExtra()).containsKey("hund");
            assertThat(Boolean.valueOf(String.valueOf(user2.getExtra().get("hund")))).isTrue();
        } finally {
            arangoDB.deleteUser(username);
        }
    }

    @ParameterizedTest
    @MethodSource("arangos")
    void replaceUser(ArangoDB arangoDB) {
        String username = "user-" + UUID.randomUUID();
        final Map<String, Object> extra = new HashMap<>();
        extra.put("hund", false);
        arangoDB.createUser(username, PW, new UserCreateOptions().extra(extra));
        try {
            extra.remove("hund");
            extra.put("mund", true);
            final UserEntity user = arangoDB.replaceUser(username, new UserUpdateOptions().extra(extra));
            assertThat(user.getExtra()).hasSize(1);
            assertThat(user.getExtra()).containsKey("mund");
            assertThat(Boolean.valueOf(String.valueOf(user.getExtra().get("mund")))).isTrue();
            final UserEntity user2 = arangoDB.getUser(username);
            assertThat(user2.getExtra()).hasSize(1);
            assertThat(user2.getExtra()).containsKey("mund");
            assertThat(Boolean.valueOf(String.valueOf(user2.getExtra().get("mund")))).isTrue();
        } finally {
            arangoDB.deleteUser(username);
        }
    }

    @ParameterizedTest
    @MethodSource("arangos")
    void updateUserDefaultDatabaseAccess(ArangoDB arangoDB) {
        String username = "user-" + UUID.randomUUID();
        arangoDB.createUser(username, PW);
        try {
            arangoDB.grantDefaultDatabaseAccess(username, Permissions.RW);
        } finally {
            arangoDB.deleteUser(username);
        }
    }

    @ParameterizedTest
    @MethodSource("arangos")
    void updateUserDefaultCollectionAccess(ArangoDB arangoDB) {
        String username = "user-" + UUID.randomUUID();
        arangoDB.createUser(username, PW);
        try {
            arangoDB.grantDefaultCollectionAccess(username, Permissions.RW);
        } finally {
            arangoDB.deleteUser(username);
        }
    }

    @ParameterizedTest
    @EnumSource(Protocol.class)
    void authenticationFailPassword(Protocol protocol) {
        assumeTrue(!protocol.equals(Protocol.VST) || BaseJunit5.isLessThanVersion(3, 12));

        final ArangoDB arangoDB = new ArangoDB.Builder()
                .loadProperties(config)
                .protocol(protocol)
                .acquireHostList(false)
                .password("no").jwt(null).build();
        Throwable thrown = catchThrowable(arangoDB::getVersion);
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
        assertThat(((ArangoDBException) thrown).getResponseCode()).isEqualTo(401);
    }

    @ParameterizedTest
    @EnumSource(Protocol.class)
    void authenticationFailUser(Protocol protocol) {
        assumeTrue(!protocol.equals(Protocol.VST) || BaseJunit5.isLessThanVersion(3, 12));

        final ArangoDB arangoDB = new ArangoDB.Builder()
                .loadProperties(config)
                .protocol(protocol)
                .acquireHostList(false)
                .user("no").jwt(null).build();
        Throwable thrown = catchThrowable(arangoDB::getVersion);
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
        assertThat(((ArangoDBException) thrown).getResponseCode()).isEqualTo(401);
    }

    @ParameterizedTest
    @MethodSource("arangos")
    void executeGetVersion(ArangoDB arangoDB) {
        Request<?> request = Request.builder()
                .db(ArangoRequestParam.SYSTEM)
                .method(Request.Method.GET)
                .path("/_api/version")
                .queryParam("details", "true")
                .build();
        final Response<RawJson> response = arangoDB.execute(request, RawJson.class);
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
    @MethodSource("arangos")
    void executeJS(ArangoDB arangoDB) {
        assumeTrue(isAtLeastVersion(3, 11));
        Request<?> request = Request.builder()
                .db(ArangoRequestParam.SYSTEM)
                .method(Request.Method.POST)
                .path("/_admin/execute")
                .body(JsonNodeFactory.instance.textNode("return 11;"))
                .build();
        final Response<Integer> response = arangoDB.execute(request, Integer.class);
        assertThat(response.getBody()).isEqualTo(11);
    }

    @ParameterizedTest
    @MethodSource("arangos")
    void getLogEntries(ArangoDB arangoDB) {
        assumeTrue(isAtLeastVersion(3, 8));
        final LogEntriesEntity logs = arangoDB.getLogEntries(null);
        assertThat(logs.getTotal()).isPositive();
        assertThat(logs.getMessages()).hasSize(logs.getTotal().intValue());
    }

    @ParameterizedTest
    @MethodSource("arangos")
    void getLogEntriesUpto(ArangoDB arangoDB) {
        assumeTrue(isAtLeastVersion(3, 8));
        final LogEntriesEntity logsUpto = arangoDB.getLogEntries(new LogOptions().upto(LogLevel.WARNING));
        assertThat(logsUpto.getMessages())
                .map(LogEntriesEntity.Message::getLevel)
                .doesNotContain("INFO");
    }

    @ParameterizedTest
    @MethodSource("arangos")
    void getLogEntriesLevel(ArangoDB arangoDB) {
        assumeTrue(isAtLeastVersion(3, 8));
        final LogEntriesEntity logsInfo = arangoDB.getLogEntries(new LogOptions().level(LogLevel.INFO));
        assertThat(logsInfo.getMessages())
                .map(LogEntriesEntity.Message::getLevel)
                .containsOnly("INFO");
    }

    @ParameterizedTest
    @MethodSource("arangos")
    void getLogEntriesStart(ArangoDB arangoDB) {
        assumeTrue(isAtLeastVersion(3, 8));
        final LogEntriesEntity logs = arangoDB.getLogEntries(null);
        final Long firstId = logs.getMessages().get(0).getId();
        final LogEntriesEntity logsStart = arangoDB.getLogEntries(new LogOptions().start(firstId + 1));
        assertThat(logsStart.getMessages())
                .map(LogEntriesEntity.Message::getId)
                .doesNotContain(firstId);
    }

    @ParameterizedTest
    @MethodSource("arangos")
    void getLogEntriesSize(ArangoDB arangoDB) {
        assumeTrue(isAtLeastVersion(3, 8));
        final LogEntriesEntity logs = arangoDB.getLogEntries(null);
        int count = logs.getMessages().size();
        assertThat(count).isPositive();
        final LogEntriesEntity logsSize = arangoDB.getLogEntries(new LogOptions().size(count - 1));
        assertThat(logsSize.getMessages()).hasSize(count - 1);
    }

    @ParameterizedTest
    @MethodSource("arangos")
    void getLogEntriesOffset(ArangoDB arangoDB) {
        assumeTrue(isAtLeastVersion(3, 8));
        final LogEntriesEntity logs = arangoDB.getLogEntries(null);
        assertThat(logs.getTotal()).isPositive();
        Long firstId = logs.getMessages().get(0).getId();
        final LogEntriesEntity logsOffset = arangoDB.getLogEntries(new LogOptions().offset(1));
        assertThat(logsOffset.getMessages())
                .map(LogEntriesEntity.Message::getId)
                .doesNotContain(firstId);
    }

    @ParameterizedTest
    @MethodSource("arangos")
    void getLogEntriesSearch(ArangoDB arangoDB) {
        assumeTrue(isAtLeastVersion(3, 8));
        final LogEntriesEntity logs = arangoDB.getLogEntries(null);
        final LogEntriesEntity logsSearch = arangoDB.getLogEntries(new LogOptions().search(getTestDb()));
        assertThat(logs.getTotal()).isGreaterThan(logsSearch.getTotal());
    }

    @ParameterizedTest
    @MethodSource("arangos")
    void getLogEntriesSortAsc(ArangoDB arangoDB) {
        assumeTrue(isAtLeastVersion(3, 8));
        final LogEntriesEntity logs = arangoDB.getLogEntries(new LogOptions().sort(SortOrder.asc));
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
    @MethodSource("arangos")
    void getLogEntriesSortDesc(ArangoDB arangoDB) {
        assumeTrue(isAtLeastVersion(3, 8));
        final LogEntriesEntity logs = arangoDB.getLogEntries(new LogOptions().sort(SortOrder.desc));
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
    @MethodSource("arangos")
    void getLogLevel(ArangoDB arangoDB) {
        assumeTrue(isAtLeastVersion(3, 7)); // it fails in 3.6 active-failover (BTS-362)
        final LogLevelEntity logLevel = arangoDB.getLogLevel();
        assertThat(logLevel.getAgency()).isEqualTo(LogLevelEntity.LogLevel.INFO);
    }

    @ParameterizedTest
    @MethodSource("arangos")
    void setLogLevel(ArangoDB arangoDB) {
        assumeTrue(isAtLeastVersion(3, 7)); // it fails in 3.6 active-failover (BTS-362)
        final LogLevelEntity entity = new LogLevelEntity();
        try {
            entity.setAgency(LogLevelEntity.LogLevel.ERROR);
            final LogLevelEntity logLevel = arangoDB.setLogLevel(entity);
            assertThat(logLevel.getAgency()).isEqualTo(LogLevelEntity.LogLevel.ERROR);
        } finally {
            entity.setAgency(LogLevelEntity.LogLevel.INFO);
            arangoDB.setLogLevel(entity);
        }
    }

    @ParameterizedTest
    @MethodSource("arangos")
    void setAllLogLevel(ArangoDB arangoDB) {
        assumeTrue(isAtLeastVersion(3, 12));
        final LogLevelEntity entity = new LogLevelEntity();
        try {
            entity.setAll(LogLevelEntity.LogLevel.ERROR);
            final LogLevelEntity logLevel = arangoDB.setLogLevel(entity);
            assertThat(logLevel.getAgency()).isEqualTo(LogLevelEntity.LogLevel.ERROR);
            assertThat(logLevel.getQueries()).isEqualTo(LogLevelEntity.LogLevel.ERROR);
            assertThat(logLevel.getRepWal()).isEqualTo(LogLevelEntity.LogLevel.ERROR);
            assertThat(logLevel.getRepState()).isEqualTo(LogLevelEntity.LogLevel.ERROR);
            LogLevelEntity retrievedLevels = arangoDB.getLogLevel();
            assertThat(retrievedLevels.getAgency()).isEqualTo(LogLevelEntity.LogLevel.ERROR);
        } finally {
            entity.setAll(LogLevelEntity.LogLevel.INFO);
            arangoDB.setLogLevel(entity);
        }
    }

    @ParameterizedTest
    @MethodSource("arangos")
    void logLevelWithServerId(ArangoDB arangoDB) {
        assumeTrue(isAtLeastVersion(3, 10));
        assumeTrue(isCluster());
        String serverId = arangoDB.getServerId();
        LogLevelOptions options = new LogLevelOptions().serverId(serverId);
        final LogLevelEntity entity = new LogLevelEntity();
        try {
            entity.setGraphs(LogLevelEntity.LogLevel.ERROR);
            final LogLevelEntity logLevel = arangoDB.setLogLevel(entity, options);
            assertThat(logLevel.getGraphs()).isEqualTo(LogLevelEntity.LogLevel.ERROR);
            assertThat(arangoDB.getLogLevel(options).getGraphs()).isEqualTo(LogLevelEntity.LogLevel.ERROR);
        } finally {
            entity.setGraphs(LogLevelEntity.LogLevel.INFO);
            arangoDB.setLogLevel(entity);
        }
    }

    @ParameterizedTest
    @MethodSource("arangos")
    void resetLogLevels(ArangoDB arangoDB) {
        assumeTrue(isAtLeastVersion(3, 12));
        LogLevelOptions options = new LogLevelOptions();
        LogLevelEntity entity = new LogLevelEntity();
        entity.setGraphs(LogLevelEntity.LogLevel.ERROR);

        LogLevelEntity err = arangoDB.setLogLevel(entity, options);
        assertThat(err.getGraphs()).isEqualTo(LogLevelEntity.LogLevel.ERROR);

        LogLevelEntity logLevel = arangoDB.resetLogLevels(options);
        assertThat(logLevel.getGraphs()).isEqualTo(LogLevelEntity.LogLevel.INFO);
    }

    @ParameterizedTest
    @MethodSource("arangos")
    void resetLogLevelsWithServerId(ArangoDB arangoDB) {
        assumeTrue(isAtLeastVersion(3, 12));
        assumeTrue(isCluster());
        String serverId = arangoDB.getServerId();
        LogLevelOptions options = new LogLevelOptions().serverId(serverId);

        LogLevelEntity entity = new LogLevelEntity();
        entity.setGraphs(LogLevelEntity.LogLevel.ERROR);

        LogLevelEntity err = arangoDB.setLogLevel(entity, options);
        assertThat(err.getGraphs()).isEqualTo(LogLevelEntity.LogLevel.ERROR);

        LogLevelEntity logLevel = arangoDB.resetLogLevels(options);
        assertThat(logLevel.getGraphs()).isEqualTo(LogLevelEntity.LogLevel.INFO);
    }

    @ParameterizedTest
    @MethodSource("arangos")
    void getQueryOptimizerRules(ArangoDB arangoDB) {
        assumeTrue(isAtLeastVersion(3, 10));
        final Collection<QueryOptimizerRule> rules = arangoDB.getQueryOptimizerRules();
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
    @MethodSource("arangos")
    void arangoDBException(ArangoDB arangoDB) {
        Throwable thrown = catchThrowable(() -> arangoDB.db("no").getInfo());
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
        ArangoDBException e = (ArangoDBException) thrown;
        assertThat(e.getResponseCode()).isEqualTo(404);
        assertThat(e.getErrorNum()).isEqualTo(1228);
    }

    @ParameterizedTest
    @MethodSource("arangos")
    void loadproperties() {
        Throwable thrown = catchThrowable(() -> new ArangoDB.Builder()
                .loadProperties(ConfigUtils.loadConfig("arangodb-bad.properties"))
        );
        assertThat(thrown).isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @MethodSource("arangos")
    void accessMultipleDatabases(ArangoDB arangoDB) {
        final ArangoDBVersion version1 = arangoDB.db(DB1).getVersion();
        assertThat(version1).isNotNull();
        final ArangoDBVersion version2 = arangoDB.db(DB2).getVersion();
        assertThat(version2).isNotNull();
    }

    @ParameterizedTest
    @MethodSource("arangos")
    @Disabled("Manual execution only")
    void queueTime(ArangoDB arangoDB) throws InterruptedException, ExecutionException {
        List<CompletableFuture<Void>> futures = IntStream.range(0, 80)
                .mapToObj(i -> CompletableFuture.runAsync(
                        () -> arangoDB.db().query("RETURN SLEEP(1)", Void.class),
                        Executors.newFixedThreadPool(80))
                )
                .collect(Collectors.toList());
        for (CompletableFuture<Void> f : futures) {
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

    @ParameterizedTest
    @MethodSource("arangos")
    void asyncAndLaterResultRetrieval(ArangoDB arangoDB) throws InterruptedException {
        Request<RawJson> request = Request.<RawJson>builder()
                .db(ArangoRequestParam.SYSTEM)
                .method(Request.Method.POST)
                .path("/_api/cursor")
                .header("x-arango-async", "store")
                .body(RawJson.of("{\"query\":\"RETURN SLEEP(0.1) || 5\"}"))
                .build();

        Response<?> response = arangoDB.execute(request, Void.class);
        String jobId = response.getHeaders().get("x-arango-async-id");

        Request<?> request2 = Request.builder()
                .db(ArangoRequestParam.SYSTEM)
                .method(Request.Method.PUT)
                .path("/_api/job/" + jobId)
                .build();

        Response<ObjectNode> response2 = arangoDB.execute(request2, ObjectNode.class);
        while (response2.getResponseCode() == 204) {
            Thread.sleep(50);
            response2 = arangoDB.execute(request2, ObjectNode.class);
        }

        assertThat(response2.getResponseCode()).isEqualTo(201);
        assertThat(response2.getBody().get("result").get(0).numberValue()).isEqualTo(5);
    }
}
