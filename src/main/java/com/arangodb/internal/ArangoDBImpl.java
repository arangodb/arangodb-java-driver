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

import com.arangodb.ArangoDB;
import com.arangodb.ArangoDBException;
import com.arangodb.ArangoDatabase;
import com.arangodb.Protocol;
import com.arangodb.entity.*;
import com.arangodb.internal.http.HttpCommunication;
import com.arangodb.internal.http.HttpProtocol;
import com.arangodb.internal.net.CommunicationProtocol;
import com.arangodb.internal.net.HostHandle;
import com.arangodb.internal.net.HostResolver;
import com.arangodb.internal.util.ArangoSerializationFactory;
import com.arangodb.internal.util.ArangoSerializationFactory.Serializer;
import com.arangodb.internal.velocystream.VstCommunicationSync;
import com.arangodb.internal.velocystream.VstProtocol;
import com.arangodb.model.DBCreateOptions;
import com.arangodb.model.LogOptions;
import com.arangodb.model.UserCreateOptions;
import com.arangodb.model.UserUpdateOptions;
import com.arangodb.util.ArangoCursorInitializer;
import com.arangodb.util.ArangoSerialization;
import com.arangodb.velocystream.Request;
import com.arangodb.velocystream.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collection;

/**
 * @author Mark Vollmary
 * @author Heiko Kernbach
 * @author Michele Rastelli
 */
public class ArangoDBImpl extends InternalArangoDB<ArangoExecutorSync> implements ArangoDB {

    private static final Logger LOGGER = LoggerFactory.getLogger(ArangoDBImpl.class);

    private ArangoCursorInitializer cursorInitializer;
    private final CommunicationProtocol cp;

    public ArangoDBImpl(final VstCommunicationSync.Builder vstBuilder, final HttpCommunication.Builder httpBuilder,
                        final ArangoSerializationFactory util, final Protocol protocol, final HostResolver hostResolver,
                        final ArangoContext context) {

        super(new ArangoExecutorSync(
                        createProtocol(vstBuilder, httpBuilder, util.get(Serializer.INTERNAL), protocol),
                        util,
                        new DocumentCache()),
                util,
                context);

        cp = createProtocol(
                new VstCommunicationSync.Builder(vstBuilder).maxConnections(1),
                new HttpCommunication.Builder(httpBuilder),
                util.get(Serializer.INTERNAL),
                protocol);

        hostResolver.init(this.executor(), util());

        LOGGER.debug("ArangoDB Client is ready to use");

    }

    private static CommunicationProtocol createProtocol(
            final VstCommunicationSync.Builder vstBuilder,
            final HttpCommunication.Builder httpBuilder,
            final ArangoSerialization util,
            final Protocol protocol) {

        return (protocol == null || Protocol.VST == protocol) ? createVST(vstBuilder, util)
                : createHTTP(httpBuilder, util);
    }

    private static CommunicationProtocol createVST(
            final VstCommunicationSync.Builder builder,
            final ArangoSerialization util) {
        return new VstProtocol(builder.build(util));
    }

    private static CommunicationProtocol createHTTP(
            final HttpCommunication.Builder builder,
            final ArangoSerialization util) {
        return new HttpProtocol(builder.build(util));
    }

    @Override
    protected ArangoExecutorSync executor() {
        return executor;
    }

    @Override
    public void shutdown() throws ArangoDBException {
        try {
            executor.disconnect();
            cp.close();
        } catch (final IOException e) {
            throw new ArangoDBException(e);
        }
    }

    @Override
    public ArangoDatabase db() {
        return db(ArangoRequestParam.SYSTEM);
    }

    @Override
    public ArangoDatabase db(final String name) {
        return new ArangoDatabaseImpl(this, name).setCursorInitializer(cursorInitializer);
    }

    @Override
    public Boolean createDatabase(final String name) throws ArangoDBException {
        return createDatabase(new DBCreateOptions().name(name));
    }

