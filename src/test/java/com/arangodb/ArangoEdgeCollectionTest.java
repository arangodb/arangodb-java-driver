package com.arangodb;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Stream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.arangodb.entity.BaseDocument;
import com.arangodb.entity.BaseEdgeDocument;
import com.arangodb.entity.CollectionType;
import com.arangodb.entity.EdgeDefinition;
import com.arangodb.entity.EdgeResult;
import com.arangodb.entity.VertexResult;
import com.arangodb.model.CollectionCreateOptions;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class ArangoEdgeCollectionTest extends BaseTest {

	private static final String GRAPH_NAME = "db_collection_test";
	private static final String EDGE_COLLECTION_NAME = "db_edge_collection_test";
	private static final String VERTEX_COLLECTION_NAME = "db_vertex_collection_test";

	@Before
	public void setup() {
		try {
			db.createCollection(VERTEX_COLLECTION_NAME, null);
		} catch (final ArangoDBException e) {
		}
		try {
			db.createCollection(EDGE_COLLECTION_NAME, new CollectionCreateOptions().type(CollectionType.EDGES));
		} catch (final ArangoDBException e) {
		}
		final Collection<EdgeDefinition> edgeDefinitions = new ArrayList<>();
		edgeDefinitions.add(new EdgeDefinition().collection(EDGE_COLLECTION_NAME).from(VERTEX_COLLECTION_NAME)
				.to(VERTEX_COLLECTION_NAME));
		db.createGraph(GRAPH_NAME, edgeDefinitions, null);
	}

	@After
	public void teardown() {
		Stream.of(VERTEX_COLLECTION_NAME, EDGE_COLLECTION_NAME).forEach(collection -> {
			try {
				db.collection(collection).drop();
			} catch (final ArangoDBException e) {
			}
		});
		db.graph(GRAPH_NAME).drop();
	}

	@Test
	public void insertEdge() {
		final VertexResult v1 = db.graph(GRAPH_NAME).vertexCollection(VERTEX_COLLECTION_NAME)
				.insertVertex(new BaseDocument(), null);
		final VertexResult v2 = db.graph(GRAPH_NAME).vertexCollection(VERTEX_COLLECTION_NAME)
				.insertVertex(new BaseDocument(), null);

		final BaseEdgeDocument value = new BaseEdgeDocument();
		value.setFrom(v1.getId());
		value.setTo(v2.getId());
		final EdgeResult edge = db.graph(GRAPH_NAME).edgeCollection(EDGE_COLLECTION_NAME).insertEdge(value, null);
		assertThat(edge, is(notNullValue()));
		final BaseDocument document = db.collection(EDGE_COLLECTION_NAME).getDocument(edge.getKey(), BaseDocument.class,
			null);
		assertThat(document, is(notNullValue()));
		assertThat(document.getKey(), is(edge.getKey()));
	}
}
