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
import com.arangodb.internal.ArangoDBImpl;
import com.arangodb.internal.InternalArangoDBBuilder;
import com.arangodb.internal.net.*;
import com.arangodb.model.DBCreateOptions;
import com.arangodb.model.LogOptions;
import com.arangodb.model.UserCreateOptions;
import com.arangodb.model.UserUpdateOptions;

import javax.annotation.concurrent.ThreadSafe;
import java.util.Collection;

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
    class Builder extends InternalArangoDBBuilder<Builder> {

        public Builder useProtocol(final Protocol protocol) {
            config.setProtocol(protocol);
            return this;
        }

        /**
         * Returns an instance of {@link ArangoDB}.
         *
         * @return {@link ArangoDB}
         */
        public ArangoDB build() {
            if (config.getHosts().isEmpty()) {
                throw new ArangoDBException("No host has been set!");
            }

            ProtocolProvider protocolProvider = protocolProvider(config.getProtocol());
            config.setProtocolModule(protocolProvider.protocolModule());

            ConnectionFactory connectionFactory = protocolProvider.createConnectionFactory();
            Collection<Host> hostList = createHostList(connectionFactory);
            HostResolver hostResolver = createHostResolver(hostList, connectionFactory);
            HostHandler hostHandler = createHostHandler(hostResolver);
            hostHandler.setJwt(config.getJwt());

            return new ArangoDBImpl(
                    config,
                    hostResolver,
                    protocolProvider,
                    hostHandler
            );
        }

    }

}
