package com.arangodb.example.graph;

import java.util.Iterator;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.arangodb.ArangoDriver;
import com.arangodb.ArangoException;
import com.arangodb.Direction;
import com.arangodb.EdgeCursor;
import com.arangodb.VertexCursor;
import com.arangodb.entity.EdgeEntity;
import com.arangodb.entity.marker.VertexEntity;
import com.arangodb.util.GraphEdgesOptions;

/**
 * Get vertices and edges example
 * 
 * @author a-brandt
 *
 */
public class GetVerticesAndEdgesExample extends BaseExample {

	private static final String DATABASE_NAME = "GetVerticesAndEdgesExample";

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
	public void getVertex() throws ArangoException {

		//
		printHeadline("get vertex");
		//

		Person personA = new Person("A", Person.MALE);
		VertexEntity<Person> v1 = createVertex(personA);

		VertexEntity<Person> vertexEntity = arangoDriver.graphGetVertex(GRAPH_NAME, VERTEXT_COLLECTION_NAME,
			v1.getDocumentKey(), Person.class);

		Assert.assertNotNull(vertexEntity);
		Assert.assertNotNull(vertexEntity.getEntity());
		Person p = vertexEntity.getEntity();
		Assert.assertEquals(personA.getName(), p.getName());
		Assert.assertNotNull(p.getDocumentHandle());
		Assert.assertNotNull(p.getDocumentKey());
		Assert.assertNotNull(p.getDocumentRevision());
	}

	@Test
	public void getVertices() throws ArangoException {

		//
		printHeadline("get all vertices");
		//

		createVertex(new Person("A", Person.MALE));
		createVertex(new Person("B", Person.FEMALE));

		//
		printHeadline("using the cursor iterator");
		//

		VertexCursor<Person> vertexCursor = arangoDriver.graphGetVertexCursor(GRAPH_NAME, Person.class, null, null,
			null);
		Assert.assertNotNull(vertexCursor);

		Iterator<VertexEntity<Person>> iterator = vertexCursor.iterator();
		while (iterator.hasNext()) {
			VertexEntity<Person> next = iterator.next();
			Assert.assertNotNull(next);
			printPerson(next.getEntity());
		}

		//
		printHeadline("using the cursor entity iterator");
		//

		vertexCursor = arangoDriver.graphGetVertexCursor(GRAPH_NAME, Person.class, null, null, null);
		Assert.assertNotNull(vertexCursor);

		Iterator<Person> entityIterator = vertexCursor.entityIterator();
		while (entityIterator.hasNext()) {
			Person next = entityIterator.next();
			Assert.assertNotNull(next);
			printPerson(next);
		}

		//
		printHeadline("using cursor as list");
		//

		vertexCursor = arangoDriver.graphGetVertexCursor(GRAPH_NAME, Person.class, null, null, null);
		Assert.assertNotNull(vertexCursor);

		List<VertexEntity<Person>> entityList = vertexCursor.asList();

		for (VertexEntity<Person> next : entityList) {
			Assert.assertNotNull(next);
			printPerson(next.getEntity());
		}

		//
		printHeadline("using cursor as entity list");
		//

		vertexCursor = arangoDriver.graphGetVertexCursor(GRAPH_NAME, Person.class, null, null, null);
		Assert.assertNotNull(vertexCursor);

		List<Person> personList = vertexCursor.asEntityList();

		for (Person next : personList) {
			Assert.assertNotNull(next);
			printPerson(next);
		}

	}

	@Test
	public void getVerticesByExample() throws ArangoException {

		//
		printHeadline("get vertices by example");
		//

		createVertex(new Person("A", Person.MALE));
		createVertex(new Person("B", Person.FEMALE));
		createVertex(new Person("C", Person.MALE));

		// get all male persons
		Person example = new Person(null, Person.MALE);

		VertexCursor<Person> vertexCursor = arangoDriver.graphGetVertexCursor(GRAPH_NAME, Person.class, example, null,
			null);
		Assert.assertNotNull(vertexCursor);

		List<Person> personList = vertexCursor.asEntityList();
		Assert.assertEquals(2, personList.size());

		for (Person next : personList) {
			Assert.assertNotNull(next);
			printPerson(next);
		}

	}

