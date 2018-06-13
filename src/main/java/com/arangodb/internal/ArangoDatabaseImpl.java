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

import java.util.Collection;
import java.util.Map;

import com.arangodb.ArangoCollection;
import com.arangodb.ArangoCursor;
import com.arangodb.ArangoDBException;
import com.arangodb.ArangoDatabase;
import com.arangodb.ArangoGraph;
import com.arangodb.entity.AqlExecutionExplainEntity;
import com.arangodb.entity.AqlFunctionEntity;
import com.arangodb.entity.AqlParseEntity;
import com.arangodb.entity.ArangoDBVersion;
import com.arangodb.entity.CollectionEntity;
import com.arangodb.entity.CursorEntity;
import com.arangodb.entity.DatabaseEntity;
import com.arangodb.entity.EdgeDefinition;
import com.arangodb.entity.GraphEntity;
import com.arangodb.entity.IndexEntity;
import com.arangodb.entity.Permissions;
import com.arangodb.entity.QueryCachePropertiesEntity;
import com.arangodb.entity.QueryEntity;
import com.arangodb.entity.QueryTrackingPropertiesEntity;
import com.arangodb.entity.TraversalEntity;
import com.arangodb.internal.net.CommunicationProtocol;
import com.arangodb.internal.net.HostHandle;
import com.arangodb.internal.velocystream.internal.ConnectionSync;
import com.arangodb.model.AqlFunctionCreateOptions;
import com.arangodb.model.AqlFunctionDeleteOptions;
import com.arangodb.model.AqlFunctionGetOptions;
import com.arangodb.model.AqlQueryExplainOptions;
import com.arangodb.model.AqlQueryOptions;
import com.arangodb.model.CollectionCreateOptions;
import com.arangodb.model.CollectionsReadOptions;
import com.arangodb.model.DocumentReadOptions;
import com.arangodb.model.GraphCreateOptions;
import com.arangodb.model.TransactionOptions;
import com.arangodb.model.TraversalOptions;
import com.arangodb.util.ArangoCursorInitializer;
import com.arangodb.util.ArangoSerialization;
import com.arangodb.velocypack.Type;
import com.arangodb.velocystream.Request;
import com.arangodb.velocystream.Response;

/**
 * @author Mark Vollmary
 *
 */
