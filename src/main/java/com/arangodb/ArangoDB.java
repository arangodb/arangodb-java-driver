package com.arangodb;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;

import com.arangodb.entity.ArangoDBVersion;
import com.arangodb.entity.UserResult;
import com.arangodb.internal.ArangoDBConstants;
import com.arangodb.internal.CollectionCache;
import com.arangodb.internal.DocumentCache;
import com.arangodb.internal.net.Communication;
import com.arangodb.internal.net.Request;
import com.arangodb.internal.net.velocystream.RequestType;
import com.arangodb.internal.velocypack.VPackConfigure;
import com.arangodb.model.DBCreateOptions;
import com.arangodb.model.OptionsBuilder;
import com.arangodb.model.UserCreateOptions;
import com.arangodb.model.UserUpdateOptions;
import com.arangodb.velocypack.Type;
import com.arangodb.velocypack.VPack;
import com.arangodb.velocypack.VPackDeserializer;
import com.arangodb.velocypack.VPackInstanceCreator;
import com.arangodb.velocypack.VPackParser;
import com.arangodb.velocypack.VPackSerializer;
import com.arangodb.velocypack.VPackSlice;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class ArangoDB extends ArangoExecuteable {

	public static class Builder {

		private static final String PROPERTY_KEY_HOST = "arangodb.host";
		private static final String PROPERTY_KEY_PORT = "arangodb.port";
		private static final String PROPERTY_KEY_TIMEOUT = "arangodb.timeout";
		private static final String DEFAULT_PROPERTY_FILE = "/arangodb.properties";

		private String host;
		private Integer port;
		private Integer timeout;
		private String user;
		private String password;
		private final VPack.Builder vpackBuilder;
		private final CollectionCache collectionCache;
		private final VPackParser vpackParser;

		public Builder() {
			super();
			vpackBuilder = new VPack.Builder();
			collectionCache = new CollectionCache();
			vpackParser = new VPackParser();
			VPackConfigure.configure(vpackBuilder, vpackParser, collectionCache);
			loadProperties(ArangoDB.class.getResourceAsStream(DEFAULT_PROPERTY_FILE));
		}

		public Builder loadProperties(final InputStream in) {
			if (in != null) {
				final Properties properties = new Properties();
				try {
					properties.load(in);
					host = getProperty(properties, PROPERTY_KEY_HOST, host, ArangoDBConstants.DEFAULT_HOST);
					port = Integer
							.parseInt(getProperty(properties, PROPERTY_KEY_PORT, port, ArangoDBConstants.DEFAULT_PORT));
					timeout = Integer.parseInt(
						getProperty(properties, PROPERTY_KEY_TIMEOUT, timeout, ArangoDBConstants.DEFAULT_TIMEOUT));
				} catch (final IOException e) {
					throw new ArangoDBException(e);
				}
			}
			return this;
		}

		private <T> String getProperty(
			final Properties properties,
			final String key,
			final T currentValue,
			final T defaultValue) {
			return properties.getProperty(key,
				currentValue != null ? currentValue.toString() : defaultValue.toString());
		}

		public Builder host(final String host) {
			this.host = host;
			return this;
		}

		public Builder port(final int port) {
			this.port = port;
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

		public <T> Builder registerSerializer(final Class<T> clazz, final VPackSerializer<T> serializer) {
			vpackBuilder.registerSerializer(clazz, serializer);
			return this;
		}

		public <T> Builder registerDeserializer(final Class<T> clazz, final VPackDeserializer<T> deserializer) {
			vpackBuilder.registerDeserializer(clazz, deserializer);
			return this;
		}

		public <T> Builder regitserInstanceCreator(final Class<T> clazz, final VPackInstanceCreator<T> creator) {
			vpackBuilder.regitserInstanceCreator(clazz, creator);
			return this;
		}

		public ArangoDB build() {
			return new ArangoDB(new Communication.Builder().host(host).port(port).timeout(timeout),
					vpackBuilder.build(), vpackBuilder.serializeNullValues(true).build(), vpackParser, collectionCache);
		}

	}

	public ArangoDB(final Communication.Builder commBuilder, final VPack vpack, final VPack vpackNull,
		final VPackParser vpackParser, final CollectionCache collectionCache) {
		super(commBuilder.build(vpack, collectionCache), vpack, vpackNull, vpackParser, new DocumentCache(),
				collectionCache);
		final Communication cacheCom = commBuilder.build(vpack, collectionCache);
		collectionCache.init(name -> {
			return new ArangoDatabase(cacheCom, vpackNull, vpack, vpackParser, documentCache, null, name);
		});
	}

	public void shutdown() {
		communication.disconnect();
	}

	public ArangoDatabase db() {
		return db(ArangoDBConstants.SYSTEM);
	}

	public ArangoDatabase db(final String name) {
		validateDBName(name);
		return new ArangoDatabase(this, name);
	}

	/**
	 * creates a new database
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Database/DatabaseManagement.html#create-database">API
	 *      Documentation</a>
	 * @param name
	 *            Has to contain a valid database name
	 * @return true if the database was created successfully.
	 * @throws ArangoDBException
	 */
	public Boolean createDatabase(final String name) throws ArangoDBException {
		validateDBName(name);
		return executeSync(createDatabaseRequest(name), createDatabaseResponseDeserializer());
	}

	/**
	 * creates a new database
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Database/DatabaseManagement.html#create-database">API
	 *      Documentation</a>
	 * @param name
	 *            Has to contain a valid database name
	 * @return true if the database was created successfully.
	 */
	public CompletableFuture<Boolean> createDatabaseAsync(final String name) {
		validateDBName(name);
		return executeAsync(createDatabaseRequest(name), createDatabaseResponseDeserializer());
	}

	private Request createDatabaseRequest(final String name) {
		final Request request = new Request(ArangoDBConstants.SYSTEM, RequestType.POST,
				ArangoDBConstants.PATH_API_DATABASE);
		request.setBody(serialize(OptionsBuilder.build(new DBCreateOptions(), name)));
		return request;
	}

	private ResponseDeserializer<Boolean> createDatabaseResponseDeserializer() {
		return response -> response.getBody().get().get(ArangoDBConstants.RESULT).getAsBoolean();
	}

	/**
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Database/DatabaseManagement.html#list-of-databases">API
	 *      Documentation</a>
	 * @return a list of all existing databases
	 * @throws ArangoDBException
	 */
	public Collection<String> getDatabases() throws ArangoDBException {
		return executeSync(getDatabasesRequest(), getDatabaseResponseDeserializer());
	}

	/**
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Database/DatabaseManagement.html#list-of-databases">API
	 *      Documentation</a>
	 * @return a list of all existing databases
	 */
	public CompletableFuture<Collection<String>> getDatabasesAsync() {
		return executeAsync(getDatabasesRequest(), getDatabaseResponseDeserializer());
	}

	private Request getDatabasesRequest() {
		return new Request(db().name(), RequestType.GET, ArangoDBConstants.PATH_API_DATABASE);
	}

	private ResponseDeserializer<Collection<String>> getDatabaseResponseDeserializer() {
		return response -> {
			final VPackSlice result = response.getBody().get().get(ArangoDBConstants.RESULT);
			return deserialize(result, new Type<Collection<String>>() {
			}.getType());
		};
	}

	/**
	 * @see <a href=
	 *      "https://docs.arangodb.com/current/HTTP/Database/DatabaseManagement.html#list-of-accessible-databases">API
	 *      Documentation</a>
	 * @return a list of all databases the current user can access
	 * @throws ArangoDBException
	 */
	public Collection<String> getAccessibleDatabases() throws ArangoDBException {
		return executeSync(getAccessibleDatabasesRequest(), getDatabaseResponseDeserializer());
	}

	/**
	 * @see <a href=
	 *      "https://docs.arangodb.com/current/HTTP/Database/DatabaseManagement.html#list-of-accessible-databases">API
	 *      Documentation</a>
	 * @return a list of all databases the current user can access
	 */
	public CompletableFuture<Collection<String>> getAccessibleDatabasesAsync() {
		return executeAsync(getAccessibleDatabasesRequest(), getDatabaseResponseDeserializer());
	}

	private Request getAccessibleDatabasesRequest() {
		return new Request(db().name(), RequestType.GET,
				createPath(ArangoDBConstants.PATH_API_DATABASE, ArangoDBConstants.USER));
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
		return executeSync(ArangoDBVersion.class, getVersionRequest());
	}

	/**
	 * Returns the server name and version number.
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/MiscellaneousFunctions/index.html#return-server-version">API
	 *      Documentation</a>
	 * @return the server version, number
	 */
	public CompletableFuture<ArangoDBVersion> getVersionAsync() {
		return executeAsync(ArangoDBVersion.class, getVersionRequest());
	}

	private Request getVersionRequest() {
		return new Request(ArangoDBConstants.SYSTEM, RequestType.GET, ArangoDBConstants.PATH_API_VERSION);
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
	public UserResult createUser(final String user, final String passwd, final UserCreateOptions options)
			throws ArangoDBException {
		return executeSync(UserResult.class, createUserRequest(user, passwd, options));
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
	 *            Additional properties of the user, can be null
	 * @return information about the user
	 */
	public CompletableFuture<UserResult> createUserAsync(
		final String user,
		final String passwd,
		final UserCreateOptions options) {
		return executeAsync(UserResult.class, createUserRequest(user, passwd, options));
	}

	private Request createUserRequest(final String user, final String passwd, final UserCreateOptions options) {
		final Request request;
		request = new Request(db().name(), RequestType.POST, ArangoDBConstants.PATH_API_USER);
		request.setBody(
			serialize(OptionsBuilder.build(options != null ? options : new UserCreateOptions(), user, passwd)));
		return request;
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
		executeSync(Void.class, deleteUserRequest(user));
	}

	/**
	 * Removes an existing user, identified by user. You need access to the _system database.
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/UserManagement/index.html#remove-user">API Documentation</a>
	 * @param user
	 *            The name of the user
	 * @return void
	 */
	public CompletableFuture<Void> deleteUserAsync(final String user) {
		return executeAsync(Void.class, deleteUserRequest(user));
	}

	private Request deleteUserRequest(final String user) {
		return new Request(db().name(), RequestType.DELETE, createPath(ArangoDBConstants.PATH_API_USER, user));
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
	public UserResult getUser(final String user) throws ArangoDBException {
		return executeSync(UserResult.class, getUserRequest(user));
	}

	/**
	 * Fetches data about the specified user. You can fetch information about yourself or you need permission to the
	 * _system database in order to execute this call.
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/UserManagement/index.html#fetch-user">API Documentation</a>
	 * @param user
	 *            The name of the user
	 * @return information about the user
	 */
	public CompletableFuture<UserResult> getUserAsync(final String user) {
		return executeAsync(UserResult.class, getUserRequest(user));
	}

	private Request getUserRequest(final String user) {
		return new Request(db().name(), RequestType.GET, createPath(ArangoDBConstants.PATH_API_USER, user));
	}

	/**
	 * Fetches data about all users. You can only execute this call if you have access to the _system database.
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/UserManagement/index.html#list-available-users">API
	 *      Documentation</a>
	 * @return informations about all users
	 * @throws ArangoDBException
	 */
	public Collection<UserResult> getUsers() throws ArangoDBException {
		return executeSync(getUsersRequest(), getUsersResponseDeserializer());
	}

	/**
	 * Fetches data about all users. You can only execute this call if you have access to the _system database.
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/UserManagement/index.html#list-available-users">API
	 *      Documentation</a>
	 * @return informations about all users
	 */
	public CompletableFuture<Collection<UserResult>> getUsersAsync() {
		return executeAsync(getUsersRequest(), getUsersResponseDeserializer());
	}

	private Request getUsersRequest() {
		return new Request(db().name(), RequestType.GET, ArangoDBConstants.PATH_API_USER);
	}

	private ResponseDeserializer<Collection<UserResult>> getUsersResponseDeserializer() {
		return (response) -> {
			final VPackSlice result = response.getBody().get().get(ArangoDBConstants.RESULT);
			return deserialize(result, new Type<Collection<UserResult>>() {
			}.getType());
		};
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
	public UserResult updateUser(final String user, final UserUpdateOptions options) throws ArangoDBException {
		return executeSync(UserResult.class, updateUserRequest(user, options));
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
	 */
	public CompletableFuture<UserResult> updateUserAsync(final String user, final UserUpdateOptions options) {
		return executeAsync(UserResult.class, updateUserRequest(user, options));
	}

	private Request updateUserRequest(final String user, final UserUpdateOptions options) {
		final Request request;
		request = new Request(db().name(), RequestType.PATCH, createPath(ArangoDBConstants.PATH_API_USER, user));
		request.setBody(serialize(options != null ? options : new UserUpdateOptions()));
		return request;
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
	public UserResult replaceUser(final String user, final UserUpdateOptions options) throws ArangoDBException {
		return executeSync(UserResult.class, replaceUserRequest(user, options));
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
	 */
	public CompletableFuture<UserResult> replaceUserAsync(final String user, final UserUpdateOptions options) {
		return executeAsync(UserResult.class, replaceUserRequest(user, options));
	}

	private Request replaceUserRequest(final String user, final UserUpdateOptions options) {
		final Request request;
		request = new Request(db().name(), RequestType.PUT, createPath(ArangoDBConstants.PATH_API_USER, user));
		request.setBody(serialize(options != null ? options : new UserUpdateOptions()));
		return request;
	}

}
