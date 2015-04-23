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

}
