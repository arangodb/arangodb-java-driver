package com.arangodb.model;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import com.arangodb.ArangoException;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public interface Executeable<T> {

	default T execute() throws ArangoException {
		try {
			return execute(null).get();
		} catch (InterruptedException | ExecutionException e) {
			throw new ArangoException(e);
		}
	}

	Future<T> execute(final ExecuteCallback<T> callback);

}
