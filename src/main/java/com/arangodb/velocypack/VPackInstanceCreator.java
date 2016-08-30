package com.arangodb.velocypack;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public interface VPackInstanceCreator<T> {

	T createInstance();

}
