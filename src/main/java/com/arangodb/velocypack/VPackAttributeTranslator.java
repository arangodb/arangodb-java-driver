package com.arangodb.velocypack;

import com.arangodb.velocypack.exception.VPackException;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public interface VPackAttributeTranslator {

	void add(String attribute, int key) throws VPackException;

	void seal() throws VPackException;

	VPackSlice translate(String attribute);

	VPackSlice translate(int key);

}
