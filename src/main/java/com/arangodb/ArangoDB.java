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

import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Properties;

import javax.net.ssl.SSLContext;

import com.arangodb.entity.ArangoDBVersion;
import com.arangodb.entity.LoadBalancingStrategy;
import com.arangodb.entity.LogEntity;
import com.arangodb.entity.LogLevelEntity;
import com.arangodb.entity.Permissions;
import com.arangodb.entity.ServerRole;
import com.arangodb.entity.UserEntity;
import com.arangodb.internal.ArangoDBConstants;
import com.arangodb.internal.ArangoDBImpl;
import com.arangodb.internal.CollectionCache;
import com.arangodb.internal.InternalArangoDBBuilder;
import com.arangodb.internal.http.HttpCommunication;
import com.arangodb.internal.net.HostHandle;
import com.arangodb.internal.net.HostHandler;
import com.arangodb.internal.net.HostResolver;
import com.arangodb.internal.util.ArangoDeserializerImpl;
import com.arangodb.internal.util.ArangoSerializerImpl;
import com.arangodb.internal.util.ArangoUtilImpl;
import com.arangodb.internal.velocypack.VPackDocumentModule;
import com.arangodb.internal.velocystream.VstCommunicationSync;
import com.arangodb.model.LogOptions;
import com.arangodb.model.UserCreateOptions;
import com.arangodb.model.UserUpdateOptions;
import com.arangodb.util.ArangoCursorInitializer;
import com.arangodb.util.ArangoDeserializer;
import com.arangodb.util.ArangoSerialization;
import com.arangodb.util.ArangoSerializer;
import com.arangodb.velocypack.VPack;
import com.arangodb.velocypack.VPackAnnotationFieldFilter;
import com.arangodb.velocypack.VPackAnnotationFieldNaming;
import com.arangodb.velocypack.VPackDeserializer;
import com.arangodb.velocypack.VPackInstanceCreator;
import com.arangodb.velocypack.VPackJsonDeserializer;
import com.arangodb.velocypack.VPackJsonSerializer;
import com.arangodb.velocypack.VPackModule;
import com.arangodb.velocypack.VPackParser;
import com.arangodb.velocypack.VPackParserModule;
import com.arangodb.velocypack.VPackSerializer;
import com.arangodb.velocypack.ValueType;
import com.arangodb.velocystream.Request;
import com.arangodb.velocystream.Response;

/**
 * @author Mark Vollmary
 *
 */
public interface ArangoDB {

	public static class Builder extends InternalArangoDBBuilder {

		private static final String PROPERTY_KEY_PROTOCOL = "arangodb.protocol";

		protected Protocol protocol;

		public Builder() {
			super();
		}

		@Override
		protected void loadProperties(final Properties properties) {
			super.loadProperties(properties);
			protocol = loadProtocol(properties, protocol);
		}

		private static Protocol loadProtocol(final Properties properties, final Protocol currentValue) {
			return Protocol.valueOf(
				getProperty(properties, PROPERTY_KEY_PROTOCOL, currentValue, ArangoDBConstants.DEFAULT_NETWORK_PROTOCOL)
						.toUpperCase());
		}

		public Builder useProtocol(final Protocol protocol) {
			this.protocol = protocol;
			return this;
		}

		@Override
		public Builder loadProperties(final InputStream in) throws ArangoDBException {
			super.loadProperties(in);
			return this;
		}

		/**
		 * @deprecated will be removed in version 4.6.0 use {@link #host(String, int)} instead
		 * 
		 * @param host
		 * @return {@link ArangoDB.Builder}
		 */
		@Deprecated
		public Builder host(final String host) {
			setHost(host);
			return this;
		}

		/**
		 * @deprecated will be removed in version 4.6.0 use {@link #host(String, int)} instead
		 * 
		 * @param port
		 * @return {@link ArangoDB.Builder}
		 */
		@Deprecated
		public Builder port(final Integer port) {
			setPort(port);
			return this;
		}

