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
import java.util.Map;

import com.arangodb.entity.CollectionEntity;
import com.arangodb.entity.DatabaseEntity;
import com.arangodb.entity.EdgeDefinition;
import com.arangodb.entity.GraphEntity;
import com.arangodb.entity.PathEntity;
import com.arangodb.entity.Permissions;
import com.arangodb.entity.QueryCachePropertiesEntity;
import com.arangodb.entity.QueryTrackingPropertiesEntity;
import com.arangodb.entity.TraversalEntity;
import com.arangodb.internal.ArangoExecutor.ResponseDeserializer;
import com.arangodb.internal.velocystream.internal.Connection;
import com.arangodb.model.AqlFunctionCreateOptions;
import com.arangodb.model.AqlFunctionDeleteOptions;
import com.arangodb.model.AqlFunctionGetOptions;
import com.arangodb.model.AqlQueryExplainOptions;
import com.arangodb.model.AqlQueryOptions;
import com.arangodb.model.AqlQueryParseOptions;
import com.arangodb.model.CollectionCreateOptions;
import com.arangodb.model.CollectionsReadOptions;
import com.arangodb.model.GraphCreateOptions;
import com.arangodb.model.OptionsBuilder;
import com.arangodb.model.TransactionOptions;
import com.arangodb.model.TraversalOptions;
import com.arangodb.model.UserAccessOptions;
import com.arangodb.util.ArangoSerialization;
import com.arangodb.velocypack.Type;
import com.arangodb.velocypack.VPackSlice;
import com.arangodb.velocypack.exception.VPackException;
import com.arangodb.velocystream.Request;
import com.arangodb.velocystream.RequestType;
import com.arangodb.velocystream.Response;

/**
 * @author Mark Vollmary
 *
 */
