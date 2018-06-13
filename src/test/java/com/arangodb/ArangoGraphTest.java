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
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import com.arangodb.ArangoDB.Builder;
import com.arangodb.entity.ArangoDBVersion.License;
import com.arangodb.entity.CollectionType;
import com.arangodb.entity.EdgeDefinition;
import com.arangodb.entity.GraphEntity;
import com.arangodb.model.CollectionCreateOptions;
import com.arangodb.model.GraphCreateOptions;

/**
 * @author Mark Vollmary
 *
 */
@RunWith(Parameterized.class)
public class ArangoGraphTest extends BaseTest {

	private static final String GRAPH_NAME = "db_collection_test";
	private static final String EDGE_COL_1 = "db_edge1_collection_test";
	private static final String EDGE_COL_2 = "db_edge2_collection_test";
	private static final String EDGE_COL_3 = "db_edge3_collection_test";
	private static final String VERTEX_COL_1 = "db_vertex1_collection_test";
	private static final String VERTEX_COL_2 = "db_vertex2_collection_test";
	private static final String VERTEX_COL_3 = "db_vertex3_collection_test";
	private static final String VERTEX_COL_4 = "db_vertex4_collection_test";

	public ArangoGraphTest(final Builder builder) {
		super(builder);
		setup();
	}

	public void setup() {
		try {
			db.graph(GRAPH_NAME).drop();
		} catch (final ArangoDBException e1) {
		}
		for (final String collection : new String[] { VERTEX_COL_1, VERTEX_COL_2, VERTEX_COL_2, VERTEX_COL_3,
				VERTEX_COL_4 }) {
			try {
				db.createCollection(collection, null);
			} catch (final ArangoDBException e) {
			}
		}
		for (final String collection : new String[] { EDGE_COL_1, EDGE_COL_2 }) {
			try {
				final CollectionCreateOptions options = new CollectionCreateOptions().type(CollectionType.EDGES);
				db.createCollection(collection, options);
			} catch (final ArangoDBException e) {
			}
		}
		final Collection<EdgeDefinition> edgeDefinitions = new ArrayList<EdgeDefinition>();
		edgeDefinitions.add(new EdgeDefinition().collection(EDGE_COL_1).from(VERTEX_COL_1).to(VERTEX_COL_2));
		edgeDefinitions
				.add(new EdgeDefinition().collection(EDGE_COL_2).from(VERTEX_COL_2).to(VERTEX_COL_1, VERTEX_COL_3));
		db.createGraph(GRAPH_NAME, edgeDefinitions, null);
	}

	@After
	public void teardown() {
		for (final String collection : new String[] { EDGE_COL_1, EDGE_COL_2, VERTEX_COL_1, VERTEX_COL_2, VERTEX_COL_3,
				VERTEX_COL_4 }) {
			final ArangoCollection c = db.collection(collection);
			if (c.exists()) {
				c.truncate();
			}
		}
	}

	@Test
	public void exists() {
		assertThat(db.graph(GRAPH_NAME).exists(), is(true));
		assertThat(db.graph(GRAPH_NAME + "no").exists(), is(false));
	}

	@Test
	public void create() {
		try {
			final GraphEntity result = db.graph(GRAPH_NAME + "_1").create(null);
			assertThat(result, is(notNullValue()));
			assertThat(result.getName(), is(GRAPH_NAME + "_1"));
		} finally {
			db.graph(GRAPH_NAME + "_1").drop();
		}
	}

	@Test
	public void getGraphs() {
		final Collection<GraphEntity> graphs = db.getGraphs();
		assertThat(graphs, is(notNullValue()));
		assertThat(graphs.size(), is(1));
		assertThat(graphs.iterator().next().getName(), is(GRAPH_NAME));
	}

	@Test
	public void getInfo() {
		final GraphEntity info = db.graph(GRAPH_NAME).getInfo();
		assertThat(info, is(notNullValue()));
		assertThat(info.getName(), is(GRAPH_NAME));
		assertThat(info.getEdgeDefinitions().size(), is(2));
		final Iterator<EdgeDefinition> iterator = info.getEdgeDefinitions().iterator();
		final EdgeDefinition e1 = iterator.next();
		assertThat(e1.getCollection(), is(EDGE_COL_1));
		assertThat(e1.getFrom(), hasItem(VERTEX_COL_1));
		assertThat(e1.getTo(), hasItem(VERTEX_COL_2));
		final EdgeDefinition e2 = iterator.next();
		assertThat(e2.getCollection(), is(EDGE_COL_2));
		assertThat(e2.getFrom(), hasItem(VERTEX_COL_2));
		assertThat(e2.getTo(), hasItems(VERTEX_COL_1, VERTEX_COL_3));
		assertThat(info.getOrphanCollections(), is(empty()));
	}