		/**
		 * Adds a host to connect to. Multiple hosts can be added to provide fallbacks.
		 * 
		 * @param host
		 *            address of the host
		 * @param port
		 *            port of the host
		 * @return {@link ArangoDB.Builder}
		 */
		public Builder host(final String host, final int port) {
			setHost(host, port);
			return this;
		}

		public Builder timeout(final Integer timeout) {
			setTimeout(timeout);
			return this;
		}

		public Builder user(final String user) {
			setUser(user);
			return this;
		}

		public Builder password(final String password) {
			setPassword(password);
			return this;
		}

		public Builder useSsl(final Boolean useSsl) {
			setUseSsl(useSsl);
			return this;
		}

		public Builder sslContext(final SSLContext sslContext) {
			setSslContext(sslContext);
			return this;
		}

		public Builder chunksize(final Integer chunksize) {
			setChunksize(chunksize);
			return this;
		}

		public Builder maxConnections(final Integer maxConnections) {
			setMaxConnections(maxConnections);
			return this;
		}

		/**
		 * Set the maximum time to life of a connection. After this time the connection will be closed automatically.
		 * 
		 * @param connectionTtl
		 *            the maximum time to life of a connection.
		 * @return {@link ArangoDB.Builder}
		 */
		public Builder connectionTtl(final Long connectionTtl) {
			setConnectionTtl(connectionTtl);
			return this;
		}

		public Builder acquireHostList(final Boolean acquireHostList) {
			setAcquireHostList(acquireHostList);
			return this;
		}

		public Builder loadBalancingStrategy(final LoadBalancingStrategy loadBalancingStrategy) {
			setLoadBalancingStrategy(loadBalancingStrategy);
			return this;
		}

		public <T> Builder registerSerializer(final Class<T> clazz, final VPackSerializer<T> serializer) {
			vpackBuilder.registerSerializer(clazz, serializer);
			return this;
		}

		/**
		 * Register a special serializer for a member class which can only be identified by its enclosing class.
		 * 
		 * @param clazz
		 *            type of the enclosing class
		 * @param serializer
		 *            serializer to register
		 * @return builder
		 */
		public <T> Builder registerEnclosingSerializer(final Class<T> clazz, final VPackSerializer<T> serializer) {
			vpackBuilder.registerEnclosingSerializer(clazz, serializer);
			return this;
		}

		public <T> Builder registerDeserializer(final Class<T> clazz, final VPackDeserializer<T> deserializer) {
			vpackBuilder.registerDeserializer(clazz, deserializer);
			return this;
		}

		public <T> Builder registerInstanceCreator(final Class<T> clazz, final VPackInstanceCreator<T> creator) {
			vpackBuilder.registerInstanceCreator(clazz, creator);
			return this;
		}

		public Builder registerJsonDeserializer(final ValueType type, final VPackJsonDeserializer deserializer) {
			vpackParserBuilder.registerDeserializer(type, deserializer);
			return this;
		}

		public Builder registerJsonDeserializer(
			final String attribute,
			final ValueType type,
			final VPackJsonDeserializer deserializer) {
			vpackParserBuilder.registerDeserializer(attribute, type, deserializer);
			return this;
		}

		public <T> Builder registerJsonSerializer(final Class<T> clazz, final VPackJsonSerializer<T> serializer) {
			vpackParserBuilder.registerSerializer(clazz, serializer);
			return this;
		}

		public <T> Builder registerJsonSerializer(
			final String attribute,
			final Class<T> clazz,
			final VPackJsonSerializer<T> serializer) {
			vpackParserBuilder.registerSerializer(attribute, clazz, serializer);
			return this;
		}

		public <A extends Annotation> Builder annotationFieldFilter(
			final Class<A> type,
			final VPackAnnotationFieldFilter<A> fieldFilter) {
			vpackBuilder.annotationFieldFilter(type, fieldFilter);
			return this;
		}

