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

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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
import com.arangodb.internal.ArangoExecutor.ResponseDeserializer;
import com.arangodb.internal.ArangoExecutorSync;
import com.arangodb.internal.CollectionCache;
import com.arangodb.internal.CollectionCache.DBAccess;
import com.arangodb.internal.DocumentCache;
import com.arangodb.internal.Host;
import com.arangodb.internal.InternalArangoDB;
import com.arangodb.internal.http.HttpCommunication;
import com.arangodb.internal.http.HttpProtocol;
import com.arangodb.internal.net.CommunicationProtocol;
import com.arangodb.internal.net.ExtendedHostResolver;
import com.arangodb.internal.net.FallbackHostHandler;
import com.arangodb.internal.net.HostHandle;
import com.arangodb.internal.net.HostHandler;
import com.arangodb.internal.net.HostResolver;
import com.arangodb.internal.net.HostResolver.EndpointResolver;
import com.arangodb.internal.net.RandomHostHandler;
import com.arangodb.internal.net.RoundRobinHostHandler;
import com.arangodb.internal.net.SimpleHostResolver;
import com.arangodb.internal.util.ArangoDeserializerImpl;
import com.arangodb.internal.util.ArangoSerializerImpl;
import com.arangodb.internal.util.ArangoUtilImpl;
import com.arangodb.internal.velocypack.VPackDocumentModule;
import com.arangodb.internal.velocypack.VPackDriverModule;
import com.arangodb.internal.velocystream.VstCommunicationSync;
import com.arangodb.internal.velocystream.VstProtocol;
import com.arangodb.internal.velocystream.internal.ConnectionSync;
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
import com.arangodb.velocypack.VPackSlice;
import com.arangodb.velocypack.ValueType;
import com.arangodb.velocypack.exception.VPackException;
import com.arangodb.velocystream.Request;
import com.arangodb.velocystream.RequestType;
import com.arangodb.velocystream.Response;

/**
 * @author Mark Vollmary
 *
 */
public class ArangoDB extends InternalArangoDB<ArangoExecutorSync, Response, ConnectionSync> {

	public static class Builder {

		private final List<Host> hosts;
		private Host host;
		private Integer timeout;
		private String user;
		private String password;
		private Boolean useSsl;
		private SSLContext sslContext;
		private Integer chunksize;
		private Integer maxConnections;
		private Long connectionTtl;
		private final VPack.Builder vpackBuilder;
		private final VPackParser.Builder vpackParserBuilder;
		private ArangoSerializer serializer;
		private ArangoDeserializer deserializer;
		private Protocol protocol;
		private Boolean acquireHostList;
		private LoadBalancingStrategy loadBalancingStrategy;

		public Builder() {
			super();
			vpackBuilder = new VPack.Builder();
			vpackParserBuilder = new VPackParser.Builder();
			vpackBuilder.registerModule(new VPackDriverModule());
			vpackParserBuilder.registerModule(new VPackDriverModule());
			host = new Host(ArangoDBConstants.DEFAULT_HOST, ArangoDBConstants.DEFAULT_PORT);
			hosts = new ArrayList<Host>();
			user = ArangoDBConstants.DEFAULT_USER;
			loadProperties(ArangoDB.class.getResourceAsStream(DEFAULT_PROPERTY_FILE));
		}

		public Builder loadProperties(final InputStream in) throws ArangoDBException {
			if (in != null) {
				final Properties properties = new Properties();
				try {
					properties.load(in);
					loadHosts(properties, this.hosts);
					final String host = loadHost(properties, this.host.getHost());
					final int port = loadPort(properties, this.host.getPort());
					this.host = new Host(host, port);
					timeout = loadTimeout(properties, timeout);
					user = loadUser(properties, user);
					password = loadPassword(properties, password);
					useSsl = loadUseSsl(properties, useSsl);
					chunksize = loadChunkSize(properties, chunksize);
					maxConnections = loadMaxConnections(properties, maxConnections);
					connectionTtl = loadConnectionTtl(properties, connectionTtl);
					protocol = loadProtocol(properties, protocol);
					acquireHostList = loadAcquireHostList(properties, acquireHostList);
					loadBalancingStrategy = loadLoadBalancingStrategy(properties, loadBalancingStrategy);
				} catch (final IOException e) {
					throw new ArangoDBException(e);
				}
			}
			return this;
		}

		/**
		 * @deprecated will be removed in version 4.2.0 use {@link #host(String, int)} instead
		 * 
		 * @param host
		 * @return
		 */
		@Deprecated
		public Builder host(final String host) {
			this.host = new Host(host, this.host.getPort());
			return this;
		}

