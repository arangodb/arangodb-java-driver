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

/**
 * Created by fbartels on 10/27/14.
 */
public interface InternalGraphDriver extends BaseDriverInterface {
  GraphEntity createGraph(String databaseName, String graphName, Boolean waitForSync) throws ArangoException;

  GraphEntity createGraph(
    String databaseName,
    String graphName,
    List<EdgeDefinitionEntity> edgeDefinitions,
    List<String> orphanCollections,
    Boolean waitForSync) throws ArangoException;

  GraphEntity createGraph(String databaseName, String documentKey, String vertices, String edges, Boolean waitForSync)
      throws ArangoException;

  GraphsEntity getGraphs(String databaseName) throws ArangoException;

  GraphEntity getGraph(String databaseName, String graphName) throws ArangoException;

  DeletedEntity deleteGraph(String databaseName, String graphName, Boolean dropCollections) throws ArangoException;

  List<String> getVertexCollections(String databaseName, String graphName) throws ArangoException;

  DeletedEntity deleteVertexCollection(
    String databaseName,
    String graphName,
    String collectionName,
    Boolean dropCollection) throws ArangoException;

  GraphEntity createVertexCollection(String databaseName, String graphName, String collectionName)
      throws ArangoException;

  List<String> getEdgeCollections(String databaseName, String graphName) throws ArangoException;

  GraphEntity createNewEdgeDefinition(String databaseName, String graphName, EdgeDefinitionEntity edgeDefinition)
      throws ArangoException;

  GraphEntity replaceEdgeDefinition(
    String databaseName,
    String graphName,
    String edgeName,
    EdgeDefinitionEntity edgeDefinition) throws ArangoException;

  GraphEntity deleteEdgeDefinition(String databaseName, String graphName, String edgeName, Boolean dropCollection)
      throws ArangoException;

  <T> DocumentEntity<T> createVertex(
    String database,
    String graphName,
    String collectionName,
    Object vertex,
    Boolean waitForSync) throws ArangoException;

  <T> DocumentEntity<T> getVertex(
    String database,
    String graphName,
    String collectionName,
    String key,
    Class<?> clazz,
    Long rev,
    Long ifNoneMatchRevision,
    Long ifMatchRevision) throws ArangoException;

  <T> DocumentEntity<T> replaceVertex(
    String database,
    String graphName,
    String collectionName,
    String key,
    Object vertex,
    Boolean waitForSync,
    Long rev,
    Long ifMatchRevision) throws ArangoException;

  DeletedEntity deleteVertex(
    String database,
    String graphName,
    String collectionName,
    String key,
    Boolean waitForSync,
    Long rev,
    Long ifMatchRevision) throws ArangoException;

  // ***********************************

  <T> DocumentEntity<T> createVertex(String database, String graphName, Object vertex, Boolean waitForSync)
      throws ArangoException;

  <T> DocumentEntity<T> getVertex(
    String database,
    String graphName,
    String key,
    Class<?> clazz,
    Long rev,
    Long ifNoneMatchRevision,
    Long ifMatchRevision) throws ArangoException;

  DeletedEntity deleteVertex(
    String database,
    String graphName,
    String key,
    Boolean waitForSync,
    Long rev,
    Long ifMatchRevision) throws ArangoException;

  <T> DocumentEntity<T> replaceVertex(
    String database,
    String graphName,
    String key,
    Object vertex,
    Boolean waitForSync,
    Long rev,
    Long ifMatchRevision) throws ArangoException;

  <T> DocumentEntity<T> updateVertex(
    String database,
    String graphName,
    String key,
    Object vertex,
    Boolean keepNull,
    Boolean waitForSync,
    Long rev,
    Long ifMatchRevision) throws ArangoException;

  <T> CursorEntity<DocumentEntity<T>> getVertices(
    String database,
    String graphName,
    String vertexKey,
    Class<?> clazz,
    Integer batchSize,
    Integer limit,
    Boolean count,
    Direction direction,
    Collection<String> labels,
    FilterCondition... properties) throws ArangoException;

  <T> CursorResultSet<DocumentEntity<T>> getVerticesWithResultSet(
    String database,
    String graphName,
    String vertexKey,
    Class<?> clazz,
    Integer batchSize,
    Integer limit,
    Boolean count,
    Direction direction,
    Collection<String> labels,
    FilterCondition... properties) throws ArangoException;

  <T> EdgeEntity<T> createEdge(
    String database,
    String graphName,
    String key,
    String fromHandle,
    String toHandle,
    Object value,
    String label,
    Boolean waitForSync) throws ArangoException;

  <T> EdgeEntity<T> getEdge(
    String database,
    String graphName,
    String key,
    Class<?> clazz,
    Long rev,
    Long ifNoneMatchRevision,
    Long ifMatchRevision) throws ArangoException;

  DeletedEntity deleteEdge(
    String database,
    String graphName,
    String key,
    Boolean waitForSync,
    Long rev,
    Long ifMatchRevision) throws ArangoException;

  <T> EdgeEntity<T> replaceEdge(
    String database,
    String graphName,
    String key,
    Object value,
    Boolean waitForSync,
    Long rev,
    Long ifMatchRevision) throws ArangoException;

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
    FilterCondition... properties) throws ArangoException;

  <T> CursorResultSet<EdgeEntity<T>> getEdgesWithResultSet(
    String database,
    String graphName,
    String vertexKey,
    Class<?> clazz,
    Integer batchSize,
    Integer limit,
    Boolean count,
    Direction direction,
    Collection<String> labels,
    FilterCondition... properties) throws ArangoException;
}
