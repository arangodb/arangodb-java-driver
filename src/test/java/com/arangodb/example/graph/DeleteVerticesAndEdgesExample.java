package com.arangodb.example.graph;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.arangodb.ArangoDriver;
import com.arangodb.ArangoException;
import com.arangodb.ErrorNums;
import com.arangodb.entity.DeletedEntity;
import com.arangodb.entity.EdgeEntity;
import com.arangodb.entity.marker.VertexEntity;

/**
 * Delete vertices and edges example
 * 
 * @author a-brandt
 *
 */
public class DeleteVerticesAndEdgesExample extends BaseExample {

	private static final String DATABASE_NAME = "DeleteVerticesAndEdgesExample";

	private static final String GRAPH_NAME = "example_graph1";
	private static final String EDGE_COLLECTION_NAME = "edgeColl1";
	private static final String VERTEXT_COLLECTION_NAME = "vertexColl1";

	public ArangoDriver arangoDriver;

	@Before
	public void _before() throws ArangoException {
		removeTestDatabase(DATABASE_NAME);

		arangoDriver = getArangoDriver(getConfiguration());
		createDatabase(arangoDriver, DATABASE_NAME);
		createGraph(arangoDriver, GRAPH_NAME, EDGE_COLLECTION_NAME, VERTEXT_COLLECTION_NAME);
	}

	@Test
	public void deleteVertex() throws ArangoException {

		//
		printHeadline("delete vertex");
		//

		VertexEntity<Person> v1 = createVertex(new Person("A", Person.MALE));

		DeletedEntity deletedEntity = arangoDriver.graphDeleteVertex(GRAPH_NAME, VERTEXT_COLLECTION_NAME,
			v1.getDocumentKey());
		Assert.assertNotNull(deletedEntity);
		Assert.assertTrue(deletedEntity.getDeleted());

		try {
			arangoDriver.graphGetVertex(GRAPH_NAME, VERTEXT_COLLECTION_NAME, v1.getDocumentKey(), Person.class);
			Assert.fail("graphGetVertex should fail");
		} catch (ArangoException ex) {
			Assert.assertEquals(ErrorNums.ERROR_ARANGO_DOCUMENT_NOT_FOUND, ex.getErrorNumber());
		}
	}

	@Test
	public void getEdge() throws ArangoException {

		//
		printHeadline("delete edge");
		//

		VertexEntity<Person> v1 = createVertex(new Person("A", Person.MALE));
		VertexEntity<Person> v2 = createVertex(new Person("B", Person.FEMALE));
		EdgeEntity<Knows> e1 = createEdge(v1, v2, new Knows(1984));

		DeletedEntity deletedEntity = arangoDriver.graphDeleteEdge(GRAPH_NAME, EDGE_COLLECTION_NAME,
			e1.getDocumentKey());
		Assert.assertNotNull(deletedEntity);
		Assert.assertTrue(deletedEntity.getDeleted());

		try {
			arangoDriver.graphGetEdge(GRAPH_NAME, EDGE_COLLECTION_NAME, e1.getDocumentKey(), Knows.class);
			Assert.fail("graphGetEdge should fail");
		} catch (ArangoException ex) {
			Assert.assertEquals(ErrorNums.ERROR_ARANGO_DOCUMENT_NOT_FOUND, ex.getErrorNumber());
		}
	}

	private VertexEntity<Person> createVertex(Person person) throws ArangoException {
		VertexEntity<Person> v = arangoDriver.graphCreateVertex(GRAPH_NAME, VERTEXT_COLLECTION_NAME, person, true);
		Assert.assertNotNull(v);
		return v;
	}

	private EdgeEntity<Knows> createEdge(VertexEntity<Person> personFrom, VertexEntity<Person> personTo, Knows knows)
			throws ArangoException {
		EdgeEntity<Knows> e = arangoDriver.graphCreateEdge(GRAPH_NAME, EDGE_COLLECTION_NAME,
			personFrom.getDocumentHandle(), personTo.getDocumentHandle(), knows, false);
		Assert.assertNotNull(e);
		return e;
	}

}