		public <A extends Annotation> Builder annotationFieldNaming(
			final Class<A> type,
			final VPackAnnotationFieldNaming<A> fieldNaming) {
			vpackBuilder.annotationFieldNaming(type, fieldNaming);
			return this;
		}

		public Builder registerModule(final VPackModule module) {
			vpackBuilder.registerModule(module);
			return this;
		}

		public Builder registerModules(final VPackModule... modules) {
			vpackBuilder.registerModules(modules);
			return this;
		}

		public Builder registerJsonModule(final VPackParserModule module) {
			vpackParserBuilder.registerModule(module);
			return this;
		}

		public Builder registerJsonModules(final VPackParserModule... module) {
			vpackParserBuilder.registerModules(module);
			return this;
		}

		/**
		 * Replace the built-in serializer with the given serializer.
		 * 
		 * <br />
		 * <b>ATTENTION!:</b> Use at your own risk
		 * 
		 * @param serializer
		 *            custom serializer
		 * @return builder
		 */
		public Builder setSerializer(final ArangoSerializer serializer) {
			serializer(serializer);
			return this;
		}

		/**
		 * Replace the built-in deserializer with the given deserializer.
		 * 
		 * <br />
		 * <b>ATTENTION!:</b> Use at your own risk
		 * 
		 * @param deserializer
		 *            custom deserializer
		 * @return builder
		 */
		public Builder setDeserializer(final ArangoDeserializer deserializer) {
			deserializer(deserializer);
			return this;
		}

		public synchronized ArangoDB build() {
			if (hosts.isEmpty()) {
				hosts.add(host);
			}
			final CollectionCache collectionCache = new CollectionCache();
			vpackBuilder.registerModule(new VPackDocumentModule(collectionCache));
			vpackParserBuilder.registerModule(new VPackDocumentModule(collectionCache));

			final VPack vpacker = vpackBuilder.serializeNullValues(false).build();
			final VPack vpackerNull = vpackBuilder.serializeNullValues(true).build();
			final VPackParser vpackParser = vpackParserBuilder.build();
			final ArangoSerializer serializerTemp = serializer != null ? serializer
					: new ArangoSerializerImpl(vpacker, vpackerNull, vpackParser);
			final ArangoDeserializer deserializerTemp = deserializer != null ? deserializer
					: new ArangoDeserializerImpl(vpackerNull, vpackParser);

			final HostResolver hostResolver = createHostResolver();
			final HostHandler hostHandler = createHostHandler(hostResolver);
			return new ArangoDBImpl(
					new VstCommunicationSync.Builder(hostHandler).timeout(timeout).user(user).password(password)
							.useSsl(useSsl).sslContext(sslContext).chunksize(chunksize).maxConnections(maxConnections)
							.connectionTtl(connectionTtl),
					new HttpCommunication.Builder(hostHandler, protocol).timeout(timeout).user(user).password(password)
							.useSsl(useSsl).sslContext(sslContext).maxConnections(maxConnections)
							.connectionTtl(connectionTtl),
					new ArangoUtilImpl(serializerTemp, deserializerTemp), collectionCache, protocol, hostResolver);
		}

	}

	/**
	 * Releases all connections to the server and clear the connection pool.
	 * 
	 * @throws ArangoDBException
	 */
	void shutdown() throws ArangoDBException;

	/**
	 * Returns a handler of the system database
	 * 
	 * @return database handler
	 */
	ArangoDatabase db();

	/**
	 * Returns a handler of the database by the given name
	 * 
	 * @param name
	 *            Name of the database
	 * @return database handler
	 */
	ArangoDatabase db(final String name);

	/**
	 * Creates a new database
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Database/DatabaseManagement.html#create-database">API
	 *      Documentation</a>
	 * @param name
	 *            Has to contain a valid database name
	 * @return true if the database was created successfully.
	 * @throws ArangoDBException
	 */
	Boolean createDatabase(final String name) throws ArangoDBException;

	/**
	 * Retrieves a list of all existing databases
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Database/DatabaseManagement.html#list-of-databases">API
	 *      Documentation</a>
	 * @return a list of all existing databases
	 * @throws ArangoDBException
	 */
	Collection<String> getDatabases() throws ArangoDBException;

