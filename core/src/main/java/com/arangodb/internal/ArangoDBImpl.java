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
import com.arangodb.internal.config.ArangoConfig;
import com.arangodb.internal.net.HostHandler;
import com.arangodb.internal.net.HostResolver;
import com.arangodb.internal.net.ProtocolProvider;
import com.arangodb.internal.serde.SerdeUtils;
import com.arangodb.model.*;
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

    public ArangoDBImpl(final ArangoConfig config,
                        final HostResolver hostResolver, final ProtocolProvider protocolProvider,
                        final HostHandler hostHandler) {
        super(new ArangoExecutorSync(protocolProvider.createProtocol(config, hostHandler), config), config.getInternalSerde());
        this.hostHandler = hostHandler;
        hostResolver.init(this.executor(), getSerde());
        LOGGER.debug("ArangoDB Client is ready to use");
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
        return db(ArangoRequestParam.SYSTEM);
    }

    @Override
    public ArangoDatabase db(final String dbName) {
        return new ArangoDatabaseImpl(this, dbName);
    }

    @Override
    public ArangoMetrics metrics() {
        return new ArangoMetricsImpl(executor.getQueueTimeMetrics());
    }

    @Override
    public Boolean createDatabase(final String dbName) {
        return createDatabase(new DBCreateOptions().name(dbName));
    }

    @Override
    public Boolean createDatabase(DBCreateOptions options) {
        return executor.execute(createDatabaseRequest(options), createDatabaseResponseDeserializer());
    }

    @Override
    public Collection<String> getDatabases() {
        return executor.execute(getDatabasesRequest(db().name()), getDatabaseResponseDeserializer());
    }

    @Override
    public Collection<String> getAccessibleDatabases() {
        return db().getAccessibleDatabases();
    }

    @Override
    public Collection<String> getAccessibleDatabasesFor(final String user) {
        return executor.execute(getAccessibleDatabasesForRequest(db().name(), user),
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
        return executor.execute(createUserRequest(db().name(), user, passwd, new UserCreateOptions()),
                UserEntity.class);
    }

    @Override
    public UserEntity createUser(final String user, final String passwd, final UserCreateOptions options) {
        return executor.execute(createUserRequest(db().name(), user, passwd, options), UserEntity.class);
    }

    @Override
    public void deleteUser(final String user) {
        executor.execute(deleteUserRequest(db().name(), user), Void.class);
    }

    @Override
    public UserEntity getUser(final String user) {
        return executor.execute(getUserRequest(db().name(), user), UserEntity.class);
    }

    @Override
    public Collection<UserEntity> getUsers() {
        return executor.execute(getUsersRequest(db().name()), getUsersResponseDeserializer());
    }

    @Override
    public UserEntity updateUser(final String user, final UserUpdateOptions options) {
        return executor.execute(updateUserRequest(db().name(), user, options), UserEntity.class);
    }

    @Override
    public UserEntity replaceUser(final String user, final UserUpdateOptions options) {
        return executor.execute(replaceUserRequest(db().name(), user, options), UserEntity.class);
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
    public <T> Response<T> execute(Request<?> request, Class<T> type) {
        return executor.execute(executeRequest(request), responseDeserializer(type));
    }

    @Override
    public LogEntriesEntity getLogEntries(final LogOptions options) {
        return executor.execute(getLogEntriesRequest(options), LogEntriesEntity.class);
    }

    @Override
    public LogLevelEntity getLogLevel() {
        return getLogLevel(new LogLevelOptions());
    }

    @Override
    public LogLevelEntity getLogLevel(final LogLevelOptions options) {
        return executor.execute(getLogLevelRequest(options), LogLevelEntity.class);
    }

    @Override
    public LogLevelEntity setLogLevel(final LogLevelEntity entity) {
        return setLogLevel(entity, new LogLevelOptions());
    }

    @Override
    public LogLevelEntity setLogLevel(final LogLevelEntity entity, final LogLevelOptions options) {
        return executor.execute(setLogLevelRequest(entity, options), LogLevelEntity.class);
    }

    @Override
    public Collection<QueryOptimizerRule> getQueryOptimizerRules() {
        return executor.execute(getQueryOptimizerRulesRequest(), SerdeUtils.constructListType(QueryOptimizerRule.class));
    }

}