public class ArangoDatabaseImpl extends
		InternalArangoDatabase<ArangoDBImpl, ArangoExecutorSync, Response, ConnectionSync> implements ArangoDatabase {

	private ArangoCursorInitializer cursorInitializer;

	protected ArangoDatabaseImpl(final ArangoDBImpl arangoDB, final String name) {
		super(arangoDB, arangoDB.executor(), arangoDB.util(), name);
	}

	protected ArangoDatabaseImpl(final CommunicationProtocol protocol, final ArangoSerialization util,
		final DocumentCache documentCache, final String name) {
		super(null, new ArangoExecutorSync(protocol, util, documentCache), util, name);
	}

	@Override
	public ArangoDBVersion getVersion() throws ArangoDBException {
		return executor.execute(getVersionRequest(), ArangoDBVersion.class);
	}

	@Override
	public boolean exists() throws ArangoDBException {
		try {
			getInfo();
			return true;
		} catch (final ArangoDBException e) {
			return false;
		}
	}

	@Override
	public Collection<String> getAccessibleDatabases() throws ArangoDBException {
		return executor.execute(getAccessibleDatabasesRequest(), getDatabaseResponseDeserializer());
	}

	@Override
	public ArangoCollection collection(final String name) {
		return new ArangoCollectionImpl(this, name);
	}

	@Override
	public CollectionEntity createCollection(final String name) throws ArangoDBException {
		return executor.execute(createCollectionRequest(name, new CollectionCreateOptions()), CollectionEntity.class);
	}

	@Override
	public CollectionEntity createCollection(final String name, final CollectionCreateOptions options)
			throws ArangoDBException {
		return executor.execute(createCollectionRequest(name, options), CollectionEntity.class);
	}

	@Override
	public Collection<CollectionEntity> getCollections() throws ArangoDBException {
		return executor.execute(getCollectionsRequest(new CollectionsReadOptions()),
			getCollectionsResponseDeserializer());
	}

	@Override
	public Collection<CollectionEntity> getCollections(final CollectionsReadOptions options) throws ArangoDBException {
		return executor.execute(getCollectionsRequest(options), getCollectionsResponseDeserializer());
	}

	@Override
	public IndexEntity getIndex(final String id) throws ArangoDBException {
		executor.validateIndexId(id);
		final String[] split = id.split("/");
		return collection(split[0]).getIndex(split[1]);
	}

	@Override
	public String deleteIndex(final String id) throws ArangoDBException {
		executor.validateIndexId(id);
		final String[] split = id.split("/");
		return collection(split[0]).deleteIndex(split[1]);
	}

	@Override
	public Boolean create() throws ArangoDBException {
		return arango().createDatabase(name());
	}

	@Override
	public Boolean drop() throws ArangoDBException {
		return executor.execute(dropRequest(), createDropResponseDeserializer());
	}

	@Override
	public void grantAccess(final String user, final Permissions permissions) throws ArangoDBException {
		executor.execute(grantAccessRequest(user, permissions), Void.class);
	}

	@Override
	public void grantAccess(final String user) throws ArangoDBException {
		executor.execute(grantAccessRequest(user, Permissions.RW), Void.class);
	}

	@Override
	public void revokeAccess(final String user) throws ArangoDBException {
		executor.execute(grantAccessRequest(user, Permissions.NONE), Void.class);
	}

	@Override
	public void resetAccess(final String user) throws ArangoDBException {
		executor.execute(resetAccessRequest(user), Void.class);
	}

	@Override
	public void grantDefaultCollectionAccess(final String user, final Permissions permissions)
			throws ArangoDBException {
		executor.execute(updateUserDefaultCollectionAccessRequest(user, permissions), Void.class);
	}

	@Override
	public Permissions getPermissions(final String user) throws ArangoDBException {
		return executor.execute(getPermissionsRequest(user), getPermissionsResponseDeserialzer());
	}

	@Override
	public <T> ArangoCursor<T> query(
		final String query,
		final Map<String, Object> bindVars,
		final AqlQueryOptions options,
		final Class<T> type) throws ArangoDBException {
		final Request request = queryRequest(query, bindVars, options);
		final HostHandle hostHandle = new HostHandle();
		final CursorEntity result = executor.execute(request, CursorEntity.class, hostHandle);
		return createCursor(result, type, hostHandle);
	}

	@Override
	public <T> ArangoCursor<T> cursor(final String cursorId, final Class<T> type) throws ArangoDBException {
		final HostHandle hostHandle = new HostHandle();
		final CursorEntity result = executor.execute(queryNextRequest(cursorId), CursorEntity.class, hostHandle);
		return createCursor(result, type, hostHandle);
	}

	private <T> ArangoCursor<T> createCursor(
		final CursorEntity result,
		final Class<T> type,
		final HostHandle hostHandle) {
		final ArangoCursorExecute execute = new ArangoCursorExecute() {
			@Override
			public CursorEntity next(final String id) {
				return executor.execute(queryNextRequest(id), CursorEntity.class, hostHandle);
			}

			@Override
			public void close(final String id) {
				executor.execute(queryCloseRequest(id), Void.class, hostHandle);
			}
		};
		return cursorInitializer != null ? cursorInitializer.createInstance(this, execute, type, result)
				: new ArangoCursorImpl<T>(this, execute, type, result);
	}

	@Override
	public AqlExecutionExplainEntity explainQuery(
		final String query,
		final Map<String, Object> bindVars,
		final AqlQueryExplainOptions options) throws ArangoDBException {
		return executor.execute(explainQueryRequest(query, bindVars, options), AqlExecutionExplainEntity.class);
	}

	@Override
	public AqlParseEntity parseQuery(final String query) throws ArangoDBException {
		return executor.execute(parseQueryRequest(query), AqlParseEntity.class);
	}

	@Override
	public void clearQueryCache() throws ArangoDBException {
		executor.execute(clearQueryCacheRequest(), Void.class);
	}

	@Override
	public QueryCachePropertiesEntity getQueryCacheProperties() throws ArangoDBException {
		return executor.execute(getQueryCachePropertiesRequest(), QueryCachePropertiesEntity.class);
	}

	@Override
	public QueryCachePropertiesEntity setQueryCacheProperties(final QueryCachePropertiesEntity properties)
			throws ArangoDBException {
		return executor.execute(setQueryCachePropertiesRequest(properties), QueryCachePropertiesEntity.class);
	}

	@Override
	public QueryTrackingPropertiesEntity getQueryTrackingProperties() throws ArangoDBException {
		return executor.execute(getQueryTrackingPropertiesRequest(), QueryTrackingPropertiesEntity.class);
	}

	@Override
	public QueryTrackingPropertiesEntity setQueryTrackingProperties(final QueryTrackingPropertiesEntity properties)
			throws ArangoDBException {
		return executor.execute(setQueryTrackingPropertiesRequest(properties), QueryTrackingPropertiesEntity.class);
	}

	@Override
	public Collection<QueryEntity> getCurrentlyRunningQueries() throws ArangoDBException {
		return executor.execute(getCurrentlyRunningQueriesRequest(), new Type<Collection<QueryEntity>>() {
		}.getType());
	}

	@Override
	public Collection<QueryEntity> getSlowQueries() throws ArangoDBException {
		return executor.execute(getSlowQueriesRequest(), new Type<Collection<QueryEntity>>() {
		}.getType());
	}

	@Override
	public void clearSlowQueries() throws ArangoDBException {
		executor.execute(clearSlowQueriesRequest(), Void.class);
	}

	@Override
	public void killQuery(final String id) throws ArangoDBException {
		executor.execute(killQueryRequest(id), Void.class);
	}

	@Override
	public void createAqlFunction(final String name, final String code, final AqlFunctionCreateOptions options)
			throws ArangoDBException {
		executor.execute(createAqlFunctionRequest(name, code, options), Void.class);
	}

	@Override
	public Integer deleteAqlFunction(final String name, final AqlFunctionDeleteOptions options)
			throws ArangoDBException {
		return executor.execute(deleteAqlFunctionRequest(name, options), deleteAqlFunctionResponseDeserializer());
	}

	@Override
	public Collection<AqlFunctionEntity> getAqlFunctions(final AqlFunctionGetOptions options) throws ArangoDBException {
		return executor.execute(getAqlFunctionsRequest(options), getAqlFunctionsResponseDeserializer());
	}

	@Override
	public ArangoGraph graph(final String name) {
		return new ArangoGraphImpl(this, name);
	}

	@Override
	public GraphEntity createGraph(final String name, final Collection<EdgeDefinition> edgeDefinitions)
			throws ArangoDBException {
		return executor.execute(createGraphRequest(name, edgeDefinitions, new GraphCreateOptions()),
			createGraphResponseDeserializer());
	}

	@Override
	public GraphEntity createGraph(
		final String name,
		final Collection<EdgeDefinition> edgeDefinitions,
		final GraphCreateOptions options) throws ArangoDBException {
		return executor.execute(createGraphRequest(name, edgeDefinitions, options), createGraphResponseDeserializer());
	}

	@Override
	public Collection<GraphEntity> getGraphs() throws ArangoDBException {
		return executor.execute(getGraphsRequest(), getGraphsResponseDeserializer());
	}

	@Override
	public <T> T transaction(final String action, final Class<T> type, final TransactionOptions options)
			throws ArangoDBException {
		return executor.execute(transactionRequest(action, options), transactionResponseDeserializer(type));
	}

	@Override
	public DatabaseEntity getInfo() throws ArangoDBException {
		return executor.execute(getInfoRequest(), getInfoResponseDeserializer());
	}

	@Override
	public <V, E> TraversalEntity<V, E> executeTraversal(
		final Class<V> vertexClass,
		final Class<E> edgeClass,
		final TraversalOptions options) throws ArangoDBException {
		final Request request = executeTraversalRequest(options);
		return executor.execute(request, executeTraversalResponseDeserializer(vertexClass, edgeClass));
	}

	@Override
	public <T> T getDocument(final String id, final Class<T> type) throws ArangoDBException {
		executor.validateDocumentId(id);
		final String[] split = id.split("/");
		return collection(split[0]).getDocument(split[1], type);
	}

	@Override
	public <T> T getDocument(final String id, final Class<T> type, final DocumentReadOptions options)
			throws ArangoDBException {
		executor.validateDocumentId(id);
		final String[] split = id.split("/");
		return collection(split[0]).getDocument(split[1], type, options);
	}

	@Override
	public void reloadRouting() throws ArangoDBException {
		executor.execute(reloadRoutingRequest(), Void.class);
	}

	protected ArangoDatabaseImpl setCursorInitializer(final ArangoCursorInitializer cursorInitializer) {
		this.cursorInitializer = cursorInitializer;
		return this;
	}

}