	@Test
	public void getVertexCollections() {
		final Collection<String> vertexCollections = db.graph(GRAPH_NAME).getVertexCollections();
		assertThat(vertexCollections, is(notNullValue()));
		assertThat(vertexCollections.size(), is(3));
		assertThat(vertexCollections, hasItems(VERTEX_COL_1, VERTEX_COL_2, VERTEX_COL_3));
	}

	@Test
	public void addVertexCollection() {
		final GraphEntity graph = db.graph(GRAPH_NAME).addVertexCollection(VERTEX_COL_4);
		assertThat(graph, is(notNullValue()));
		final Collection<String> vertexCollections = db.graph(GRAPH_NAME).getVertexCollections();
		assertThat(vertexCollections, hasItems(VERTEX_COL_1, VERTEX_COL_2, VERTEX_COL_3, VERTEX_COL_4));
		setup();
	}

	@Test
	public void getEdgeCollections() {
		final Collection<String> edgeCollections = db.graph(GRAPH_NAME).getEdgeDefinitions();
		assertThat(edgeCollections, is(notNullValue()));
		assertThat(edgeCollections.size(), is(2));
		assertThat(edgeCollections, hasItems(EDGE_COL_1, EDGE_COL_2));
	}

	@Test
	public void addEdgeDefinition() {
		final GraphEntity graph = db.graph(GRAPH_NAME)
				.addEdgeDefinition(new EdgeDefinition().collection(EDGE_COL_3).from(VERTEX_COL_1).to(VERTEX_COL_2));
		assertThat(graph, is(notNullValue()));
		final Collection<EdgeDefinition> edgeDefinitions = graph.getEdgeDefinitions();
		assertThat(edgeDefinitions.size(), is(3));
		int count = 0;
		for (final EdgeDefinition e : edgeDefinitions) {
			if (e.getCollection().equals(EDGE_COL_3)) {
				count++;
			}
		}
		assertThat(count, is(1));
		for (final EdgeDefinition e : edgeDefinitions) {
			if (e.getCollection().equals(EDGE_COL_3)) {
				assertThat(e.getFrom(), hasItem(VERTEX_COL_1));
				assertThat(e.getTo(), hasItem(VERTEX_COL_2));
			}
		}
		setup();
	}

	@Test
	public void replaceEdgeDefinition() {
		final GraphEntity graph = db.graph(GRAPH_NAME)
				.replaceEdgeDefinition(new EdgeDefinition().collection(EDGE_COL_1).from(VERTEX_COL_3).to(VERTEX_COL_4));
		final Collection<EdgeDefinition> edgeDefinitions = graph.getEdgeDefinitions();
		assertThat(edgeDefinitions.size(), is(2));
		int count = 0;
		for (final EdgeDefinition e : edgeDefinitions) {
			if (e.getCollection().equals(EDGE_COL_1)) {
				count++;
			}
		}
		assertThat(count, is(1));
		for (final EdgeDefinition e : edgeDefinitions) {
			if (e.getCollection().equals(EDGE_COL_1)) {
				assertThat(e.getFrom(), hasItem(VERTEX_COL_3));
				assertThat(e.getTo(), hasItem(VERTEX_COL_4));
			}
		}
		setup();
	}

	@Test
	public void removeEdgeDefinition() {
		final GraphEntity graph = db.graph(GRAPH_NAME).removeEdgeDefinition(EDGE_COL_1);
		final Collection<EdgeDefinition> edgeDefinitions = graph.getEdgeDefinitions();
		assertThat(edgeDefinitions.size(), is(1));
		assertThat(edgeDefinitions.iterator().next().getCollection(), is(EDGE_COL_2));
		setup();
	}

	@Test
	public void smartGraph() {
		if (arangoDB.getVersion().getLicense() == License.ENTERPRISE) {
			for (final String collection : new String[] { EDGE_COL_1, EDGE_COL_2, VERTEX_COL_1, VERTEX_COL_2,
					VERTEX_COL_3, VERTEX_COL_4 }) {
				try {
					db.collection(collection).drop();
				} catch (final ArangoDBException e) {
				}
			}
			try {
				db.graph(GRAPH_NAME).drop();
			} catch (final ArangoDBException e) {
			}
			final Collection<EdgeDefinition> edgeDefinitions = new ArrayList<EdgeDefinition>();
			edgeDefinitions.add(new EdgeDefinition().collection(EDGE_COL_1).from(VERTEX_COL_1).to(VERTEX_COL_2));
			edgeDefinitions
					.add(new EdgeDefinition().collection(EDGE_COL_2).from(VERTEX_COL_2).to(VERTEX_COL_1, VERTEX_COL_3));
			final GraphEntity graph = db.createGraph(GRAPH_NAME, edgeDefinitions,
				new GraphCreateOptions().isSmart(true).smartGraphAttribute("test").numberOfShards(2));
			assertThat(graph, is(notNullValue()));
			assertThat(graph.getIsSmart(), is(true));
			assertThat(graph.getSmartGraphAttribute(), is("test"));
			assertThat(graph.getNumberOfShards(), is(2));
		}
	}
}
