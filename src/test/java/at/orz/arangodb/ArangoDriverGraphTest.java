/*
 * Copyright (C) 2012,2013 tamtam180
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

package at.orz.arangodb;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.GrayFilter;

import org.junit.Ignore;
import org.junit.Test;

import at.orz.arangodb.entity.DeletedEntity;
import at.orz.arangodb.entity.EdgeDefinitionEntity;
import at.orz.arangodb.entity.EdgeDefinitionsEntity;
import at.orz.arangodb.entity.GraphEntity;
import at.orz.arangodb.entity.GraphsEntity;

/**
 * @author tamtam180 - kirscheless at gmail.com
 * 
 */
public class ArangoDriverGraphTest extends BaseGraphTest {

	public ArangoDriverGraphTest(ArangoConfigure configure, ArangoDriver driver) {
		super(configure, driver);
	}
	
	@Test
	@Ignore
	public void test_get_graphs() throws ArangoException {
		GraphsEntity graphs = driver.getGraphs();
		assertThat(graphs.getGraphs().size(), is(0));
		driver.createGraph("UnitTestGraph1", true);
		driver.createGraph("UnitTestGraph2", true);
		driver.createGraph("UnitTestGraph3", true);
		graphs = driver.getGraphs();
		assertThat(graphs.getGraphs().size(), is(3));
	}
	
	

	@Test
	@Ignore
	public void test_create_graph() throws ArangoException {
		
		String graphName = "unitTestGraph";
		
		List<EdgeDefinitionEntity> edgeDefinitions = new ArrayList<EdgeDefinitionEntity>();
		List<String> orphanCollections = new ArrayList<String>();

		// create
		GraphEntity entity1 = driver.createGraph(graphName, edgeDefinitions, orphanCollections, true);
			
		assertThat(entity1.getCode(), is(201));
		assertThat(entity1.getDocumentRevision(), is(not(0L)));
		assertThat(entity1.getDocumentHandle(), is("_graphs/" + graphName));
		assertThat(entity1.getDocumentKey(), is(graphName));
		assertThat(entity1.getOrphanCollections(), is(orphanCollections));

	}

	@Test
	public void test_create_graph2() throws ArangoException {
		
		String graphName = "unitTestGraph";
		
		List<EdgeDefinitionEntity> edgeDefinitions = this.createEdgeDefinitions(2);

		List<String> orphanCollections = this.createOrphanCollections(2);

		// create
		GraphEntity graph = driver.createGraph(graphName, edgeDefinitions, orphanCollections, true);
		assertThat(graph.getCode(), is(201));
		assertThat(graph.getDocumentRevision(), is(not(0L)));
		assertThat(graph.getDocumentHandle(), is("_graphs/" + graphName));
		assertThat(graph.getName(), is(graphName));
		assertThat(graph.getOrphanCollections(), is(orphanCollections));
	}
	
