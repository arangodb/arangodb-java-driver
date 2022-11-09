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

import com.arangodb.config.ConfigPropertiesProvider;
import com.arangodb.config.ConfigPropertyKey;
import com.arangodb.entity.*;
import com.arangodb.internal.*;
import com.arangodb.internal.http.HttpCommunication;
import com.arangodb.internal.http.HttpConnectionFactory;
import com.arangodb.internal.net.*;
import com.arangodb.internal.serde.InternalSerde;
import com.arangodb.internal.velocystream.VstCommunicationSync;
import com.arangodb.internal.velocystream.VstConnectionFactorySync;
import com.arangodb.model.DBCreateOptions;
import com.arangodb.model.LogOptions;
import com.arangodb.model.UserCreateOptions;
import com.arangodb.model.UserUpdateOptions;
import com.arangodb.serde.ArangoSerde;
import com.arangodb.serde.JacksonSerde;

import javax.annotation.concurrent.ThreadSafe;
import javax.net.ssl.SSLContext;
import java.util.Collection;
import java.util.Locale;

/**
 * Central access point for applications to communicate with an ArangoDB server.
 *
 * <p>
 * Will be instantiated through {@link ArangoDB.Builder}
 * </p>
 *
 * <pre>
 * ArangoDB arango = new ArangoDB.Builder().build();
 * ArangoDB arango = new ArangoDB.Builder().host("127.0.0.1", 8529).build();
 * </pre>
 *
 * @author Mark Vollmary
 * @author Michele Rastelli
 */
@ThreadSafe
public interface ArangoDB extends ArangoSerdeAccessor {

    /**
     * Releases all connections to the server and clear the connection pool.
     */
    void shutdown();

    /**
     * Updates the JWT used for requests authorization. It does not change already existing VST connections, since VST
     * connections are authenticated during the initialization phase.
     *
     * @param jwt token to use
     */
    void updateJwt(String jwt);

    /**
     * Returns a {@code ArangoDatabase} instance for the {@code _system} database.
     *
     * @return database handler
     */
    ArangoDatabase db();

    /**
     * Returns a {@code ArangoDatabase} instance for the given database name.
     *
     * @param dbName Name of the database
     * @return database handler
     */
    ArangoDatabase db(DbName dbName);

    /**
     * @return entry point for accessing client metrics
     */
    ArangoMetrics metrics();

    /**
     * Creates a new database with the given name.
     *
     * @param dbName Name of the database to create
     * @return true if the database was created successfully.
     * @see <a href="https://www.arangodb.com/docs/stable/http/database-database-management.html#create-database">API
     * Documentation</a>
     */
    Boolean createDatabase(DbName dbName);

    /**
     * Creates a new database with the given name.
     *
     * @param options Creation options
     * @return true if the database was created successfully.
     * @see <a href="https://www.arangodb.com/docs/stable/http/database-database-management.html#create-database">API
     * Documentation</a>
     * @since ArangoDB 3.6.0
     */
    Boolean createDatabase(DBCreateOptions options);

    /**
     * Retrieves a list of all existing databases
     *
     * @return a list of all existing databases
     * @see <a href="https://www.arangodb.com/docs/stable/http/database-database-management.html#list-of-databases">API
     * Documentation</a>
     */
    Collection<String> getDatabases();

    /**
     * Retrieves a list of all databases the current user can access
     *
     * @return a list of all databases the current user can access
     * @see <a href=
     * "https://www.arangodb.com/docs/stable/http/database-database-management.html#list-of-accessible-databases">API
     * Documentation</a>
     */
    Collection<String> getAccessibleDatabases();

    /**
     * List available database to the specified user
     *
     * @param user The name of the user for which you want to query the databases
     * @return list of database names which are available for the specified user
     * @see <a href=
     * "https://www.arangodb.com/docs/stable/http/user-management.html#list-the-accessible-databases-for-a-user">API
     * Documentation</a>
     */
    Collection<String> getAccessibleDatabasesFor(String user);

    /**
     * Returns the server name and version number.
     *
     * @return the server version, number
     * @see <a href="https://www.arangodb.com/docs/stable/http/miscellaneous-functions.html#return-server-version">API
     * Documentation</a>
     */
    ArangoDBVersion getVersion();

    /**
     * Returns the server storage engine.
     *
     * @return the storage engine name
     * @see <a
     * href="https://www.arangodb.com/docs/stable/http/miscellaneous-functions.html#return-server-database-engine-type">API
     * Documentation</a>
     */
    ArangoDBEngine getEngine();

    /**
     * Returns the server role.
     *
     * @return the server role
     */
    ServerRole getRole();

