package com.arangodb.velocypack.internal.util;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class ObjectArrayUtil {

	private static final Map<Byte, Integer> FIRST_SUB_MAP;

	static {
		FIRST_SUB_MAP = new HashMap<Byte, Integer>();

		FIRST_SUB_MAP.put((byte) 0x00, 0); // None
		FIRST_SUB_MAP.put((byte) 0x01, 1); // empty array
		FIRST_SUB_MAP.put((byte) 0x02, 2); // array without index table
		FIRST_SUB_MAP.put((byte) 0x03, 3); // array without index table
		FIRST_SUB_MAP.put((byte) 0x04, 5); // array without index table
		FIRST_SUB_MAP.put((byte) 0x05, 9); // array without index table
		FIRST_SUB_MAP.put((byte) 0x06, 3); // array with index table
		FIRST_SUB_MAP.put((byte) 0x07, 5); // array with index table
		FIRST_SUB_MAP.put((byte) 0x08, 9); // array with index table
		FIRST_SUB_MAP.put((byte) 0x09, 9); // array with index table
		FIRST_SUB_MAP.put((byte) 0x0a, 1); // empty object
		FIRST_SUB_MAP.put((byte) 0x0b, 3); // object with sorted index table
		FIRST_SUB_MAP.put((byte) 0x0c, 5); // object with sorted index table
		FIRST_SUB_MAP.put((byte) 0x0d, 9); // object with sorted index table
		FIRST_SUB_MAP.put((byte) 0x0e, 9); // object with sorted index table
		FIRST_SUB_MAP.put((byte) 0x0f, 3); // object with unsorted index table
		FIRST_SUB_MAP.put((byte) 0x10, 5); // object with unsorted index table
		FIRST_SUB_MAP.put((byte) 0x11, 9); // object with unsorted index table
		FIRST_SUB_MAP.put((byte) 0x12, 9); // object with unsorted index table
	}

	public static int getFirstSubMap(final byte key) {
		return FIRST_SUB_MAP.get(key);
	}

	private static final Map<Byte, Integer> OFFSET_SIZE;
	static {
		OFFSET_SIZE = new HashMap<Byte, Integer>();

		OFFSET_SIZE.put((byte) 0x00, 0); // None
		OFFSET_SIZE.put((byte) 0x01, 1); // empty array
		OFFSET_SIZE.put((byte) 0x02, 1); // array without index table
		OFFSET_SIZE.put((byte) 0x03, 2); // array without index table
		OFFSET_SIZE.put((byte) 0x04, 4); // array without index table
		OFFSET_SIZE.put((byte) 0x05, 8); // array without index table
		OFFSET_SIZE.put((byte) 0x06, 1); // array with index table
		OFFSET_SIZE.put((byte) 0x07, 2); // array with index table
		OFFSET_SIZE.put((byte) 0x08, 4); // array with index table
		OFFSET_SIZE.put((byte) 0x09, 8); // array with index table
		OFFSET_SIZE.put((byte) 0x0a, 1); // empty object
		OFFSET_SIZE.put((byte) 0x0b, 1); // object with sorted index table
		OFFSET_SIZE.put((byte) 0x0c, 2); // object with sorted index table
		OFFSET_SIZE.put((byte) 0x0d, 4); // object with sorted index table
		OFFSET_SIZE.put((byte) 0x0e, 8); // object with sorted index table
		OFFSET_SIZE.put((byte) 0x0f, 1); // object with unsorted index table
		OFFSET_SIZE.put((byte) 0x10, 2); // object with unsorted index table
		OFFSET_SIZE.put((byte) 0x11, 4); // object with unsorted index table
		OFFSET_SIZE.put((byte) 0x12, 8); // object with unsorted index table
	}

	private ObjectArrayUtil() {
		super();
	}

	public static int getOffsetSize(final byte key) {
		return OFFSET_SIZE.get(key);
	}
}
