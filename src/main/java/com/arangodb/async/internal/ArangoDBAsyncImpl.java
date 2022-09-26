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

package com.arangodb.async.internal;

import com.arangodb.ArangoDBException;
import com.arangodb.ArangoMetrics;
import com.arangodb.DbName;
import com.arangodb.async.ArangoDBAsync;
import com.arangodb.async.ArangoDatabaseAsync;
import com.arangodb.async.internal.velocystream.VstCommunicationAsync;
import com.arangodb.entity.*;
import com.arangodb.internal.*;
import com.arangodb.internal.net.CommunicationProtocol;
import com.arangodb.internal.net.HostHandler;
import com.arangodb.internal.net.HostResolver;
import com.arangodb.internal.util.ArangoSerializationFactory;
import com.arangodb.internal.util.ArangoSerializationFactory.Serializer;
import com.arangodb.internal.velocystream.VstCommunication;
import com.arangodb.internal.velocystream.VstCommunicationSync;
import com.arangodb.internal.velocystream.VstProtocol;
import com.arangodb.internal.velocystream.internal.VstConnectionSync;
import com.arangodb.model.DBCreateOptions;
import com.arangodb.model.LogOptions;
import com.arangodb.model.UserCreateOptions;
import com.arangodb.model.UserUpdateOptions;
import com.arangodb.velocypack.Type;
import com.arangodb.velocystream.Request;
import com.arangodb.velocystream.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

/**
 * @author Mark Vollmary
 */
public class ArangoDBAsyncImpl extends InternalArangoDB<ArangoExecutorAsync> implements ArangoDBAsync {

    private static final Logger LOGGER = LoggerFactory.getLogger(ArangoDBAsyncImpl.class);

    private final CommunicationProtocol cp;
    private final HostHandler asyncHostHandler;
    private final HostHandler syncHostHandler;

    public ArangoDBAsyncImpl(
            final VstCommunicationAsync.Builder asyncCommBuilder,
            final ArangoSerializationFactory util,
            final VstCommunicationSync.Builder syncCommBuilder,
            final HostResolver asyncHostResolver,
            final HostResolver syncHostResolver,
            final HostHandler asyncHostHandler,
            final HostHandler syncHostHandler,
            final ArangoContext context,
            final int responseQueueTimeSamples,
            final int timeoutMs
    ) {

        super(new ArangoExecutorAsync(asyncCommBuilder.build(util.get(Serializer.INTERNAL)), util, new DocumentCache(),
                new QueueTimeMetricsImpl(responseQueueTimeSamples), timeoutMs), util, context);

        final VstCommunication<Response, VstConnectionSync> cacheCom = syncCommBuilder.build(util.get(Serializer.INTERNAL));

        cp = new VstProtocol(cacheCom);
        this.asyncHostHandler = asyncHostHandler;
        this.syncHostHandler = syncHostHandler;

        ArangoExecutorSync arangoExecutorSync = new ArangoExecutorSync(cp, util, new DocumentCache(),
                new QueueTimeMetricsImpl(responseQueueTimeSamples), timeoutMs);
        asyncHostResolver.init(arangoExecutorSync, util.get(Serializer.INTERNAL));
        syncHostResolver.init(arangoExecutorSync, util.get(Serializer.INTERNAL));

    }

    @Override
    protected ArangoExecutorAsync executor() {
        return executor;
    }

    @Override
    public void shutdown() throws ArangoDBException {
        try {
            executor.disconnect();
        } finally {
            try {
                cp.close();
            } catch (final IOException e) {
                LOGGER.error("Got exception during shutdown:", e);
            }
        }
    }

    @Override
    public void updateJwt(String jwt) {
        asyncHostHandler.setJwt(jwt);
        syncHostHandler.setJwt(jwt);
        cp.setJwt(jwt);
        executor.setJwt(jwt);
    }

    @Override
    public ArangoDatabaseAsync db() {
        return db(DbName.SYSTEM);
    }

    @Override
    public ArangoDatabaseAsync db(final DbName name) {
        return new ArangoDatabaseAsyncImpl(this, name);
    }

    @Override
    public ArangoMetrics metrics() {
        return new ArangoMetricsImpl(executor.getQueueTimeMetrics());
    }

