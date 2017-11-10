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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Properties;

import com.arangodb.ArangoDBException;
import com.arangodb.Protocol;
import com.arangodb.entity.LogLevelEntity;
import com.arangodb.entity.Permissions;
import com.arangodb.entity.ServerRole;
import com.arangodb.entity.UserEntity;
import com.arangodb.internal.ArangoExecutor.ResponseDeserializer;
import com.arangodb.internal.velocystream.internal.VstConnection;
import com.arangodb.model.DBCreateOptions;
import com.arangodb.model.LogOptions;
import com.arangodb.model.OptionsBuilder;
import com.arangodb.model.UserAccessOptions;
import com.arangodb.model.UserCreateOptions;
import com.arangodb.model.UserUpdateOptions;
import com.arangodb.util.ArangoSerialization;
import com.arangodb.velocypack.Type;
import com.arangodb.velocypack.VPackSlice;
import com.arangodb.velocypack.exception.VPackException;
import com.arangodb.velocystream.Request;
import com.arangodb.velocystream.RequestType;
import com.arangodb.velocystream.Response;

/**
 * @author Mark Vollmary
 * @param <R>
 * @param <C>
 *
 */
public class InternalArangoDB<E extends ArangoExecutor, R, C extends VstConnection> extends ArangoExecuteable<E, R, C> {

	private static final String PROPERTY_KEY_HOSTS = "arangodb.hosts";
	private static final String PROPERTY_KEY_HOST = "arangodb.host";
	private static final String PROPERTY_KEY_PORT = "arangodb.port";
	private static final String PROPERTY_KEY_TIMEOUT = "arangodb.timeout";
	private static final String PROPERTY_KEY_USER = "arangodb.user";
	private static final String PROPERTY_KEY_PASSWORD = "arangodb.password";
	private static final String PROPERTY_KEY_USE_SSL = "arangodb.usessl";
	private static final String PROPERTY_KEY_V_STREAM_CHUNK_CONTENT_SIZE = "arangodb.chunksize";
	private static final String PROPERTY_KEY_MAX_CONNECTIONS = "arangodb.connections.max";
	private static final String PROPERTY_KEY_PROTOCOL = "arangodb.protocol";
	protected static final String DEFAULT_PROPERTY_FILE = "/arangodb.properties";

	public InternalArangoDB(final E executor, final ArangoSerialization util) {
		super(executor, util);
	}

	protected static void loadHosts(final Properties properties, final Collection<Host> hosts) {
		final String hostsProp = properties.getProperty(PROPERTY_KEY_HOSTS);
		if (hostsProp != null) {
			final String[] hostsSplit = hostsProp.split(",");
			for (final String host : hostsSplit) {
				final String[] split = host.split(":");
				if (split.length != 2 || !split[1].matches("[0-9]+")) {
					throw new ArangoDBException(String.format(
						"Could not load property-value arangodb.hosts=%s. Expected format ip:port,ip:port,...",
						hostsProp));
				} else {
					hosts.add(new Host(split[0], Integer.valueOf(split[1])));
				}
			}
		}
	}

	protected static String loadHost(final Properties properties, final String currentValue) {
		final String host = getProperty(properties, PROPERTY_KEY_HOST, currentValue, ArangoDBConstants.DEFAULT_HOST);
		if (host.contains(":")) {
			throw new ArangoDBException(String.format(
				"Could not load property-value arangodb.host=%s. Expect only ip. Do you mean arangodb.hosts=ip:port ?",
				host));
		}
		return host;
	}

	protected static Integer loadPort(final Properties properties, final int currentValue) {
		return Integer
				.parseInt(getProperty(properties, PROPERTY_KEY_PORT, currentValue, ArangoDBConstants.DEFAULT_PORT));
	}

	protected static Integer loadTimeout(final Properties properties, final Integer currentValue) {
		return Integer.parseInt(
			getProperty(properties, PROPERTY_KEY_TIMEOUT, currentValue, ArangoDBConstants.DEFAULT_TIMEOUT));
	}

	protected static String loadUser(final Properties properties, final String currentValue) {
		return getProperty(properties, PROPERTY_KEY_USER, currentValue, ArangoDBConstants.DEFAULT_USER);
	}

