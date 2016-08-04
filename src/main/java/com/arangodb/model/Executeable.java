package com.arangodb.model;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import com.arangodb.ArangoException;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public interface Executeable<T> {

	default T execute() throws ArangoException {
		try {
			return executeAsync().get();
		} catch (InterruptedException | ExecutionException e) {
			throw new ArangoException(e);
		}
	}

	CompletableFuture<T> executeAsync();

}
