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

package com.arangodb.async;

import com.arangodb.ArangoDBException;
import com.arangodb.entity.*;
import com.arangodb.model.LogOptions;
import com.arangodb.model.LogOptions.SortOrder;
import com.arangodb.model.UserCreateOptions;
import com.arangodb.model.UserUpdateOptions;
import com.arangodb.velocypack.exception.VPackException;
import com.arangodb.velocystream.Request;
import com.arangodb.velocystream.RequestType;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TestRule;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * @author Mark Vollmary
 * @author Michele Rastelli
 */
public class ArangoDBTest {

    private static final String ROOT = "root";
    private static final String USER = "mit dem mund";
    private static final String PW = "machts der hund";

    @ClassRule
    public static TestRule acquireHostListRule = TestUtils.acquireHostListRule;

    @Test
    public void getVersion() throws InterruptedException, ExecutionException {
        final ArangoDBAsync arangoDB = new ArangoDBAsync.Builder().build();
        arangoDB.getVersion()
                .whenComplete((version, ex) -> {
                    assertThat(version, is(notNullValue()));
                    assertThat(version.getServer(), is(notNullValue()));
                    assertThat(version.getVersion(), is(notNullValue()));
                })
                .get();
    }

    @Test(timeout = 2000)
    public void nestedGetVersion() {
        final ArangoDBAsync arangoDB = new ArangoDBAsync.Builder().build();
        for (int i = 0; i < 100; i++) {
            try {
                arangoDB.getVersion()
                        .whenComplete((v1, ex1) -> {
                            assertThat(v1, is(notNullValue()));
                            assertThat(v1.getServer(), is(notNullValue()));
                            assertThat(v1.getVersion(), is(notNullValue()));
                            try {
                                arangoDB.getVersion()
                                        .whenComplete((v2, ex2) -> {
                                            assertThat(v2, is(notNullValue()));
                                            assertThat(v2.getServer(), is(notNullValue()));
                                            assertThat(v2.getVersion(), is(notNullValue()));
                                            try {
                                                arangoDB.getVersion()
                                                        .whenComplete((v3, ex3) -> {
                                                            assertThat(v3, is(notNullValue()));
                                                            assertThat(v3.getServer(), is(notNullValue()));
                                                            assertThat(v3.getVersion(), is(notNullValue()));
                                                        })
                                                        .get();
                                            } catch (InterruptedException | ExecutionException e) {
                                                e.printStackTrace();
                                                fail();
                                            }
                                        })
                                        .get();
                            } catch (InterruptedException | ExecutionException e) {
                                e.printStackTrace();
                                fail();
                            }
                        })
                        .get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
                fail();
            }
        }
    }

    @Test
    public void createDatabase() throws InterruptedException, ExecutionException {
        final ArangoDBAsync arangoDB = new ArangoDBAsync.Builder().build();
        arangoDB.createDatabase(BaseTest.TEST_DB)
                .whenComplete((result, ex) -> assertThat(result, is(true)))
                .get();
        arangoDB.db(BaseTest.TEST_DB).drop().get();
    }

    @Test
    public void deleteDatabase() throws InterruptedException, ExecutionException {
        final ArangoDBAsync arangoDB = new ArangoDBAsync.Builder().build();
        final Boolean resultCreate = arangoDB.createDatabase(BaseTest.TEST_DB).get();
        assertThat(resultCreate, is(true));
        arangoDB.db(BaseTest.TEST_DB).drop()
                .whenComplete((resultDelete, ex) -> assertThat(resultDelete, is(true)))
                .get();
    }