		/**
		 * @deprecated will be removed in version 4.2.0 use {@link #host(String, int)} instead
		 * 
		 * @param port
		 * @return
		 */
		@Deprecated
		public Builder port(final Integer port) {
			host = new Host(host.getHost(), port);
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
			hosts.add(new Host(host, port));
			return this;
		}

		public Builder timeout(final Integer timeout) {
			this.timeout = timeout;
			return this;
		}

		public Builder user(final String user) {
			this.user = user;
			return this;
		}

		public Builder password(final String password) {
			this.password = password;
			return this;
		}

		public Builder useSsl(final Boolean useSsl) {
			this.useSsl = useSsl;
			return this;
		}

		public Builder sslContext(final SSLContext sslContext) {
			this.sslContext = sslContext;
			return this;
		}

		public Builder chunksize(final Integer chunksize) {
			this.chunksize = chunksize;
			return this;
		}

		public Builder maxConnections(final Integer maxConnections) {
			this.maxConnections = maxConnections;
			return this;
		}

		/**
		 * Set the maximum time to life of a connection. After this time the connection will be closed automatically.
		 * Only used by VelocyStream protocol!
		 * 
		 * @param connectionTtl
		 *            the maximum time to life of a connection.
		 * @return {@link ArangoDB.Builder}
		 */
		public Builder connectionTtl(final Long connectionTtl) {
			this.connectionTtl = connectionTtl;
			return this;
		}

		public Builder useProtocol(final Protocol protocol) {
			this.protocol = protocol;
			return this;
		}

		public Builder acquireHostList(final Boolean acquireHostList) {
			this.acquireHostList = acquireHostList;
			return this;
		}

		public Builder loadBalancingStrategy(final LoadBalancingStrategy loadBalancingStrategy) {
			this.loadBalancingStrategy = loadBalancingStrategy;
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
			this.serializer = serializer;
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
			this.deserializer = deserializer;
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
			return new ArangoDB(
					new VstCommunicationSync.Builder(hostHandler).timeout(timeout).user(user).password(password)
							.useSsl(useSsl).sslContext(sslContext).chunksize(chunksize).maxConnections(maxConnections)
							.connectionTtl(connectionTtl),
					new HttpCommunication.Builder(hostHandler, protocol).timeout(timeout).user(user).password(password)
							.useSsl(useSsl).sslContext(sslContext).maxConnections(maxConnections)
							.connectionTtl(connectionTtl),
					new ArangoUtilImpl(serializerTemp, deserializerTemp), collectionCache, protocol, hostResolver);
		}

		private HostResolver createHostResolver() {
			return acquireHostList != null && acquireHostList.booleanValue()
					? new ExtendedHostResolver(new ArrayList<Host>(hosts))
					: new SimpleHostResolver(new ArrayList<Host>(hosts));
		}

		private HostHandler createHostHandler(final HostResolver hostResolver) {
			final HostHandler hostHandler;
			if (loadBalancingStrategy != null) {
				switch (loadBalancingStrategy) {
				case ONE_RANDOM:
					hostHandler = new RandomHostHandler(hostResolver, new FallbackHostHandler(hostResolver));
					break;
				case ROUND_ROBIN:
					hostHandler = new RoundRobinHostHandler(hostResolver);
					break;
				case NONE:
				default:
					hostHandler = new FallbackHostHandler(hostResolver);
					break;
				}
			} else {
				hostHandler = new FallbackHostHandler(hostResolver);
			}
			return hostHandler;
		}

	}

	private ArangoCursorInitializer cursorInitializer;
	private CommunicationProtocol cp;

