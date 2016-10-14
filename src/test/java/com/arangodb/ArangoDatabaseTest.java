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

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Ignore;
import org.junit.Test;

import com.arangodb.entity.AqlExecutionExplainEntity;
import com.arangodb.entity.AqlExecutionExplainEntity.ExecutionNode;
import com.arangodb.entity.AqlExecutionExplainEntity.ExecutionPlan;
import com.arangodb.entity.AqlFunctionEntity;
import com.arangodb.entity.AqlParseEntity;
import com.arangodb.entity.AqlParseEntity.AstNode;
import com.arangodb.entity.BaseDocument;
import com.arangodb.entity.BaseEdgeDocument;
import com.arangodb.entity.CollectionEntity;
import com.arangodb.entity.CollectionType;
import com.arangodb.entity.DatabaseEntity;
import com.arangodb.entity.GraphEntity;
import com.arangodb.entity.IndexEntity;
import com.arangodb.entity.PathEntity;
import com.arangodb.entity.QueryCachePropertiesEntity;
import com.arangodb.entity.QueryCachePropertiesEntity.CacheMode;
import com.arangodb.entity.QueryEntity;
import com.arangodb.entity.QueryTrackingPropertiesEntity;
import com.arangodb.entity.TraversalEntity;
import com.arangodb.model.AqlFunctionDeleteOptions;
import com.arangodb.model.AqlQueryOptions;
import com.arangodb.model.CollectionCreateOptions;
import com.arangodb.model.CollectionsReadOptions;
import com.arangodb.model.TransactionOptions;
import com.arangodb.model.TraversalOptions;
import com.arangodb.model.TraversalOptions.Direction;
import com.arangodb.velocypack.VPackBuilder;
import com.arangodb.velocypack.VPackSlice;
import com.arangodb.velocypack.exception.VPackException;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class ArangoDatabaseTest extends BaseTest {

	private static final String COLLECTION_NAME = "db_test";
	private static final String GRAPH_NAME = "graph_test";

	@Test
	public void createCollection() {
		try {
			final CollectionEntity result = db.createCollection(COLLECTION_NAME, null);
			assertThat(result, is(notNullValue()));
			assertThat(result.getId(), is(notNullValue()));
		} finally {
			db.collection(COLLECTION_NAME).drop();
		}
	}

	@Test
	public void deleteCollection() {
		db.createCollection(COLLECTION_NAME, null);
		db.collection(COLLECTION_NAME).drop();
		try {
			db.collection(COLLECTION_NAME).getInfo();
			fail();
		} catch (final ArangoDBException e) {
		}
	}

	@Test
	public void getIndex() {
		try {
			db.createCollection(COLLECTION_NAME, null);
			final Collection<String> fields = new ArrayList<String>();
			fields.add("a");
			final IndexEntity createResult = db.collection(COLLECTION_NAME).createHashIndex(fields, null);
			final IndexEntity readResult = db.getIndex(createResult.getId());
			assertThat(readResult.getId(), is(createResult.getId()));
			assertThat(readResult.getType(), is(createResult.getType()));
		} finally {
			db.collection(COLLECTION_NAME).drop();
		}
	}

	@Test
	public void deleteIndex() {
		try {
			db.createCollection(COLLECTION_NAME, null);
			final Collection<String> fields = new ArrayList<String>();
			fields.add("a");
			final IndexEntity createResult = db.collection(COLLECTION_NAME).createHashIndex(fields, null);
			final String id = db.deleteIndex(createResult.getId());
			assertThat(id, is(createResult.getId()));
			try {
				db.getIndex(id);
				fail();
			} catch (final ArangoDBException e) {
			}
		} finally {
			db.collection(COLLECTION_NAME).drop();
		}
	}

	@Test
	public void getCollections() {
		try {
			final Collection<CollectionEntity> systemCollections = db.getCollections(null);
			db.createCollection(COLLECTION_NAME + "1", null);
			db.createCollection(COLLECTION_NAME + "2", null);
			final Collection<CollectionEntity> collections = db.getCollections(null);
			assertThat(collections.size(), is(2 + systemCollections.size()));
			assertThat(collections, is(notNullValue()));
		} finally {
			db.collection(COLLECTION_NAME + "1").drop();
			db.collection(COLLECTION_NAME + "2").drop();
		}
	}

	@Test
	public void getCollectionsExcludeSystem() {
		try {
			final CollectionsReadOptions options = new CollectionsReadOptions().excludeSystem(true);
			final Collection<CollectionEntity> systemCollections = db.getCollections(options);
			assertThat(systemCollections.size(), is(0));
			db.createCollection(COLLECTION_NAME + "1", null);
			db.createCollection(COLLECTION_NAME + "2", null);
			final Collection<CollectionEntity> collections = db.getCollections(options);
			assertThat(collections.size(), is(2));
			assertThat(collections, is(notNullValue()));
		} finally {
			db.collection(COLLECTION_NAME + "1").drop();
			db.collection(COLLECTION_NAME + "2").drop();
		}
	}

	@Test
	public void grantAccess() {
		try {
			arangoDB.createUser("user1", "1234", null);
			db.grantAccess("user1");
		} finally {
			arangoDB.deleteUser("user1");
		}
	}

	@Test(expected = ArangoDBException.class)
	public void grantAccessUserNotFound() {
		db.grantAccess("user1");
	}

	@Test
	public void revokeAccess() {
		try {
			arangoDB.createUser("user1", "1234", null);
			db.revokeAccess("user1");
		} finally {
			arangoDB.deleteUser("user1");
		}
	}

	@Test(expected = ArangoDBException.class)
	public void revokeAccessUserNotFound() {
		db.revokeAccess("user1");
	}

	@Test
	public void query() {
		try {
			db.createCollection(COLLECTION_NAME, null);
			for (int i = 0; i < 10; i++) {
				db.collection(COLLECTION_NAME).insertDocument(new BaseDocument(), null);
			}
			final ArangoCursor<String> cursor = db.query("for i in db_test return i._id", null, null, String.class);
			assertThat(cursor, is(notNullValue()));
			for (int i = 0; i < 10; i++, cursor.next()) {
				assertThat(cursor.hasNext(), is(i != 10));
			}
		} finally {
			db.collection(COLLECTION_NAME).drop();
		}
	}

	@Test
	public void queryForEach() {
		try {
			db.createCollection(COLLECTION_NAME, null);
			for (int i = 0; i < 10; i++) {
				db.collection(COLLECTION_NAME).insertDocument(new BaseDocument(), null);
			}
			final ArangoCursor<String> cursor = db.query("for i in db_test return i._id", null, null, String.class);
			assertThat(cursor, is(notNullValue()));
			final AtomicInteger i = new AtomicInteger(0);
			for (; cursor.hasNext(); cursor.next()) {
				i.incrementAndGet();
			}
			assertThat(i.get(), is(10));
		} finally {
			db.collection(COLLECTION_NAME).drop();
		}
	}

	@Test
	public void queryStream() {
		try {
			db.createCollection(COLLECTION_NAME, null);
			for (int i = 0; i < 10; i++) {
				db.collection(COLLECTION_NAME).insertDocument(new BaseDocument(), null);
			}
			final ArangoCursor<String> cursor = db.query("for i in db_test return i._id", null, null, String.class);
			assertThat(cursor, is(notNullValue()));
			final AtomicInteger i = new AtomicInteger(0);
			for (; cursor.hasNext(); cursor.next()) {
				i.incrementAndGet();
			}
			assertThat(i.get(), is(10));
		} finally {
			db.collection(COLLECTION_NAME).drop();
		}
	}

	@Test
	public void queryWithCount() {
		try {
			db.createCollection(COLLECTION_NAME, null);
			for (int i = 0; i < 10; i++) {
				db.collection(COLLECTION_NAME).insertDocument(new BaseDocument(), null);
			}

			final ArangoCursor<String> cursor = db.query("for i in db_test Limit 6 return i._id", null,
				new AqlQueryOptions().count(true), String.class);
			assertThat(cursor, is(notNullValue()));
			for (int i = 0; i < 6; i++, cursor.next()) {
				assertThat(cursor.hasNext(), is(i != 6));
			}
			assertThat(cursor.getCount(), is(6));

		} finally {
			db.collection(COLLECTION_NAME).drop();
		}
	}

	@Test
	public void queryWithLimitAndFullCount() {
		try {
			db.createCollection(COLLECTION_NAME, null);
			for (int i = 0; i < 10; i++) {
				db.collection(COLLECTION_NAME).insertDocument(new BaseDocument(), null);
			}

			final ArangoCursor<String> cursor = db.query("for i in db_test Limit 5 return i._id", null,
				new AqlQueryOptions().fullCount(true), String.class);
			assertThat(cursor, is(notNullValue()));
			for (int i = 0; i < 5; i++, cursor.next()) {
				assertThat(cursor.hasNext(), is(i != 5));
			}
			assertThat(cursor.getStats(), is(notNullValue()));
			assertThat(cursor.getStats().getFullCount(), is(10L));

		} finally {
			db.collection(COLLECTION_NAME).drop();
		}
	}

	@Test
	public void queryWithBatchSize() {
		try {
			db.createCollection(COLLECTION_NAME, null);
			for (int i = 0; i < 10; i++) {
				db.collection(COLLECTION_NAME).insertDocument(new BaseDocument(), null);
			}

			final ArangoCursor<String> cursor = db.query("for i in db_test return i._id", null,
				new AqlQueryOptions().batchSize(5).count(true), String.class);

			assertThat(cursor, is(notNullValue()));
			for (int i = 0; i < 10; i++, cursor.next()) {
				assertThat(cursor.hasNext(), is(i != 10));
			}

		} finally {
			db.collection(COLLECTION_NAME).drop();
		}
	}

	@Test
	public void queryStreamWithBatchSize() {
		try {
			db.createCollection(COLLECTION_NAME, null);
			for (int i = 0; i < 10; i++) {
				db.collection(COLLECTION_NAME).insertDocument(new BaseDocument(), null);
			}

			final ArangoCursor<String> cursor = db.query("for i in db_test return i._id", null,
				new AqlQueryOptions().batchSize(5).count(true), String.class);

			assertThat(cursor, is(notNullValue()));
			final AtomicInteger i = new AtomicInteger(0);
			for (; cursor.hasNext(); cursor.next()) {
				i.incrementAndGet();
			}
			assertThat(i.get(), is(10));
		} finally {
			db.collection(COLLECTION_NAME).drop();
		}
	}

	/**
	 * ignored. takes to long
	 */
	@Test
	@Ignore
	public void queryWithTTL() throws InterruptedException {
		// set TTL to 1 seconds and get the second batch after 2 seconds!
		final int ttl = 1;
		final int wait = 2;
		try {
			db.createCollection(COLLECTION_NAME, null);
			for (int i = 0; i < 10; i++) {
				db.collection(COLLECTION_NAME).insertDocument(new BaseDocument(), null);
			}

			final ArangoCursor<String> cursor = db.query("for i in db_test return i._id", null,
				new AqlQueryOptions().batchSize(5).ttl(ttl), String.class);

			assertThat(cursor, is(notNullValue()));

			for (int i = 0; i < 10; i++, cursor.next()) {
				assertThat(cursor.hasNext(), is(i != 10));
				if (i == 1) {
					Thread.sleep(wait * 1000);
				}
			}
			fail("this should fail");
		} catch (final ArangoDBException ex) {
			assertThat(ex.getMessage(), is("Response: 404, Error: 1600 - cursor not found"));
		} finally {
			db.collection(COLLECTION_NAME).drop();
		}
	}

	@Test
	public void changeQueryCache() {
		try {
			QueryCachePropertiesEntity properties = db.getQueryCacheProperties();
			assertThat(properties, is(notNullValue()));
			assertThat(properties.getMode(), is(CacheMode.off));
			assertThat(properties.getMaxResults(), greaterThan(0L));

			properties.setMode(CacheMode.on);
			properties = db.setQueryCacheProperties(properties);
			assertThat(properties, is(notNullValue()));
			assertThat(properties.getMode(), is(CacheMode.on));

			properties = db.getQueryCacheProperties();
			assertThat(properties.getMode(), is(CacheMode.on));
		} finally {
			final QueryCachePropertiesEntity properties = new QueryCachePropertiesEntity();
			properties.setMode(CacheMode.off);
			db.setQueryCacheProperties(properties);
		}
	}

	@Test
	public void queryWithCache() throws InterruptedException {
		try {
			db.createCollection(COLLECTION_NAME, null);
			for (int i = 0; i < 10; i++) {
				db.collection(COLLECTION_NAME).insertDocument(new BaseDocument(), null);
			}

			final QueryCachePropertiesEntity properties = new QueryCachePropertiesEntity();
			properties.setMode(CacheMode.on);
			db.setQueryCacheProperties(properties);

			final ArangoCursor<String> cursor = db.query("FOR t IN db_test FILTER t.age >= 10 SORT t.age RETURN t._id",
				null, new AqlQueryOptions().cache(true), String.class);

			assertThat(cursor, is(notNullValue()));
			assertThat(cursor.isCached(), is(false));

			final ArangoCursor<String> cachedCursor = db.query(
				"FOR t IN db_test FILTER t.age >= 10 SORT t.age RETURN t._id", null, new AqlQueryOptions().cache(true),
				String.class);

			assertThat(cachedCursor, is(notNullValue()));
			assertThat(cachedCursor.isCached(), is(true));

		} finally {
			db.collection(COLLECTION_NAME).drop();
			final QueryCachePropertiesEntity properties = new QueryCachePropertiesEntity();
			properties.setMode(CacheMode.off);
			db.setQueryCacheProperties(properties);
		}
	}

	@Test
	public void changeQueryTrackingProperties() {
		try {
			QueryTrackingPropertiesEntity properties = db.getQueryTrackingProperties();
			assertThat(properties, is(notNullValue()));
			assertThat(properties.getEnabled(), is(true));
			assertThat(properties.getTrackSlowQueries(), is(true));
			assertThat(properties.getMaxQueryStringLength(), greaterThan(0L));
			assertThat(properties.getMaxSlowQueries(), greaterThan(0L));
			assertThat(properties.getSlowQueryThreshold(), greaterThan(0L));
			properties.setEnabled(false);
			properties = db.setQueryTrackingProperties(properties);
			assertThat(properties, is(notNullValue()));
			assertThat(properties.getEnabled(), is(false));
			properties = db.getQueryTrackingProperties();
			assertThat(properties.getEnabled(), is(false));
		} finally {
			final QueryTrackingPropertiesEntity properties = new QueryTrackingPropertiesEntity();
			properties.setEnabled(true);
			db.setQueryTrackingProperties(properties);
		}
	}

	@Test
	public void queryWithBindVars() throws InterruptedException {
		try {
			db.createCollection(COLLECTION_NAME, null);
			for (int i = 0; i < 10; i++) {
				final BaseDocument baseDocument = new BaseDocument();
				baseDocument.addAttribute("age", 20 + i);
				db.collection(COLLECTION_NAME).insertDocument(baseDocument, null);
			}
			final Map<String, Object> bindVars = new HashMap<String, Object>();
			bindVars.put("@coll", COLLECTION_NAME);
			bindVars.put("age", 25);

			final ArangoCursor<String> cursor = db.query("FOR t IN @@coll FILTER t.age >= @age SORT t.age RETURN t._id",
				bindVars, null, String.class);

			assertThat(cursor, is(notNullValue()));

			for (int i = 0; i < 5; i++, cursor.next()) {
				assertThat(cursor.hasNext(), is(i != 5));
			}

		} finally {
			db.collection(COLLECTION_NAME).drop();
		}
	}

	@Test
	public void queryWithWarning() {
		final ArangoCursor<String> cursor = arangoDB.db().query("return _users + 1", null, null, String.class);

		assertThat(cursor, is(notNullValue()));
		assertThat(cursor.getWarnings(), is(notNullValue()));
		assertThat(cursor.getWarnings(), is(not(empty())));
	}

	@Test
	public void queryClose() throws IOException {
		final ArangoCursor<String> cursor = arangoDB.db().query("for i in _apps return i._id", null,
			new AqlQueryOptions().batchSize(1), String.class);
		cursor.close();
		int count = 0;
		try {
			for (; cursor.hasNext(); cursor.next(), count++) {
			}
			fail();
		} catch (final ArangoDBException e) {
			assertThat(count, is(1));
		}

	}

	@Test
	public void explainQuery() {
		final AqlExecutionExplainEntity explain = arangoDB.db().explainQuery("for i in _apps return i", null, null);
		assertThat(explain, is(notNullValue()));
		assertThat(explain.getPlan(), is(notNullValue()));
		assertThat(explain.getPlans(), is(nullValue()));
		final ExecutionPlan plan = explain.getPlan();
		assertThat(plan.getCollections().size(), is(1));
		assertThat(plan.getCollections().iterator().next().getName(), is("_apps"));
		assertThat(plan.getCollections().iterator().next().getType(), is("read"));
		assertThat(plan.getEstimatedCost(), is(5));
		assertThat(plan.getEstimatedNrItems(), is(2));
		assertThat(plan.getVariables().size(), is(1));
		assertThat(plan.getVariables().iterator().next().getName(), is("i"));
		assertThat(plan.getNodes().size(), is(3));
		final Iterator<ExecutionNode> iterator = plan.getNodes().iterator();
		final ExecutionNode singletonNode = iterator.next();
		assertThat(singletonNode.getType(), is("SingletonNode"));
		final ExecutionNode collectionNode = iterator.next();
		assertThat(collectionNode.getType(), is("EnumerateCollectionNode"));
		assertThat(collectionNode.getDatabase(), is("_system"));
		assertThat(collectionNode.getCollection(), is("_apps"));
		assertThat(collectionNode.getOutVariable(), is(notNullValue()));
		assertThat(collectionNode.getOutVariable().getName(), is("i"));
		final ExecutionNode returnNode = iterator.next();
		assertThat(returnNode.getType(), is("ReturnNode"));
		assertThat(returnNode.getInVariable(), is(notNullValue()));
		assertThat(returnNode.getInVariable().getName(), is("i"));
	}

	@Test
	public void parseQuery() {
		final AqlParseEntity parse = arangoDB.db().parseQuery("for i in _apps return i");
		assertThat(parse, is(notNullValue()));
		assertThat(parse.getBindVars(), is(empty()));
		assertThat(parse.getCollections().size(), is(1));
		assertThat(parse.getCollections().iterator().next(), is("_apps"));
		assertThat(parse.getAst().size(), is(1));
		final AstNode root = parse.getAst().iterator().next();
		assertThat(root.getType(), is("root"));
		assertThat(root.getName(), is(nullValue()));
		assertThat(root.getSubNodes(), is(notNullValue()));
		assertThat(root.getSubNodes().size(), is(2));
		final Iterator<AstNode> iterator = root.getSubNodes().iterator();
		final AstNode for_ = iterator.next();
		assertThat(for_.getType(), is("for"));
		assertThat(for_.getSubNodes(), is(notNullValue()));
		assertThat(for_.getSubNodes().size(), is(2));
		final Iterator<AstNode> iterator2 = for_.getSubNodes().iterator();
		final AstNode first = iterator2.next();
		assertThat(first.getType(), is("variable"));
		assertThat(first.getName(), is("i"));
		final AstNode second = iterator2.next();
		assertThat(second.getType(), is("collection"));
		assertThat(second.getName(), is("_apps"));
		final AstNode return_ = iterator.next();
		assertThat(return_.getType(), is("return"));
		assertThat(return_.getSubNodes(), is(notNullValue()));
		assertThat(return_.getSubNodes().size(), is(1));
		assertThat(return_.getSubNodes().iterator().next().getType(), is("reference"));
		assertThat(return_.getSubNodes().iterator().next().getName(), is("i"));
	}

	@Test
	@Ignore
	public void getCurrentlyRunningQueries() throws InterruptedException, ExecutionException {
		final Thread t = new Thread() {
			@Override
			public void run() {
				super.run();
				db.query("return sleep(0.2)", null, null, Void.class);
			}
		};
		t.start();
		Thread.sleep(100);
		try {
			final Collection<QueryEntity> currentlyRunningQueries = db.getCurrentlyRunningQueries();
			assertThat(currentlyRunningQueries, is(notNullValue()));
			assertThat(currentlyRunningQueries.size(), is(1));
			final QueryEntity queryEntity = currentlyRunningQueries.iterator().next();
			assertThat(queryEntity.getQuery(), is("return sleep(0.2)"));
		} finally {
			t.join();
		}
	}

	@Test
	@Ignore
	public void getAndClearSlowQueries() throws InterruptedException, ExecutionException {
		final QueryTrackingPropertiesEntity properties = db.getQueryTrackingProperties();
		final Long slowQueryThreshold = properties.getSlowQueryThreshold();
		try {
			properties.setSlowQueryThreshold(1L);
			db.setQueryTrackingProperties(properties);

			db.query("return sleep(1.1)", null, null, Void.class);
			final Collection<QueryEntity> slowQueries = db.getSlowQueries();
			assertThat(slowQueries, is(notNullValue()));
			assertThat(slowQueries.size(), is(1));
			final QueryEntity queryEntity = slowQueries.iterator().next();
			assertThat(queryEntity.getQuery(), is("return sleep(1.1)"));

			db.clearSlowQueries();
			assertThat(db.getSlowQueries().size(), is(0));
		} finally {
			properties.setSlowQueryThreshold(slowQueryThreshold);
			db.setQueryTrackingProperties(properties);
		}
	}

	@Test
	@Ignore
	public void killQuery() throws InterruptedException, ExecutionException {
		final Thread t = new Thread() {
			@Override
			public void run() {
				super.run();
				try {
					db.query("return sleep(0.2)", null, null, Void.class);
					fail();
				} catch (final ArangoDBException e) {
				}
			}
		};
		t.start();
		Thread.sleep(100);
		final Collection<QueryEntity> currentlyRunningQueries = db.getCurrentlyRunningQueries();
		assertThat(currentlyRunningQueries, is(notNullValue()));
		assertThat(currentlyRunningQueries.size(), is(1));

		final QueryEntity queryEntity = currentlyRunningQueries.iterator().next();
		db.killQuery(queryEntity.getId());
	}

	@Test
	public void createGetDeleteAqlFunction() {
		final Collection<AqlFunctionEntity> aqlFunctionsInitial = db.getAqlFunctions(null);
		assertThat(aqlFunctionsInitial, is(empty()));
		try {
			db.createAqlFunction("myfunctions::temperature::celsiustofahrenheit",
				"function (celsius) { return celsius * 1.8 + 32; }", null);

			final Collection<AqlFunctionEntity> aqlFunctions = db.getAqlFunctions(null);
			assertThat(aqlFunctions.size(), is(greaterThan(aqlFunctionsInitial.size())));
		} finally {
			db.deleteAqlFunction("myfunctions::temperature::celsiustofahrenheit", null);

			final Collection<AqlFunctionEntity> aqlFunctions = db.getAqlFunctions(null);
			assertThat(aqlFunctions.size(), is(aqlFunctionsInitial.size()));
		}
	}

	@Test
	public void createGetDeleteAqlFunctionWithNamespace() {
		final Collection<AqlFunctionEntity> aqlFunctionsInitial = db.getAqlFunctions(null);
		assertThat(aqlFunctionsInitial, is(empty()));
		try {
			db.createAqlFunction("myfunctions::temperature::celsiustofahrenheit1",
				"function (celsius) { return celsius * 1.8 + 32; }", null);
			db.createAqlFunction("myfunctions::temperature::celsiustofahrenheit2",
				"function (celsius) { return celsius * 1.8 + 32; }", null);

		} finally {
			db.deleteAqlFunction("myfunctions::temperature", new AqlFunctionDeleteOptions().group(true));

			final Collection<AqlFunctionEntity> aqlFunctions = db.getAqlFunctions(null);
			assertThat(aqlFunctions.size(), is(aqlFunctionsInitial.size()));
		}
	}

	@Test
	public void createGraph() {
		try {
			final GraphEntity result = db.createGraph(GRAPH_NAME, null, null);
			assertThat(result, is(notNullValue()));
			assertThat(result.getName(), is(GRAPH_NAME));
		} finally {
			db.graph(GRAPH_NAME).drop();
		}
	}

	@Test
	public void getGraphs() {
		try {
			db.createGraph(GRAPH_NAME, null, null);
			final Collection<GraphEntity> graphs = db.getGraphs();
			assertThat(graphs, is(notNullValue()));
			assertThat(graphs.size(), is(1));
		} finally {
			db.graph(GRAPH_NAME).drop();
		}
	}

	@Test
	public void transactionString() {
		final TransactionOptions options = new TransactionOptions().params("test");
		final String result = db.transaction("function (params) {return params;}", String.class, options);
		assertThat(result, is("test"));
	}

	@Test
	public void transactionNumber() {
		final TransactionOptions options = new TransactionOptions().params(5);
		final Integer result = db.transaction("function (params) {return params;}", Integer.class, options);
		assertThat(result, is(5));
	}

	@Test
	public void transactionVPack() throws VPackException {
		final TransactionOptions options = new TransactionOptions().params(new VPackBuilder().add("test").slice());
		final VPackSlice result = db.transaction("function (params) {return params;}", VPackSlice.class, options);
		assertThat(result.isString(), is(true));
		assertThat(result.getAsString(), is("test"));
	}

	@Test
	public void transactionEmpty() {
		db.transaction("function () {}", null, null);
	}

	@Test
	public void transactionallowImplicit() {
		try {
			db.createCollection("someCollection", null);
			db.createCollection("someOtherCollection", null);
			final String action = "function (params) {" + "var db = require('internal').db;"
					+ "return {'a':db.someCollection.all().toArray()[0], 'b':db.someOtherCollection.all().toArray()[0]};"
					+ "}";
			final TransactionOptions options = new TransactionOptions().readCollections("someCollection");
			db.transaction(action, VPackSlice.class, options);
			try {
				options.allowImplicit(false);
				db.transaction(action, VPackSlice.class, options);
				fail();
			} catch (final ArangoDBException e) {
			}
		} finally {
			db.collection("someCollection").drop();
			db.collection("someOtherCollection").drop();
		}
	}

	@Test
	public void getInfo() {
		final DatabaseEntity info = db.getInfo();
		assertThat(info, is(notNullValue()));
		assertThat(info.getId(), is(notNullValue()));
		assertThat(info.getName(), is(TEST_DB));
		assertThat(info.getPath(), is(notNullValue()));
		assertThat(info.getIsSystem(), is(false));
	}

	@Test
	public void executeTraversal() {
		try {
			db.createCollection("person", null);
			db.createCollection("knows", new CollectionCreateOptions().type(CollectionType.EDGES));
			for (final String e : new String[] { "Alice", "Bob", "Charlie", "Dave", "Eve" }) {
				final BaseDocument doc = new BaseDocument();
				doc.setKey(e);
				db.collection("person").insertDocument(doc, null);
			}
			for (final String[] e : new String[][] { new String[] { "Alice", "Bob" }, new String[] { "Bob", "Charlie" },
					new String[] { "Bob", "Dave" }, new String[] { "Eve", "Alice" }, new String[] { "Eve", "Bob" } }) {
				final BaseEdgeDocument edge = new BaseEdgeDocument();
				edge.setKey(e[0] + "_knows_" + e[1]);
				edge.setFrom("person/" + e[0]);
				edge.setTo("person/" + e[1]);
				db.collection("knows").insertDocument(edge, null);
			}
			final TraversalOptions options = new TraversalOptions().edgeCollection("knows").startVertex("person/Alice")
					.direction(Direction.outbound);
			final TraversalEntity<BaseDocument, BaseEdgeDocument> traversal = db.executeTraversal(BaseDocument.class,
				BaseEdgeDocument.class, options);

			assertThat(traversal, is(notNullValue()));

			final Collection<BaseDocument> vertices = traversal.getVertices();
			assertThat(vertices, is(notNullValue()));
			assertThat(vertices.size(), is(4));

			final Iterator<BaseDocument> verticesIterator = vertices.iterator();
			for (final String e : new String[] { "Alice", "Bob", "Charlie", "Dave" }) {
				assertThat(verticesIterator.next().getKey(), is(e));
			}

			final Collection<PathEntity<BaseDocument, BaseEdgeDocument>> paths = traversal.getPaths();
			assertThat(paths, is(notNullValue()));
			assertThat(paths.size(), is(4));

			assertThat(paths.iterator().hasNext(), is(true));
			final PathEntity<BaseDocument, BaseEdgeDocument> first = paths.iterator().next();
			assertThat(first, is(notNullValue()));
			assertThat(first.getEdges().size(), is(0));
			assertThat(first.getVertices().size(), is(1));
			assertThat(first.getVertices().iterator().next().getKey(), is("Alice"));
		} finally {
			db.collection("person").drop();
			db.collection("knows").drop();
		}
	}

	@Test
	public void getDocument() {
		try {
			db.createCollection(COLLECTION_NAME);
			final BaseDocument value = new BaseDocument();
			value.setKey("123");
			db.collection(COLLECTION_NAME).insertDocument(value);
			final BaseDocument document = db.getDocument(COLLECTION_NAME + "/123", BaseDocument.class);
			assertThat(document, is(notNullValue()));
			assertThat(document.getKey(), is("123"));
		} finally {
			db.collection(COLLECTION_NAME).drop();
		}
	}

	@Test(expected = ArangoDBException.class)
	public void getDocumentWrongId() {
		db.getDocument("123", BaseDocument.class);
	}

	@Test
	public void reloadRouting() {
		db.reloadRouting();
	}
}