	@Test
	@Ignore
	public void test_get_graph() throws ArangoException {
	    String graphName = "UnitTestGraph";
	    driver.createGraph(graphName, this.createEdgeDefinitions(2), this.createOrphanCollections(2), true);
	    GraphEntity graph = driver.getGraph(graphName);
        assertThat(graph.getOrphanCollections().size(), is(2));
        assertThat(graph.getName(), is(graphName));
        assertThat(graph.getEdgeDefinitions().size(), is(2));
        assertThat(graph.getEdgeDefinitions().get(0).getCollection().startsWith("edge"), is(true));

	}
	
	
/*
	@Test
	public void test_drop_Graph() throws ArangoException {
		String graphName = "unitTestGraph";
		
		// create
		GraphEntity entity1 = driver.createGraph(
				graphName,
				new ArrayList<EdgeDefinitionEntity>(),
				new ArrayList<String>(),
				true
				);
			
		assertThat(entity1.getCode(), is(201));
		assertThat(entity1.getDocumentRevision(), is(not(0L)));
		assertThat(entity1.getDocumentHandle(), is("_graphs/" + graphName));
		
	}
	
	
	
	
	
	@Test
	@Ignore
	public void test_create_graph_202() throws ArangoException {

		// in 1.4.0 manual
		// 202 is returned if the graph was created successfully and waitForSync
		// was false.

		GraphEntity entity = driver.createGraph("g1", "vcol1", "ecol1", false);
		assertThat(entity.getCode(), is(202));
		assertThat(entity.getDocumentRevision(), is(not(0L)));
		assertThat(entity.getDocumentHandle(), is("_graphs/g1"));
		assertThat(entity.getDocumentKey(), is("g1"));
//		assertThat(entity.getVertices(), is("vcol1"));
//		assertThat(entity.getEdges(), is("ecol1"));

	}

	@Test
	@Ignore
	public void test_create_graph_dup() throws ArangoException {

		GraphEntity entity = driver.createGraph("g1", "vcol1", "ecol1", true);
		assertThat(entity.getCode(), is(201));
		assertThat(entity.getDocumentRevision(), is(not(0L)));
		assertThat(entity.getDocumentHandle(), is("_graphs/g1"));
		assertThat(entity.getDocumentKey(), is("g1"));
//		assertThat(entity.getVertices(), is("vcol1"));
//		assertThat(entity.getEdges(), is("ecol1"));

		try {
			driver.createGraph("g1", "vcol1", "ecol1", false);
			fail();
		} catch (ArangoException e) {
			assertThat(e.getCode(), is(400));
			assertThat(e.getErrorNumber(), is(1902)); // graph with the name
														// already exists
		}

	}

	// TODO: errorNum: 1902 : "found graph but has different <name>"

	@Test
	@Ignore
	public void get_graphs() throws ArangoException {

		driver.createGraph("g1", "v1", "e1", null);
		driver.createGraph("g2", "v2", "e2", null);
		driver.createGraph("g3", "v3", "e3", null);

		GraphsEntity graphs = driver.getGraphs();
		assertThat(graphs.isError(), is(false));
		assertThat(graphs.getCode(), is(200));
		assertThat(graphs.getGraphs().size(), is(3));

		Collections.sort(graphs.getGraphs(), new Comparator<GraphEntity>() {
			public int compare(GraphEntity o1, GraphEntity o2) {
				return o1.getDocumentKey().compareTo(o2.getDocumentKey());
			}
		});

		GraphEntity g = graphs.getGraphs().get(0);
		assertThat(g.getDocumentRevision(), is(not(0L)));
		assertThat(g.getDocumentHandle(), is("_graphs/g1"));
		assertThat(g.getDocumentKey(), is("g1"));
//		assertThat(g.getVertices(), is("v1"));
//		assertThat(g.getEdges(), is("e1"));

		g = graphs.getGraphs().get(1);
		assertThat(g.getDocumentRevision(), is(not(0L)));
		assertThat(g.getDocumentHandle(), is("_graphs/g2"));
		assertThat(g.getDocumentKey(), is("g2"));
//		assertThat(g.getVertices(), is("v2"));
//		assertThat(g.getEdges(), is("e2"));

		g = graphs.getGraphs().get(2);
		assertThat(g.getDocumentRevision(), is(not(0L)));
		assertThat(g.getDocumentHandle(), is("_graphs/g3"));
		assertThat(g.getDocumentKey(), is("g3"));
//		assertThat(g.getVertices(), is("v3"));
//		assertThat(g.getEdges(), is("e3"));

	}

	@Test
	@Ignore
	public void test_get_graphOLD() throws ArangoException {

		driver.createGraph("g1", "v1", "e1", null);
		GraphEntity g1 = driver.getGraph("g1");
		assertThat(g1.getDocumentRevision(), is(not(0L)));
		assertThat(g1.getDocumentHandle(), is("_graphs/g1"));
		assertThat(g1.getDocumentKey(), is("g1"));
//		assertThat(g1.getVertices(), is("v1"));
//		assertThat(g1.getEdges(), is("e1"));

	}

	@Test
	public void test_get_graph_404() throws ArangoException {

		try {
			driver.getGraph("g1");
			fail();
		} catch (ArangoException e) {
			assertThat(e.getCode(), is(404));
			assertThat(e.getErrorNumber(), is(1901));
		}

	}

	@Test
	public void test_get_graph_none_match_eq() throws ArangoException {

		GraphEntity g = driver.createGraph("g1", "v1", "e1", null);
		GraphEntity g1 = driver.getGraph("g1", g.getDocumentRevision(), null);
		assertThat(g1.getStatusCode(), is(304));
		assertThat(g1.isNotModified(), is(true));

	}

	@Test
	public void test_get_graph_none_match_ne() throws ArangoException {

		GraphEntity g = driver.createGraph("g1", "v1", "e1", null);

		GraphEntity g1 = driver.getGraph("g1", g.getDocumentRevision() + 1, null);
		assertThat(g1.isNotModified(), is(false));
		assertThat(g1.getDocumentRevision(), is(not(0L)));
		assertThat(g1.getDocumentHandle(), is("_graphs/g1"));
		assertThat(g1.getDocumentKey(), is("g1"));
//		assertThat(g1.getVertices(), is("v1"));
//		assertThat(g1.getEdges(), is("e1"));

	}

	@Test
	public void test_get_graph_match_eq() throws ArangoException {

		GraphEntity g = driver.createGraph("g1", "v1", "e1", null);

		GraphEntity g1 = driver.getGraph("g1", null, g.getDocumentRevision());
		assertThat(g1.isNotModified(), is(false));
		assertThat(g1.getDocumentRevision(), is(not(0L)));
		assertThat(g1.getDocumentHandle(), is("_graphs/g1"));
		assertThat(g1.getDocumentKey(), is("g1"));
//		assertThat(g1.getVertices(), is("v1"));
//		assertThat(g1.getEdges(), is("e1"));

	}

	@Test
	public void test_get_graph_match_ne() throws ArangoException {

		GraphEntity g = driver.createGraph("g1", "v1", "e1", null);

		try {
			driver.getGraph("g1", null, g.getDocumentRevision() + 1);
			fail();
		} catch (ArangoException e) {
			assertThat(e.getCode(), is(412));
			assertThat(e.getErrorNumber(), is(1901)); // wrong revision
		}

	}

	@Test
	public void test_delete_graph() throws ArangoException {

		GraphEntity g1 = driver.createGraph("g1", "v1", "e1", false);
		assertThat(g1.getCode(), is(201));

		DeletedEntity del = driver.deleteGraph("g1");
		assertThat(del.getCode(), is(200));
		assertThat(del.getDeleted(), is(true));

	}

	@Test
	public void test_delete_graph_404() throws ArangoException {

		try {
			driver.deleteGraph("g1");
			fail();
		} catch (ArangoException e) {
			assertThat(e.getCode(), is(404));
			assertThat(e.getErrorNumber(), is(1901));
		}

	}

	@Test
	public void test_delete_graph_ifmatch_ok() throws ArangoException {

		GraphEntity g1 = driver.createGraph("g1", "v1", "e1", false);
		assertThat(g1.getCode(), is(201));

		DeletedEntity del = driver.deleteGraph("g1", g1.getDocumentRevision());
		assertThat(del.getCode(), is(200));
		assertThat(del.getDeleted(), is(true));

	}

	@Test
	public void test_delete_graph_ifmatch_ng_412() throws ArangoException {

		GraphEntity g1 = driver.createGraph("g1", "v1", "e1", false);
		assertThat(g1.getCode(), is(201));

		try {
			driver.deleteGraph("g1", 10L);
			fail();
		} catch (ArangoException e) {
			assertThat(e.getCode(), is(412));
			assertThat(e.getErrorNumber(), is(1901)); // wrong revision
			assertThat(e.getMessage(), is("[1901]wrong revision"));
		}

	}
	*/
    private List<EdgeDefinitionEntity> createEdgeDefinitions(int count) {
        List<EdgeDefinitionEntity> edgeDefinitions = new ArrayList<EdgeDefinitionEntity>(); 
        for (int i = 1; i<=count; i++) {
            EdgeDefinitionEntity edgeDefinition = new EdgeDefinitionEntity();
            edgeDefinition.setCollection("edge" + i);
            List<String> from = new ArrayList<String>();
            from.add("from" + i + "-1");
            from.add("from" + i + "-2");
            from.add("from" + i + "-3");
            edgeDefinition.setFrom(from);
            List<String> to = new ArrayList<String>();
            to.add("to" + i + "-1");
            to.add("to" + i + "-2");
            to.add("to" + i + "-3");
            edgeDefinition.setTo(to);
            edgeDefinitions.add(edgeDefinition);
        }
        return edgeDefinitions;
    }

    private List<String> createOrphanCollections(int count) {
        List<String> orphanCollections = new ArrayList<String>(); 
        for (int i = 1; i<=count; i++) {
            orphanCollections.add("orphan" + i);
        }
        return orphanCollections;
    }

}
