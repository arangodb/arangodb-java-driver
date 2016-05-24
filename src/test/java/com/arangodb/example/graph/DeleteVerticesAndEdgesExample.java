package com.arangodb.example.graph;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.arangodb.ArangoConfigure;
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

	/**
	 * @param configure
	 * @param driver
	 */
	public DeleteVerticesAndEdgesExample(final ArangoConfigure configure, final ArangoDriver driver) {
		super(configure, driver);
	}

	@Before
	public void _before() throws ArangoException {
		removeTestDatabase(DATABASE_NAME);

		createDatabase(driver, DATABASE_NAME);
		createGraph(driver, GRAPH_NAME, EDGE_COLLECTION_NAME, VERTEXT_COLLECTION_NAME);
	}

	@After
	public void _after() {
		removeTestDatabase(DATABASE_NAME);
	}

	@Test
	public void deleteVertex() throws ArangoException {

		//
		printHeadline("delete vertex");
		//

		final VertexEntity<Person> v1 = createVertex(new Person("A", Person.MALE));

		final DeletedEntity deletedEntity = driver.graphDeleteVertex(GRAPH_NAME, VERTEXT_COLLECTION_NAME,
			v1.getDocumentKey());
		Assert.assertNotNull(deletedEntity);
		Assert.assertTrue(deletedEntity.getDeleted());

		try {
			driver.graphGetVertex(GRAPH_NAME, VERTEXT_COLLECTION_NAME, v1.getDocumentKey(), Person.class);
			Assert.fail("graphGetVertex should fail");
		} catch (final ArangoException ex) {
			Assert.assertEquals(ErrorNums.ERROR_ARANGO_DOCUMENT_NOT_FOUND, ex.getErrorNumber());
		}
	}

	@Test
	public void getEdge() throws ArangoException {

		//
		printHeadline("delete edge");
		//

		final VertexEntity<Person> v1 = createVertex(new Person("A", Person.MALE));
		final VertexEntity<Person> v2 = createVertex(new Person("B", Person.FEMALE));
		final EdgeEntity<Knows> e1 = createEdge(v1, v2, new Knows(1984));

		final DeletedEntity deletedEntity = driver.graphDeleteEdge(GRAPH_NAME, EDGE_COLLECTION_NAME,
			e1.getDocumentKey());
		Assert.assertNotNull(deletedEntity);
		Assert.assertTrue(deletedEntity.getDeleted());

		try {
			driver.graphGetEdge(GRAPH_NAME, EDGE_COLLECTION_NAME, e1.getDocumentKey(), Knows.class);
			Assert.fail("graphGetEdge should fail");
		} catch (final ArangoException ex) {
			Assert.assertEquals(ErrorNums.ERROR_ARANGO_DOCUMENT_NOT_FOUND, ex.getErrorNumber());
		}
	}

	private VertexEntity<Person> createVertex(final Person person) throws ArangoException {
		final VertexEntity<Person> v = driver.graphCreateVertex(GRAPH_NAME, VERTEXT_COLLECTION_NAME, person, true);
		Assert.assertNotNull(v);
		return v;
	}

	private EdgeEntity<Knows> createEdge(
		final VertexEntity<Person> personFrom,
		final VertexEntity<Person> personTo,
		final Knows knows) throws ArangoException {
		final EdgeEntity<Knows> e = driver.graphCreateEdge(GRAPH_NAME, EDGE_COLLECTION_NAME,
			personFrom.getDocumentHandle(), personTo.getDocumentHandle(), knows, false);
		Assert.assertNotNull(e);
		return e;
	}

}
