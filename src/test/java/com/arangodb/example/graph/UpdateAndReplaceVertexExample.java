package com.arangodb.example.graph;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.arangodb.ArangoConfigure;
import com.arangodb.ArangoDriver;
import com.arangodb.ArangoException;
import com.arangodb.entity.marker.VertexEntity;

/**
 * Update and replace example
 * 
 * @author a-brandt
 *
 */
public class UpdateAndReplaceVertexExample extends BaseExample {

	private static final String DATABASE_NAME = "UpdateAndReplaceVertexExample";

	private static final String GRAPH_NAME = "example_graph1";
	private static final String EDGE_COLLECTION_NAME = "edgeColl1";
	private static final String VERTEXT_COLLECTION_NAME = "vertexColl1";

	/**
	 * @param configure
	 * @param driver
	 */
	public UpdateAndReplaceVertexExample(final ArangoConfigure configure, final ArangoDriver driver) {
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
	public void replaceVertex() throws ArangoException {

		//
		printHeadline("replace vertex");
		//

		final Person personA = new Person("A", Person.MALE);
		final VertexEntity<Person> v1 = createVertex(personA);

		final Person personB = new Person("B", Person.FEMALE);

		final VertexEntity<Person> replaceVertex = driver.graphReplaceVertex(GRAPH_NAME, VERTEXT_COLLECTION_NAME,
			v1.getDocumentKey(), personB);
		// document handle is unchanged
		Assert.assertNotNull(replaceVertex);
		Assert.assertEquals(v1.getDocumentHandle(), replaceVertex.getDocumentHandle());
		// document revision has changed
		Assert.assertNotEquals(v1.getDocumentRevision(), replaceVertex.getDocumentRevision());
		// name changed to "B"
		Assert.assertNotNull(replaceVertex.getEntity());
		Assert.assertEquals(personB.getName(), replaceVertex.getEntity().getName());
	}

	@Test
	public void updateVertex() throws ArangoException {

		//
		printHeadline("update vertex");
		//

		final Person personA = new Person("A", Person.MALE);
		final VertexEntity<Person> v1 = createVertex(personA);

		// update one attribute (gender)
		personA.setGender(Person.FEMALE);

		final VertexEntity<Person> updateVertex = driver.graphUpdateVertex(GRAPH_NAME, VERTEXT_COLLECTION_NAME,
			v1.getDocumentKey(), personA, true);

		// document handle is unchanged
		Assert.assertNotNull(updateVertex);
		Assert.assertEquals(v1.getDocumentHandle(), updateVertex.getDocumentHandle());
		// document revision has changed
		Assert.assertNotEquals(v1.getDocumentRevision(), updateVertex.getDocumentRevision());
		// gender changed to Person.FEMALE
		Assert.assertNotNull(updateVertex.getEntity());
		Assert.assertEquals(Person.FEMALE, updateVertex.getEntity().getGender());
		Assert.assertEquals(personA.getName(), updateVertex.getEntity().getName());
	}

	@Test
	public void updateOneAttribute() throws ArangoException {

		//
		printHeadline("update one vertex attribute");
		//

		final Person personA = new Person("A", Person.MALE);
		final VertexEntity<Person> v1 = createVertex(personA);

		// update one attribute (gender)
		final Person update = new Person(null, Person.FEMALE);

		driver.graphUpdateVertex(GRAPH_NAME, VERTEXT_COLLECTION_NAME, v1.getDocumentKey(), update, true);

		// reload vertex
		final VertexEntity<Person> updateVertex = driver.graphGetVertex(GRAPH_NAME, VERTEXT_COLLECTION_NAME,
			v1.getDocumentKey(), Person.class);

		// document handle is unchanged
		Assert.assertNotNull(updateVertex);
		Assert.assertEquals(v1.getDocumentHandle(), updateVertex.getDocumentHandle());
		// document revision has changed
		Assert.assertNotEquals(v1.getDocumentRevision(), updateVertex.getDocumentRevision());
		// gender changed to Person.FEMALE
		Assert.assertNotNull(updateVertex.getEntity());
		Assert.assertEquals(update.getGender(), updateVertex.getEntity().getGender());
		Assert.assertEquals(personA.getName(), updateVertex.getEntity().getName());
	}

	private VertexEntity<Person> createVertex(final Person person) throws ArangoException {
		final VertexEntity<Person> v = driver.graphCreateVertex(GRAPH_NAME, VERTEXT_COLLECTION_NAME, person, true);
		Assert.assertNotNull(v);
		return v;
	}

}
