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

package com.arangodb.example.graph;

import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Test;

import com.arangodb.ArangoCursor;
import com.arangodb.ArangoDBException;

/**
 * Graph traversals in AQL
 * 
 * @see <a href="https://docs.arangodb.com/current/AQL/Graphs/Traversals.html">Graph traversals in AQL</a>
 * 
 * @author a-brandt
 *
 */
public class GraphTraversalsInAQL extends BaseGraphTest {

	@Test
	public void queryAllVertices() throws ArangoDBException {
		String queryString = "FOR v IN 1..3 OUTBOUND 'circles/A' GRAPH 'traversalGraph' RETURN v._key";
		ArangoCursor<String> cursor = db.query(queryString, null, null, String.class);
		Collection<String> collection = toCollection(cursor);
		assertThat(collection.size(), is(10));

		queryString = "FOR v IN 1..3 OUTBOUND 'circles/A' edges RETURN v._key";
		cursor = db.query(queryString, null, null, String.class);
		collection = toCollection(cursor);
		assertThat(collection.size(), is(10));
	}

	@Test
	public void queryDepthTwo() throws ArangoDBException {
		String queryString = "FOR v IN 2..2 OUTBOUND 'circles/A' GRAPH 'traversalGraph' return v._key";
		ArangoCursor<String> cursor = db.query(queryString, null, null, String.class);
		Collection<String> collection = toCollection(cursor);
		assertThat(collection.size(), is(4));
		assertThat(collection, hasItems("C", "E", "H", "J"));

		queryString = "FOR v IN 2 OUTBOUND 'circles/A' GRAPH 'traversalGraph' return v._key";
		cursor = db.query(queryString, null, null, String.class);
		collection = toCollection(cursor);
		assertThat(collection.size(), is(4));
		assertThat(collection, hasItems("C", "E", "H", "J"));
	}

	@Test
	public void queryWithFilter() throws ArangoDBException {
		String queryString = "FOR v, e, p IN 1..3 OUTBOUND 'circles/A' GRAPH 'traversalGraph' FILTER p.vertices[1]._key != 'G' RETURN v._key";
		ArangoCursor<String> cursor = db.query(queryString, null, null, String.class);
		Collection<String> collection = toCollection(cursor);
		assertThat(collection.size(), is(5));
		assertThat(collection, hasItems("B", "C", "D", "E", "F"));

		queryString = "FOR v, e, p IN 1..3 OUTBOUND 'circles/A' GRAPH 'traversalGraph' FILTER p.edges[0].label != 'right_foo' RETURN v._key";
		cursor = db.query(queryString, null, null, String.class);
		collection = toCollection(cursor);
		assertThat(collection.size(), is(5));
		assertThat(collection, hasItems("B", "C", "D", "E", "F"));

		queryString = "FOR v,e,p IN 1..3 OUTBOUND 'circles/A' GRAPH 'traversalGraph' FILTER p.vertices[1]._key != 'G' FILTER p.edges[1].label != 'left_blub' return v._key";
		cursor = db.query(queryString, null, null, String.class);

		collection = toCollection(cursor);
		assertThat(collection.size(), is(3));
		assertThat(collection, hasItems("B", "C", "D"));

		queryString = "FOR v,e,p IN 1..3 OUTBOUND 'circles/A' GRAPH 'traversalGraph' FILTER p.vertices[1]._key != 'G' AND    p.edges[1].label != 'left_blub' return v._key";
		cursor = db.query(queryString, null, null, String.class);
		collection = toCollection(cursor);
		assertThat(collection.size(), is(3));
		assertThat(collection, hasItems("B", "C", "D"));
	}

	@Test
	public void queryOutboundInbound() throws ArangoDBException {
		String queryString = "FOR v IN 1..3 OUTBOUND 'circles/E' GRAPH 'traversalGraph' return v._key";
		ArangoCursor<String> cursor = db.query(queryString, null, null, String.class);
		Collection<String> collection = toCollection(cursor);
		assertThat(collection.size(), is(1));
		assertThat(collection, hasItems("F"));

		queryString = "FOR v IN 1..3 INBOUND 'circles/E' GRAPH 'traversalGraph' return v._key";
		cursor = db.query(queryString, null, null, String.class);
		collection = toCollection(cursor);
		assertThat(collection.size(), is(2));
		assertThat(collection, hasItems("B", "A"));

		queryString = "FOR v IN 1..3 ANY 'circles/E' GRAPH 'traversalGraph' return v._key";
		cursor = db.query(queryString, null, null, String.class);

		collection = toCollection(cursor);
		assertThat(collection.size(), is(6));
		assertThat(collection, hasItems("F", "B", "C", "D", "A", "G"));
	}

	private <T> Collection<T> toCollection(ArangoCursor<T> cursor) {
		List<T> result = new ArrayList<>();
		cursor.iterator().forEachRemaining(result::add);
		return result;
	}

}
