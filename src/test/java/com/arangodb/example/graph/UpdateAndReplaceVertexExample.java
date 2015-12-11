package com.arangodb.example.graph;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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

	public ArangoDriver arangoDriver;

	@Before
	public void _before() throws ArangoException {
		removeTestDatabase(DATABASE_NAME);

		arangoDriver = getArangoDriver(getConfiguration());
		createDatabase(arangoDriver, DATABASE_NAME);
		createGraph(arangoDriver, GRAPH_NAME, EDGE_COLLECTION_NAME, VERTEXT_COLLECTION_NAME);
	}

	@Test
	public void replaceVertex() throws ArangoException {

		//
		printHeadline("replace vertex");
		//

		Person personA = new Person("A", Person.MALE);
		VertexEntity<Person> v1 = createVertex(personA);

		Person personB = new Person("B", Person.FEMALE);

		VertexEntity<Person> replaceVertex = arangoDriver.graphReplaceVertex(GRAPH_NAME, VERTEXT_COLLECTION_NAME,
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

		Person personA = new Person("A", Person.MALE);
		VertexEntity<Person> v1 = createVertex(personA);

		// update one attribute (gender)
		personA.setGender(Person.FEMALE);

		VertexEntity<Person> updateVertex = arangoDriver.graphUpdateVertex(GRAPH_NAME, VERTEXT_COLLECTION_NAME,
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

		Person personA = new Person("A", Person.MALE);
		VertexEntity<Person> v1 = createVertex(personA);

		// update one attribute (gender)
		Person update = new Person(null, Person.FEMALE);

		arangoDriver.graphUpdateVertex(GRAPH_NAME, VERTEXT_COLLECTION_NAME, v1.getDocumentKey(), update, true);

		// reload vertex
		VertexEntity<Person> updateVertex = arangoDriver.graphGetVertex(GRAPH_NAME, VERTEXT_COLLECTION_NAME,
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

	private VertexEntity<Person> createVertex(Person person) throws ArangoException {
		VertexEntity<Person> v = arangoDriver.graphCreateVertex(GRAPH_NAME, VERTEXT_COLLECTION_NAME, person, true);
		Assert.assertNotNull(v);
		return v;
	}

}
