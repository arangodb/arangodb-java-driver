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

package com.arangodb.internal;

import com.arangodb.*;
import com.arangodb.entity.*;
import com.arangodb.internal.serde.SerdeUtils;
import com.arangodb.model.*;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

/**
 * @author Mark Vollmary
 * @author Heiko Kernbach
 * @author Michele Rastelli
 */
public class ArangoDBAsyncImpl extends InternalArangoDB implements ArangoDBAsync {

    private final ArangoDB arangoDB;

    public ArangoDBAsyncImpl(final ArangoDBImpl arangoDB) {
        super(arangoDB);
        this.arangoDB = arangoDB;
    }

    @Override
    public void shutdown() {
        arangoDB.shutdown();
    }

    @Override
    public void updateJwt(String jwt) {
        arangoDB.updateJwt(jwt);
    }

    @Override
    public ArangoDatabaseAsync db() {
        return db(ArangoRequestParam.SYSTEM);
    }

    @Override
    public ArangoDatabaseAsync db(final String dbName) {
        return new ArangoDatabaseAsyncImpl(this, dbName);
    }

    @Override
    public ArangoMetrics metrics() {
        return new ArangoMetricsImpl(executorAsync().getQueueTimeMetrics());
    }

    @Override
    public CompletableFuture<Boolean> createDatabase(final String dbName) {
        return createDatabase(new DBCreateOptions().name(dbName));
    }

    @Override
    public CompletableFuture<Boolean> createDatabase(DBCreateOptions options) {
        return executorAsync().execute(() -> createDatabaseRequest(options), createDatabaseResponseDeserializer());
    }

    @Override
    public CompletableFuture<Collection<String>> getDatabases() {
        return executorAsync().execute(() -> getDatabasesRequest(ArangoRequestParam.SYSTEM), getDatabaseResponseDeserializer());
    }

    @Override
    public CompletableFuture<Collection<String>> getAccessibleDatabases() {
        return db().getAccessibleDatabases();
    }

    @Override
    public CompletableFuture<Collection<String>> getAccessibleDatabasesFor(final String user) {
        return executorAsync().execute(() -> getAccessibleDatabasesForRequest(ArangoRequestParam.SYSTEM, user),
                getAccessibleDatabasesForResponseDeserializer());
    }

    @Override
    public CompletableFuture<ArangoDBVersion> getVersion() {
        return db().getVersion();
    }

    @Override
    public CompletableFuture<ArangoDBEngine> getEngine() {
        return db().getEngine();
    }

    @Override
    public CompletableFuture<ServerRole> getRole() {
        return executorAsync().execute(this::getRoleRequest, getRoleResponseDeserializer());
    }

    @Override
    public CompletableFuture<String> getServerId() {
        return executorAsync().execute(this::getServerIdRequest, getServerIdResponseDeserializer());
    }

    @Override
    public CompletableFuture<UserEntity> createUser(final String user, final String passwd) {
        return executorAsync().execute(() -> createUserRequest(ArangoRequestParam.SYSTEM, user, passwd, new UserCreateOptions()),
                UserEntity.class);
    }

    @Override
    public CompletableFuture<UserEntity> createUser(final String user, final String passwd, final UserCreateOptions options) {
        return executorAsync().execute(() -> createUserRequest(ArangoRequestParam.SYSTEM, user, passwd, options), UserEntity.class);
    }

    @Override
    public CompletableFuture<Void> deleteUser(final String user) {
        return executorAsync().execute(() -> deleteUserRequest(ArangoRequestParam.SYSTEM, user), Void.class);
    }

    @Override
    public CompletableFuture<UserEntity> getUser(final String user) {
        return executorAsync().execute(() -> getUserRequest(ArangoRequestParam.SYSTEM, user), UserEntity.class);
    }

    @Override
    public CompletableFuture<Collection<UserEntity>> getUsers() {
        return executorAsync().execute(() -> getUsersRequest(ArangoRequestParam.SYSTEM), getUsersResponseDeserializer());
    }

    @Override
    public CompletableFuture<UserEntity> updateUser(final String user, final UserUpdateOptions options) {
        return executorAsync().execute(() -> updateUserRequest(ArangoRequestParam.SYSTEM, user, options), UserEntity.class);
    }

    @Override
    public CompletableFuture<UserEntity> replaceUser(final String user, final UserUpdateOptions options) {
        return executorAsync().execute(() -> replaceUserRequest(ArangoRequestParam.SYSTEM, user, options), UserEntity.class);
    }

    @Override
    public CompletableFuture<Void> grantDefaultDatabaseAccess(final String user, final Permissions permissions) {
        return executorAsync().execute(() -> updateUserDefaultDatabaseAccessRequest(user, permissions), Void.class);
    }

    @Override
    public CompletableFuture<Void> grantDefaultCollectionAccess(final String user, final Permissions permissions) {
        return executorAsync().execute(() -> updateUserDefaultCollectionAccessRequest(user, permissions), Void.class);
    }

    @Override
    public <T> CompletableFuture<Response<T>> execute(Request<?> request, Class<T> type) {
        return executorAsync().execute(() -> executeRequest(request), responseDeserializer(type));
    }

    @Override
    public CompletableFuture<LogEntriesEntity> getLogEntries(final LogOptions options) {
        return executorAsync().execute(() -> getLogEntriesRequest(options), LogEntriesEntity.class);
    }

    @Override
    public CompletableFuture<LogLevelEntity> getLogLevel() {
        return getLogLevel(new LogLevelOptions());
    }

    @Override
    public CompletableFuture<LogLevelEntity> getLogLevel(final LogLevelOptions options) {
        return executorAsync().execute(() -> getLogLevelRequest(options), LogLevelEntity.class);
    }

    @Override
    public CompletableFuture<LogLevelEntity> setLogLevel(final LogLevelEntity entity) {
        return setLogLevel(entity, new LogLevelOptions());
    }

    @Override
    public CompletableFuture<LogLevelEntity> setLogLevel(final LogLevelEntity entity, final LogLevelOptions options) {
        return executorAsync().execute(() -> setLogLevelRequest(entity, options), LogLevelEntity.class);
    }

    @Override
    public CompletableFuture<LogLevelEntity> resetLogLevels(LogLevelOptions options) {
        return executorAsync().execute(() -> resetLogLevelsRequest(options), LogLevelEntity.class);
    }

    @Override
    public CompletableFuture<Collection<QueryOptimizerRule>> getQueryOptimizerRules() {
        return executorAsync().execute(this::getQueryOptimizerRulesRequest, SerdeUtils.constructListType(QueryOptimizerRule.class));
    }

}