	protected static String loadPassword(final Properties properties, final String currentValue) {
		return getProperty(properties, PROPERTY_KEY_PASSWORD, currentValue, null);
	}

	protected static Boolean loadUseSsl(final Properties properties, final Boolean currentValue) {
		return Boolean.parseBoolean(
			getProperty(properties, PROPERTY_KEY_USE_SSL, currentValue, ArangoDBConstants.DEFAULT_USE_SSL));
	}

	protected static Integer loadChunkSize(final Properties properties, final Integer currentValue) {
		return Integer.parseInt(getProperty(properties, PROPERTY_KEY_V_STREAM_CHUNK_CONTENT_SIZE, currentValue,
			ArangoDBConstants.CHUNK_DEFAULT_CONTENT_SIZE));
	}

	protected static Integer loadMaxConnections(final Properties properties, final Integer currentValue) {
		return Integer.parseInt(getProperty(properties, PROPERTY_KEY_MAX_CONNECTIONS, currentValue,
			ArangoDBConstants.MAX_CONNECTIONS_VST_DEFAULT));
	}

	protected static Protocol loadProtocol(final Properties properties, final Protocol currentValue) {
		return Protocol.valueOf(
			getProperty(properties, PROPERTY_KEY_PROTOCOL, currentValue, ArangoDBConstants.DEFAULT_NETWORK_PROTOCOL)
					.toUpperCase());
	}

	private static <T> String getProperty(
		final Properties properties,
		final String key,
		final T currentValue,
		final T defaultValue) {
		return properties.getProperty(key,
			currentValue != null ? currentValue.toString() : defaultValue != null ? defaultValue.toString() : null);
	}

	protected Request getRoleRequest() {
		return new Request(ArangoDBConstants.SYSTEM, RequestType.GET, ArangoDBConstants.PATH_API_ROLE);
	}

	protected ResponseDeserializer<ServerRole> getRoleResponseDeserializer() {
		return new ResponseDeserializer<ServerRole>() {
			@Override
			public ServerRole deserialize(final Response response) throws VPackException {
				return util().deserialize(response.getBody().get(ArangoDBConstants.ROLE), ServerRole.class);
			}
		};
	}

	protected Request createDatabaseRequest(final String name) {
		final Request request = new Request(ArangoDBConstants.SYSTEM, RequestType.POST,
				ArangoDBConstants.PATH_API_DATABASE);
		request.setBody(util().serialize(OptionsBuilder.build(new DBCreateOptions(), name)));
		return request;
	}

	protected ResponseDeserializer<Boolean> createDatabaseResponseDeserializer() {
		return new ResponseDeserializer<Boolean>() {
			@Override
			public Boolean deserialize(final Response response) throws VPackException {
				return response.getBody().get(ArangoDBConstants.RESULT).getAsBoolean();
			}
		};
	}

	protected Request getDatabasesRequest(final String database) {
		return new Request(database, RequestType.GET, ArangoDBConstants.PATH_API_DATABASE);
	}

	protected ResponseDeserializer<Collection<String>> getDatabaseResponseDeserializer() {
		return new ResponseDeserializer<Collection<String>>() {
			@Override
			public Collection<String> deserialize(final Response response) throws VPackException {
				final VPackSlice result = response.getBody().get(ArangoDBConstants.RESULT);
				return util().deserialize(result, new Type<Collection<String>>() {
				}.getType());
			}
		};
	}

	protected Request getAccessibleDatabasesForRequest(final String database, final String user) {
		return new Request(database, RequestType.GET,
				executor.createPath(ArangoDBConstants.PATH_API_USER, user, ArangoDBConstants.DATABASE));
	}

	protected ResponseDeserializer<Collection<String>> getAccessibleDatabasesForResponseDeserializer() {
		return new ResponseDeserializer<Collection<String>>() {
			@Override
			public Collection<String> deserialize(final Response response) throws VPackException {
				final VPackSlice result = response.getBody().get(ArangoDBConstants.RESULT);
				final Collection<String> dbs = new ArrayList<String>();
				for (final Iterator<Entry<String, VPackSlice>> iterator = result.objectIterator(); iterator
						.hasNext();) {
					dbs.add(iterator.next().getKey());
				}
				return dbs;
			}
		};
	}