	/**
	 * Retrieves a list of all databases the current user can access
	 * 
	 * @see <a href=
	 *      "https://docs.arangodb.com/current/HTTP/Database/DatabaseManagement.html#list-of-accessible-databases">API
	 *      Documentation</a>
	 * @return a list of all databases the current user can access
	 * @throws ArangoDBException
	 */
	Collection<String> getAccessibleDatabases() throws ArangoDBException;

	/**
	 * List available database to the specified user
	 * 
	 * @see <a href=
	 *      "https://docs.arangodb.com/current/HTTP/UserManagement/index.html#list-the-databases-available-to-a-user">API
	 *      Documentation</a>
	 * @param user
	 *            The name of the user for which you want to query the databases
	 * @return
	 * @throws ArangoDBException
	 */
	Collection<String> getAccessibleDatabasesFor(final String user) throws ArangoDBException;

	/**
	 * Returns the server name and version number.
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/MiscellaneousFunctions/index.html#return-server-version">API
	 *      Documentation</a>
	 * @return the server version, number
	 * @throws ArangoDBException
	 */
	ArangoDBVersion getVersion() throws ArangoDBException;

	/**
	 * Returns the server role.
	 * 
	 * @return the server role
	 * @throws ArangoDBException
	 */
	ServerRole getRole() throws ArangoDBException;

	/**
	 * Create a new user. This user will not have access to any database. You need permission to the _system database in
	 * order to execute this call.
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/UserManagement/index.html#create-user">API Documentation</a>
	 * @param user
	 *            The name of the user
	 * @param passwd
	 *            The user password
	 * @return information about the user
	 * @throws ArangoDBException
	 */
	UserEntity createUser(final String user, final String passwd) throws ArangoDBException;

	/**
	 * Create a new user. This user will not have access to any database. You need permission to the _system database in
	 * order to execute this call.
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/UserManagement/index.html#create-user">API Documentation</a>
	 * @param user
	 *            The name of the user
	 * @param passwd
	 *            The user password
	 * @param options
	 *            Additional options, can be null
	 * @return information about the user
	 * @throws ArangoDBException
	 */
	UserEntity createUser(final String user, final String passwd, final UserCreateOptions options)
			throws ArangoDBException;

	/**
	 * Removes an existing user, identified by user. You need access to the _system database.
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/UserManagement/index.html#remove-user">API Documentation</a>
	 * @param user
	 *            The name of the user
	 * @throws ArangoDBException
	 */
	void deleteUser(final String user) throws ArangoDBException;

	/**
	 * Fetches data about the specified user. You can fetch information about yourself or you need permission to the
	 * _system database in order to execute this call.
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/UserManagement/index.html#fetch-user">API Documentation</a>
	 * @param user
	 *            The name of the user
	 * @return information about the user
	 * @throws ArangoDBException
	 */
	UserEntity getUser(final String user) throws ArangoDBException;

	/**
	 * Fetches data about all users. You can only execute this call if you have access to the _system database.
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/UserManagement/index.html#list-available-users">API
	 *      Documentation</a>
	 * @return informations about all users
	 * @throws ArangoDBException
	 */
	Collection<UserEntity> getUsers() throws ArangoDBException;

	/**
	 * Partially updates the data of an existing user. The name of an existing user must be specified in user. You can
	 * only change the password of your self. You need access to the _system database to change the active flag.
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/UserManagement/index.html#update-user">API Documentation</a>
	 * @param user
	 *            The name of the user
	 * @param options
	 *            Properties of the user to be changed
	 * @return information about the user
	 * @throws ArangoDBException
	 */
	UserEntity updateUser(final String user, final UserUpdateOptions options) throws ArangoDBException;

