/**
 * Copyright 2004-2014 triAGENS GmbH, Cologne, Germany
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
 * @author fbartels
 * @author gschwab
 * @author Copyright 2014, triAGENS GmbH, Cologne, Germany
 */

package com.arangodb;

import java.util.Collection;
import java.util.List;

import com.arangodb.entity.CursorEntity;
import com.arangodb.entity.DeletedEntity;
import com.arangodb.entity.Direction;
import com.arangodb.entity.DocumentEntity;
import com.arangodb.entity.EdgeDefinitionEntity;
import com.arangodb.entity.EdgeEntity;
import com.arangodb.entity.FilterCondition;
import com.arangodb.entity.GraphEntity;
import com.arangodb.entity.GraphsEntity;
import com.arangodb.impl.BaseDriverInterface;

public interface InternalGraphDriver extends BaseDriverInterface {

  /**
   * Creates an empty graph.
   * 
   * @param databaseName
   * @param graphName
   * @param waitForSync
   * @return GraphEntity
   * @throws ArangoException
   */
  GraphEntity createGraph(String databaseName, String graphName, Boolean waitForSync) throws ArangoException;

  /**
   * Creates a graph.
   * 
   * @param databaseName
   * @param graphName
   * @param edgeDefinitions
   * @param orphanCollections
   * @param waitForSync
   * @return GraphEntity
   * @throws ArangoException
   */
  GraphEntity createGraph(
    String databaseName,
    String graphName,
    List<EdgeDefinitionEntity> edgeDefinitions,
    List<String> orphanCollections,
    Boolean waitForSync) throws ArangoException;

  /**
   * Returns a GraphsEntity containing all graph as GraphEntity object.
   * 
   * @param databaseName
   * @return GraphsEntity
   * @throws ArangoException
   */
  GraphsEntity getGraphs(String databaseName) throws ArangoException;

  /**
   * Creates a list of the names of all available graphs.
   * 
   * @param databaseName
   * @return List<String>
   * @throws ArangoException
   */
  List<String> getGraphList(String databaseName) throws ArangoException;

  /**
   * Get graph object by name, including its edge definitions and vertex
   * collections.
   * 
   * @param databaseName
   * @param graphName
   * @return GraphEntity
   * @throws ArangoException
   */
  GraphEntity getGraph(String databaseName, String graphName) throws ArangoException;

  /**
   * Delete a graph by its name. If dropCollections is true, all collections of
   * the graph will be dropped, if they are not used in another graph.
   * 
   * @param databaseName
   * @param graphName
   * @param dropCollections
   * @return DeletedEntity
   * @throws ArangoException
   */
  DeletedEntity deleteGraph(String databaseName, String graphName, Boolean dropCollections) throws ArangoException;

  /**
   * Returns a list of names of all vertex collections of a graph, defined in
   * the graphs edgeDefinitions (in "from", "to", and "orphanCollections")
   *
   * @param databaseName
   * @param graphName
   * @return List<String>
   * @throws ArangoException
   */
  List<String> getVertexCollections(String databaseName, String graphName) throws ArangoException;

  /**
   * 
   * @param databaseName
   * @param graphName
   * @param collectionName
   * @return GraphEntity
   * @throws ArangoException
   */
  GraphEntity createVertexCollection(String databaseName, String graphName, String collectionName)
      throws ArangoException;

  /**
   * 
   * @param databaseName
   * @param graphName
   * @param collectionName
   * @param dropCollection
   * @return DeletedEntity
   * @throws ArangoException
   */
  DeletedEntity deleteVertexCollection(
    String databaseName,
    String graphName,
    String collectionName,
    Boolean dropCollection) throws ArangoException;

  /**
   * Returns a list of names of all edge collections of a graph that are defined
   * in the graphs edgeDefinitions
   * 
   * @param databaseName
   * @param graphName
   * @return List<String>
   * @throws ArangoException
   */
  List<String> getEdgeCollections(String databaseName, String graphName) throws ArangoException;

