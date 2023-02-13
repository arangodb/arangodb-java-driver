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

import com.arangodb.*;
import com.arangodb.async.internal.ArangoDBAsyncImpl;
import com.arangodb.entity.*;
import com.arangodb.internal.InternalArangoDBBuilder;
import com.arangodb.internal.net.*;
import com.arangodb.model.DBCreateOptions;
import com.arangodb.model.LogOptions;
import com.arangodb.model.UserCreateOptions;
import com.arangodb.model.UserUpdateOptions;

import javax.annotation.concurrent.ThreadSafe;
import java.util.Collection;
import java.util.ServiceLoader;
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
@ThreadSafe
public interface ArangoDBAsync extends ArangoSerdeAccessor {

    void shutdown();

    /**
     * Updates the JWT used for requests authorization. It does not change already existing VST connections, since VST
     * connections are authenticated during the initialization phase.
     *
     * @param jwt token to use
     */
    void updateJwt(String jwt);

    /**
     * Returns a handler of the system database
     *
     * @return database handler
     */
    ArangoDatabaseAsync db();

    /**
     * Returns a handler of the database by the given name
     *
     * @param dbName Name of the database
     * @return database handler
     */
    ArangoDatabaseAsync db(final DbName dbName);

    /**
     * @return entry point for accessing client metrics
     */
    ArangoMetrics metrics();

    /**
     * Creates a new database
     *
     * @param dbName database name
     * @return true if the database was created successfully.
     * @see <a href="https://www.arangodb.com/docs/stable/http/database-database-management.html#create-database">API
     * Documentation</a>
     */
    CompletableFuture<Boolean> createDatabase(final DbName dbName);

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
    <T, U> CompletableFuture<Response<U>> execute(final Request<T> request, final Class<U> type);

    /**
     * Returns the server logs
     *
     * @param options Additional options, can be null
     * @return the log messages
     * @see <a href= "https://www.arangodb.com/docs/stable/http/administration-and-monitoring.html#read-global-logs-from-the-server">API
     * Documentation</a>
     * @since ArangoDB 3.8
     */
    CompletableFuture<LogEntriesEntity> getLogEntries(final LogOptions options);

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
     * @return the list of available rules and their respective flags
     * @since ArangoDB 3.10
     */
    CompletableFuture<Collection<QueryOptimizerRule>> getQueryOptimizerRules();

    /**
     * Builder class to build an instance of {@link ArangoDBAsync}.
     *
     * @author Mark Vollmary
     */
    class Builder extends InternalArangoDBBuilder<Builder> {

        private AsyncProtocolProvider asyncProtocolProvider(Protocol protocol) {
            ServiceLoader<AsyncProtocolProvider> loader = ServiceLoader.load(AsyncProtocolProvider.class);
            for (AsyncProtocolProvider p : loader) {
                if (p.supportsProtocol(protocol)) {
                    return p;
                }
            }
            throw new ArangoDBException("No ProtocolProvider found for protocol: " + protocol);
        }

        /**
         * Returns an instance of {@link ArangoDBAsync}.
         *
         * @return {@link ArangoDBAsync}
         */
        public ArangoDBAsync build() {
            if (config.getHosts().isEmpty()) {
                throw new ArangoDBException("No host has been set!");
            }

            AsyncProtocolProvider asyncProtocolProvider = asyncProtocolProvider(Protocol.VST);
            ProtocolProvider protocolProvider = protocolProvider(Protocol.VST);

            config.setProtocol(Protocol.VST);
            config.setProtocolModule(protocolProvider.protocolModule());

            final int max = config.getMaxConnections();
            final ConnectionFactory asyncConnectionFactory = asyncProtocolProvider.createConnectionFactory(config);
            final ConnectionFactory syncConnectionFactory = protocolProvider.createConnectionFactory(config);
            final HostResolver asyncHostResolver = createHostResolver(createHostList(max, asyncConnectionFactory), max,
                    asyncConnectionFactory);
            final HostResolver syncHostResolver = createHostResolver(createHostList(max, syncConnectionFactory), max,
                    syncConnectionFactory);
            final HostHandler asyncHostHandler = createHostHandler(asyncHostResolver);
            final HostHandler syncHostHandler = createHostHandler(syncHostResolver);
            return new ArangoDBAsyncImpl(
                    config,
                    asyncHostResolver,
                    syncHostResolver,
                    asyncProtocolProvider,
                    protocolProvider,
                    asyncHostHandler,
                    syncHostHandler
            );
        }
    }
}
