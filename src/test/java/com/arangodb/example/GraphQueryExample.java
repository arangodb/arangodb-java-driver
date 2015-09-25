/*
 * Copyright (C) 2012 tamtam180
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.arangodb.example;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.arangodb.ArangoConfigure;
import com.arangodb.ArangoDriver;
import com.arangodb.ArangoException;
import com.arangodb.Direction;
import com.arangodb.EdgeCursor;
import com.arangodb.VertexCursor;
import com.arangodb.entity.CollectionOptions;
import com.arangodb.entity.CollectionType;
import com.arangodb.entity.EdgeDefinitionEntity;
import com.arangodb.entity.EdgeEntity;
import com.arangodb.entity.GraphEntity;
import com.arangodb.entity.ShortestPathEntity;
import com.arangodb.entity.marker.VertexEntity;
import com.arangodb.util.GraphEdgesOptions;
import com.arangodb.util.ShortestPathOptions;

/**
 * AQL example with new cursor implementation
 * 
 * @author tamtam180 - kirscheless at gmail.com
 * @author a-brandt
 *
 */
public class GraphQueryExample {

	private static final String GRAPH_NAME = "example_graph1";
	private static final String EDGE_COLLECTION_NAME = "edgeColl1";
	private static final String VERTEXT_COLLECTION_NAME = "vertexColl1";

	public static void main(String[] args) {

		// Initialize configure
		ArangoConfigure configure = new ArangoConfigure();
		configure.init();

		// Create Driver (this instance is thread-safe)
		ArangoDriver driver = new ArangoDriver(configure);

		createExampleGraph(driver);

		try {
			// get all vertices
			VertexCursor<Person> vertexCursor = driver.graphGetVertexCursor(GRAPH_NAME, Person.class, null, null, null);
			Iterator<VertexEntity<Person>> vertexIterator = vertexCursor.iterator();
			while (vertexIterator.hasNext()) {
				VertexEntity<Person> entity = vertexIterator.next();
				Person person = entity.getEntity();
				System.out.printf("%20s  %15s%n", entity.getDocumentHandle(), person.name);
			}

			// get vertices by example
			vertexCursor = driver.graphGetVertexCursor(GRAPH_NAME, Person.class, new Person("C"), null, null);
			vertexIterator = vertexCursor.iterator();
			while (vertexIterator.hasNext()) {
				VertexEntity<Person> entity = vertexIterator.next();
				Person person = entity.getEntity();
				System.out.printf("%20s  %15s%n", entity.getDocumentHandle(), person.name);
			}

			// get all edges
			EdgeCursor<Knows> edgeCursor = driver.graphGetEdgeCursor(GRAPH_NAME, Knows.class, null, null, null);
			Iterator<EdgeEntity<Knows>> edgeIterator = edgeCursor.iterator();
			while (edgeIterator.hasNext()) {
				EdgeEntity<Knows> entity = edgeIterator.next();
				Knows knows = entity.getEntity();
				System.out.printf("%20s  %20s->%20s %d%n", entity.getDocumentHandle(), entity.getFromVertexHandle(),
					entity.getToVertexHandle(), knows.getSince());
			}

			// get all outbound edges of a vertex
			edgeCursor = driver.graphGetEdgeCursor(GRAPH_NAME, Knows.class, new Person("C"),
				new GraphEdgesOptions().setDirection(Direction.OUTBOUND), null);
			edgeIterator = edgeCursor.iterator();
			while (edgeIterator.hasNext()) {
				EdgeEntity<Knows> entity = edgeIterator.next();
				Knows knows = entity.getEntity();
				System.out.printf("%20s  %20s->%20s %d%n", entity.getDocumentHandle(), entity.getFromVertexHandle(),
					entity.getToVertexHandle(), knows.getSince());
			}

			// path A -> F
			ShortestPathEntity<Person, Knows> shortestPath = driver.graphGetShortestPath(GRAPH_NAME, new Person("A"),
				new Person("F"), new ShortestPathOptions().setDirection(Direction.OUTBOUND), Person.class, Knows.class);

			printShortestPath(shortestPath);

			// path F -> A (empty result)
			shortestPath = driver.graphGetShortestPath(GRAPH_NAME, new Person("F"), new Person("A"),
				new ShortestPathOptions().setDirection(Direction.OUTBOUND), Person.class, Knows.class);

			printShortestPath(shortestPath);

		} catch (ArangoException e) {
			e.printStackTrace();
		} finally {
			configure.shutdown();
		}

	}