  /**
   * Adds a new edge definition to an existing graph.
   * 
   * @param databaseName
   * @param graphName
   * @param edgeDefinition
   * @return GraphEntity
   * @throws ArangoException
   */
  GraphEntity createEdgeDefinition(String databaseName, String graphName, EdgeDefinitionEntity edgeDefinition)
      throws ArangoException;

  /**
   * Replaces an existing edge definition to an existing graph. This will also
   * change the edge definitions of all other graphs using this definition as
   * well.
   * 
   * @param databaseName
   * @param graphName
   * @param edgeName
   * @param edgeDefinition
   * @return GraphEntity
   * @throws ArangoException
   */
  GraphEntity replaceEdgeDefinition(
    String databaseName,
    String graphName,
    String edgeName,
    EdgeDefinitionEntity edgeDefinition) throws ArangoException;

  /**
   * Removes an existing edge definition from this graph. All data stored in the
   * collections is dropped as well as long as it is not used in other graphs.
   * 
   * @param databaseName
   * @param graphName
   * @param edgeName
   * @param dropCollection
   * @return GraphEntity
   * @throws ArangoException
   */
  GraphEntity deleteEdgeDefinition(String databaseName, String graphName, String edgeName, Boolean dropCollection)
      throws ArangoException;

  /**
   * Stores a new vertex with the information contained within the document into
   * the given collection.
   * 
   * @param database
   * @param graphName
   * @param collectionName
   * @param vertex
   * @param waitForSync
   * @return <T> DocumentEntity<T>
   * @throws ArangoException
   */
  <T> DocumentEntity<T> createVertex(
    String database,
    String graphName,
    String collectionName,
    T vertex,
    Boolean waitForSync) throws ArangoException;

  /**
   * Gets a vertex with the given key if it is contained within your graph.
   * 
   * @param database
   * @param graphName
   * @param collectionName
   * @param key
   * @param clazz
   * @param ifMatchRevision
   * @param ifNoneMatchRevision
   * @return
   * @throws ArangoException
   */
  <T> DocumentEntity<T> getVertex(
    String database,
    String graphName,
    String collectionName,
    String key,
    Class<?> clazz,
    Long ifMatchRevision,
    Long ifNoneMatchRevision) throws ArangoException;

  /**
   * Replaces a vertex with the given key by the content in the body. This will
   * only run successfully if the vertex is contained within the graph.
   * 
   * @param database
   * @param graphName
   * @param collectionName
   * @param key
   * @param vertex
   * @param waitForSync
   * @param ifMatchRevision
   * @param ifNoneMatchRevision
   * @return <T> DocumentEntity<T>
   * @throws ArangoException
   */
  <T> DocumentEntity<T> replaceVertex(
    String database,
    String graphName,
    String collectionName,
    String key,
    Object vertex,
    Boolean waitForSync,
    Long ifMatchRevision,
    Long ifNoneMatchRevision) throws ArangoException;

  /**
   * Updates a vertex with the given key by adding the content in the body. This
   * will only run successfully if the vertex is contained within the graph.
   * 
   * @param databaseName
   * @param graphName
   * @param collectionName
   * @param key
   * @param vertex
   * @param keepNull
   * @param waitForSync
   * @param ifMatchRevision
   * @param ifNoneMatchRevision
   * @return
   * @throws ArangoException
   */
  <T> DocumentEntity<T> updateVertex(
    String databaseName,
    String graphName,
    String collectionName,
    String key,
    Object vertex,
    Boolean keepNull,
    Boolean waitForSync,
    Long ifMatchRevision,
    Long ifNoneMatchRevision) throws ArangoException;

  /**
   * Deletes a vertex with the given key, if it is contained within the graph.
   * Furthermore all edges connected to this vertex will be deleted.
   * 
   * @param database
   * @param graphName
   * @param collectionName
   * @param key
   * @param waitForSync
   * @param ifMatchRevision
   * @param ifNoneMatchRevision
   * @return
   * @throws ArangoException
   */
  DeletedEntity deleteVertex(
    String database,
    String graphName,
    String collectionName,
    String key,
    Boolean waitForSync,
    Long ifMatchRevision,
    Long ifNoneMatchRevision) throws ArangoException;