	/**
	 * Replaces the data of an existing user. The name of an existing user must be specified in user. You can only
	 * change the password of your self. You need access to the _system database to change the active flag.
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/UserManagement/index.html#replace-user">API
	 *      Documentation</a>
	 * @param user
	 *            The name of the user
	 * @param options
	 *            Additional properties of the user, can be null
	 * @return information about the user
	 * @throws ArangoDBException
	 */
	UserEntity replaceUser(final String user, final UserUpdateOptions options) throws ArangoDBException;

	/**
	 * @deprecated use {@link #grantDefaultDatabaseAccess(String, Permissions)} instead
	 * 
	 * @param user
	 *            The name of the user
	 * @param permissions
	 *            The permissions the user grant
	 * @since ArangoDB 3.2.0
	 * @throws ArangoDBException
	 */
	@Deprecated
	void updateUserDefaultDatabaseAccess(final String user, final Permissions permissions) throws ArangoDBException;

	/**
	 * Sets the default access level for databases for the user <code>user</code>. You need permission to the _system
	 * database in order to execute this call.
	 * 
	 * @param user
	 *            The name of the user
	 * @param permissions
	 *            The permissions the user grant
	 * @since ArangoDB 3.2.0
	 * @throws ArangoDBException
	 */
	void grantDefaultDatabaseAccess(final String user, final Permissions permissions) throws ArangoDBException;

	/**
	 * @deprecated user {@link #grantDefaultCollectionAccess(String, Permissions)} instead
	 * 
	 * @param user
	 *            The name of the user
	 * @param permissions
	 *            The permissions the user grant
	 * @since ArangoDB 3.2.0
	 * @throws ArangoDBException
	 */
	@Deprecated
	void updateUserDefaultCollectionAccess(final String user, final Permissions permissions) throws ArangoDBException;

	/**
	 * Sets the default access level for collections for the user <code>user</code>. You need permission to the _system
	 * database in order to execute this call.
	 * 
	 * @param user
	 *            The name of the user
	 * @param permissions
	 *            The permissions the user grant
	 * @since ArangoDB 3.2.0
	 * @throws ArangoDBException
	 */
	void grantDefaultCollectionAccess(final String user, final Permissions permissions) throws ArangoDBException;

	/**
	 * Generic Execute. Use this method to execute custom FOXX services.
	 * 
	 * @param request
	 *            VelocyStream request
	 * @return VelocyStream response
	 * @throws ArangoDBException
	 */
	Response execute(final Request request) throws ArangoDBException;

	/**
	 * Generic Execute. Use this method to execute custom FOXX services.
	 * 
	 * @param request
	 *            VelocyStream request
	 * @param hostHandle
	 *            Used to stick to a specific host when using {@link LoadBalancingStrategy#ROUND_ROBIN}
	 * @return VelocyStream response
	 * @throws ArangoDBException
	 */
	Response execute(final Request request, final HostHandle hostHandle) throws ArangoDBException;

	/**
	 * Returns fatal, error, warning or info log messages from the server's global log.
	 * 
	 * @see <a href=
	 *      "https://docs.arangodb.com/current/HTTP/AdministrationAndMonitoring/index.html#read-global-logs-from-the-server">API
	 *      Documentation</a>
	 * @param options
	 *            Additional options, can be null
	 * @return the log messages
	 * @throws ArangoDBException
	 */
	LogEntity getLogs(final LogOptions options) throws ArangoDBException;

	/**
	 * Returns the server's current loglevel settings.
	 * 
	 * @return the server's current loglevel settings
	 * @since ArangoDB 3.1.0
	 * @throws ArangoDBException
	 */
	LogLevelEntity getLogLevel() throws ArangoDBException;

	/**
	 * Modifies and returns the server's current loglevel settings.
	 * 
	 * @param entity
	 *            loglevel settings
	 * @return the server's current loglevel settings
	 * @since ArangoDB 3.1.0
	 * @throws ArangoDBException
	 */
	LogLevelEntity setLogLevel(final LogLevelEntity entity) throws ArangoDBException;

	ArangoDB _setCursorInitializer(final ArangoCursorInitializer cursorInitializer);

	ArangoSerialization util();
}
