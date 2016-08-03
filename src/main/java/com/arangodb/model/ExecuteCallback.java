package com.arangodb.model;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public interface ExecuteCallback<T> {

	void process(ExecuteResult<T> result);

}