    @Test
    public void getDatabases() throws InterruptedException, ExecutionException {
        final ArangoDBAsync arangoDB = new ArangoDBAsync.Builder().build();
        try {
            Collection<String> dbs = arangoDB.getDatabases().get();
            assertThat(dbs, is(notNullValue()));
            assertThat(dbs.size(), is(greaterThan(0)));
            final int dbCount = dbs.size();
            assertThat(dbs.iterator().next(), is("_system"));
            arangoDB.createDatabase(BaseTest.TEST_DB).get();
            dbs = arangoDB.getDatabases().get();
            assertThat(dbs.size(), is(greaterThan(dbCount)));
            assertThat(dbs, hasItem("_system"));
            assertThat(dbs, hasItem(BaseTest.TEST_DB));
        } finally {
            arangoDB.db(BaseTest.TEST_DB).drop().get();
        }
    }

    @Test
    public void getAccessibleDatabases() throws InterruptedException, ExecutionException {
        final ArangoDBAsync arangoDB = new ArangoDBAsync.Builder().build();
        arangoDB.getAccessibleDatabases()
                .whenComplete((dbs, ex) -> {
                    assertThat(dbs, is(notNullValue()));
                    assertThat(dbs.size(), greaterThan(0));
                    assertThat(dbs, hasItem("_system"));
                })
                .get();
    }

    @Test
    public void getAccessibleDatabasesFor() throws InterruptedException, ExecutionException {
        final ArangoDBAsync arangoDB = new ArangoDBAsync.Builder().build();
        arangoDB.getAccessibleDatabasesFor("root")
                .whenComplete((dbs, ex) -> {
                    assertThat(dbs, is(notNullValue()));
                    assertThat(dbs, is(notNullValue()));
                    assertThat(dbs.size(), greaterThan(0));
                    assertThat(dbs, hasItem("_system"));
                })
                .get();
    }

    @Test
    public void createUser() throws InterruptedException, ExecutionException {
        final ArangoDBAsync arangoDB = new ArangoDBAsync.Builder().build();
        try {
            arangoDB.createUser(USER, PW, null)
                    .whenComplete((result, ex) -> {
                        assertThat(result, is(notNullValue()));
                        assertThat(result.getUser(), is(USER));
                    })
                    .get();
        } finally {
            arangoDB.deleteUser(USER).get();
        }
    }

    @Test
    public void deleteUser() throws InterruptedException, ExecutionException {
        final ArangoDBAsync arangoDB = new ArangoDBAsync.Builder().build();
        arangoDB.createUser(USER, PW, null).get();
        arangoDB.deleteUser(USER).get();
    }

    @Test
    public void getUserRoot() throws InterruptedException, ExecutionException {
        final ArangoDBAsync arangoDB = new ArangoDBAsync.Builder().build();
        arangoDB.getUser(ROOT)
                .whenComplete((user, ex) -> {
                    assertThat(user, is(notNullValue()));
                    assertThat(user.getUser(), is(ROOT));
                })
                .get();
    }

    @Test
    public void getUser() throws InterruptedException, ExecutionException {
        final ArangoDBAsync arangoDB = new ArangoDBAsync.Builder().build();
        try {
            arangoDB.createUser(USER, PW, null).get();
            arangoDB.getUser(USER)
                    .whenComplete((user, ex) -> assertThat(user.getUser(), is(USER)))
                    .get();
        } finally {
            arangoDB.deleteUser(USER).get();
        }

    }

    @Test
    public void getUsersOnlyRoot() throws InterruptedException, ExecutionException {
        final ArangoDBAsync arangoDB = new ArangoDBAsync.Builder().build();
        arangoDB.getUsers()
                .whenComplete((users, ex) -> {
                    assertThat(users, is(notNullValue()));
                    assertThat(users.size(), greaterThan(0));
                })
                .get();
    }

    @Test
    public void getUsers() throws InterruptedException, ExecutionException {
        final ArangoDBAsync arangoDB = new ArangoDBAsync.Builder().build();
        try {
            arangoDB.createUser(USER, PW, null).get();
            arangoDB.getUsers()
                    .whenComplete((users, ex) -> {
                        assertThat(users, is(notNullValue()));
                        assertThat(users.size(), is(2));
                        for (final UserEntity user : users) {
                            assertThat(user.getUser(), anyOf(is(ROOT), is(USER)));
                        }
                    })
                    .get();
        } finally {
            arangoDB.deleteUser(USER).get();
        }
    }