    @Override
    public CompletableFuture<Boolean> createDatabase(final DbName name) {
        return createDatabase(new DBCreateOptions().name(name));
    }

    @Override
    public CompletableFuture<Boolean> createDatabase(DBCreateOptions options) {
        return executor.execute(createDatabaseRequest(options), createDatabaseResponseDeserializer());
    }

    @Override
    public CompletableFuture<Collection<String>> getDatabases() {
        return executor.execute(getDatabasesRequest(db().dbName()), getDatabaseResponseDeserializer());
    }

    @Override
    public CompletableFuture<Collection<String>> getAccessibleDatabases() {
        return db().getAccessibleDatabases();
    }

    @Override
    public CompletableFuture<Collection<String>> getAccessibleDatabasesFor(final String user) {
        return executor.execute(getAccessibleDatabasesForRequest(db().dbName(), user),
                getAccessibleDatabasesForResponseDeserializer());
    }

    @Override
    public CompletableFuture<ArangoDBVersion> getVersion() {
        return db().getVersion();
    }

    @Override
    public CompletableFuture<ServerRole> getRole() {
        return executor.execute(getRoleRequest(), getRoleResponseDeserializer());
    }

    @Override
    public CompletableFuture<UserEntity> createUser(final String user, final String passwd) {
        return executor.execute(createUserRequest(db().dbName(), user, passwd, new UserCreateOptions()),
                UserEntity.class);
    }

    @Override
    public CompletableFuture<UserEntity> createUser(
            final String user,
            final String passwd,
            final UserCreateOptions options) {
        return executor.execute(createUserRequest(db().dbName(), user, passwd, options), UserEntity.class);
    }

    @Override
    public CompletableFuture<Void> deleteUser(final String user) {
        return executor.execute(deleteUserRequest(db().dbName(), user), Void.class);
    }

    @Override
    public CompletableFuture<UserEntity> getUser(final String user) {
        return executor.execute(getUserRequest(db().dbName(), user), UserEntity.class);
    }

    @Override
    public CompletableFuture<Collection<UserEntity>> getUsers() {
        return executor.execute(getUsersRequest(db().dbName()), getUsersResponseDeserializer());
    }

    @Override
    public CompletableFuture<UserEntity> updateUser(final String user, final UserUpdateOptions options) {
        return executor.execute(updateUserRequest(db().dbName(), user, options), UserEntity.class);
    }

    @Override
    public CompletableFuture<UserEntity> replaceUser(final String user, final UserUpdateOptions options) {
        return executor.execute(replaceUserRequest(db().dbName(), user, options), UserEntity.class);
    }

    @Override
    public CompletableFuture<Void> grantDefaultDatabaseAccess(final String user, final Permissions permissions) {
        return executor.execute(updateUserDefaultDatabaseAccessRequest(user, permissions), Void.class);
    }

    @Override
    public CompletableFuture<Void> grantDefaultCollectionAccess(final String user, final Permissions permissions) {
        return executor.execute(updateUserDefaultCollectionAccessRequest(user, permissions), Void.class);
    }

    @Override
    public CompletableFuture<Response> execute(final Request request) {
        return executor.execute(request, response -> response);
    }

    @Override
    public CompletableFuture<LogEntity> getLogs(final LogOptions options) {
        return executor.execute(getLogsRequest(options), LogEntity.class);
    }

    @Override
    public CompletableFuture<LogEntriesEntity> getLogEntries(final LogOptions options) {
        return executor.execute(getLogEntriesRequest(options), LogEntriesEntity.class);
    }

    @Override
    public CompletableFuture<LogLevelEntity> getLogLevel() {
        return executor.execute(getLogLevelRequest(), LogLevelEntity.class);
    }

    @Override
    public CompletableFuture<LogLevelEntity> setLogLevel(final LogLevelEntity entity) {
        return executor.execute(setLogLevelRequest(entity), LogLevelEntity.class);
    }

    @Override
    public CompletableFuture<Collection<QueryOptimizerRule>> getQueryOptimizerRules() throws ArangoDBException {
        return executor.execute(getQueryOptimizerRulesRequest(), new Type<Collection<QueryOptimizerRule>>() {
        }.getType());
    }

}
