package com.arangodb.model;

import java.util.Optional;

import com.arangodb.ArangoException;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public interface ExecuteResult<T> {

	default boolean isSuccess() {
		return getResult().isPresent();
	}

	Optional<ArangoException> getException();

	Optional<T> getResult();

}
