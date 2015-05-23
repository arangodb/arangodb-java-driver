package com.arangodb;

import java.util.Map;

import com.arangodb.entity.CursorEntity;
import com.arangodb.entity.DefaultEntity;
import com.arangodb.entity.DocumentEntity;
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
	public <V, E> ShortestPathEntity<V, E> getShortesPath(
		String database,
		String graphName,
		Object startVertexExample,
		Object endVertexExample,
		ShortestPathOptions shortestPathOptions,
		AqlQueryOptions aqlQueryOptions,
		Class<V> vertexClass,
		Class<E> edgeClass) throws ArangoException;

	@Deprecated
	<T> CursorEntity<T> executeQuery(
		String database,
		String query,
		Map<String, Object> bindVars,
		Class<T> clazz,
		Boolean calcCount,
		Integer batchSize,
		Boolean fullCount) throws ArangoException;

	@Deprecated
	<T> CursorResultSet<T> executeQueryWithResultSet(
		String database,
		String query,
		Map<String, Object> bindVars,
		Class<T> clazz,
		Boolean calcCount,
		Integer batchSize,
		Boolean fullCount) throws ArangoException;

	@Deprecated
	<T> CursorResultSet<T> executeQueryWithResultSet(
		String database,
		String query,
		Map<String, Object> bindVars,
		Class<T> clazz,
		Boolean calcCount,
		Integer batchSize) throws ArangoException;
}
