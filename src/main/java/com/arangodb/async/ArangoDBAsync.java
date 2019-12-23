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
import com.arangodb.ArangoSerializationAccessor;
import com.arangodb.Protocol;
import com.arangodb.async.internal.ArangoDBAsyncImpl;
import com.arangodb.async.internal.velocystream.VstCommunicationAsync;
import com.arangodb.async.internal.velocystream.VstConnectionFactoryAsync;
import com.arangodb.entity.*;
import com.arangodb.internal.ArangoContext;
import com.arangodb.internal.ArangoDefaults;
import com.arangodb.internal.InternalArangoDBBuilder;
import com.arangodb.internal.net.ConnectionFactory;
import com.arangodb.internal.net.HostHandler;
import com.arangodb.internal.net.HostResolver;
import com.arangodb.internal.util.ArangoDeserializerImpl;
import com.arangodb.internal.util.ArangoSerializationFactory;
import com.arangodb.internal.util.ArangoSerializerImpl;
import com.arangodb.internal.util.DefaultArangoSerialization;
import com.arangodb.internal.velocystream.VstCommunicationSync;
import com.arangodb.model.DBCreateOptions;
import com.arangodb.model.LogOptions;
import com.arangodb.model.UserCreateOptions;
import com.arangodb.model.UserUpdateOptions;
import com.arangodb.util.ArangoDeserializer;
import com.arangodb.util.ArangoSerialization;
import com.arangodb.util.ArangoSerializer;
import com.arangodb.velocypack.*;
import com.arangodb.velocystream.Request;
import com.arangodb.velocystream.Response;

import javax.net.ssl.SSLContext;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

/**
 * Central access point for applications to communicate with an ArangoDB server.
 *
 * <p>
 * Will be instantiated through {@link ArangoDBAsync.Builder}
 * </p>
 *
 * <pre>
 * ArangoDBAsync arango = new ArangoDBAsync.Builder().build();
 * ArangoDBAsync arango = new ArangoDBAsync.Builder().host("127.0.0.1", 8529).build();
 * </pre>
 *
 * @author Mark Vollmary
 */
public interface ArangoDBAsync extends ArangoSerializationAccessor {

    void shutdown() throws ArangoDBException;

    /**
     * Returns a handler of the system database
     *
     * @return database handler
     */
    ArangoDatabaseAsync db();

    /**
     * Returns a handler of the database by the given name
     *
     * @param name Name of the database
     * @return database handler
     */
    ArangoDatabaseAsync db(final String name);

    /**
     * Creates a new database
     *
     * @param name Has to contain a valid database name
     * @return true if the database was created successfully.
     * @see <a href="https://www.arangodb.com/docs/stable/http/database-database-management.html#create-database">API
     * Documentation</a>
     */
    CompletableFuture<Boolean> createDatabase(final String name);

    /**
     * Creates a new database
     *
     * @param options Creation options
     * @return true if the database was created successfully.
     * @see <a href="https://www.arangodb.com/docs/stable/http/database-database-management.html#create-database">API
     * Documentation</a>
     * @since ArangoDB 3.6.0
     */
    CompletableFuture<Boolean> createDatabase(final DBCreateOptions options);

    /**
     * Retrieves a list of all existing databases
     *
     * @return a list of all existing databases
     * @see <a href="https://www.arangodb.com/docs/stable/http/database-database-management.html#list-of-databases">API
     * Documentation</a>
     */
    CompletableFuture<Collection<String>> getDatabases();

    /**
     * Retrieves a list of all databases the current user can access
     *
     * @return a list of all databases the current user can access
     * @see <a href=
     * "https://www.arangodb.com/docs/stable/http/database-database-management.html#list-of-accessible-databases">API
     * Documentation</a>
     */
    CompletableFuture<Collection<String>> getAccessibleDatabases();

    /**
     * List available database to the specified user
     *
     * @param user The name of the user for which you want to query the databases
     * @return
     * @see <a href=
     * "https://www.arangodb.com/docs/stable/http/user-management.html#list-the-accessible-databases-for-a-user">API
     * Documentation</a>
     */
    CompletableFuture<Collection<String>> getAccessibleDatabasesFor(final String user);