	protected Request createUserRequest(
		final String database,
		final String user,
		final String passwd,
		final UserCreateOptions options) {
		final Request request;
		request = new Request(database, RequestType.POST, ArangoDBConstants.PATH_API_USER);
		request.setBody(
			util().serialize(OptionsBuilder.build(options != null ? options : new UserCreateOptions(), user, passwd)));
		return request;
	}

	protected Request deleteUserRequest(final String database, final String user) {
		return new Request(database, RequestType.DELETE, executor.createPath(ArangoDBConstants.PATH_API_USER, user));
	}

	protected Request getUsersRequest(final String database) {
		return new Request(database, RequestType.GET, ArangoDBConstants.PATH_API_USER);
	}

	protected Request getUserRequest(final String database, final String user) {
		return new Request(database, RequestType.GET, executor.createPath(ArangoDBConstants.PATH_API_USER, user));
	}

	protected ResponseDeserializer<Collection<UserEntity>> getUsersResponseDeserializer() {
		return new ResponseDeserializer<Collection<UserEntity>>() {
			@Override
			public Collection<UserEntity> deserialize(final Response response) throws VPackException {
				final VPackSlice result = response.getBody().get(ArangoDBConstants.RESULT);
				return util().deserialize(result, new Type<Collection<UserEntity>>() {
				}.getType());
			}
		};
	}

	protected Request updateUserRequest(final String database, final String user, final UserUpdateOptions options) {
		final Request request;
		request = new Request(database, RequestType.PATCH, executor.createPath(ArangoDBConstants.PATH_API_USER, user));
		request.setBody(util().serialize(options != null ? options : new UserUpdateOptions()));
		return request;
	}

	protected Request replaceUserRequest(final String database, final String user, final UserUpdateOptions options) {
		final Request request;
		request = new Request(database, RequestType.PUT, executor.createPath(ArangoDBConstants.PATH_API_USER, user));
		request.setBody(util().serialize(options != null ? options : new UserUpdateOptions()));
		return request;
	}

	protected Request updateUserDefaultDatabaseAccessRequest(final String user, final Permissions permissions) {
		return new Request(ArangoDBConstants.SYSTEM, RequestType.PUT,
				executor.createPath(ArangoDBConstants.PATH_API_USER, user, ArangoDBConstants.DATABASE, "*"))
						.setBody(util().serialize(OptionsBuilder.build(new UserAccessOptions(), permissions)));
	}

	protected Request updateUserDefaultCollectionAccessRequest(final String user, final Permissions permissions) {
		return new Request(ArangoDBConstants.SYSTEM, RequestType.PUT,
				executor.createPath(ArangoDBConstants.PATH_API_USER, user, ArangoDBConstants.DATABASE, "*", "*"))
						.setBody(util().serialize(OptionsBuilder.build(new UserAccessOptions(), permissions)));
	}

	protected Request getLogsRequest(final LogOptions options) {
		final LogOptions params = options != null ? options : new LogOptions();
		return new Request(ArangoDBConstants.SYSTEM, RequestType.GET, ArangoDBConstants.PATH_API_ADMIN_LOG)
				.putQueryParam(LogOptions.PROPERTY_UPTO, params.getUpto())
				.putQueryParam(LogOptions.PROPERTY_LEVEL, params.getLevel())
				.putQueryParam(LogOptions.PROPERTY_START, params.getStart())
				.putQueryParam(LogOptions.PROPERTY_SIZE, params.getSize())
				.putQueryParam(LogOptions.PROPERTY_OFFSET, params.getOffset())
				.putQueryParam(LogOptions.PROPERTY_SEARCH, params.getSearch())
				.putQueryParam(LogOptions.PROPERTY_SORT, params.getSort());
	}

	protected Request getLogLevelRequest() {
		return new Request(ArangoDBConstants.SYSTEM, RequestType.GET, ArangoDBConstants.PATH_API_ADMIN_LOG_LEVEL);
	}

	protected Request setLogLevelRequest(final LogLevelEntity entity) {
		return new Request(ArangoDBConstants.SYSTEM, RequestType.PUT, ArangoDBConstants.PATH_API_ADMIN_LOG_LEVEL)
				.setBody(util().serialize(entity));
	}

}