    @Test
    public void updateUserNoOptions() throws InterruptedException, ExecutionException {
        final ArangoDBAsync arangoDB = new ArangoDBAsync.Builder().build();
        try {
            arangoDB.createUser(USER, PW, null).get();
            arangoDB.updateUser(USER, null).get();
        } finally {
            arangoDB.deleteUser(USER).get();
        }
    }

    @Test
    public void updateUser() throws InterruptedException, ExecutionException {
        final ArangoDBAsync arangoDB = new ArangoDBAsync.Builder().build();
        try {
            final Map<String, Object> extra = new HashMap<>();
            extra.put("hund", false);
            arangoDB.createUser(USER, PW, new UserCreateOptions().extra(extra)).get();
            extra.put("hund", true);
            extra.put("mund", true);
            {
                arangoDB.updateUser(USER, new UserUpdateOptions().extra(extra))
                        .whenComplete((user, ex) -> {
                            assertThat(user, is(notNullValue()));
                            assertThat(user.getExtra().size(), is(2));
                            assertThat(user.getExtra().get("hund"), is(notNullValue()));
                            assertThat(Boolean.valueOf(String.valueOf(user.getExtra().get("hund"))), is(true));
                        })
                        .get();
            }
            arangoDB.getUser(USER)
                    .whenComplete((user2, ex) -> {
                        assertThat(user2.getExtra().size(), is(2));
                        assertThat(user2.getExtra().get("hund"), is(notNullValue()));
                        assertThat(Boolean.valueOf(String.valueOf(user2.getExtra().get("hund"))), is(true));
                    })
                    .get();
        } finally {
            arangoDB.deleteUser(USER).get();
        }
    }

    @Test
    public void replaceUser() throws InterruptedException, ExecutionException {
        final ArangoDBAsync arangoDB = new ArangoDBAsync.Builder().build();
        try {
            final Map<String, Object> extra = new HashMap<>();
            extra.put("hund", false);
            arangoDB.createUser(USER, PW, new UserCreateOptions().extra(extra)).get();
            extra.remove("hund");
            extra.put("mund", true);
            {
                arangoDB.replaceUser(USER, new UserUpdateOptions().extra(extra))
                        .whenComplete((user, ex) -> {
                            assertThat(user, is(notNullValue()));
                            assertThat(user.getExtra().size(), is(1));
                            assertThat(user.getExtra().get("mund"), is(notNullValue()));
                            assertThat(Boolean.valueOf(String.valueOf(user.getExtra().get("mund"))), is(true));
                        })
                        .get();
            }
            {
                arangoDB.getUser(USER)
                        .whenComplete((user2, ex) -> {
                            assertThat(user2.getExtra().size(), is(1));
                            assertThat(user2.getExtra().get("mund"), is(notNullValue()));
                            assertThat(Boolean.valueOf(String.valueOf(user2.getExtra().get("mund"))), is(true));
                        })
                        .get();
            }
        } finally {
            arangoDB.deleteUser(USER).get();
        }
    }

    @Test
    public void updateUserDefaultDatabaseAccess() throws InterruptedException, ExecutionException {
        final ArangoDBAsync arangoDB = new ArangoDBAsync.Builder().build();
        try {
            arangoDB.createUser(USER, PW).get();
            arangoDB.grantDefaultDatabaseAccess(USER, Permissions.RW).get();
        } finally {
            arangoDB.deleteUser(USER).get();
        }
    }

    @Test
    public void updateUserDefaultCollectionAccess() throws InterruptedException, ExecutionException {
        final ArangoDBAsync arangoDB = new ArangoDBAsync.Builder().build();
        try {
            arangoDB.createUser(USER, PW).get();
            arangoDB.grantDefaultCollectionAccess(USER, Permissions.RW).get();
        } finally {
            arangoDB.deleteUser(USER).get();
        }
    }