    /**
     * Returns the server name and version number.
     *
     * @return the server version, number
     * @see <a href="https://www.arangodb.com/docs/stable/http/miscellaneous-functions.html#return-server-version">API
     * Documentation</a>
     */
    CompletableFuture<ArangoDBVersion> getVersion();

    /**
     * Returns the server role.
     *
     * @return the server role
     */
    CompletableFuture<ServerRole> getRole();

    /**
     * Create a new user. This user will not have access to any database. You need permission to the _system database in
     * order to execute this call.
     *
     * @param user   The name of the user
     * @param passwd The user password
     * @return information about the user
     * @see <a href="https://www.arangodb.com/docs/stable/http/user-management.html#create-user">API Documentation</a>
     */
    CompletableFuture<UserEntity> createUser(final String user, final String passwd);

    /**
     * Create a new user. This user will not have access to any database. You need permission to the _system database in
     * order to execute this call.
     *
     * @param user    The name of the user
     * @param passwd  The user password
     * @param options Additional properties of the user, can be null
     * @return information about the user
     * @see <a href="https://www.arangodb.com/docs/stable/http/user-management.html#create-user">API Documentation</a>
     */
    CompletableFuture<UserEntity> createUser(final String user, final String passwd, final UserCreateOptions options);

    /**
     * Removes an existing user, identified by user. You need access to the _system database.
     *
     * @param user The name of the user
     * @return void
     * @see <a href="https://www.arangodb.com/docs/stable/http/user-management.html#remove-user">API Documentation</a>
     */
    CompletableFuture<Void> deleteUser(final String user);

    /**
     * Fetches data about the specified user. You can fetch information about yourself or you need permission to the
     * _system database in order to execute this call.
     *
     * @param user The name of the user
     * @return information about the user
     * @see <a href="https://www.arangodb.com/docs/stable/http/user-management.html#fetch-user">API Documentation</a>
     */
    CompletableFuture<UserEntity> getUser(final String user);

    /**
     * Fetches data about all users. You can only execute this call if you have access to the _system database.
     *
     * @return informations about all users
     * @see <a href="https://www.arangodb.com/docs/stable/http/user-management.html#list-available-users">API
     * Documentation</a>
     */
    CompletableFuture<Collection<UserEntity>> getUsers();

    /**
     * Partially updates the data of an existing user. The name of an existing user must be specified in user. You can
     * only change the password of your self. You need access to the _system database to change the active flag.
     *
     * @param user    The name of the user
     * @param options Properties of the user to be changed
     * @return information about the user
     * @see <a href="https://www.arangodb.com/docs/stable/http/user-management.html#modify-user">API Documentation</a>
     */
    CompletableFuture<UserEntity> updateUser(final String user, final UserUpdateOptions options);

    /**
     * Replaces the data of an existing user. The name of an existing user must be specified in user. You can only
     * change the password of your self. You need access to the _system database to change the active flag.
     *
     * @param user    The name of the user
     * @param options Additional properties of the user, can be null
     * @return information about the user
     * @see <a href="https://www.arangodb.com/docs/stable/http/user-management.html#replace-user">API
     * Documentation</a>
     */
    CompletableFuture<UserEntity> replaceUser(final String user, final UserUpdateOptions options);

    /**
     * Sets the default access level for databases for the user <code>user</code>. You need permission to the _system
     * database in order to execute this call.
     *
     * @param user        The name of the user
     * @param permissions The permissions the user grant
     * @return void
     * @since ArangoDB 3.2.0
     */
    CompletableFuture<Void> grantDefaultDatabaseAccess(final String user, final Permissions permissions);

    /**
     * Sets the default access level for collections for the user <code>user</code>. You need permission to the _system
     * database in order to execute this call.
     *
     * @param user        The name of the user
     * @param permissions The permissions the user grant
     * @return void
     * @since ArangoDB 3.2.0
     */
    CompletableFuture<Void> grantDefaultCollectionAccess(final String user, final Permissions permissions);

    /**
     * Generic Execute. Use this method to execute custom FOXX services.
     *
     * @param request VelocyStream request
     * @return VelocyStream response
     */
    CompletableFuture<Response> execute(final Request request);

