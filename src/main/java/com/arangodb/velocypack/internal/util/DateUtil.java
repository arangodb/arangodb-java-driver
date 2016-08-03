package com.arangodb.velocypack.internal.util;

import java.util.Date;

/**
 * @author Mark - mark@arangodb.com
 *
 */
public class DateUtil {

	private DateUtil() {
		super();
	}

	public static Date toDate(final byte[] array, final int offset, final int length) {
		final long milliseconds = NumberUtil.toLong(array, offset, length);
		return new Date(milliseconds);
	}
}
