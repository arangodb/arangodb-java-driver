package com.arangodb;

import java.util.Map;

import com.arangodb.entity.CursorEntity;
import com.arangodb.entity.DefaultEntity;
import com.arangodb.entity.DocumentEntity;
import com.arangodb.entity.QueriesResultEntity;
import com.arangodb.entity.QueryTrackingPropertiesEntity;
import com.arangodb.entity.ShortestPathEntity;
import com.arangodb.impl.BaseDriverInterface;
import com.arangodb.util.AqlQueryOptions;
import com.arangodb.util.ShortestPathOptions;

/**
 * Created by fbartels on 10/27/14.
 * 
 */
public interface InternalCursorDriver extends BaseDriverInterface {

	CursorEntity<?> validateQuery(String database, String query) throws ArangoException;

	// request without DocumentEntity
	<T> CursorEntity<T> executeCursorEntityQuery(
		String database,
		String query,
		Map<String, Object> bindVars,
		AqlQueryOptions aqlQueryOptions,
		Class<?>... clazz) throws ArangoException;

	<T> CursorEntity<T> continueQuery(String database, long cursorId, Class<?>... clazz) throws ArangoException;

	DefaultEntity finishQuery(String database, long cursorId) throws ArangoException;

	// request a cursor without DocumentEntity
	<T> CursorResult<T> executeAqlQuery(
		String database,
		String query,
		Map<String, Object> bindVars,
		AqlQueryOptions aqlQueryOptions,
		Class<T> clazz) throws ArangoException;

	// request a cursor without DocumentEntity
	CursorRawResult executeAqlQueryRaw(
		String database,
		String query,
		Map<String, Object> bindVars,
		AqlQueryOptions aqlQueryOptions) throws ArangoException;

	// return the raw JSON response from server
	String executeAqlQueryJSON(
		String database,
		String query,
		Map<String, Object> bindVars,
		AqlQueryOptions aqlQueryOptions) throws ArangoException;

	// request a cursor with DocumentEntity
	<T, S extends DocumentEntity<T>> DocumentCursorResult<T, S> executeBaseCursorQuery(
		String database,
		String query,
		Map<String, Object> bindVars,
		AqlQueryOptions aqlQueryOptions,
		Class<S> classDocumentEntity,
		Class<T> clazz) throws ArangoException;

	/**
	 * Get the shortest path from a vertex to another vertex
	 * 
	 * @param database
	 *            the database name
	 * @param graphName
	 *            the graph name
	 * @param startVertexExample
	 *            a start vertex example object (or null)
	 * @param endVertexExample
	 *            a start vertex example object (or null)
	 * @param shortestPathOptions
	 *            shortest path options
	 * @param aqlQueryOptions
	 *            AQL query options
	 * @param vertexClass
	 *            the vertex class
	 * @param edgeClass
	 *            the edge class
	 * @return a ShortestPathEntity object
	 * @throws ArangoException
	 */
	public <V, E> ShortestPathEntity<V, E> getShortestPath(
		String database,
		String graphName,
		Object startVertexExample,
		Object endVertexExample,
		ShortestPathOptions shortestPathOptions,
		AqlQueryOptions aqlQueryOptions,
		Class<V> vertexClass,
		Class<E> edgeClass,
		ArangoDriver driver) throws ArangoException;

	QueryTrackingPropertiesEntity getQueryTrackingProperties(String database) throws ArangoException;

	QueryTrackingPropertiesEntity setQueryTrackingProperties(String database, QueryTrackingPropertiesEntity properties)
			throws ArangoException;

	QueriesResultEntity getCurrentlyRunningQueries(String database) throws ArangoException;

	QueriesResultEntity getSlowQueries(String database) throws ArangoException;

	DefaultEntity deleteSlowQueries(String database) throws ArangoException;

	DefaultEntity killQuery(String database, String id) throws ArangoException;
}