    /**
     * Returns the id of a server in a cluster.
     *
     * @return the server id
     * @see <a
     * href="https://www.arangodb.com/docs/stable/http/administration-and-monitoring.html#return-id-of-a-server-in-a-cluster">API
     * Documentation</a>
     */
    String getServerId();

    /**
     * Create a new user. This user will not have access to any database. You need permission to the _system database in
     * order to execute this call.
     *
     * @param user   The name of the user
     * @param passwd The user password
     * @return information about the user
     * @see <a href="https://www.arangodb.com/docs/stable/http/user-management.html#create-user">API Documentation</a>
     */
    UserEntity createUser(String user, String passwd);

    /**
     * Create a new user. This user will not have access to any database. You need permission to the _system database in
     * order to execute this call.
     *
     * @param user    The name of the user
     * @param passwd  The user password
     * @param options Additional options, can be null
     * @return information about the user
     * @see <a href="https://www.arangodb.com/docs/stable/http/user-management.html#create-user">API Documentation</a>
     */
    UserEntity createUser(String user, String passwd, UserCreateOptions options);

    /**
     * Removes an existing user, identified by user. You need access to the _system database.
     *
     * @param user The name of the user
     * @see <a href="https://www.arangodb.com/docs/stable/http/user-management.html#remove-user">API Documentation</a>
     */
    void deleteUser(String user);

    /**
     * Fetches data about the specified user. You can fetch information about yourself or you need permission to the
     * _system database in order to execute this call.
     *
     * @param user The name of the user
     * @return information about the user
     * @see <a href="https://www.arangodb.com/docs/stable/http/user-management.html#fetch-user">API Documentation</a>
     */
    UserEntity getUser(String user);

    /**
     * Fetches data about all users. You can only execute this call if you have access to the _system database.
     *
     * @return informations about all users
     * @see <a href="https://www.arangodb.com/docs/stable/http/user-management.html#list-available-users">API
     * Documentation</a>
     */
    Collection<UserEntity> getUsers();

    /**
     * Partially updates the data of an existing user. The name of an existing user must be specified in user. You can
     * only change the password of your self. You need access to the _system database to change the active flag.
     *
     * @param user    The name of the user
     * @param options Properties of the user to be changed
     * @return information about the user
     * @see <a href="https://www.arangodb.com/docs/stable/http/user-management.html#modify-user">API Documentation</a>
     */
    UserEntity updateUser(String user, UserUpdateOptions options);

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
    UserEntity replaceUser(String user, UserUpdateOptions options);

    /**
     * Sets the default access level for databases for the user {@code user}. You need permission to the _system
     * database in order to execute this call.
     *
     * @param user        The name of the user
     * @param permissions The permissions the user grant
     * @since ArangoDB 3.2.0
     */
    void grantDefaultDatabaseAccess(String user, Permissions permissions);

    /**
     * Sets the default access level for collections for the user {@code user}. You need permission to the _system
     * database in order to execute this call.
     *
     * @param user        The name of the user
     * @param permissions The permissions the user grant
     * @since ArangoDB 3.2.0
     */
    void grantDefaultCollectionAccess(String user, Permissions permissions);

    /**
     * Execute custom requests. Requests can be programmatically built by setting low level detail such as method, path,
     * query parameters, headers and body payload.
     * This method can be used to call FOXX services, API endpoints not (yet) implemented in this driver or trigger
     * async jobs, see
     * <a href="https://www.arangodb.com/docs/stable/http/async-results-management.html#fire-and-forget">Fire and Forget</a>
     * and
     * <a href="https://www.arangodb.com/docs/stable/http/async-results-management.html#async-execution-and-later-result-retrieval">Async Execution and later Result Retrieval</a>
     *
     * @param request request
     * @param type    Deserialization target type for the response body (POJO or {@link com.arangodb.util.RawData})
     * @return response
     */
    <T, U> Response<U> execute(Request<T> request, Class<U> type);

    /**
     * Returns the server logs
     *
     * @param options Additional options, can be null
     * @return the log messages
     * @see <a href=
     * "https://www.arangodb.com/docs/stable/http/administration-and-monitoring.html#read-global-logs-from-the-server">API
     * Documentation</a>
     * @since ArangoDB 3.8
     */
    LogEntriesEntity getLogEntries(LogOptions options);

    /**
     * Returns the server's current loglevel settings.
     *
     * @return the server's current loglevel settings
     * @since ArangoDB 3.1.0
     */
    LogLevelEntity getLogLevel();

    /**
     * Modifies and returns the server's current loglevel settings.
     *
     * @param entity loglevel settings
     * @return the server's current loglevel settings
     * @since ArangoDB 3.1.0
     */
    LogLevelEntity setLogLevel(LogLevelEntity entity);

    /**
     * @return the list of available rules and their respective flags
     * @since ArangoDB 3.10
     */
    Collection<QueryOptimizerRule> getQueryOptimizerRules();