    /**
     * Returns fatal, error, warning or info log messages from the server's global log.
     *
     * @param options Additional options, can be null
     * @return the log messages
     * @see <a href=
     * "https://www.arangodb.com/docs/stable/http/administration-and-monitoring.html#read-global-logs-from-the-server">API
     * Documentation</a>
     */
    CompletableFuture<LogEntity> getLogs(final LogOptions options);

    /**
     * Returns the server's current loglevel settings.
     *
     * @return the server's current loglevel settings
     */
    CompletableFuture<LogLevelEntity> getLogLevel();

    /**
     * Modifies and returns the server's current loglevel settings.
     *
     * @param entity loglevel settings
     * @return the server's current loglevel settings
     */
    CompletableFuture<LogLevelEntity> setLogLevel(final LogLevelEntity entity);

    /**
     * Builder class to build an instance of {@link ArangoDBAsync}.
     *
     * @author Mark Vollmary
     */
    @SuppressWarnings("unused")
    class Builder extends InternalArangoDBBuilder {

        public Builder() {
            super();
        }

        @Override
        public Builder loadProperties(final InputStream in) throws ArangoDBException {
            super.loadProperties(in);
            return this;
        }

        /**
         * Adds a host to connect to. Multiple hosts can be added to provide fallbacks.
         *
         * @param host address of the host
         * @param port port of the host
         * @return {@link ArangoDBAsync.Builder}
         */
        public Builder host(final String host, final int port) {
            setHost(host, port);
            return this;
        }

        /**
         * Sets the timeout in milliseconds. It is used as socket timeout when opening a VecloyStream.
         *
         * @param timeout timeout in milliseconds
         * @return {@link ArangoDBAsync.Builder}
         */
        public Builder timeout(final Integer timeout) {
            setTimeout(timeout);
            return this;
        }

        /**
         * Sets the username to use for authentication.
         *
         * @param user the user in the database (default: <code>root</code>)
         * @return {@link ArangoDBAsync.Builder}
         */
        public Builder user(final String user) {
            setUser(user);
            return this;
        }

        /**
         * Sets the password for the user for authentication.
         *
         * @param password the password of the user in the database (default: <code>null</code>)
         * @return {@link ArangoDBAsync.Builder}
         */
        public Builder password(final String password) {
            setPassword(password);
            return this;
        }

        /**
         * If set to <code>true</code> SSL will be used when connecting to an ArangoDB server.
         *
         * @param useSsl whether or not use SSL (default: <code>false</code>)
         * @return {@link ArangoDBAsync.Builder}
         */
        public Builder useSsl(final Boolean useSsl) {
            setUseSsl(useSsl);
            return this;
        }

        /**
         * Sets the SSL context to be used when <code>true</code> is passed through {@link #useSsl(Boolean)}.
         *
         * @param sslContext SSL context to be used
         * @return {@link ArangoDBAsync.Builder}
         */
        public Builder sslContext(final SSLContext sslContext) {
            setSslContext(sslContext);
            return this;
        }

        /**
         * Sets the chunk size when {@link Protocol#VST} is used.
         *
         * @param chunksize size of a chunk in bytes
         * @return {@link ArangoDBAsync.Builder}
         */
        public Builder chunksize(final Integer chunksize) {
            setChunksize(chunksize);
            return this;
        }

        /**
         * Sets the maximum number of connections the built in connection pool will open.
         *
         * <p>
         * In an ArangoDB cluster setup with {@link LoadBalancingStrategy#ROUND_ROBIN} set, this value should be at
         * least as high as the number of ArangoDB coordinators in the cluster.
         * </p>
         *
         * @param maxConnections max number of connections (default: 1)
         * @return {@link ArangoDBAsync.Builder}
         */
        public Builder maxConnections(final Integer maxConnections) {
            setMaxConnections(maxConnections);
            return this;
        }

        /**
         * Set the maximum time to life of a connection. After this time the connection will be closed automatically.
         *
         * @param connectionTtl the maximum time to life of a connection.
         * @return {@link ArangoDBAsync.Builder}
         */
        public Builder connectionTtl(final Long connectionTtl) {
            setConnectionTtl(connectionTtl);
            return this;
        }

