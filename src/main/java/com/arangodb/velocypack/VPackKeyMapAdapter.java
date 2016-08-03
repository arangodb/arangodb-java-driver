package com.arangodb.velocypack;

/**
 * @author Mark - mark@arangodb.com
 *
 */
public interface VPackKeyMapAdapter<T> {

	String serialize(T key);

	T deserialize(String key);

}
