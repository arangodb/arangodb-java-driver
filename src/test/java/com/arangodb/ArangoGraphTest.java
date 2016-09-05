package com.arangodb;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.stream.Stream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.arangodb.entity.BaseDocument;
import com.arangodb.entity.CollectionType;
import com.arangodb.entity.DocumentCreateResult;
import com.arangodb.entity.EdgeDefinition;
import com.arangodb.entity.GraphResult;
import com.arangodb.model.CollectionCreateOptions;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class ArangoGraphTest extends BaseTest {

	private static final String GRAPH_NAME = "db_collection_test";
	private static final String EDGE_COL_1 = "edge1";
	private static final String EDGE_COL_2 = "edge2";
	private static final String VERTEX_COL_1 = "vertex1";
	private static final String VERTEX_COL_2 = "vertex2";
	private static final String VERTEX_COL_3 = "vertex3";
	private static final String VERTEX_COL_4 = "vertex4";

	@Before
	public void setup() {
		Stream.of(VERTEX_COL_1, VERTEX_COL_2, VERTEX_COL_2, VERTEX_COL_3, VERTEX_COL_4).forEach(collection -> {
			try {
				db.createCollection(collection, null);
			} catch (final ArangoDBException e) {
			}
		});
		Stream.of(EDGE_COL_1, EDGE_COL_2).forEach(collection -> {
			try {
				final CollectionCreateOptions options = new CollectionCreateOptions().type(CollectionType.EDGES);
				db.createCollection(collection, options);
			} catch (final ArangoDBException e) {
			}
		});
		final Collection<EdgeDefinition> edgeDefinitions = new ArrayList<>();
		edgeDefinitions.add(new EdgeDefinition().collection(EDGE_COL_1).from(VERTEX_COL_1).to(VERTEX_COL_2));
		edgeDefinitions
				.add(new EdgeDefinition().collection(EDGE_COL_2).from(VERTEX_COL_2).to(VERTEX_COL_1, VERTEX_COL_3));
		db.createGraph(GRAPH_NAME, edgeDefinitions, null);
	}

	@After
	public void teardown() {
		Stream.of(EDGE_COL_1, EDGE_COL_2, VERTEX_COL_1, VERTEX_COL_2, VERTEX_COL_3, VERTEX_COL_4)
				.forEach(collection -> {
					try {
						db.collection(collection).drop();
					} catch (final ArangoDBException e) {
					}
				});
		db.graph(GRAPH_NAME).drop();
	}

	@Test
	public void getGraphs() {
		final Collection<GraphResult> graphs = db.getGraphs();
		assertThat(graphs, is(notNullValue()));
		assertThat(graphs.size(), is(1));
	}

	@Test
	public void getInfo() {
		final GraphResult info = db.graph(GRAPH_NAME).getInfo();
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
		final GraphResult graph = db.graph(GRAPH_NAME).addVertexCollection(VERTEX_COL_4);
		assertThat(graph, is(notNullValue()));
		final Collection<String> vertexCollections = db.graph(GRAPH_NAME).getVertexCollections();
		assertThat(vertexCollections, hasItems(VERTEX_COL_1, VERTEX_COL_2, VERTEX_COL_3, VERTEX_COL_4));
	}

	@Test
	public void dropVertexCollection() {
		db.graph(GRAPH_NAME).addVertexCollection(VERTEX_COL_4);
		db.graph(GRAPH_NAME).vertexCollection(VERTEX_COL_4).drop();
		final Collection<String> vertexCollections = db.graph(GRAPH_NAME).getVertexCollections();
		assertThat(vertexCollections, not(hasItem(VERTEX_COL_4)));
	}

	@Test
	public void insertVertex() {
		final DocumentCreateResult<BaseDocument> vertex = db.graph(GRAPH_NAME).vertexCollection(VERTEX_COL_1)
				.insertVertex(new BaseDocument(), null);
		assertThat(vertex, is(notNullValue()));
		final BaseDocument document = db.collection(VERTEX_COL_1).getDocument(vertex.getKey(), BaseDocument.class,
			null);
		assertThat(document, is(notNullValue()));
		assertThat(document.getKey(), is(vertex.getKey()));
	}
}