    @Override
    public Boolean createDatabase(DBCreateOptions options) throws ArangoDBException {
        return executor.execute(createDatabaseRequest(options), createDatabaseResponseDeserializer());
    }

    @Override
    public Collection<String> getDatabases() throws ArangoDBException {
        return executor.execute(getDatabasesRequest(db().name()), getDatabaseResponseDeserializer());
    }

    @Override
    public Collection<String> getAccessibleDatabases() throws ArangoDBException {
        return db().getAccessibleDatabases();
    }

    @Override
    public Collection<String> getAccessibleDatabasesFor(final String user) throws ArangoDBException {
        return executor.execute(getAccessibleDatabasesForRequest(db().name(), user),
                getAccessibleDatabasesForResponseDeserializer());
    }

    @Override
    public ArangoDBVersion getVersion() throws ArangoDBException {
        return db().getVersion();
    }

    @Override
    public ArangoDBEngine getEngine() throws ArangoDBException {
        return db().getEngine();
    }

    @Override
    public ServerRole getRole() throws ArangoDBException {
        return executor.execute(getRoleRequest(), getRoleResponseDeserializer());
    }

    @Override
    public UserEntity createUser(final String user, final String passwd) throws ArangoDBException {
        return executor.execute(createUserRequest(db().name(), user, passwd, new UserCreateOptions()),
                UserEntity.class);
    }

    @Override
    public UserEntity createUser(final String user, final String passwd, final UserCreateOptions options)
            throws ArangoDBException {
        return executor.execute(createUserRequest(db().name(), user, passwd, options), UserEntity.class);
    }

    @Override
    public void deleteUser(final String user) throws ArangoDBException {
        executor.execute(deleteUserRequest(db().name(), user), Void.class);
    }

    @Override
    public UserEntity getUser(final String user) throws ArangoDBException {
        return executor.execute(getUserRequest(db().name(), user), UserEntity.class);
    }

    @Override
    public Collection<UserEntity> getUsers() throws ArangoDBException {
        return executor.execute(getUsersRequest(db().name()), getUsersResponseDeserializer());
    }

    @Override
    public UserEntity updateUser(final String user, final UserUpdateOptions options) throws ArangoDBException {
        return executor.execute(updateUserRequest(db().name(), user, options), UserEntity.class);
    }

    @Override
    public UserEntity replaceUser(final String user, final UserUpdateOptions options) throws ArangoDBException {
        return executor.execute(replaceUserRequest(db().name(), user, options), UserEntity.class);
    }

    @Override
    public void grantDefaultDatabaseAccess(final String user, final Permissions permissions) throws ArangoDBException {
        executor.execute(updateUserDefaultDatabaseAccessRequest(user, permissions), Void.class);
    }

    @Override
    public void grantDefaultCollectionAccess(final String user, final Permissions permissions)
            throws ArangoDBException {
        executor.execute(updateUserDefaultCollectionAccessRequest(user, permissions), Void.class);
    }

    @Override
    public Response execute(final Request request) throws ArangoDBException {
        return executor.execute(request, response -> response);
    }

    @Override
    public Response execute(final Request request, final HostHandle hostHandle) throws ArangoDBException {
        return executor.execute(request, response -> response, hostHandle);
    }

    @Override
    public LogEntity getLogs(final LogOptions options) throws ArangoDBException {
        return executor.execute(getLogsRequest(options), LogEntity.class);
    }

    @Override
    public LogLevelEntity getLogLevel() throws ArangoDBException {
        return executor.execute(getLogLevelRequest(), LogLevelEntity.class);
    }

    @Override
    public LogLevelEntity setLogLevel(final LogLevelEntity entity) throws ArangoDBException {
        return executor.execute(setLogLevelRequest(entity), LogLevelEntity.class);
    }

    @Override
    public ArangoDBImpl _setCursorInitializer(final ArangoCursorInitializer cursorInitializer) {
        this.cursorInitializer = cursorInitializer;
        return this;
    }

}
