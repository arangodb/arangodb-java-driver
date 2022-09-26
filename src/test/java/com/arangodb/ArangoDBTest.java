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
import com.arangodb.mapping.ArangoJack;
import com.arangodb.model.*;
import com.arangodb.model.LogOptions.SortOrder;
import com.arangodb.util.TestUtils;
import com.arangodb.velocypack.exception.VPackException;
import com.arangodb.velocystream.Request;
import com.arangodb.velocystream.RequestType;
import com.arangodb.velocystream.Response;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
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

    private static final DbName DB1 = DbName.of("ArangoDBTest_db1");
    private static final DbName DB2 = DbName.of("ArangoDBTest_db2");

    private static final String ROOT = "root";
    private static final String PW = "machts der hund";

    private static Boolean extendedNames;

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

    private boolean supportsExtendedNames(ArangoDB arangoDB) {
        if (extendedNames == null) {
            try {
                ArangoDatabase testDb = arangoDB.db(DbName.of("test-" + TestUtils.generateRandomDbName(20, true)));
                testDb.create();
                extendedNames = true;
                testDb.drop();
            } catch (ArangoDBException e) {
                extendedNames = false;
            }
        }
        return extendedNames;
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("arangos")
    void getVersion(ArangoDB arangoDB) {
        final ArangoDBVersion version = arangoDB.getVersion();
        assertThat(version.getServer()).isNotNull();
        assertThat(version.getVersion()).isNotNull();
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("arangos")
    void createAndDeleteDatabase(ArangoDB arangoDB) {
        final DbName dbName = DbName.of("testDB-" + TestUtils.generateRandomDbName(20, supportsExtendedNames(arangoDB)));
        final Boolean resultCreate;
        resultCreate = arangoDB.createDatabase(dbName);
        assertThat(resultCreate).isTrue();
        final Boolean resultDelete = arangoDB.db(dbName).drop();
        assertThat(resultDelete).isTrue();
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("arangos")
    void createWithNotNormalizedName(ArangoDB arangoDB) {
        assumeTrue(supportsExtendedNames(arangoDB));

        final String dbName = "testDB-\u006E\u0303\u00f1";
        DbName normalized = DbName.normalize(dbName);
        arangoDB.createDatabase(normalized);
        arangoDB.db(normalized).drop();

        Throwable thrown = catchThrowable(() -> DbName.of(dbName));
        assertThat(thrown)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not normalized");
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("arangos")
    void createDatabaseWithOptions(ArangoDB arangoDB) {
        assumeTrue(isCluster());
        assumeTrue(isAtLeastVersion(3, 6));
        final DbName dbName = DbName.of("testDB-" + TestUtils.generateRandomDbName(20, supportsExtendedNames(arangoDB)));
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
        assertThat(info.getReplicationFactor()).isEqualTo(2);
        assertThat(info.getWriteConcern()).isEqualTo(2);
        assertThat(info.getSharding()).isEmpty();
        assertThat(info.getSatellite()).isNull();

        final Boolean resultDelete = arangoDB.db(dbName).drop();
        assertThat(resultDelete).isTrue();
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("arangos")
    void createDatabaseWithOptionsSatellite(ArangoDB arangoDB) {
        assumeTrue(isCluster());
        assumeTrue(isEnterprise());
        assumeTrue(isAtLeastVersion(3, 6));

        final DbName dbName = DbName.of("testDB-" + TestUtils.generateRandomDbName(20, supportsExtendedNames(arangoDB)));
        final Boolean resultCreate = arangoDB.createDatabase(new DBCreateOptions()
                .name(dbName)
                .options(new DatabaseOptions()
                        .writeConcern(2)
                        .satellite(true)
                        .sharding("")
                )
        );
        assertThat(resultCreate).isTrue();

        DatabaseEntity info = arangoDB.db(dbName).getInfo();
        assertThat(info.getReplicationFactor()).isNull();
        assertThat(info.getSatellite()).isTrue();
        assertThat(info.getWriteConcern()).isEqualTo(2);
        assertThat(info.getSharding()).isEmpty();

        final Boolean resultDelete = arangoDB.db(dbName).drop();
        assertThat(resultDelete).isTrue();
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("arangos")
    void createDatabaseWithUsers(ArangoDB arangoDB) throws InterruptedException {
        final DbName dbName = DbName.of("testDB-" + TestUtils.generateRandomDbName(20, supportsExtendedNames(arangoDB)));
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
        assertThat(info.getName()).isEqualTo(dbName.get());

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
                .serializer(new ArangoJack())
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

    @ParameterizedTest(name = "{index}")
    @MethodSource("arangos")
    void getDatabases(ArangoDB arangoDB) {
        Collection<String> dbs = arangoDB.getDatabases();
        assertThat(dbs).contains("_system", DB1.get());
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("arangos")
    void getAccessibleDatabases(ArangoDB arangoDB) {
        final Collection<String> dbs = arangoDB.getAccessibleDatabases();
        assertThat(dbs).contains("_system");
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("arangos")
    void getAccessibleDatabasesFor(ArangoDB arangoDB) {
        final Collection<String> dbs = arangoDB.getAccessibleDatabasesFor("root");
        assertThat(dbs).contains("_system");
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("arangos")
    void createUser(ArangoDB arangoDB) {
        String username = "user-" + UUID.randomUUID();
        final UserEntity result = arangoDB.createUser(username, PW, null);
        assertThat(result.getUser()).isEqualTo(username);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("arangos")
    void deleteUser(ArangoDB arangoDB) {
        String username = "user-" + UUID.randomUUID();
        arangoDB.createUser(username, PW, null);
        arangoDB.deleteUser(username);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("arangos")
    void getUserRoot(ArangoDB arangoDB) {
        final UserEntity user = arangoDB.getUser(ROOT);
        assertThat(user.getUser()).isEqualTo(ROOT);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("arangos")
    void getUser(ArangoDB arangoDB) {
        String username = "user-" + UUID.randomUUID();
        arangoDB.createUser(username, PW, null);
        final UserEntity user = arangoDB.getUser(username);
        assertThat(user.getUser()).isEqualTo(username);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("arangos")
    void getUsersOnlyRoot(ArangoDB arangoDB) {
        final Collection<UserEntity> users = arangoDB.getUsers();
        assertThat(users).isNotEmpty();
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("arangos")
    void getUsers(ArangoDB arangoDB) {
        String username = "user-" + UUID.randomUUID();
        // Allow & account for pre-existing users other than ROOT:
        final Collection<UserEntity> initialUsers = arangoDB.getUsers();

        arangoDB.createUser(username, PW, null);
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
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("arangos")
    void updateUserNoOptions(ArangoDB arangoDB) {
        String username = "user-" + UUID.randomUUID();
        arangoDB.createUser(username, PW, null);
        arangoDB.updateUser(username, null);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("arangos")
    void updateUser(ArangoDB arangoDB) {
        String username = "user-" + UUID.randomUUID();
        final Map<String, Object> extra = new HashMap<>();
        extra.put("hund", false);
        arangoDB.createUser(username, PW, new UserCreateOptions().extra(extra));
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
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("arangos")
    void replaceUser(ArangoDB arangoDB) {
        String username = "user-" + UUID.randomUUID();
        final Map<String, Object> extra = new HashMap<>();
        extra.put("hund", false);
        arangoDB.createUser(username, PW, new UserCreateOptions().extra(extra));
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
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("arangos")
    void updateUserDefaultDatabaseAccess(ArangoDB arangoDB) {
        String username = "user-" + UUID.randomUUID();
        arangoDB.createUser(username, PW);
        arangoDB.grantDefaultDatabaseAccess(username, Permissions.RW);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("arangos")
    void updateUserDefaultCollectionAccess(ArangoDB arangoDB) {
        String username = "user-" + UUID.randomUUID();
        arangoDB.createUser(username, PW);
        arangoDB.grantDefaultCollectionAccess(username, Permissions.RW);
    }

    @Test
    void authenticationFailPassword() {
        final ArangoDB arangoDB = new ArangoDB.Builder().password("no").jwt(null).serializer(new ArangoJack()).build();
        Throwable thrown = catchThrowable(arangoDB::getVersion);
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
        assertThat(((ArangoDBException) thrown).getResponseCode()).isEqualTo(401);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("arangos")
    void authenticationFailUser() {
        final ArangoDB arangoDB = new ArangoDB.Builder().user("no").jwt(null).serializer(new ArangoJack()).build();
        Throwable thrown = catchThrowable(arangoDB::getVersion);
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
        assertThat(((ArangoDBException) thrown).getResponseCode()).isEqualTo(401);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("arangos")
    void execute(ArangoDB arangoDB) throws VPackException {
        final Response response = arangoDB.execute(new Request(DbName.SYSTEM, RequestType.GET, "/_api/version"));
        assertThat(response.getBody().get("version").isString()).isTrue();
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("arangos")
    void getLogs(ArangoDB arangoDB) {
        assumeTrue(isAtLeastVersion(3, 7)); // it fails in 3.6 active-failover (BTS-362)
        final LogEntity logs = arangoDB.getLogs(null);
        assertThat(logs.getTotalAmount()).isPositive();
        assertThat(logs.getLid()).hasSize(logs.getTotalAmount().intValue());
        assertThat(logs.getLevel()).hasSize(logs.getTotalAmount().intValue());
        assertThat(logs.getTimestamp()).hasSize(logs.getTotalAmount().intValue());
        assertThat(logs.getText()).hasSize(logs.getTotalAmount().intValue());
    }

    @Disabled
    @ParameterizedTest(name = "{index}")
    @MethodSource("arangos")
    void getLogsUpto(ArangoDB arangoDB) {
        assumeTrue(isAtLeastVersion(3, 7)); // it fails in 3.6 active-failover (BTS-362)
        final LogEntity logsUpto = arangoDB.getLogs(new LogOptions().upto(LogLevel.WARNING));
        assertThat(logsUpto.getLevel())
                .isNotEmpty()
                .doesNotContain(LogLevel.INFO);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("arangos")
    void getLogsLevel(ArangoDB arangoDB) {
        assumeTrue(isAtLeastVersion(3, 7)); // it fails in 3.6 active-failover (BTS-362)
        final LogEntity logsInfo = arangoDB.getLogs(new LogOptions().level(LogLevel.INFO));
        assertThat(logsInfo.getLevel()).containsOnly(LogLevel.INFO);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("arangos")
    void getLogsStart(ArangoDB arangoDB) {
        assumeTrue(isAtLeastVersion(3, 7)); // it fails in 3.6 active-failover (BTS-362)
        final LogEntity logs = arangoDB.getLogs(null);
        assertThat(logs.getLid()).isNotEmpty();
        final LogEntity logsStart = arangoDB.getLogs(new LogOptions().start(logs.getLid().get(0) + 1));
        assertThat(logsStart.getLid())
                .isNotEmpty()
                .doesNotContain(logs.getLid().get(0));
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("arangos")
    void getLogsSize(ArangoDB arangoDB) {
        assumeTrue(isAtLeastVersion(3, 7)); // it fails in 3.6 active-failover (BTS-362)
        final LogEntity logs = arangoDB.getLogs(null);
        assertThat(logs.getLid()).isNotEmpty();
        final LogEntity logsSize = arangoDB.getLogs(new LogOptions().size(logs.getLid().size() - 1));
        assertThat(logsSize.getLid()).hasSize(logs.getLid().size() - 1);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("arangos")
    void getLogsOffset(ArangoDB arangoDB) {
        assumeTrue(isAtLeastVersion(3, 7));  // it fails in 3.6 active-failover (BTS-362)
        assumeTrue(isLessThanVersion(3, 9)); // deprecated
        final LogEntity logs = arangoDB.getLogs(null);
        assertThat(logs.getTotalAmount()).isPositive();
        final LogEntity logsOffset = arangoDB.getLogs(new LogOptions().offset(1));
        assertThat(logsOffset.getLid())
                .isNotEmpty()
                .doesNotContain(logs.getLid().get(0));
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("arangos")
    void getLogsSearch(ArangoDB arangoDB) {
        assumeTrue(isAtLeastVersion(3, 7)); // it fails in 3.6 active-failover (BTS-362)
        final LogEntity logs = arangoDB.getLogs(null);
        final LogEntity logsSearch = arangoDB.getLogs(new LogOptions().search(TEST_DB.get()));
        assertThat(logs.getTotalAmount()).isGreaterThan(logsSearch.getTotalAmount());
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("arangos")
    void getLogsSortAsc(ArangoDB arangoDB) {
        assumeTrue(isAtLeastVersion(3, 7)); // it fails in 3.6 active-failover (BTS-362)
        final LogEntity logs = arangoDB.getLogs(new LogOptions().sort(SortOrder.asc));
        long lastId = -1;
        for (final Long id : logs.getLid()) {
            assertThat(id).isGreaterThan(lastId);
            lastId = id;
        }
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("arangos")
    void getLogsSortDesc(ArangoDB arangoDB) {
        assumeTrue(isAtLeastVersion(3, 7)); // it fails in 3.6 active-failover (BTS-362)
        final LogEntity logs = arangoDB.getLogs(new LogOptions().sort(SortOrder.desc));
        long lastId = Long.MAX_VALUE;
        for (final Long id : logs.getLid()) {
            assertThat(lastId).isGreaterThan(id);
            lastId = id;
        }
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("arangos")
    void getLogEntries(ArangoDB arangoDB) {
        assumeTrue(isAtLeastVersion(3, 8));
        final LogEntriesEntity logs = arangoDB.getLogEntries(null);
        assertThat(logs.getTotal()).isPositive();
        assertThat(logs.getMessages()).hasSize(logs.getTotal().intValue());
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("arangos")
    void getLogEntriesUpto(ArangoDB arangoDB) {
        assumeTrue(isAtLeastVersion(3, 8));
        final LogEntriesEntity logsUpto = arangoDB.getLogEntries(new LogOptions().upto(LogLevel.WARNING));
        assertThat(logsUpto.getMessages())
                .map(LogEntriesEntity.Message::getLevel)
                .doesNotContain("INFO");
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("arangos")
    void getLogEntriesLevel(ArangoDB arangoDB) {
        assumeTrue(isAtLeastVersion(3, 8));
        final LogEntriesEntity logsInfo = arangoDB.getLogEntries(new LogOptions().level(LogLevel.INFO));
        assertThat(logsInfo.getMessages())
                .map(LogEntriesEntity.Message::getLevel)
                .containsOnly("INFO");
    }

    @ParameterizedTest(name = "{index}")
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

    @ParameterizedTest(name = "{index}")
    @MethodSource("arangos")
    void getLogEntriesSize(ArangoDB arangoDB) {
        assumeTrue(isAtLeastVersion(3, 8));
        final LogEntriesEntity logs = arangoDB.getLogEntries(null);
        int count = logs.getMessages().size();
        assertThat(count).isPositive();
        final LogEntriesEntity logsSize = arangoDB.getLogEntries(new LogOptions().size(count - 1));
        assertThat(logsSize.getMessages()).hasSize(count - 1);
    }

    @ParameterizedTest(name = "{index}")
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

    @ParameterizedTest(name = "{index}")
    @MethodSource("arangos")
    void getLogEntriesSearch(ArangoDB arangoDB) {
        assumeTrue(isAtLeastVersion(3, 8));
        final LogEntriesEntity logs = arangoDB.getLogEntries(null);
        final LogEntriesEntity logsSearch = arangoDB.getLogEntries(new LogOptions().search(TEST_DB.get()));
        assertThat(logs.getTotal()).isGreaterThan(logsSearch.getTotal());
    }

    @ParameterizedTest(name = "{index}")
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

    @ParameterizedTest(name = "{index}")
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

    @ParameterizedTest(name = "{index}")
    @MethodSource("arangos")
    void getLogLevel(ArangoDB arangoDB) {
        assumeTrue(isAtLeastVersion(3, 7)); // it fails in 3.6 active-failover (BTS-362)
        final LogLevelEntity logLevel = arangoDB.getLogLevel();
        assertThat(logLevel.getAgency()).isEqualTo(LogLevelEntity.LogLevel.INFO);
    }

    @ParameterizedTest(name = "{index}")
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

    @ParameterizedTest(name = "{index}")
    @MethodSource("arangos")
    void setAllLogLevel(ArangoDB arangoDB) {
        assumeTrue(isAtLeastVersion(3, 9));
        final LogLevelEntity entity = new LogLevelEntity();
        try {
            entity.setAll(LogLevelEntity.LogLevel.ERROR);
            final LogLevelEntity logLevel = arangoDB.setLogLevel(entity);
            assertThat(logLevel.getAgency()).isEqualTo(LogLevelEntity.LogLevel.ERROR);
            assertThat(logLevel.getQueries()).isEqualTo(LogLevelEntity.LogLevel.ERROR);
            LogLevelEntity retrievedLevels = arangoDB.getLogLevel();
            assertThat(retrievedLevels.getAgency()).isEqualTo(LogLevelEntity.LogLevel.ERROR);
        } finally {
            entity.setAll(LogLevelEntity.LogLevel.INFO);
            arangoDB.setLogLevel(entity);
        }
    }

    @ParameterizedTest(name = "{index}")
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

    @ParameterizedTest(name = "{index}")
    @MethodSource("arangos")
    void arangoDBException(ArangoDB arangoDB) {
        Throwable thrown = catchThrowable(() -> arangoDB.db(DbName.of("no")).getInfo());
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
        ArangoDBException e = (ArangoDBException) thrown;
        assertThat(e.getResponseCode()).isEqualTo(404);
        assertThat(e.getErrorNum()).isEqualTo(1228);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("arangos")
    void fallbackHost() {
        final ArangoDB arangoDB = new ArangoDB.Builder().host("not-accessible", 8529).host("127.0.0.1", 8529).serializer(new ArangoJack()).build();
        final ArangoDBVersion version = arangoDB.getVersion();
        assertThat(version).isNotNull();
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("arangos")
    void loadproperties() {
        Throwable thrown = catchThrowable(() ->
                new ArangoDB.Builder().loadProperties(ArangoDBTest.class.getResourceAsStream("/arangodb-bad.properties"))
        );
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("arangos")
    void loadproperties2() {
        Throwable thrown = catchThrowable(() ->
                new ArangoDB.Builder().loadProperties(ArangoDBTest.class.getResourceAsStream("/arangodb-bad2.properties"))
        );
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("arangos")
    void accessMultipleDatabases(ArangoDB arangoDB) {
        final ArangoDBVersion version1 = arangoDB.db(DB1).getVersion();
        assertThat(version1).isNotNull();
        final ArangoDBVersion version2 = arangoDB.db(DB2).getVersion();
        assertThat(version2).isNotNull();
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("arangos")
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
            assertThat(avg).isPositive();
        } else {
            assertThat(avg).isEqualTo(0.0);
            assertThat(values).isEmpty();
        }

    }
}