public class InternalArangoDatabase<A extends InternalArangoDB<E, R, C>, E extends ArangoExecutor, R, C extends Connection>
		extends ArangoExecuteable<E, R, C> {

	private final String name;
	private final A arango;

	public InternalArangoDatabase(final A arango, final E executor, final ArangoSerialization util, final String name) {
		super(executor, util);
		this.arango = arango;
		this.name = name;
	}

	public A arango() {
		return arango;
	}

	public String name() {
		return name;
	}

	protected ResponseDeserializer<Collection<String>> getDatabaseResponseDeserializer() {
		return arango.getDatabaseResponseDeserializer();
	}

	protected Request getAccessibleDatabasesRequest() {
		return new Request(name, RequestType.GET,
				executor.createPath(ArangoDBConstants.PATH_API_DATABASE, ArangoDBConstants.USER));
	}

	protected Request getVersionRequest() {
		return new Request(name, RequestType.GET, ArangoDBConstants.PATH_API_VERSION);
	}

	protected Request createCollectionRequest(final String name, final CollectionCreateOptions options) {
		return new Request(name(), RequestType.POST, ArangoDBConstants.PATH_API_COLLECTION).setBody(
			util().serialize(OptionsBuilder.build(options != null ? options : new CollectionCreateOptions(), name)));
	}

	protected Request getCollectionsRequest(final CollectionsReadOptions options) {
		final Request request;
		request = new Request(name(), RequestType.GET, ArangoDBConstants.PATH_API_COLLECTION);
		final CollectionsReadOptions params = (options != null ? options : new CollectionsReadOptions());
		request.putQueryParam(ArangoDBConstants.EXCLUDE_SYSTEM, params.getExcludeSystem());
		return request;
	}

	protected ResponseDeserializer<Collection<CollectionEntity>> getCollectionsResponseDeserializer() {
		return new ResponseDeserializer<Collection<CollectionEntity>>() {
			@Override
			public Collection<CollectionEntity> deserialize(final Response response) throws VPackException {
				final VPackSlice result = response.getBody().get(ArangoDBConstants.RESULT);
				return util().deserialize(result, new Type<Collection<CollectionEntity>>() {
				}.getType());
			}
		};
	}

	protected Request dropRequest() {
		return new Request(ArangoDBConstants.SYSTEM, RequestType.DELETE,
				executor.createPath(ArangoDBConstants.PATH_API_DATABASE, name));
	}

	protected ResponseDeserializer<Boolean> createDropResponseDeserializer() {
		return new ResponseDeserializer<Boolean>() {
			@Override
			public Boolean deserialize(final Response response) throws VPackException {
				return response.getBody().get(ArangoDBConstants.RESULT).getAsBoolean();
			}
		};
	}

	protected Request grantAccessRequest(final String user, final Permissions permissions) {
		return new Request(ArangoDBConstants.SYSTEM, RequestType.PUT,
				executor.createPath(ArangoDBConstants.PATH_API_USER, user, ArangoDBConstants.DATABASE, name)).setBody(
					util().serialize(OptionsBuilder.build(new UserAccessOptions(), permissions.toString())));
	}

	protected Request resetAccessRequest(final String user) {
		return new Request(ArangoDBConstants.SYSTEM, RequestType.DELETE,
				executor.createPath(ArangoDBConstants.PATH_API_USER, user, ArangoDBConstants.DATABASE, name));
	}

	protected Request queryRequest(
		final String query,
		final Map<String, Object> bindVars,
		final AqlQueryOptions options) {
		return new Request(name, RequestType.POST, ArangoDBConstants.PATH_API_CURSOR).setBody(
			util().serialize(OptionsBuilder.build(options != null ? options : new AqlQueryOptions(), query, bindVars)));
	}

	protected Request queryNextRequest(final String id) {
		return new Request(name, RequestType.PUT, executor.createPath(ArangoDBConstants.PATH_API_CURSOR, id));
	}

	protected Request queryCloseRequest(final String id) {
		return new Request(name, RequestType.DELETE, executor.createPath(ArangoDBConstants.PATH_API_CURSOR, id));
	}

	protected Request explainQueryRequest(
		final String query,
		final Map<String, Object> bindVars,
		final AqlQueryExplainOptions options) {
		return new Request(name, RequestType.POST, ArangoDBConstants.PATH_API_EXPLAIN).setBody(util().serialize(
			OptionsBuilder.build(options != null ? options : new AqlQueryExplainOptions(), query, bindVars)));
	}

	protected Request parseQueryRequest(final String query) {
		return new Request(name, RequestType.POST, ArangoDBConstants.PATH_API_QUERY)
				.setBody(util().serialize(OptionsBuilder.build(new AqlQueryParseOptions(), query)));
	}

	protected Request clearQueryCacheRequest() {
		return new Request(name, RequestType.DELETE, ArangoDBConstants.PATH_API_QUERY_CACHE);
	}

	protected Request getQueryCachePropertiesRequest() {
		return new Request(name, RequestType.GET, ArangoDBConstants.PATH_API_QUERY_CACHE_PROPERTIES);
	}

	protected Request setQueryCachePropertiesRequest(final QueryCachePropertiesEntity properties) {
		return new Request(name, RequestType.PUT, ArangoDBConstants.PATH_API_QUERY_CACHE_PROPERTIES)
				.setBody(util().serialize(properties));
	}

	protected Request getQueryTrackingPropertiesRequest() {
		return new Request(name, RequestType.GET, ArangoDBConstants.PATH_API_QUERY_PROPERTIES);
	}

	protected Request setQueryTrackingPropertiesRequest(final QueryTrackingPropertiesEntity properties) {
		return new Request(name, RequestType.PUT, ArangoDBConstants.PATH_API_QUERY_PROPERTIES)
				.setBody(util().serialize(properties));
	}

	protected Request getCurrentlyRunningQueriesRequest() {
		return new Request(name, RequestType.GET, ArangoDBConstants.PATH_API_QUERY_CURRENT);
	}

	protected Request getSlowQueriesRequest() {
		return new Request(name, RequestType.GET, ArangoDBConstants.PATH_API_QUERY_SLOW);
	}

	protected Request clearSlowQueriesRequest() {
		return new Request(name, RequestType.DELETE, ArangoDBConstants.PATH_API_QUERY_SLOW);
	}

	protected Request killQueryRequest(final String id) {
		return new Request(name, RequestType.DELETE, executor.createPath(ArangoDBConstants.PATH_API_QUERY, id));
	}

	protected Request createAqlFunctionRequest(
		final String name,
		final String code,
		final AqlFunctionCreateOptions options) {
		return new Request(name(), RequestType.POST, ArangoDBConstants.PATH_API_AQLFUNCTION).setBody(util().serialize(
			OptionsBuilder.build(options != null ? options : new AqlFunctionCreateOptions(), name, code)));
	}

	protected Request deleteAqlFunctionRequest(final String name, final AqlFunctionDeleteOptions options) {
		final Request request = new Request(name(), RequestType.DELETE,
				executor.createPath(ArangoDBConstants.PATH_API_AQLFUNCTION, name));
		final AqlFunctionDeleteOptions params = options != null ? options : new AqlFunctionDeleteOptions();
		request.putQueryParam(ArangoDBConstants.GROUP, params.getGroup());
		return request;
	}

	protected Request getAqlFunctionsRequest(final AqlFunctionGetOptions options) {
		final Request request = new Request(name(), RequestType.GET, ArangoDBConstants.PATH_API_AQLFUNCTION);
		final AqlFunctionGetOptions params = options != null ? options : new AqlFunctionGetOptions();
		request.putQueryParam(ArangoDBConstants.NAMESPACE, params.getNamespace());
		return request;
	}

	protected Request createGraphRequest(
		final String name,
		final Collection<EdgeDefinition> edgeDefinitions,
		final GraphCreateOptions options) {
		return new Request(name(), RequestType.POST, ArangoDBConstants.PATH_API_GHARIAL).setBody(util().serialize(
			OptionsBuilder.build(options != null ? options : new GraphCreateOptions(), name, edgeDefinitions)));
	}

	protected ResponseDeserializer<GraphEntity> createGraphResponseDeserializer() {
		return new ResponseDeserializer<GraphEntity>() {
			@Override
			public GraphEntity deserialize(final Response response) throws VPackException {
				return util().deserialize(response.getBody().get(ArangoDBConstants.GRAPH), GraphEntity.class);
			}
		};
	}

	protected Request getGraphsRequest() {
		return new Request(name, RequestType.GET, ArangoDBConstants.PATH_API_GHARIAL);
	}

	protected ResponseDeserializer<Collection<GraphEntity>> getGraphsResponseDeserializer() {
		return new ResponseDeserializer<Collection<GraphEntity>>() {
			@Override
			public Collection<GraphEntity> deserialize(final Response response) throws VPackException {
				return util().deserialize(response.getBody().get(ArangoDBConstants.GRAPHS),
					new Type<Collection<GraphEntity>>() {
					}.getType());
			}
		};
	}

	protected Request transactionRequest(final String action, final TransactionOptions options) {
		return new Request(name, RequestType.POST, ArangoDBConstants.PATH_API_TRANSACTION).setBody(
			util().serialize(OptionsBuilder.build(options != null ? options : new TransactionOptions(), action)));
	}

	protected <T> ResponseDeserializer<T> transactionResponseDeserializer(final Class<T> type) {
		return new ResponseDeserializer<T>() {
			@Override
			public T deserialize(final Response response) throws VPackException {
				final VPackSlice body = response.getBody();
				if (body != null) {
					final VPackSlice result = body.get(ArangoDBConstants.RESULT);
					if (!result.isNone()) {
						return util().deserialize(result, type);
					}
				}
				return null;
			}
		};
	}

	protected Request getInfoRequest() {
		return new Request(name, RequestType.GET,
				executor.createPath(ArangoDBConstants.PATH_API_DATABASE, ArangoDBConstants.CURRENT));
	}

	protected ResponseDeserializer<DatabaseEntity> getInfoResponseDeserializer() {
		return new ResponseDeserializer<DatabaseEntity>() {
			@Override
			public DatabaseEntity deserialize(final Response response) throws VPackException {
				return util().deserialize(response.getBody().get(ArangoDBConstants.RESULT), DatabaseEntity.class);
			}
		};
	}

	protected Request executeTraversalRequest(final TraversalOptions options) {
		return new Request(name, RequestType.POST, ArangoDBConstants.PATH_API_TRAVERSAL)
				.setBody(util().serialize(options != null ? options : new TransactionOptions()));
	}

	@SuppressWarnings("hiding")
	protected <E, V> ResponseDeserializer<TraversalEntity<V, E>> executeTraversalResponseDeserializer(
		final Class<V> vertexClass,
		final Class<E> edgeClass) {
		return new ResponseDeserializer<TraversalEntity<V, E>>() {
			@Override
			public TraversalEntity<V, E> deserialize(final Response response) throws VPackException {
				final TraversalEntity<V, E> result = new TraversalEntity<V, E>();
				final VPackSlice visited = response.getBody().get(ArangoDBConstants.RESULT)
						.get(ArangoDBConstants.VISITED);
				result.setVertices(deserializeVertices(vertexClass, visited));

				final Collection<PathEntity<V, E>> paths = new ArrayList<PathEntity<V, E>>();
				for (final Iterator<VPackSlice> iterator = visited.get("paths").arrayIterator(); iterator.hasNext();) {
					final PathEntity<V, E> path = new PathEntity<V, E>();
					final VPackSlice next = iterator.next();
					path.setEdges(deserializeEdges(edgeClass, next));
					path.setVertices(deserializeVertices(vertexClass, next));
					paths.add(path);
				}
				result.setPaths(paths);
				return result;
			}
		};
	}

	@SuppressWarnings("unchecked")
	protected <V> Collection<V> deserializeVertices(final Class<V> vertexClass, final VPackSlice vpack)
			throws VPackException {
		final Collection<V> vertices = new ArrayList<V>();
		for (final Iterator<VPackSlice> iterator = vpack.get(ArangoDBConstants.VERTICES).arrayIterator(); iterator
				.hasNext();) {
			vertices.add((V) util().deserialize(iterator.next(), vertexClass));
		}
		return vertices;
	}

	@SuppressWarnings({ "hiding", "unchecked" })
	protected <E> Collection<E> deserializeEdges(final Class<E> edgeClass, final VPackSlice next)
			throws VPackException {
		final Collection<E> edges = new ArrayList<E>();
		for (final Iterator<VPackSlice> iteratorEdge = next.get(ArangoDBConstants.EDGES).arrayIterator(); iteratorEdge
				.hasNext();) {
			edges.add((E) util().deserialize(iteratorEdge.next(), edgeClass));
		}
		return edges;
	}

	protected Request reloadRoutingRequest() {
		return new Request(name, RequestType.POST, ArangoDBConstants.PATH_API_ADMIN_ROUTING_RELOAD);
	}
}