	public ArangoDB(final VstCommunicationSync.Builder vstBuilder, final HttpCommunication.Builder httpBuilder,
		final ArangoSerialization util, final CollectionCache collectionCache, final Protocol protocol,
		final HostResolver hostResolver) {
		super(new ArangoExecutorSync(createProtocol(vstBuilder, httpBuilder, util, collectionCache, protocol), util,
				new DocumentCache()), util);
		cp = createProtocol(new VstCommunicationSync.Builder(vstBuilder).maxConnections(1),
			new HttpCommunication.Builder(httpBuilder).maxConnections(1), util, collectionCache, protocol);
		collectionCache.init(new DBAccess() {
			@Override
			public ArangoDatabase db(final String name) {
				return new ArangoDatabase(cp, util, executor.documentCache(), name)
						.setCursorInitializer(cursorInitializer);
			}
		});
		hostResolver.init(new EndpointResolver() {
			@Override
			public Collection<String> resolve(final boolean closeConnections) throws ArangoDBException {
				Collection<String> response;
				try {
					response = executor.execute(
						new Request(ArangoDBConstants.SYSTEM, RequestType.GET, ArangoDBConstants.PATH_ENDPOINTS),
						new ResponseDeserializer<Collection<String>>() {
							@Override
							public Collection<String> deserialize(final Response response) throws VPackException {
								final VPackSlice field = response.getBody().get(ArangoDBConstants.ENDPOINTS);
								Collection<String> endpoints;
								if (field.isNone()) {
									endpoints = Collections.<String> emptyList();
								} else {
									final Collection<Map<String, String>> tmp = util().deserialize(field,
										Collection.class);
									endpoints = new ArrayList<String>();
									for (final Map<String, String> map : tmp) {
										for (final String value : map.values()) {
											endpoints.add(value);
										}
									}
								}
								return endpoints;
							}
						}, null);
				} catch (final ArangoDBException e) {
					final Integer responseCode = e.getResponseCode();
					if (responseCode != null && responseCode == 403) {
						response = Collections.<String> emptyList();
					} else {
						throw e;
					}
				} finally {
					if (closeConnections) {
						ArangoDB.this.shutdown();
					}
				}
				return response;
			}
		});
	}

	private static CommunicationProtocol createProtocol(
		final VstCommunicationSync.Builder vstBuilder,
		final HttpCommunication.Builder httpBuilder,
		final ArangoSerialization util,
		final CollectionCache collectionCache,
		final Protocol protocol) {
		return (protocol == null || Protocol.VST == protocol) ? createVST(vstBuilder, util, collectionCache)
				: createHTTP(httpBuilder, util);
	}

