package com.arangodb.velocypack.internal.util;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class StringUtil {

	private StringUtil() {
		super();
	}

	public static String toString(final byte[] array, final int offset, final int length) {
		return new String(array, offset, length);
	}

}