	private static void printShortestPath(ShortestPathEntity<Person, Knows> shortestPath) {
		if (shortestPath.getDistance() > -1) {
			System.out.printf("%s -> %s : distance = %d, getVertices().size() = %d, edges.size() = %d%n", shortestPath
					.getVertices().get(0).getDocumentHandle(),
				shortestPath.getVertices().get(shortestPath.getVertices().size() - 1).getDocumentHandle(),
				shortestPath.getDistance(), shortestPath.getVertices().size(), shortestPath.getEdges().size());
		} else {
			System.out.println("no path");
		}
	}

	private static void createExampleGraph(ArangoDriver driver) {
		try {
			GraphEntity graph = driver.getGraph(GRAPH_NAME);
			if (graph != null) {
				driver.deleteGraph(GRAPH_NAME, true);
			}
		} catch (ArangoException e) {
		}

		// create test collection
		try {
			driver.createCollection(EDGE_COLLECTION_NAME, new CollectionOptions().setType(CollectionType.EDGE));
		} catch (ArangoException e) {
		}
		try {
			driver.createCollection(VERTEXT_COLLECTION_NAME, new CollectionOptions().setType(CollectionType.DOCUMENT));
		} catch (ArangoException e) {
		}

		try {
			EdgeDefinitionEntity ed = new EdgeDefinitionEntity();
			ed.setCollection(EDGE_COLLECTION_NAME);
			ed.getFrom().add(VERTEXT_COLLECTION_NAME);
			ed.getTo().add(VERTEXT_COLLECTION_NAME);
			List<EdgeDefinitionEntity> edgeDefinitions = new ArrayList<EdgeDefinitionEntity>();
			edgeDefinitions.add(ed);

			driver.createGraph(GRAPH_NAME, edgeDefinitions, null, true);

			// remove all elements of test collection
			driver.truncateCollection(EDGE_COLLECTION_NAME);
			driver.truncateCollection(VERTEXT_COLLECTION_NAME);

			// create some persons
			VertexEntity<Person> v1 = driver.graphCreateVertex(GRAPH_NAME, VERTEXT_COLLECTION_NAME, new Person("A"),
				true);
			VertexEntity<Person> v2 = driver.graphCreateVertex(GRAPH_NAME, VERTEXT_COLLECTION_NAME, new Person("B"),
				false);
			VertexEntity<Person> v3 = driver.graphCreateVertex(GRAPH_NAME, VERTEXT_COLLECTION_NAME, new Person("C"),
				false);
			VertexEntity<Person> v4 = driver.graphCreateVertex(GRAPH_NAME, VERTEXT_COLLECTION_NAME, new Person("D"),
				false);
			VertexEntity<Person> v5 = driver.graphCreateVertex(GRAPH_NAME, VERTEXT_COLLECTION_NAME, new Person("C"),
				false);
			VertexEntity<Person> v6 = driver.graphCreateVertex(GRAPH_NAME, VERTEXT_COLLECTION_NAME, new Person("F"),
				false);

			driver.graphCreateEdge(GRAPH_NAME, EDGE_COLLECTION_NAME, v1.getDocumentHandle(), v2.getDocumentHandle(),
				new Knows(1984), false);
			driver.graphCreateEdge(GRAPH_NAME, EDGE_COLLECTION_NAME, v2.getDocumentHandle(), v6.getDocumentHandle(),
				new Knows(1990), false);
			driver.graphCreateEdge(GRAPH_NAME, EDGE_COLLECTION_NAME, v1.getDocumentHandle(), v3.getDocumentHandle(),
				new Knows(1992), false);
			driver.graphCreateEdge(GRAPH_NAME, EDGE_COLLECTION_NAME, v3.getDocumentHandle(), v4.getDocumentHandle(),
				new Knows(1996), false);
			driver.graphCreateEdge(GRAPH_NAME, EDGE_COLLECTION_NAME, v4.getDocumentHandle(), v6.getDocumentHandle(),
				new Knows(2003), false);
			driver.graphCreateEdge(GRAPH_NAME, EDGE_COLLECTION_NAME, v4.getDocumentHandle(), v5.getDocumentHandle(),
				new Knows(2013), false);
			driver.graphCreateEdge(GRAPH_NAME, EDGE_COLLECTION_NAME, v3.getDocumentHandle(), v5.getDocumentHandle(),
				new Knows(2003), false);

		} catch (ArangoException e) {
			e.printStackTrace();
			System.exit(1);
		}

	}

	public static class Person {
		private String name;

		public Person(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

	}

	public static class Knows {
		private Integer since;

		public Knows(Integer since) {
			this.since = since;
		}

		public Integer getSince() {
			return since;
		}

		public void setSince(Integer since) {
			this.since = since;
		}
	}

}