	private static CommunicationProtocol createVST(
		final VstCommunicationSync.Builder builder,
		final ArangoSerialization util,
		final CollectionCache collectionCache) {
		return new VstProtocol(builder.build(util, collectionCache));
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

	public void shutdown() throws ArangoDBException {
		try {
			executor.disconnect();
			cp.close();
		} catch (final IOException e) {
			throw new ArangoDBException(e);
		}
	}

	/**
	 * Returns a handler of the system database
	 * 
	 * @return database handler
	 */
	public ArangoDatabase db() {
		return db(ArangoDBConstants.SYSTEM);
	}

	/**
	 * Returns a handler of the database by the given name
	 * 
	 * @param name
	 *            Name of the database
	 * @return database handler
	 */
	public ArangoDatabase db(final String name) {
		return new ArangoDatabase(this, name).setCursorInitializer(cursorInitializer);
	}

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
	public Boolean createDatabase(final String name) throws ArangoDBException {
		return executor.execute(createDatabaseRequest(name), createDatabaseResponseDeserializer());
	}

	/**
	 * Retrieves a list of all existing databases
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Database/DatabaseManagement.html#list-of-databases">API
	 *      Documentation</a>
	 * @return a list of all existing databases
	 * @throws ArangoDBException
	 */
	public Collection<String> getDatabases() throws ArangoDBException {
		return executor.execute(getDatabasesRequest(db().name()), getDatabaseResponseDeserializer());
	}

	/**
	 * Retrieves a list of all databases the current user can access
	 * 
	 * @see <a href=
	 *      "https://docs.arangodb.com/current/HTTP/Database/DatabaseManagement.html#list-of-accessible-databases">API
	 *      Documentation</a>
	 * @return a list of all databases the current user can access
	 * @throws ArangoDBException
	 */
	public Collection<String> getAccessibleDatabases() throws ArangoDBException {
		return db().getAccessibleDatabases();
	}

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
	public Collection<String> getAccessibleDatabasesFor(final String user) throws ArangoDBException {
		return executor.execute(getAccessibleDatabasesForRequest(db().name(), user),
			getAccessibleDatabasesForResponseDeserializer());
	}

	/**
	 * Returns the server name and version number.
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/MiscellaneousFunctions/index.html#return-server-version">API
	 *      Documentation</a>
	 * @return the server version, number
	 * @throws ArangoDBException
	 */
	public ArangoDBVersion getVersion() throws ArangoDBException {
		return db().getVersion();
	}

	/**
	 * Returns the server role.
	 * 
	 * @return the server role
	 * @throws ArangoDBException
	 */
	public ServerRole getRole() throws ArangoDBException {
		return executor.execute(getRoleRequest(), getRoleResponseDeserializer());
	}

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
	public UserEntity createUser(final String user, final String passwd) throws ArangoDBException {
		return executor.execute(createUserRequest(db().name(), user, passwd, new UserCreateOptions()),
			UserEntity.class);
	}

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
	public UserEntity createUser(final String user, final String passwd, final UserCreateOptions options)
			throws ArangoDBException {
		return executor.execute(createUserRequest(db().name(), user, passwd, options), UserEntity.class);
	}

	/**
	 * Removes an existing user, identified by user. You need access to the _system database.
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/UserManagement/index.html#remove-user">API Documentation</a>
	 * @param user
	 *            The name of the user
	 * @throws ArangoDBException
	 */
	public void deleteUser(final String user) throws ArangoDBException {
		executor.execute(deleteUserRequest(db().name(), user), Void.class);
	}

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
	public UserEntity getUser(final String user) throws ArangoDBException {
		return executor.execute(getUserRequest(db().name(), user), UserEntity.class);
	}

	/**
	 * Fetches data about all users. You can only execute this call if you have access to the _system database.
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/UserManagement/index.html#list-available-users">API
	 *      Documentation</a>
	 * @return informations about all users
	 * @throws ArangoDBException
	 */
	public Collection<UserEntity> getUsers() throws ArangoDBException {
		return executor.execute(getUsersRequest(db().name()), getUsersResponseDeserializer());
	}

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
	public UserEntity updateUser(final String user, final UserUpdateOptions options) throws ArangoDBException {
		return executor.execute(updateUserRequest(db().name(), user, options), UserEntity.class);
	}

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
	public UserEntity replaceUser(final String user, final UserUpdateOptions options) throws ArangoDBException {
		return executor.execute(replaceUserRequest(db().name(), user, options), UserEntity.class);
	}

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
	public void updateUserDefaultDatabaseAccess(final String user, final Permissions permissions)
			throws ArangoDBException {
		executor.execute(updateUserDefaultDatabaseAccessRequest(user, permissions), Void.class);
	}

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
	public void grantDefaultDatabaseAccess(final String user, final Permissions permissions) throws ArangoDBException {
		executor.execute(updateUserDefaultDatabaseAccessRequest(user, permissions), Void.class);
	}

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
	public void updateUserDefaultCollectionAccess(final String user, final Permissions permissions)
			throws ArangoDBException {
		executor.execute(updateUserDefaultCollectionAccessRequest(user, permissions), Void.class);
	}

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
	public void grantDefaultCollectionAccess(final String user, final Permissions permissions)
			throws ArangoDBException {
		executor.execute(updateUserDefaultCollectionAccessRequest(user, permissions), Void.class);
	}

	/**
	 * Generic Execute. Use this method to execute custom FOXX services.
	 * 
	 * @param request
	 *            VelocyStream request
	 * @return VelocyStream response
	 * @throws ArangoDBException
	 */
	public Response execute(final Request request) throws ArangoDBException {
		return executor.execute(request, new ResponseDeserializer<Response>() {
			@Override
			public Response deserialize(final Response response) throws VPackException {
				return response;
			}
		});
	}

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
	public Response execute(final Request request, final HostHandle hostHandle) throws ArangoDBException {
		return executor.execute(request, new ResponseDeserializer<Response>() {
			@Override
			public Response deserialize(final Response response) throws VPackException {
				return response;
			}
		}, hostHandle);
	}

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
	public LogEntity getLogs(final LogOptions options) throws ArangoDBException {
		return executor.execute(getLogsRequest(options), LogEntity.class);
	}

	/**
	 * Returns the server's current loglevel settings.
	 * 
	 * @return the server's current loglevel settings
	 * @since ArangoDB 3.1.0
	 * @throws ArangoDBException
	 */
	public LogLevelEntity getLogLevel() throws ArangoDBException {
		return executor.execute(getLogLevelRequest(), LogLevelEntity.class);
	}

	/**
	 * Modifies and returns the server's current loglevel settings.
	 * 
	 * @param entity
	 *            loglevel settings
	 * @return the server's current loglevel settings
	 * @since ArangoDB 3.1.0
	 * @throws ArangoDBException
	 */
	public LogLevelEntity setLogLevel(final LogLevelEntity entity) throws ArangoDBException {
		return executor.execute(setLogLevelRequest(entity), LogLevelEntity.class);
	}

	public ArangoDB _setCursorInitializer(final ArangoCursorInitializer cursorInitializer) {
		this.cursorInitializer = cursorInitializer;
		return this;
	}
}