  /**
   * 
   * 
   * @param database
   * @param graphName
   * @param edgeCollectionName
   * @param fromHandle
   * @param toHandle
   * @param value
   * @param waitForSync
   * @return <T> EdgeEntity<T>
   * @throws ArangoException
   */
  <T> EdgeEntity<T> createEdge(
    String database,
    String graphName,
    String edgeCollectionName,
    String fromHandle,
    String toHandle,
    Object value,
    Boolean waitForSync) throws ArangoException;

  /**
   * 
   * 
   * @param database
   * @param graphName
   * @param edgeCollectionName
   * @param key
   * @param fromHandle
   * @param toHandle
   * @param value
   * @param waitForSync
   * @return <T> EdgeEntity<T>
   * @throws ArangoException
   */
  <T> EdgeEntity<T> createEdge(
    String database,
    String graphName,
    String edgeCollectionName,
    String key,
    String fromHandle,
    String toHandle,
    Object value,
    Boolean waitForSync) throws ArangoException;

  /**
   * Loads an edge with the given key if it is contained within your graph.
   * 
   * @param database
   * @param graphName
   * @param edgeCollectionName
   * @param key
   * @param clazz
   * @param rev
   * @param ifNoneMatchRevision
   * @param ifMatchRevision
   * @return <T> EdgeEntity<T>
   * @throws ArangoException
   */
  <T> EdgeEntity<T> getEdge(
    String database,
    String graphName,
    String edgeCollectionName,
    String key,
    Class<?> clazz,
    Long ifMatchRevision,
    Long ifNoneMatchRevision) throws ArangoException;

  /**
   * Deletes an edge with the given id, if it is contained within the graph.
   * 
   * @param database
   * @param graphName
   * @param edgeCollectionName
   * @param key
   * @param waitForSync
   * @param rev
   * @param ifMatchRevision
   * @return DeletedEntity
   * @throws ArangoException
   */
  DeletedEntity deleteEdge(
    String database,
    String graphName,
    String edgeCollectionName,
    String key,
    Boolean waitForSync,
    Long rev,
    Long ifMatchRevision) throws ArangoException;

  /**
   * Replaces an edge with the given key by the content in the body. This will
   * only run successfully if the edge is contained within the graph.
   * 
   * @param database
   * @param graphName
   * @param edgeCollectionName
   * @param key
   * @param value
   * @param waitForSync
   * @param ifMatchRevision
   * @param ifNoneMatchRevision
   * @return <T> EdgeEntity<T>
   * @throws ArangoException
   */
  <T> EdgeEntity<T> replaceEdge(
    String database,
    String graphName,
    String edgeCollectionName,
    String key,
    Object value,
    Boolean waitForSync,
    Long ifMatchRevision,
    Long ifNoneMatchRevision) throws ArangoException;

  /**
   * Updates an edge with the given key by adding the content in the body. This
   * will only run successfully if the edge is contained within the graph.
   * 
   * @param database
   * @param graphName
   * @param edgeCollectionName
   * @param key
   * @param value
   * @param waitForSync
   * @param keepNull
   * @param ifMatchRevision
   * @param ifNoneMatchRevision
   * @return <T> EdgeEntity<T>
   * @throws ArangoException
   */
  <T> EdgeEntity<T> updateEdge(
    String database,
    String graphName,
    String edgeCollectionName,
    String key,
    Object value,
    Boolean waitForSync,
    Boolean keepNull,
    Long ifMatchRevision,
    Long ifNoneMatchRevision) throws ArangoException;

  <T> CursorEntity<EdgeEntity<T>> getEdges(
    String database,
    String graphName,
    String vertexKey,
    Class<?> clazz,
    Integer batchSize,
    Integer limit,
    Boolean count,
    Direction direction,
    Collection<String> labels,
    ArangoDriver driver,
    FilterCondition... properties) throws ArangoException;

}