        /**
         * Whether or not the driver should acquire a list of available coordinators in an ArangoDB cluster or a single
         * server with active failover.
         *
         * <p>
         * The host list will be used for failover and load balancing.
         * </p>
         *
         * @param acquireHostList whether or not automatically acquire a list of available hosts (default: false)
         * @return {@link ArangoDBAsync.Builder}
         */
        public Builder acquireHostList(final Boolean acquireHostList) {
            setAcquireHostList(acquireHostList);
            return this;
        }

        /**
         * Sets the load balancing strategy to be used in an ArangoDB cluster setup.
         *
         * @param loadBalancingStrategy the load balancing strategy to be used (default: {@link LoadBalancingStrategy#NONE}
         * @return {@link ArangoDBAsync.Builder}
         */
        public Builder loadBalancingStrategy(final LoadBalancingStrategy loadBalancingStrategy) {
            setLoadBalancingStrategy(loadBalancingStrategy);
            return this;
        }

        /**
         * Register a custom {@link VPackSerializer} for a specific type to be used within the internal serialization
         * process.
         *
         * <p>
         * <strong>Attention:</strong>can not be used together with {@link #serializer(ArangoSerialization)}
         * </p>
         *
         * @param clazz      the type the serializer should be registered for
         * @param serializer serializer to register
         * @return {@link ArangoDBAsync.Builder}
         */
        public <T> Builder registerSerializer(final Class<T> clazz, final VPackSerializer<T> serializer) {
            vpackBuilder.registerSerializer(clazz, serializer);
            return this;
        }

        /**
         * Register a special serializer for a member class which can only be identified by its enclosing class.
         *
         * <p>
         * <strong>Attention:</strong>can not be used together with {@link #serializer(ArangoSerialization)}
         * </p>
         *
         * @param clazz      the type of the enclosing class
         * @param serializer serializer to register
         * @return {@link ArangoDBAsync.Builder}
         */
        public <T> Builder registerEnclosingSerializer(final Class<T> clazz, final VPackSerializer<T> serializer) {
            vpackBuilder.registerEnclosingSerializer(clazz, serializer);
            return this;
        }

        /**
         * Register a custom {@link VPackDeserializer} for a specific type to be used within the internal serialization
         * process.
         *
         * <p>
         * <strong>Attention:</strong>can not be used together with {@link #serializer(ArangoSerialization)}
         * </p>
         *
         * @param clazz        the type the serializer should be registered for
         * @param deserializer
         * @return {@link ArangoDBAsync.Builder}
         */
        public <T> Builder registerDeserializer(final Class<T> clazz, final VPackDeserializer<T> deserializer) {
            vpackBuilder.registerDeserializer(clazz, deserializer);
            return this;
        }

        /**
         * Register a custom {@link VPackInstanceCreator} for a specific type to be used within the internal
         * serialization process.
         *
         * <p>
         * <strong>Attention:</strong>can not be used together with {@link #serializer(ArangoSerialization)}
         * </p>
         *
         * @param clazz   the type the instance creator should be registered for
         * @param creator
         * @return {@link ArangoDBAsync.Builder}
         */
        public <T> Builder registerInstanceCreator(final Class<T> clazz, final VPackInstanceCreator<T> creator) {
            vpackBuilder.registerInstanceCreator(clazz, creator);
            return this;
        }

        /**
         * Register a custom {@link VPackJsonDeserializer} for a specific type to be used within the internal
         * serialization process.
         *
         * <p>
         * <strong>Attention:</strong>can not be used together with {@link #serializer(ArangoSerialization)}
         * </p>
         *
         * @param type         the type the serializer should be registered for
         * @param deserializer
         * @return {@link ArangoDBAsync.Builder}
         */
        public Builder registerJsonDeserializer(final ValueType type, final VPackJsonDeserializer deserializer) {
            vpackParserBuilder.registerDeserializer(type, deserializer);
            return this;
        }

        /**
         * Register a custom {@link VPackJsonDeserializer} for a specific type and attribute name to be used within the
         * internal serialization process.
         *
         * <p>
         * <strong>Attention:</strong>can not be used together with {@link #serializer(ArangoSerialization)}
         * </p>
         *
         * @param attribute
         * @param type         the type the serializer should be registered for
         * @param deserializer
         * @return {@link ArangoDBAsync.Builder}
         */
        public Builder registerJsonDeserializer(
                final String attribute,
                final ValueType type,
                final VPackJsonDeserializer deserializer) {
            vpackParserBuilder.registerDeserializer(attribute, type, deserializer);
            return this;
        }

