package com.arangodb;

import java.util.Iterator;
import java.util.List;

import com.arangodb.entity.DocumentEntity;

/**
 * @author a-brandt
 */
public class BaseCursorProxy<T, S extends DocumentEntity<T>> implements Iterable<S> {

	private BaseCursor<T, S> baseCursor;

	public BaseCursorProxy(BaseCursor<T, S> baseCursor) {
		this.baseCursor = baseCursor;
	}

	@Override
	public Iterator<S> iterator() {
		return baseCursor.iterator();
	}

	/**
	 * Returns an iterator over a entity set of elements of type T.
	 * 
	 * @return an Iterator.
	 */
	public Iterator<T> entityIterator() {
		return baseCursor.entityIterator();
	}

	/**
	 * Returns the DocumentEntity objects as a list
	 * 
	 * @return list of DocumentEntity objects
	 */
	public List<S> asList() {
		return baseCursor.asList();
	}

	/**
	 * Returns the entities of DocumentEntity objects as a list
	 * 
	 * @return list of DocumentEntity objects
	 */
	public List<T> asEntityList() {
		return baseCursor.asEntityList();
	}

	/**
	 * Close cursor (removes cursor from database)
	 * 
	 * @throws ArangoException
	 */
	public void close() throws ArangoException {
		baseCursor.close();
	}

	/**
	 * Get total number of results (if requested)
	 * 
	 * @return total number of results
	 */
	public int getCount() {
		return baseCursor.getCount();
	}

	/**
	 * Get total number of results for queries with LIMIT clause
	 * 
	 * @return total number of results
	 */
	public int getFullCount() {
		return baseCursor.getFullCount();
	}

	/**
	 * Return a single instance that matches the query, or null if the query
	 * returns no results.
	 * 
	 * Throws NonUniqueResultException (RuntimeException) if there is more than
	 * one matching result
	 * 
	 * @return the single result or null
	 */
	public S getUniqueResult() {
		return baseCursor.getUniqueResult();
	}

	/**
	 * If the resource has been modified it returns true
	 *
	 * @return boolean
	 */
	public boolean isNotModified() {
		return baseCursor.getEntity().isNotModified();
	}

	/**
	 * If the request is unauthorized this returns true
	 *
	 * @return boolean
	 */
	public boolean isUnauthorized() {
		return baseCursor.getEntity().isUnauthorized();
	}

	public boolean isError() {
		return baseCursor.getEntity().isError();
	}

	public int getCode() {
		return baseCursor.getEntity().getCode();
	}

	public int getErrorNumber() {
		return baseCursor.getEntity().getErrorNumber();
	}

	public String getErrorMessage() {
		return baseCursor.getEntity().getErrorMessage();
	}

	public long getEtag() {
		return baseCursor.getEntity().getEtag();
	}

	public int getStatusCode() {
		return baseCursor.getEntity().getStatusCode();
	}

	public String getRequestId() {
		return baseCursor.getEntity().getRequestId();
	}

	/**
	 * Returns true if the cursor can load more data from the database
	 * 
	 * @return true, if the cursor can load more data from the database
	 */
	public boolean hasMore() {
		return baseCursor.getEntity().hasMore();
	}

	/**
	 * Returns the cursor identifier
	 * 
	 * @return the cursor identifier
	 */
	public Long getCursorId() {
		return baseCursor.getEntity().getCursorId();
	}

}
