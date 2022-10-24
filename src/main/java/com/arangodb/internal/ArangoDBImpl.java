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
import com.arangodb.internal.http.HttpCommunication;
import com.arangodb.internal.http.HttpProtocol;
import com.arangodb.internal.net.CommunicationProtocol;
import com.arangodb.internal.net.HostHandle;
import com.arangodb.internal.net.HostHandler;
import com.arangodb.internal.net.HostResolver;
import com.arangodb.internal.serde.InternalSerde;
import com.arangodb.internal.serde.SerdeUtils;
import com.arangodb.internal.velocystream.VstCommunicationSync;
import com.arangodb.internal.velocystream.VstProtocol;
import com.arangodb.model.DBCreateOptions;
import com.arangodb.model.LogOptions;
import com.arangodb.model.UserCreateOptions;
import com.arangodb.model.UserUpdateOptions;
import com.arangodb.velocystream.Request;
import com.arangodb.velocystream.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

/**
 * @author Mark Vollmary
 * @author Heiko Kernbach
 * @author Michele Rastelli
 */
public class ArangoDBImpl extends InternalArangoDB<ArangoExecutorSync> implements ArangoDB {

    private static final Logger LOGGER = LoggerFactory.getLogger(ArangoDBImpl.class);
    private final HostHandler hostHandler;

    public ArangoDBImpl(final VstCommunicationSync.Builder vstBuilder, final HttpCommunication.Builder httpBuilder,
                        final InternalSerde util, final Protocol protocol, final HostResolver hostResolver,
                        final HostHandler hostHandler, int responseQueueTimeSamples,
                        final int timeoutMs) {

        super(new ArangoExecutorSync(
                        createProtocol(vstBuilder, httpBuilder, util, protocol),
                        util, new QueueTimeMetricsImpl(responseQueueTimeSamples), timeoutMs),
                util);

        this.hostHandler = hostHandler;
        hostResolver.init(this.executor(), getSerde());
        LOGGER.debug("ArangoDB Client is ready to use");
    }

    private static CommunicationProtocol createProtocol(
            final VstCommunicationSync.Builder vstBuilder,
            final HttpCommunication.Builder httpBuilder,
            final InternalSerde util,
            final Protocol protocol) {

        return (protocol == null || Protocol.VST == protocol) ? createVST(vstBuilder, util)
                : createHTTP(httpBuilder, util);
    }

    private static CommunicationProtocol createVST(
            final VstCommunicationSync.Builder builder,
            final InternalSerde util) {
        return new VstProtocol(builder.build(util));
    }

    private static CommunicationProtocol createHTTP(
            final HttpCommunication.Builder builder,
            final InternalSerde util) {
        return new HttpProtocol(builder.serde(util).build());
    }

    @Override
    protected ArangoExecutorSync executor() {
        return executor;
    }

    @Override
    public void shutdown() {
        executor.disconnect();
    }

    @Override
    public void updateJwt(String jwt) {
        hostHandler.setJwt(jwt);
        executor.setJwt(jwt);
    }

    @Override
    public ArangoDatabase db() {
        return db(DbName.SYSTEM);
    }

    @Override
    public ArangoDatabase db(final DbName dbName) {
        return new ArangoDatabaseImpl(this, dbName);
    }

    @Override
    public ArangoMetrics metrics() {
        return new ArangoMetricsImpl(executor.getQueueTimeMetrics());
    }

    @Override
    public Boolean createDatabase(final DbName dbName) {
        return createDatabase(new DBCreateOptions().name(dbName));
    }

    @Override
    public Boolean createDatabase(DBCreateOptions options) {
        return executor.execute(createDatabaseRequest(options), createDatabaseResponseDeserializer());
    }

    @Override
    public Collection<String> getDatabases() {
        return executor.execute(getDatabasesRequest(db().dbName()), getDatabaseResponseDeserializer());
    }

    @Override
    public Collection<String> getAccessibleDatabases() {
        return db().getAccessibleDatabases();
    }

    @Override
    public Collection<String> getAccessibleDatabasesFor(final String user) {
        return executor.execute(getAccessibleDatabasesForRequest(db().dbName(), user),
                getAccessibleDatabasesForResponseDeserializer());
    }

    @Override
    public ArangoDBVersion getVersion() {
        return db().getVersion();
    }

    @Override
    public ArangoDBEngine getEngine() {
        return db().getEngine();
    }

    @Override
    public ServerRole getRole() {
        return executor.execute(getRoleRequest(), getRoleResponseDeserializer());
    }

    @Override
    public String getServerId() {
        return executor.execute(getServerIdRequest(), getServerIdResponseDeserializer());
    }

    @Override
    public UserEntity createUser(final String user, final String passwd) {
        return executor.execute(createUserRequest(db().dbName(), user, passwd, new UserCreateOptions()),
                UserEntity.class);
    }

    @Override
    public UserEntity createUser(final String user, final String passwd, final UserCreateOptions options) {
        return executor.execute(createUserRequest(db().dbName(), user, passwd, options), UserEntity.class);
    }

    @Override
    public void deleteUser(final String user) {
        executor.execute(deleteUserRequest(db().dbName(), user), Void.class);
    }

    @Override
    public UserEntity getUser(final String user) {
        return executor.execute(getUserRequest(db().dbName(), user), UserEntity.class);
    }

    @Override
    public Collection<UserEntity> getUsers() {
        return executor.execute(getUsersRequest(db().dbName()), getUsersResponseDeserializer());
    }

    @Override
    public UserEntity updateUser(final String user, final UserUpdateOptions options) {
        return executor.execute(updateUserRequest(db().dbName(), user, options), UserEntity.class);
    }

    @Override
    public UserEntity replaceUser(final String user, final UserUpdateOptions options) {
        return executor.execute(replaceUserRequest(db().dbName(), user, options), UserEntity.class);
    }

    @Override
    public void grantDefaultDatabaseAccess(final String user, final Permissions permissions) {
        executor.execute(updateUserDefaultDatabaseAccessRequest(user, permissions), Void.class);
    }

    @Override
    public void grantDefaultCollectionAccess(final String user, final Permissions permissions) {
        executor.execute(updateUserDefaultCollectionAccessRequest(user, permissions), Void.class);
    }

    @Override
    public Response execute(final Request request) {
        return executor.execute(request, response -> response);
    }

    @Override
    public Response execute(final Request request, final HostHandle hostHandle) {
        return executor.execute(request, response -> response, hostHandle);
    }

    @Override
    public LogEntriesEntity getLogEntries(final LogOptions options) {
        return executor.execute(getLogEntriesRequest(options), LogEntriesEntity.class);
    }

    @Override
    public LogLevelEntity getLogLevel() {
        return executor.execute(getLogLevelRequest(), LogLevelEntity.class);
    }

    @Override
    public LogLevelEntity setLogLevel(final LogLevelEntity entity) {
        return executor.execute(setLogLevelRequest(entity), LogLevelEntity.class);
    }

    @Override
    public Collection<QueryOptimizerRule> getQueryOptimizerRules() {
        return executor.execute(getQueryOptimizerRulesRequest(), SerdeUtils.constructListType(QueryOptimizerRule.class));
    }

}