    /**
     * Builder class to build an instance of {@link ArangoDB}.
     *
     * @author Mark Vollmary
     */
    class Builder extends InternalArangoDBBuilder {

        protected Protocol protocol = ArangoDefaults.DEFAULT_NETWORK_PROTOCOL;

        public Builder() {
            super();
        }

        private static Protocol loadProtocol(final ConfigPropertiesProvider properties, final Protocol currentValue) {
            return Protocol.valueOf(getProperty(properties, ConfigPropertyKey.PROTOCOL, currentValue).toUpperCase(Locale.ROOT));
        }

        public Builder loadProperties(final ConfigPropertiesProvider properties) {
            doLoadProperties(properties);
            protocol = loadProtocol(properties, protocol);
            return this;
        }

        public Builder useProtocol(final Protocol protocol) {
            this.protocol = protocol;
            return this;
        }

        /**
         * Adds a host to connect to. Multiple hosts can be added to provide fallbacks.
         *
         * @param host address of the host
         * @param port port of the host
         * @return {@link ArangoDB.Builder}
         */
        public Builder host(final String host, final int port) {
            setHost(host, port);
            return this;
        }

        /**
         * Sets the connection and request timeout in milliseconds.
         *
         * @param timeout timeout in milliseconds
         * @return {@link ArangoDB.Builder}
         */
        public Builder timeout(final Integer timeout) {
            setTimeout(timeout);
            return this;
        }

        /**
         * Sets the username to use for authentication.
         *
         * @param user the user in the database (default: {@code root})
         * @return {@link ArangoDB.Builder}
         */
        public Builder user(final String user) {
            setUser(user);
            return this;
        }

        /**
         * Sets the password for the user for authentication.
         *
         * @param password the password of the user in the database (default: {@code null})
         * @return {@link ArangoDB.Builder}
         */
        public Builder password(final String password) {
            setPassword(password);
            return this;
        }

        /**
         * Sets the JWT for the user authentication.
         *
         * @param jwt token to use (default: {@code null})
         * @return {@link ArangoDB.Builder}
         */
        public Builder jwt(final String jwt) {
            setJwt(jwt);
            return this;
        }

        /**
         * If set to {@code true} SSL will be used when connecting to an ArangoDB server.
         *
         * @param useSsl whether or not use SSL (default: {@code false})
         * @return {@link ArangoDB.Builder}
         */
        public Builder useSsl(final Boolean useSsl) {
            setUseSsl(useSsl);
            return this;
        }

        /**
         * Sets the SSL context to be used when {@code true} is passed through {@link #useSsl(Boolean)}.
         *
         * @param sslContext SSL context to be used
         * @return {@link ArangoDB.Builder}
         */
        public Builder sslContext(final SSLContext sslContext) {
            setSslContext(sslContext);
            return this;
        }

        /**
         * Set whether hostname verification is enabled
         *
         * @param verifyHost {@code true} if enabled
         * @return {@link ArangoDB.Builder}
         */
        public Builder verifyHost(final Boolean verifyHost) {
            setVerifyHost(verifyHost);
            return this;
        }

        /**
         * Sets the chunk size when {@link Protocol#VST} is used.
         *
         * @param chunksize size of a chunk in bytes
         * @return {@link ArangoDB.Builder}
         */
        public Builder chunksize(final Integer chunksize) {
            setChunkSize(chunksize);
            return this;
        }

        /**
         * Sets the maximum number of connections the built in connection pool will open per host.
         *
         * <p>
         * Defaults:
         * </p>
         *
         * <pre>
         * {@link Protocol#VST} == 1
         * {@link Protocol#HTTP_JSON} == 20
         * {@link Protocol#HTTP_VPACK} == 20
         * </pre>
         *
         * @param maxConnections max number of connections
         * @return {@link ArangoDB.Builder}
         */
        public Builder maxConnections(final Integer maxConnections) {
            setMaxConnections(maxConnections);
            return this;
        }

        /**
         * Set the maximum time to life of a connection. After this time the connection will be closed automatically.
         *
         * @param connectionTtl the maximum time to life of a connection in milliseconds
         * @return {@link ArangoDB.Builder}
         */
        public Builder connectionTtl(final Long connectionTtl) {
            setConnectionTtl(connectionTtl);
            return this;
        }

        /**
         * Set the keep-alive interval for VST connections. If set, every VST connection will perform a no-op request
         * every {@code keepAliveInterval} seconds, to avoid to be closed due to inactivity by the server (or by the
         * external environment, eg. firewall, intermediate routers, operating system).
         *
         * @param keepAliveInterval interval in seconds
         * @return {@link ArangoDB.Builder}
         */
        public Builder keepAliveInterval(final Integer keepAliveInterval) {
            setKeepAliveInterval(keepAliveInterval);
            return this;
        }