    @Test
    public void authenticationFailPassword() throws InterruptedException {
        final ArangoDBAsync arangoDB = new ArangoDBAsync.Builder().password("no").build();
        try {
            arangoDB.getVersion().get();
            fail();
        } catch (final ExecutionException exception) {
            assertThat(exception.getCause(), instanceOf(ArangoDBException.class));
        }
    }

    @Test
    public void authenticationFailUser() throws InterruptedException {
        final ArangoDBAsync arangoDB = new ArangoDBAsync.Builder().user("no").build();
        try {
            arangoDB.getVersion().get();
            fail();
        } catch (final ExecutionException exception) {
            assertThat(exception.getCause(), instanceOf(ArangoDBException.class));
        }
    }

    @Test
    public void execute() throws VPackException, InterruptedException, ExecutionException {
        final ArangoDBAsync arangoDB = new ArangoDBAsync.Builder().build();
        arangoDB
                .execute(new Request("_system", RequestType.GET, "/_api/version"))
                .whenComplete((response, ex) -> {
                    assertThat(response.getBody(), is(notNullValue()));
                    assertThat(response.getBody().get("version").isString(), is(true));
                })
                .get();
    }

    @Test
    public void getLogs() throws InterruptedException, ExecutionException {
        final ArangoDBAsync arangoDB = new ArangoDBAsync.Builder().build();
        arangoDB.getLogs(null)
                .whenComplete((logs, ex) -> {
                    assertThat(logs, is(notNullValue()));
                    assertThat(logs.getTotalAmount(), greaterThan(0L));
                    assertThat((long) logs.getLid().size(), is(logs.getTotalAmount()));
                    assertThat((long) logs.getLevel().size(), is(logs.getTotalAmount()));
                    assertThat((long) logs.getTimestamp().size(), is(logs.getTotalAmount()));
                    assertThat((long) logs.getText().size(), is(logs.getTotalAmount()));
                })
                .get();
    }

    @Test
    public void getLogsUpto() throws InterruptedException, ExecutionException {
        final ArangoDBAsync arangoDB = new ArangoDBAsync.Builder().build();
        final LogEntity logs = arangoDB.getLogs(null).get();
        arangoDB.getLogs(new LogOptions().upto(LogLevel.WARNING))
                .whenComplete((logsUpto, ex) -> {
                    assertThat(logsUpto, is(notNullValue()));
                    assertThat(logs.getTotalAmount() >= logsUpto.getTotalAmount(), is(true));
                    assertThat(logsUpto.getLevel(), not(contains(LogLevel.INFO)));
                })
                .get();
    }

    @Test
    public void getLogsLevel() throws InterruptedException, ExecutionException {
        final ArangoDBAsync arangoDB = new ArangoDBAsync.Builder().build();
        final LogEntity logs = arangoDB.getLogs(null).get();
        arangoDB.getLogs(new LogOptions().level(LogLevel.INFO))
                .whenComplete((logsInfo, ex) -> {
                    assertThat(logsInfo, is(notNullValue()));
                    assertThat(logs.getTotalAmount() >= logsInfo.getTotalAmount(), is(true));
                    assertThat(logsInfo.getLevel(), everyItem(is(LogLevel.INFO)));
                })
                .get();
    }

    @Test
    public void getLogsStart() throws InterruptedException, ExecutionException {
        final ArangoDBAsync arangoDB = new ArangoDBAsync.Builder().build();
        final LogEntity logs = arangoDB.getLogs(null).get();
        assertThat(logs.getLid(), not(empty()));
        arangoDB.getLogs(new LogOptions().start(logs.getLid().get(0) + 1))
                .whenComplete((logsStart, ex) -> {
                    assertThat(logsStart, is(notNullValue()));
                    assertThat(logsStart.getLid(), not(contains(logs.getLid().get(0))));
                })
                .get();
    }

