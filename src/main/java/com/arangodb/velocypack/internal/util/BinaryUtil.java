/*
 * DISCLAIMER
 *
 * Copyright 2016 ArangoDB GmbH, Cologne, Germany
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Copyright holder is ArangoDB GmbH, Cologne, Germany
 */

package com.arangodb.velocypack.internal.util;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class BinaryUtil {

	private BinaryUtil() {
		super();
	}

	public static byte[] toBinary(final byte[] array, final int offset, final int length) {
		final byte[] result = new byte[length];
		for (int i = offset, j = 0; j < length; i++, j++) {
			result[j] = array[i];
		}
		return result;
	}

}
