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

import java.util.HashMap;
import java.util.Map;

import com.arangodb.velocypack.ValueType;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class ValueTypeUtil {

	private static final Map<Byte, ValueType> MAP;

	static {
		MAP = new HashMap<Byte, ValueType>();

		MAP.put((byte) 0x00, ValueType.NONE);
		MAP.put((byte) 0x01, ValueType.ARRAY);
		MAP.put((byte) 0x02, ValueType.ARRAY);
		MAP.put((byte) 0x03, ValueType.ARRAY);
		MAP.put((byte) 0x04, ValueType.ARRAY);
		MAP.put((byte) 0x05, ValueType.ARRAY);
		MAP.put((byte) 0x06, ValueType.ARRAY);
		MAP.put((byte) 0x07, ValueType.ARRAY);
		MAP.put((byte) 0x08, ValueType.ARRAY);
		MAP.put((byte) 0x09, ValueType.ARRAY);
		MAP.put((byte) 0x0a, ValueType.OBJECT);
		MAP.put((byte) 0x0b, ValueType.OBJECT);
		MAP.put((byte) 0x0c, ValueType.OBJECT);
		MAP.put((byte) 0x0d, ValueType.OBJECT);
		MAP.put((byte) 0x0e, ValueType.OBJECT);
		MAP.put((byte) 0x0f, ValueType.OBJECT);
		MAP.put((byte) 0x10, ValueType.OBJECT);
		MAP.put((byte) 0x11, ValueType.OBJECT);
		MAP.put((byte) 0x12, ValueType.OBJECT);
		MAP.put((byte) 0x13, ValueType.ARRAY);
		MAP.put((byte) 0x14, ValueType.OBJECT);
		MAP.put((byte) 0x15, ValueType.NONE);
		MAP.put((byte) 0x16, ValueType.NONE);
		MAP.put((byte) 0x17, ValueType.ILLEGAL);
		MAP.put((byte) 0x18, ValueType.NULL);
		MAP.put((byte) 0x19, ValueType.BOOL);
		MAP.put((byte) 0x1a, ValueType.BOOL);
		MAP.put((byte) 0x1b, ValueType.DOUBLE);
		MAP.put((byte) 0x1c, ValueType.UTC_DATE);
		MAP.put((byte) 0x1d, ValueType.EXTERNAL);
		MAP.put((byte) 0x1e, ValueType.MIN_KEY);
		MAP.put((byte) 0x1f, ValueType.MAX_KEY);
		MAP.put((byte) 0x20, ValueType.INT);
		MAP.put((byte) 0x21, ValueType.INT);
		MAP.put((byte) 0x22, ValueType.INT);
		MAP.put((byte) 0x23, ValueType.INT);
		MAP.put((byte) 0x24, ValueType.INT);
		MAP.put((byte) 0x25, ValueType.INT);
		MAP.put((byte) 0x26, ValueType.INT);
		MAP.put((byte) 0x27, ValueType.INT);
		MAP.put((byte) 0x28, ValueType.UINT);
		MAP.put((byte) 0x29, ValueType.UINT);
		MAP.put((byte) 0x2a, ValueType.UINT);
		MAP.put((byte) 0x2b, ValueType.UINT);
		MAP.put((byte) 0x2c, ValueType.UINT);
		MAP.put((byte) 0x2d, ValueType.UINT);
		MAP.put((byte) 0x2e, ValueType.UINT);
		MAP.put((byte) 0x2f, ValueType.UINT);
		MAP.put((byte) 0x30, ValueType.SMALLINT);
		MAP.put((byte) 0x31, ValueType.SMALLINT);
		MAP.put((byte) 0x32, ValueType.SMALLINT);
		MAP.put((byte) 0x33, ValueType.SMALLINT);
		MAP.put((byte) 0x34, ValueType.SMALLINT);
		MAP.put((byte) 0x35, ValueType.SMALLINT);
		MAP.put((byte) 0x36, ValueType.SMALLINT);
		MAP.put((byte) 0x37, ValueType.SMALLINT);
		MAP.put((byte) 0x38, ValueType.SMALLINT);
		MAP.put((byte) 0x39, ValueType.SMALLINT);
		MAP.put((byte) 0x3a, ValueType.SMALLINT);
		MAP.put((byte) 0x3b, ValueType.SMALLINT);
		MAP.put((byte) 0x3c, ValueType.SMALLINT);
		MAP.put((byte) 0x3d, ValueType.SMALLINT);
		MAP.put((byte) 0x3e, ValueType.SMALLINT);
		MAP.put((byte) 0x3f, ValueType.SMALLINT);
		MAP.put((byte) 0x40, ValueType.STRING);
		MAP.put((byte) 0x41, ValueType.STRING);
		MAP.put((byte) 0x42, ValueType.STRING);
		MAP.put((byte) 0x43, ValueType.STRING);
		MAP.put((byte) 0x44, ValueType.STRING);
		MAP.put((byte) 0x45, ValueType.STRING);
		MAP.put((byte) 0x46, ValueType.STRING);
		MAP.put((byte) 0x47, ValueType.STRING);
		MAP.put((byte) 0x48, ValueType.STRING);
		MAP.put((byte) 0x49, ValueType.STRING);
		MAP.put((byte) 0x4a, ValueType.STRING);
		MAP.put((byte) 0x4b, ValueType.STRING);
		MAP.put((byte) 0x4c, ValueType.STRING);
		MAP.put((byte) 0x4d, ValueType.STRING);
		MAP.put((byte) 0x4e, ValueType.STRING);
		MAP.put((byte) 0x4f, ValueType.STRING);
		MAP.put((byte) 0x50, ValueType.STRING);
		MAP.put((byte) 0x51, ValueType.STRING);
		MAP.put((byte) 0x52, ValueType.STRING);
		MAP.put((byte) 0x53, ValueType.STRING);
		MAP.put((byte) 0x54, ValueType.STRING);
		MAP.put((byte) 0x55, ValueType.STRING);
		MAP.put((byte) 0x56, ValueType.STRING);
		MAP.put((byte) 0x57, ValueType.STRING);
		MAP.put((byte) 0x58, ValueType.STRING);
		MAP.put((byte) 0x59, ValueType.STRING);
		MAP.put((byte) 0x5a, ValueType.STRING);
		MAP.put((byte) 0x5b, ValueType.STRING);
		MAP.put((byte) 0x5c, ValueType.STRING);
		MAP.put((byte) 0x5d, ValueType.STRING);
		MAP.put((byte) 0x5e, ValueType.STRING);
		MAP.put((byte) 0x5f, ValueType.STRING);
		MAP.put((byte) 0x60, ValueType.STRING);
		MAP.put((byte) 0x61, ValueType.STRING);
		MAP.put((byte) 0x62, ValueType.STRING);
		MAP.put((byte) 0x63, ValueType.STRING);
		MAP.put((byte) 0x64, ValueType.STRING);
		MAP.put((byte) 0x65, ValueType.STRING);
		MAP.put((byte) 0x66, ValueType.STRING);
		MAP.put((byte) 0x67, ValueType.STRING);
		MAP.put((byte) 0x68, ValueType.STRING);
		MAP.put((byte) 0x69, ValueType.STRING);
		MAP.put((byte) 0x6a, ValueType.STRING);
		MAP.put((byte) 0x6b, ValueType.STRING);
		MAP.put((byte) 0x6c, ValueType.STRING);
		MAP.put((byte) 0x6d, ValueType.STRING);
		MAP.put((byte) 0x6e, ValueType.STRING);
		MAP.put((byte) 0x6f, ValueType.STRING);
		MAP.put((byte) 0x70, ValueType.STRING);
		MAP.put((byte) 0x71, ValueType.STRING);
		MAP.put((byte) 0x72, ValueType.STRING);
		MAP.put((byte) 0x73, ValueType.STRING);
		MAP.put((byte) 0x74, ValueType.STRING);
		MAP.put((byte) 0x75, ValueType.STRING);
		MAP.put((byte) 0x76, ValueType.STRING);
		MAP.put((byte) 0x77, ValueType.STRING);
		MAP.put((byte) 0x78, ValueType.STRING);
		MAP.put((byte) 0x79, ValueType.STRING);
		MAP.put((byte) 0x7a, ValueType.STRING);
		MAP.put((byte) 0x7b, ValueType.STRING);
		MAP.put((byte) 0x7c, ValueType.STRING);
		MAP.put((byte) 0x7d, ValueType.STRING);
		MAP.put((byte) 0x7e, ValueType.STRING);
		MAP.put((byte) 0x7f, ValueType.STRING);
		MAP.put((byte) 0x80, ValueType.STRING);
		MAP.put((byte) 0x81, ValueType.STRING);
		MAP.put((byte) 0x82, ValueType.STRING);
		MAP.put((byte) 0x83, ValueType.STRING);
		MAP.put((byte) 0x84, ValueType.STRING);
		MAP.put((byte) 0x85, ValueType.STRING);
		MAP.put((byte) 0x86, ValueType.STRING);
		MAP.put((byte) 0x87, ValueType.STRING);
		MAP.put((byte) 0x88, ValueType.STRING);
		MAP.put((byte) 0x89, ValueType.STRING);
		MAP.put((byte) 0x8a, ValueType.STRING);
		MAP.put((byte) 0x8b, ValueType.STRING);
		MAP.put((byte) 0x8c, ValueType.STRING);
		MAP.put((byte) 0x8d, ValueType.STRING);
		MAP.put((byte) 0x8e, ValueType.STRING);
		MAP.put((byte) 0x8f, ValueType.STRING);
		MAP.put((byte) 0x90, ValueType.STRING);
		MAP.put((byte) 0x91, ValueType.STRING);
		MAP.put((byte) 0x92, ValueType.STRING);
		MAP.put((byte) 0x93, ValueType.STRING);
		MAP.put((byte) 0x94, ValueType.STRING);
		MAP.put((byte) 0x95, ValueType.STRING);
		MAP.put((byte) 0x96, ValueType.STRING);
		MAP.put((byte) 0x97, ValueType.STRING);
		MAP.put((byte) 0x98, ValueType.STRING);
		MAP.put((byte) 0x99, ValueType.STRING);
		MAP.put((byte) 0x9a, ValueType.STRING);
		MAP.put((byte) 0x9b, ValueType.STRING);
		MAP.put((byte) 0x9c, ValueType.STRING);
		MAP.put((byte) 0x9d, ValueType.STRING);
		MAP.put((byte) 0x9e, ValueType.STRING);
		MAP.put((byte) 0x9f, ValueType.STRING);
		MAP.put((byte) 0xa0, ValueType.STRING);
		MAP.put((byte) 0xa1, ValueType.STRING);
		MAP.put((byte) 0xa2, ValueType.STRING);
		MAP.put((byte) 0xa3, ValueType.STRING);
		MAP.put((byte) 0xa4, ValueType.STRING);
		MAP.put((byte) 0xa5, ValueType.STRING);
		MAP.put((byte) 0xa6, ValueType.STRING);
		MAP.put((byte) 0xa7, ValueType.STRING);
		MAP.put((byte) 0xa8, ValueType.STRING);
		MAP.put((byte) 0xa9, ValueType.STRING);
		MAP.put((byte) 0xaa, ValueType.STRING);
		MAP.put((byte) 0xab, ValueType.STRING);
		MAP.put((byte) 0xac, ValueType.STRING);
		MAP.put((byte) 0xad, ValueType.STRING);
		MAP.put((byte) 0xae, ValueType.STRING);
		MAP.put((byte) 0xaf, ValueType.STRING);
		MAP.put((byte) 0xb0, ValueType.STRING);
		MAP.put((byte) 0xb1, ValueType.STRING);
		MAP.put((byte) 0xb2, ValueType.STRING);
		MAP.put((byte) 0xb3, ValueType.STRING);
		MAP.put((byte) 0xb4, ValueType.STRING);
		MAP.put((byte) 0xb5, ValueType.STRING);
		MAP.put((byte) 0xb6, ValueType.STRING);
		MAP.put((byte) 0xb7, ValueType.STRING);
		MAP.put((byte) 0xb8, ValueType.STRING);
		MAP.put((byte) 0xb9, ValueType.STRING);
		MAP.put((byte) 0xba, ValueType.STRING);
		MAP.put((byte) 0xbb, ValueType.STRING);
		MAP.put((byte) 0xbc, ValueType.STRING);
		MAP.put((byte) 0xbd, ValueType.STRING);
		MAP.put((byte) 0xbe, ValueType.STRING);
		MAP.put((byte) 0xbf, ValueType.STRING);
		MAP.put((byte) 0xc0, ValueType.BINARY);
		MAP.put((byte) 0xc1, ValueType.BINARY);
		MAP.put((byte) 0xc2, ValueType.BINARY);
		MAP.put((byte) 0xc3, ValueType.BINARY);
		MAP.put((byte) 0xc4, ValueType.BINARY);
		MAP.put((byte) 0xc5, ValueType.BINARY);
		MAP.put((byte) 0xc6, ValueType.BINARY);
		MAP.put((byte) 0xc7, ValueType.BINARY);
		MAP.put((byte) 0xc8, ValueType.BCD);
		MAP.put((byte) 0xc9, ValueType.BCD);
		MAP.put((byte) 0xca, ValueType.BCD);
		MAP.put((byte) 0xcb, ValueType.BCD);
		MAP.put((byte) 0xcc, ValueType.BCD);
		MAP.put((byte) 0xcd, ValueType.BCD);
		MAP.put((byte) 0xce, ValueType.BCD);
		MAP.put((byte) 0xcf, ValueType.BCD);
		MAP.put((byte) 0xd0, ValueType.BCD);
		MAP.put((byte) 0xd1, ValueType.BCD);
		MAP.put((byte) 0xd2, ValueType.BCD);
		MAP.put((byte) 0xd3, ValueType.BCD);
		MAP.put((byte) 0xd4, ValueType.BCD);
		MAP.put((byte) 0xd5, ValueType.BCD);
		MAP.put((byte) 0xd6, ValueType.BCD);
		MAP.put((byte) 0xd7, ValueType.BCD);
		MAP.put((byte) 0xd8, ValueType.NONE);
		MAP.put((byte) 0xd9, ValueType.NONE);
		MAP.put((byte) 0xda, ValueType.NONE);
		MAP.put((byte) 0xdb, ValueType.NONE);
		MAP.put((byte) 0xdc, ValueType.NONE);
		MAP.put((byte) 0xdd, ValueType.NONE);
		MAP.put((byte) 0xde, ValueType.NONE);
		MAP.put((byte) 0xdf, ValueType.NONE);
		MAP.put((byte) 0xe0, ValueType.NONE);
		MAP.put((byte) 0xe1, ValueType.NONE);
		MAP.put((byte) 0xe2, ValueType.NONE);
		MAP.put((byte) 0xe3, ValueType.NONE);
		MAP.put((byte) 0xe4, ValueType.NONE);
		MAP.put((byte) 0xe5, ValueType.NONE);
		MAP.put((byte) 0xe6, ValueType.NONE);
		MAP.put((byte) 0xe7, ValueType.NONE);
		MAP.put((byte) 0xe8, ValueType.NONE);
		MAP.put((byte) 0xe9, ValueType.NONE);
		MAP.put((byte) 0xea, ValueType.NONE);
		MAP.put((byte) 0xeb, ValueType.NONE);
		MAP.put((byte) 0xec, ValueType.NONE);
		MAP.put((byte) 0xed, ValueType.NONE);
		MAP.put((byte) 0xee, ValueType.NONE);
		MAP.put((byte) 0xef, ValueType.NONE);
		MAP.put((byte) 0xf0, ValueType.CUSTOM);
		MAP.put((byte) 0xf1, ValueType.CUSTOM);
		MAP.put((byte) 0xf2, ValueType.CUSTOM);
		MAP.put((byte) 0xf3, ValueType.CUSTOM);
		MAP.put((byte) 0xf4, ValueType.CUSTOM);
		MAP.put((byte) 0xf5, ValueType.CUSTOM);
		MAP.put((byte) 0xf6, ValueType.CUSTOM);
		MAP.put((byte) 0xf7, ValueType.CUSTOM);
		MAP.put((byte) 0xf8, ValueType.CUSTOM);
		MAP.put((byte) 0xf9, ValueType.CUSTOM);
		MAP.put((byte) 0xfa, ValueType.CUSTOM);
		MAP.put((byte) 0xfb, ValueType.CUSTOM);
		MAP.put((byte) 0xfc, ValueType.CUSTOM);
		MAP.put((byte) 0xfd, ValueType.CUSTOM);
		MAP.put((byte) 0xfe, ValueType.CUSTOM);
		MAP.put((byte) 0xff, ValueType.CUSTOM);
	}

	private ValueTypeUtil() {
		super();
	}

	public static ValueType get(final byte key) {
		return MAP.get(key);
	}

}