    @Test
    public void getLogsSize() throws InterruptedException, ExecutionException {
        final ArangoDBAsync arangoDB = new ArangoDBAsync.Builder().build();
        final LogEntity logs = arangoDB.getLogs(null).get();
        assertThat(logs.getLid().size(), greaterThan(0));
        arangoDB.getLogs(new LogOptions().size(logs.getLid().size() - 1))
                .whenComplete((logsSize, ex) -> {
                    assertThat(logsSize, is(notNullValue()));
                    assertThat(logsSize.getLid().size(), is(logs.getLid().size() - 1));
                })
                .get();
    }

    @Test
    public void getLogsOffset() throws InterruptedException, ExecutionException {
        final ArangoDBAsync arangoDB = new ArangoDBAsync.Builder().build();
        final LogEntity logs = arangoDB.getLogs(null).get();
        assertThat(logs.getTotalAmount(), greaterThan(0L));
        arangoDB.getLogs(new LogOptions().offset((int) (logs.getTotalAmount() - 1)))
                .whenComplete((logsOffset, ex) -> {
                    assertThat(logsOffset, is(notNullValue()));
                    assertThat(logsOffset.getLid().size(), is(1));
                })
                .get();
    }

    @Test
    public void getLogsSearch() throws InterruptedException, ExecutionException {
        final ArangoDBAsync arangoDB = new ArangoDBAsync.Builder().build();
        final LogEntity logs = arangoDB.getLogs(null).get();
        arangoDB.getLogs(new LogOptions().search(BaseTest.TEST_DB))
                .whenComplete((logsSearch, ex) -> {
                    assertThat(logsSearch, is(notNullValue()));
                    assertThat(logs.getTotalAmount(), greaterThan(logsSearch.getTotalAmount()));
                })
                .get();
    }

    @Test
    public void getLogsSortAsc() throws InterruptedException, ExecutionException {
        final ArangoDBAsync arangoDB = new ArangoDBAsync.Builder().build();
        arangoDB.getLogs(new LogOptions().sort(SortOrder.asc))
                .whenComplete((logs, ex) -> {
                    assertThat(logs, is(notNullValue()));
                    long lastId = -1;
                    for (final Long id : logs.getLid()) {
                        assertThat(id, greaterThan(lastId));
                        lastId = id;
                    }
                })
                .get();
    }

    @Test
    public void getLogsSortDesc() throws InterruptedException, ExecutionException {
        final ArangoDBAsync arangoDB = new ArangoDBAsync.Builder().build();
        arangoDB.getLogs(new LogOptions().sort(SortOrder.desc))
                .whenComplete((logs, ex) -> {
                    assertThat(logs, is(notNullValue()));
                    long lastId = Long.MAX_VALUE;
                    for (final Long id : logs.getLid()) {
                        assertThat(lastId, greaterThan(id));
                        lastId = id;
                    }
                })
                .get();
    }

    @Test
    public void getLogLevel() throws InterruptedException, ExecutionException {
        final ArangoDBAsync arangoDB = new ArangoDBAsync.Builder().build();
        arangoDB.getLogLevel()
                .whenComplete((logLevel, ex) -> {
                    assertThat(logLevel, is(notNullValue()));
                    assertThat(logLevel.getAgency(), is(LogLevelEntity.LogLevel.INFO));
                })
                .get();
    }

    @Test
    public void setLogLevel() throws InterruptedException, ExecutionException {
        final ArangoDBAsync arangoDB = new ArangoDBAsync.Builder().build();
        final LogLevelEntity entity = new LogLevelEntity();
        try {
            entity.setAgency(LogLevelEntity.LogLevel.ERROR);
            arangoDB.setLogLevel(entity)
                    .whenComplete((logLevel, ex) -> {
                        assertThat(logLevel, is(notNullValue()));
                        assertThat(logLevel.getAgency(), is(LogLevelEntity.LogLevel.ERROR));
                    })
                    .get();
        } finally {
            entity.setAgency(LogLevelEntity.LogLevel.INFO);
            arangoDB.setLogLevel(entity).get();
        }
    }
}
