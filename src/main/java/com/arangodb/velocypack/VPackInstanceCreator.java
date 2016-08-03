package com.arangodb.velocypack;

/**
 * @author Mark - mark@arangodb.com
 *
 */
public interface VPackInstanceCreator<T> {

	T createInstance();

}