        /**
         * Register a custom {@link VPackJsonSerializer} for a specific type to be used within the internal
         * serialization process.
         *
         * <p>
         * <strong>Attention:</strong>can not be used together with {@link #serializer(ArangoSerialization)}
         * </p>
         *
         * @param clazz      the type the serializer should be registered for
         * @param serializer
         * @return {@link ArangoDBAsync.Builder}
         */
        public <T> Builder registerJsonSerializer(final Class<T> clazz, final VPackJsonSerializer<T> serializer) {
            vpackParserBuilder.registerSerializer(clazz, serializer);
            return this;
        }

        /**
         * Register a custom {@link VPackJsonSerializer} for a specific type and attribute name to be used within the
         * internal serialization process.
         *
         * <p>
         * <strong>Attention:</strong>can not be used together with {@link #serializer(ArangoSerialization)}
         * </p>
         *
         * @param attribute
         * @param clazz      the type the serializer should be registered for
         * @param serializer
         * @return {@link ArangoDBAsync.Builder}
         */
        public <T> Builder registerJsonSerializer(
                final String attribute,
                final Class<T> clazz,
                final VPackJsonSerializer<T> serializer) {
            vpackParserBuilder.registerSerializer(attribute, clazz, serializer);
            return this;
        }

        /**
         * Register a custom {@link VPackAnnotationFieldFilter} for a specific type to be used within the internal
         * serialization process.
         *
         * <p>
         * <strong>Attention:</strong>can not be used together with {@link #serializer(ArangoSerialization)}
         * </p>
         *
         * @param type        the type the serializer should be registered for
         * @param fieldFilter
         * @return {@link ArangoDBAsync.Builder}
         */
        public <A extends Annotation> Builder annotationFieldFilter(
                final Class<A> type,
                final VPackAnnotationFieldFilter<A> fieldFilter) {
            vpackBuilder.annotationFieldFilter(type, fieldFilter);
            return this;
        }

        /**
         * Register a custom {@link VPackAnnotationFieldNaming} for a specific type to be used within the internal
         * serialization process.
         *
         * <p>
         * <strong>Attention:</strong>can not be used together with {@link #serializer(ArangoSerialization)}
         * </p>
         *
         * @param type        the type the serializer should be registered for
         * @param fieldNaming
         * @return {@link ArangoDBAsync.Builder}
         */
        public <A extends Annotation> Builder annotationFieldNaming(
                final Class<A> type,
                final VPackAnnotationFieldNaming<A> fieldNaming) {
            vpackBuilder.annotationFieldNaming(type, fieldNaming);
            return this;
        }

        /**
         * Register a {@link VPackModule} to be used within the internal serialization process.
         *
         * <p>
         * <strong>Attention:</strong>can not be used together with {@link #serializer(ArangoSerialization)}
         * </p>
         *
         * @param module module to register
         * @return {@link ArangoDBAsync.Builder}
         */
        public Builder registerModule(final VPackModule module) {
            vpackBuilder.registerModule(module);
            return this;
        }

        /**
         * Register a list of {@link VPackModule} to be used within the internal serialization process.
         *
         * <p>
         * <strong>Attention:</strong>can not be used together with {@link #serializer(ArangoSerialization)}
         * </p>
         *
         * @param modules modules to register
         * @return {@link ArangoDBAsync.Builder}
         */
        public Builder registerModules(final VPackModule... modules) {
            vpackBuilder.registerModules(modules);
            return this;
        }

        /**
         * Register a {@link VPackParserModule} to be used within the internal serialization process.
         *
         * <p>
         * <strong>Attention:</strong>can not be used together with {@link #serializer(ArangoSerialization)}
         * </p>
         *
         * @param module module to register
         * @return {@link ArangoDBAsync.Builder}
         */
        public Builder registerJsonModule(final VPackParserModule module) {
            vpackParserBuilder.registerModule(module);
            return this;
        }