        /**
         * Whether or not the driver should acquire a list of available coordinators in an ArangoDB cluster or a single
         * server with active failover. In case of Active-Failover deployment set to {@code true} to enable automatic
         * master discovery.
         *
         * <p>
         * The host list will be used for failover and load balancing.
         * </p>
         *
         * @param acquireHostList whether or not automatically acquire a list of available hosts (default: false)
         * @return {@link ArangoDB.Builder}
         */
        public Builder acquireHostList(final Boolean acquireHostList) {
            setAcquireHostList(acquireHostList);
            return this;
        }

        /**
         * Setting the Interval for acquireHostList
         *
         * @param acquireHostListInterval Interval in milliseconds
         * @return {@link ArangoDB.Builder}
         */
        public Builder acquireHostListInterval(final Integer acquireHostListInterval) {
            setAcquireHostListInterval(acquireHostListInterval);
            return this;
        }

        /**
         * Sets the load balancing strategy to be used in an ArangoDB cluster setup. In case of Active-Failover
         * deployment set to {@link LoadBalancingStrategy#NONE} or not set at all, since that would be the default.
         *
         * @param loadBalancingStrategy the load balancing strategy to be used (default:
         *                              {@link LoadBalancingStrategy#NONE}
         * @return {@link ArangoDB.Builder}
         */
        public Builder loadBalancingStrategy(final LoadBalancingStrategy loadBalancingStrategy) {
            setLoadBalancingStrategy(loadBalancingStrategy);
            return this;
        }

        /**
         * Setting the amount of samples kept for queue time metrics
         *
         * @param responseQueueTimeSamples amount of samples to keep
         * @return {@link ArangoDB.Builder}
         */
        public Builder responseQueueTimeSamples(final Integer responseQueueTimeSamples) {
            setResponseQueueTimeSamples(responseQueueTimeSamples);
            return this;
        }

        /**
         * Replace the built-in serializer/deserializer with the given one.
         * <p>
         * <br />
         * <b>ATTENTION!:</b> Any registered custom serializer/deserializer or module will be ignored.
         *
         * @param serialization custom serializer/deserializer
         * @return {@link ArangoDB.Builder}
         */
        public Builder serializer(final ArangoSerde serialization) {
            setSerializer(serialization);
            return this;
        }

        /**
         * Returns an instance of {@link ArangoDB}.
         *
         * @return {@link ArangoDB}
         */
        public ArangoDB build() {
            if (hosts.isEmpty()) {
                throw new ArangoDBException("No host has been set!");
            }

            final ArangoSerde userSerde = customSerializer != null ? customSerializer :
                    JacksonSerde.of(ContentType.of(protocol));
            final InternalSerde serde = InternalSerde.of(ContentType.of(protocol), userSerde);

            int protocolMaxConnections;
            switch (protocol) {
                case VST:
                    protocolMaxConnections = ArangoDefaults.MAX_CONNECTIONS_VST_DEFAULT;
                    break;
                case HTTP_JSON:
                case HTTP_VPACK:
                    protocolMaxConnections = ArangoDefaults.MAX_CONNECTIONS_HTTP_DEFAULT;
                    break;
                case HTTP2_JSON:
                case HTTP2_VPACK:
                    protocolMaxConnections = ArangoDefaults.MAX_CONNECTIONS_HTTP2_DEFAULT;
                    break;
                default:
                    throw new IllegalArgumentException();
            }

            final int max = maxConnections != null ? Math.max(1, maxConnections) : protocolMaxConnections;

            final ConnectionFactory connectionFactory = Protocol.VST == protocol
                    ? new VstConnectionFactorySync(timeout, connectionTtl, keepAliveInterval, useSsl, sslContext)
                    : new HttpConnectionFactory(timeout, user, password, useSsl, sslContext, verifyHost, serde,
                    protocol, connectionTtl);

            final Collection<Host> hostList = createHostList(max, connectionFactory);
            final HostResolver hostResolver = createHostResolver(hostList, max, connectionFactory);
            final HostHandler hostHandler = createHostHandler(hostResolver);
            hostHandler.setJwt(jwt);

            return new ArangoDBImpl(
                    new VstCommunicationSync.Builder(hostHandler).timeout(timeout).user(user).password(password)
                            .jwt(jwt).useSsl(useSsl).sslContext(sslContext).chunksize(chunkSize)
                            .maxConnections(maxConnections).connectionTtl(connectionTtl),
                    new HttpCommunication.Builder().hostHandler(hostHandler),
                    serde,
                    protocol,
                    hostResolver,
                    hostHandler,
                    responseQueueTimeSamples, timeout);
        }

    }

}