	@Test
	public void getEdge() throws ArangoException {

		//
		printHeadline("get edge");
		//

		VertexEntity<Person> v1 = createVertex(new Person("A", Person.MALE));
		VertexEntity<Person> v2 = createVertex(new Person("B", Person.FEMALE));
		Knows knows = new Knows(1984);
		EdgeEntity<Knows> e1 = createEdge(v1, v2, knows);

		EdgeEntity<Knows> edgeEntity = arangoDriver.graphGetEdge(GRAPH_NAME, EDGE_COLLECTION_NAME, e1.getDocumentKey(),
			Knows.class);
		Assert.assertNotNull(edgeEntity);
		Knows entity = edgeEntity.getEntity();
		Assert.assertNotNull(entity);
		Assert.assertNotNull(entity.getDocumentHandle());
		Assert.assertNotNull(entity.getDocumentKey());
		Assert.assertNotNull(entity.getDocumentRevision());
		Assert.assertNotNull(entity.getFromVertexHandle());
		Assert.assertNotNull(entity.getToVertexHandle());
		Assert.assertNotNull(entity.getSince());
		Assert.assertEquals(v1.getDocumentHandle(), entity.getFromVertexHandle());
		Assert.assertEquals(v2.getDocumentHandle(), entity.getToVertexHandle());
		Assert.assertEquals(knows.getSince(), entity.getSince());
	}

	@Test
	public void getEdges() throws ArangoException {

		//
		printHeadline("get all edges");
		//

		VertexEntity<Person> v1 = createVertex(new Person("A", Person.MALE));
		VertexEntity<Person> v2 = createVertex(new Person("B", Person.FEMALE));
		createEdge(v1, v2, new Knows(1984));
		VertexEntity<Person> v3 = createVertex(new Person("C", Person.MALE));
		createEdge(v1, v3, new Knows(1995));
		createEdge(v2, v3, new Knows(2005));

		EdgeCursor<Knows> cursor = arangoDriver.graphGetEdgeCursor(GRAPH_NAME, Knows.class, null, null, null);
		List<Knows> list = cursor.asEntityList();
		Assert.assertEquals(3, list.size());

		for (Knows knows : list) {
			printKnows(knows);
		}
	}

	@Test
	public void getEdgesByVertexExample() throws ArangoException {

		//
		printHeadline("get edges by vertex example");
		//

		VertexEntity<Person> v1 = createVertex(new Person("A", Person.MALE));
		VertexEntity<Person> v2 = createVertex(new Person("B", Person.FEMALE));
		createEdge(v1, v2, new Knows(1984));
		VertexEntity<Person> v3 = createVertex(new Person("C", Person.MALE));
		createEdge(v1, v3, new Knows(1995));
		createEdge(v2, v3, new Knows(2005));

		// get all edges of female persons (inbound and outbound)
		Person example = new Person(null, Person.FEMALE);

		EdgeCursor<Knows> cursor = arangoDriver.graphGetEdgeCursor(GRAPH_NAME, Knows.class, example, null, null);
		List<Knows> list = cursor.asEntityList();
		Assert.assertEquals(2, list.size());

		for (Knows knows : list) {
			printKnows(knows);
		}

		// get all edges of female persons (outbound)
		GraphEdgesOptions graphEdgesOptions = new GraphEdgesOptions().setDirection(Direction.OUTBOUND);

		cursor = arangoDriver.graphGetEdgeCursor(GRAPH_NAME, Knows.class, example, graphEdgesOptions, null);
		list = cursor.asEntityList();
		Assert.assertEquals(1, list.size());

		for (Knows knows : list) {
			printKnows(knows);
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

	private void printPerson(Person person) {
		if (person == null) {
			System.out.println("Person is null");
		} else {
			System.out
					.println("Person " + person.getDocumentKey() + ", " + person.getName() + ", " + person.getGender());
		}
	}

	private void printKnows(Knows knows) {
		if (knows == null) {
			System.out.println("Knows is null");
		} else {
			System.out.println("Knows " + knows.getDocumentKey() + ", " + knows.fromVertexHandle + " -> "
					+ knows.toVertexHandle + " = " + knows.getSince());
		}
	}
}