        /**
         * Register a list of {@link VPackParserModule} to be used within the internal serialization process.
         *
         * <p>
         * <strong>Attention:</strong>can not be used together with {@link #serializer(ArangoSerialization)}
         * </p>
         *
         * @param modules modules to register
         * @return {@link ArangoDBAsync.Builder}
         */
        public Builder registerJsonModules(final VPackParserModule... modules) {
            vpackParserBuilder.registerModules(modules);
            return this;
        }

        /**
         * Replace the built-in serializer with the given serializer.
         * <p>
         * <br />
         * <b>ATTENTION!:</b> Use at your own risk
         *
         * @param serializer custom serializer
         * @return {@link ArangoDBAsync.Builder}
         * @deprecated use {@link #serializer(ArangoSerialization)} instead
         */
        @Deprecated
        public Builder setSerializer(final ArangoSerializer serializer) {
            serializer(serializer);
            return this;
        }

        /**
         * Replace the built-in deserializer with the given deserializer.
         * <p>
         * <br />
         * <b>ATTENTION!:</b> Use at your own risk
         *
         * @param deserializer custom deserializer
         * @return {@link ArangoDBAsync.Builder}
         * @deprecated use {@link #serializer(ArangoSerialization)} instead
         */
        @Deprecated
        public Builder setDeserializer(final ArangoDeserializer deserializer) {
            deserializer(deserializer);
            return this;
        }

        /**
         * Replace the built-in serializer/deserializer with the given one.
         * <p>
         * <br />
         * <b>ATTENTION!:</b> Any registered custom serializer/deserializer or module will be ignored.
         *
         * @param serialization custom serializer/deserializer
         * @return {@link ArangoDBAsync.Builder}
         */
        public Builder serializer(final ArangoSerialization serialization) {
            setSerializer(serialization);
            return this;
        }

        /**
         * Returns an instance of {@link ArangoDBAsync}.
         *
         * @return {@link ArangoDBAsync}
         */
        public synchronized ArangoDBAsync build() {
            if (hosts.isEmpty()) {
                hosts.add(host);
            }
            final VPack vpacker = vpackBuilder.serializeNullValues(false).build();
            final VPack vpackerNull = vpackBuilder.serializeNullValues(true).build();
            final VPackParser vpackParser = vpackParserBuilder.build();
            final ArangoSerializer serializerTemp = serializer != null ? serializer
                    : new ArangoSerializerImpl(vpacker, vpackerNull, vpackParser);
            final ArangoDeserializer deserializerTemp = deserializer != null ? deserializer
                    : new ArangoDeserializerImpl(vpackerNull, vpackParser);
            final DefaultArangoSerialization internal = new DefaultArangoSerialization(serializerTemp,
                    deserializerTemp);
            final ArangoSerialization custom = customSerializer != null ? customSerializer : internal;
            final ArangoSerializationFactory util = new ArangoSerializationFactory(internal, custom);

            final int max = maxConnections != null ? Math.max(1, maxConnections)
                    : ArangoDefaults.MAX_CONNECTIONS_VST_DEFAULT;
            final ConnectionFactory connectionFactory = new VstConnectionFactoryAsync(host, timeout, connectionTtl,
                    useSsl, sslContext);
            final HostResolver hostResolver = createHostResolver(createHostList(max, connectionFactory), max,
                    connectionFactory);
            final HostHandler hostHandler = createHostHandler(hostResolver);
            return new ArangoDBAsyncImpl(asyncBuilder(hostHandler), util, syncBuilder(hostHandler), hostResolver,
                    new ArangoContext());
        }

        private VstCommunicationAsync.Builder asyncBuilder(final HostHandler hostHandler) {
            return new VstCommunicationAsync.Builder(hostHandler).timeout(timeout).user(user).password(password)
                    .useSsl(useSsl).sslContext(sslContext).chunksize(chunksize).maxConnections(maxConnections)
                    .connectionTtl(connectionTtl);
        }

        private VstCommunicationSync.Builder syncBuilder(final HostHandler hostHandler) {
            return new VstCommunicationSync.Builder(hostHandler).timeout(timeout).user(user).password(password)
                    .useSsl(useSsl).sslContext(sslContext).chunksize(chunksize).maxConnections(maxConnections)
                    .connectionTtl(connectionTtl);
        }

    }
}
