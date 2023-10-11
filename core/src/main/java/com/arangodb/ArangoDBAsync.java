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
import com.arangodb.model.*;

import javax.annotation.concurrent.ThreadSafe;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

/**
 * Asynchronous version of {@link ArangoDB}
 */
@ThreadSafe
public interface ArangoDBAsync extends ArangoSerdeAccessor {

//    /**
//     * Returns a {@code ArangoDatabase} instance for the {@code _system} database.
//     *
//     * @return database handler
//     */
//    ArangoDatabase db();
//
//    /**
//     * Returns a {@code ArangoDatabase} instance for the given database name.
//     *
//     * @param name Name of the database
//     * @return database handler
//     */
//    ArangoDatabase db(String name);
//
//    /**
//     * @return entry point for accessing client metrics
//     */
//    ArangoMetrics metrics();

    /**
     * Asynchronous version of {@link ArangoDB#createDatabase(String)}
     */
    CompletableFuture<Boolean> createDatabase(String name);

    /**
     * Asynchronous version of {@link ArangoDB#createDatabase(DBCreateOptions)}
     */
    CompletableFuture<Boolean> createDatabase(DBCreateOptions options);

    /**
     * Asynchronous version of {@link ArangoDB#getDatabases()}
     */
    CompletableFuture<Collection<String>> getDatabases();

    /**
     * Asynchronous version of {@link ArangoDB#getAccessibleDatabases()}
     */
    CompletableFuture<Collection<String>> getAccessibleDatabases();

    /**
     * Asynchronous version of {@link ArangoDB#getAccessibleDatabasesFor(String)}
     */
    CompletableFuture<Collection<String>> getAccessibleDatabasesFor(String user);

    /**
     * Asynchronous version of {@link ArangoDB#getVersion()}
     */
    CompletableFuture<ArangoDBVersion> getVersion();

    /**
     * Asynchronous version of {@link ArangoDB#getEngine()}
     */
    CompletableFuture<ArangoDBEngine> getEngine();

    /**
     * Asynchronous version of {@link ArangoDB#getRole()}
     */
    CompletableFuture<ServerRole> getRole();

    /**
     * Asynchronous version of {@link ArangoDB#getServerId()}
     */
    CompletableFuture<String> getServerId();

    /**
     * Asynchronous version of {@link ArangoDB#createUser(String, String)}
     */
    CompletableFuture<UserEntity> createUser(String user, String passwd);

    /**
     * Asynchronous version of {@link ArangoDB#createUser(String, String, UserCreateOptions)}
     */
    CompletableFuture<UserEntity> createUser(String user, String passwd, UserCreateOptions options);

    /**
     * Asynchronous version of {@link ArangoDB#deleteUser(String)}
     */
    CompletableFuture<Void> deleteUser(String user);

    /**
     * Asynchronous version of {@link ArangoDB#getUser(String)}
     */
    CompletableFuture<UserEntity> getUser(String user);

    /**
     * Asynchronous version of {@link ArangoDB#getUsers()}
     */
    CompletableFuture<Collection<UserEntity>> getUsers();

    /**
     * Asynchronous version of {@link ArangoDB#updateUser(String, UserUpdateOptions)}
     */
    CompletableFuture<UserEntity> updateUser(String user, UserUpdateOptions options);

    /**
     * Asynchronous version of {@link ArangoDB#replaceUser(String, UserUpdateOptions)}
     */
    CompletableFuture<UserEntity> replaceUser(String user, UserUpdateOptions options);

    /**
     * Asynchronous version of {@link ArangoDB#grantDefaultDatabaseAccess(String, Permissions)}
     */
    CompletableFuture<Void> grantDefaultDatabaseAccess(String user, Permissions permissions);

    /**
     * Asynchronous version of {@link ArangoDB#grantDefaultCollectionAccess(String, Permissions)}
     */
    CompletableFuture<Void> grantDefaultCollectionAccess(String user, Permissions permissions);

    /**
     * Asynchronous version of {@link ArangoDB#execute(Request, Class)}
     */
    <T> CompletableFuture<Response<T>> execute(Request<?> request, Class<T> type);

    /**
     * Asynchronous version of {@link ArangoDB#getLogEntries(LogOptions)}
     */
    CompletableFuture<LogEntriesEntity> getLogEntries(LogOptions options);

    /**
     * Asynchronous version of {@link ArangoDB#getLogLevel()}
     */
    CompletableFuture<LogLevelEntity> getLogLevel();

    /**
     * Asynchronous version of {@link ArangoDB#getLogLevel(LogLevelOptions)}
     */
    CompletableFuture<LogLevelEntity> getLogLevel(LogLevelOptions options);

    /**
     * Asynchronous version of {@link ArangoDB#setLogLevel(LogLevelEntity)}
     */
    CompletableFuture<LogLevelEntity> setLogLevel(LogLevelEntity entity);

    /**
     * Asynchronous version of {@link ArangoDB#setLogLevel(LogLevelEntity, LogLevelOptions)}
     */
    CompletableFuture<LogLevelEntity> setLogLevel(LogLevelEntity entity, LogLevelOptions options);

    /**
     * Asynchronous version of {@link ArangoDB#getQueryOptimizerRules()}
     */
    CompletableFuture<Collection<QueryOptimizerRule>> getQueryOptimizerRules();

}
