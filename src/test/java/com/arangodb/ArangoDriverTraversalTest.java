/**
 * Copyright 2004-2015 triAGENS GmbH, Cologne, Germany
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Copyright holder is triAGENS GmbH, Cologne, Germany
 *
 * @author a-brandt
 * @author Copyright 2015, triAGENS GmbH, Cologne, Germany
 */

package com.arangodb;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.arangodb.entity.BaseDocument;
import com.arangodb.entity.BaseEntity;
import com.arangodb.entity.EdgeDefinitionEntity;
import com.arangodb.entity.PathEntity;
import com.arangodb.entity.TraversalEntity;
import com.arangodb.entity.marker.VertexEntity;
import com.arangodb.util.TraversalQueryOptions;

/**
 * @author a-brandt
 */
public class ArangoDriverTraversalTest extends BaseGraphTest {

	private final String graphName = "TraversalTestGraph";
	private final String vertexCollectionName = "person";
	private final String edgeCollectionName = "knows";

	@Override
	@Before
	public void _before() throws ArangoException {
		super._before();

		final List<EdgeDefinitionEntity> edgeDefinitions = new ArrayList<EdgeDefinitionEntity>();
		final EdgeDefinitionEntity edgeDefinition = new EdgeDefinitionEntity();
		edgeDefinition.setCollection(edgeCollectionName);

		final List<String> from = new ArrayList<String>();
		from.add(vertexCollectionName);
		edgeDefinition.setFrom(from);

		final List<String> to = new ArrayList<String>();
		to.add(vertexCollectionName);
		edgeDefinition.setTo(to);

		edgeDefinitions.add(edgeDefinition);

		driver.createGraph(graphName, edgeDefinitions, null, true);

		final VertexEntity<Person> alice = createPerson("Alice");
		final VertexEntity<Person> bob = createPerson("Bob");
		final VertexEntity<Person> charlie = createPerson("Charlie");
		final VertexEntity<Person> dave = createPerson("Dave");
		final VertexEntity<Person> eve = createPerson("Eve");

		driver.graphCreateEdge(graphName, edgeCollectionName, "Alice_knows_Bob", alice.getDocumentHandle(),
			bob.getDocumentHandle());
		driver.graphCreateEdge(graphName, edgeCollectionName, "Bob_knows_Charlie", bob.getDocumentHandle(),
			charlie.getDocumentHandle());
		driver.graphCreateEdge(graphName, edgeCollectionName, "Bob_knows_Dave", bob.getDocumentHandle(),
			dave.getDocumentHandle());
		driver.graphCreateEdge(graphName, edgeCollectionName, "Eve_knows_Alice", eve.getDocumentHandle(),
			alice.getDocumentHandle());
		driver.graphCreateEdge(graphName, edgeCollectionName, "Eve_knows_Bob", eve.getDocumentHandle(),
			bob.getDocumentHandle());
	}

	@SuppressWarnings("rawtypes")
	@Test
	public void test_getTraversal() throws ArangoException {
		final TraversalQueryOptions traversalQueryOptions = new TraversalQueryOptions();

		traversalQueryOptions.setGraphName(graphName);
		traversalQueryOptions.setStartVertex("person/Alice");
		traversalQueryOptions.setDirection(Direction.OUTBOUND);

		final Class<Person> vertexClass = Person.class;
		final Class<Map> edgeClass = Map.class;

		final TraversalEntity<Person, Map> traversal = driver.getTraversal(traversalQueryOptions, vertexClass,
			edgeClass);

		assertThat(traversal, is(notNullValue()));

		final List<VertexEntity<Person>> vertices = traversal.getVertices();
		assertThat(vertices, is(notNullValue()));
		assertThat(vertices.size(), is(4));
		assertThat(vertices.get(0).getEntity().getName(), is("Alice"));
		assertThat(vertices.get(1).getEntity().getName(), is("Bob"));
		assertThat(vertices.get(2).getEntity().getName(), is("Charlie"));
		assertThat(vertices.get(3).getEntity().getName(), is("Dave"));

		final List<PathEntity<Person, Map>> paths = traversal.getPaths();
		assertThat(paths, is(notNullValue()));
		assertThat(paths.size(), is(4));

		// start vertex!
		assertThat(paths.get(0).getEdges().size(), is(0));
		assertThat(paths.get(0).getVertices().size(), is(1));
		assertThat(paths.get(0).getVertices().get(0).getEntity().getName(), is("Alice"));

		assertThat(paths.get(3).getEdges().size(), is(2));
		assertThat(paths.get(3).getVertices().size(), is(3));
	}

	@Test
	public void test_getTraversalWithBaseDocument() throws ArangoException {
		final TraversalQueryOptions traversalQueryOptions = new TraversalQueryOptions();

		traversalQueryOptions.setGraphName(graphName);
		traversalQueryOptions.setStartVertex("person/Alice");
		traversalQueryOptions.setDirection(Direction.OUTBOUND);

		final TraversalEntity<BaseDocument, BaseDocument> traversal = driver.getTraversal(traversalQueryOptions,
			BaseDocument.class, BaseDocument.class);

		assertThat(traversal, is(notNullValue()));

		final List<VertexEntity<BaseDocument>> vertices = traversal.getVertices();
		assertThat(vertices, is(notNullValue()));
		assertThat(vertices.size(), is(4));
		assertThat(vertices.get(0).getEntity().getProperties().size(), is(1));
		assertThat((String) vertices.get(0).getEntity().getAttribute("name"), is("Alice"));
		assertThat((String) vertices.get(1).getEntity().getAttribute("name"), is("Bob"));
		assertThat((String) vertices.get(2).getEntity().getAttribute("name"), is("Charlie"));
		assertThat((String) vertices.get(3).getEntity().getAttribute("name"), is("Dave"));

		final List<PathEntity<BaseDocument, BaseDocument>> paths = traversal.getPaths();
		assertThat(paths, is(notNullValue()));
		assertThat(paths.size(), is(4));

		// start vertex!
		assertThat(paths.get(0).getEdges().size(), is(0));
		assertThat(paths.get(0).getVertices().size(), is(1));
		assertThat((String) paths.get(0).getVertices().get(0).getEntity().getAttribute("name"), is("Alice"));

		assertThat(paths.get(3).getEdges().size(), is(2));
		assertThat(paths.get(3).getVertices().size(), is(3));
	}

	private VertexEntity<Person> createPerson(final String name) throws ArangoException {
		return driver.graphCreateVertex(graphName, vertexCollectionName, name, new Person(name), true);
	}

	public class Person extends BaseEntity {

		private static final long serialVersionUID = 1L;

		private String name;

		public Person(final String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}

		public void setName(final String name) {
			this.name = name;
		}
	}

}
