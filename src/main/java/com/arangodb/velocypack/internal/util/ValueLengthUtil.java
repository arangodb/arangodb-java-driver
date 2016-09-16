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

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class ValueLengthUtil {

	private static final int DOUBLE_BYTES = 8;
	private static final int LONG_BYTES = 8;
	private static final int CHARACTER_BYTES = 2;

	private static final Map<Byte, Integer> MAP;

	static {
		MAP = new HashMap<>();
		MAP.put((byte) 0x00, 1);
		MAP.put((byte) 0x01, 1);
		MAP.put((byte) 0x02, 0);
		MAP.put((byte) 0x03, 0);
		MAP.put((byte) 0x04, 0);
		MAP.put((byte) 0x05, 0);
		MAP.put((byte) 0x06, 0);
		MAP.put((byte) 0x07, 0);
		MAP.put((byte) 0x08, 0);
		MAP.put((byte) 0x09, 0);
		MAP.put((byte) 0x0a, 1);
		MAP.put((byte) 0x0b, 0);
		MAP.put((byte) 0x0c, 0);
		MAP.put((byte) 0x0d, 0);
		MAP.put((byte) 0x0e, 0);
		MAP.put((byte) 0x0f, 0);
		MAP.put((byte) 0x10, 0);
		MAP.put((byte) 0x11, 0);
		MAP.put((byte) 0x12, 0);
		MAP.put((byte) 0x13, 0);
		MAP.put((byte) 0x14, 0);
		MAP.put((byte) 0x15, 0);
		MAP.put((byte) 0x16, 0);
		MAP.put((byte) 0x17, 1);
		MAP.put((byte) 0x18, 1);
		MAP.put((byte) 0x19, 1);
		MAP.put((byte) 0x1a, 1);
		MAP.put((byte) 0x1b, 1 + DOUBLE_BYTES);
		MAP.put((byte) 0x1c, 1 + LONG_BYTES);
		MAP.put((byte) 0x1d, 1 + CHARACTER_BYTES);
		MAP.put((byte) 0x1e, 1);
		MAP.put((byte) 0x1f, 1);
		MAP.put((byte) 0x20, 2);
		MAP.put((byte) 0x21, 3);
		MAP.put((byte) 0x22, 4);
		MAP.put((byte) 0x23, 5);
		MAP.put((byte) 0x24, 6);
		MAP.put((byte) 0x25, 7);
		MAP.put((byte) 0x26, 8);
		MAP.put((byte) 0x27, 9);
		MAP.put((byte) 0x28, 2);
		MAP.put((byte) 0x29, 3);
		MAP.put((byte) 0x2a, 4);
		MAP.put((byte) 0x2b, 5);
		MAP.put((byte) 0x2c, 6);
		MAP.put((byte) 0x2d, 7);
		MAP.put((byte) 0x2e, 8);
		MAP.put((byte) 0x2f, 9);
		MAP.put((byte) 0x30, 1);
		MAP.put((byte) 0x31, 1);
		MAP.put((byte) 0x32, 1);
		MAP.put((byte) 0x33, 1);
		MAP.put((byte) 0x34, 1);
		MAP.put((byte) 0x35, 1);
		MAP.put((byte) 0x36, 1);
		MAP.put((byte) 0x37, 1);
		MAP.put((byte) 0x38, 1);
		MAP.put((byte) 0x39, 1);
		MAP.put((byte) 0x3a, 1);
		MAP.put((byte) 0x3b, 1);
		MAP.put((byte) 0x3c, 1);
		MAP.put((byte) 0x3d, 1);
		MAP.put((byte) 0x3e, 1);
		MAP.put((byte) 0x3f, 1);
		MAP.put((byte) 0x40, 1);
		MAP.put((byte) 0x41, 2);
		MAP.put((byte) 0x42, 3);
		MAP.put((byte) 0x43, 4);
		MAP.put((byte) 0x44, 5);
		MAP.put((byte) 0x45, 6);
		MAP.put((byte) 0x46, 7);
		MAP.put((byte) 0x47, 8);
		MAP.put((byte) 0x48, 9);
		MAP.put((byte) 0x49, 10);
		MAP.put((byte) 0x4a, 11);
		MAP.put((byte) 0x4b, 12);
		MAP.put((byte) 0x4c, 13);
		MAP.put((byte) 0x4d, 14);
		MAP.put((byte) 0x4e, 15);
		MAP.put((byte) 0x4f, 16);
		MAP.put((byte) 0x50, 17);
		MAP.put((byte) 0x51, 18);
		MAP.put((byte) 0x52, 19);
		MAP.put((byte) 0x53, 20);
		MAP.put((byte) 0x54, 21);
		MAP.put((byte) 0x55, 22);
		MAP.put((byte) 0x56, 23);
		MAP.put((byte) 0x57, 24);
		MAP.put((byte) 0x58, 25);
		MAP.put((byte) 0x59, 26);
		MAP.put((byte) 0x5a, 27);
		MAP.put((byte) 0x5b, 28);
		MAP.put((byte) 0x5c, 29);
		MAP.put((byte) 0x5d, 30);
		MAP.put((byte) 0x5e, 31);
		MAP.put((byte) 0x5f, 32);
		MAP.put((byte) 0x60, 33);
		MAP.put((byte) 0x61, 34);
		MAP.put((byte) 0x62, 35);
		MAP.put((byte) 0x63, 36);
		MAP.put((byte) 0x64, 37);
		MAP.put((byte) 0x65, 38);
		MAP.put((byte) 0x66, 39);
		MAP.put((byte) 0x67, 40);
		MAP.put((byte) 0x68, 41);
		MAP.put((byte) 0x69, 42);
		MAP.put((byte) 0x6a, 43);
		MAP.put((byte) 0x6b, 44);
		MAP.put((byte) 0x6c, 45);
		MAP.put((byte) 0x6d, 46);
		MAP.put((byte) 0x6e, 47);
		MAP.put((byte) 0x6f, 48);
		MAP.put((byte) 0x70, 49);
		MAP.put((byte) 0x71, 50);
		MAP.put((byte) 0x72, 51);
		MAP.put((byte) 0x73, 52);
		MAP.put((byte) 0x74, 53);
		MAP.put((byte) 0x75, 54);
		MAP.put((byte) 0x76, 55);
		MAP.put((byte) 0x77, 56);
		MAP.put((byte) 0x78, 57);
		MAP.put((byte) 0x79, 58);
		MAP.put((byte) 0x7a, 59);
		MAP.put((byte) 0x7b, 60);
		MAP.put((byte) 0x7c, 61);
		MAP.put((byte) 0x7d, 62);
		MAP.put((byte) 0x7e, 63);
		MAP.put((byte) 0x7f, 64);
		MAP.put((byte) 0x80, 65);
		MAP.put((byte) 0x81, 66);
		MAP.put((byte) 0x82, 67);
		MAP.put((byte) 0x83, 68);
		MAP.put((byte) 0x84, 69);
		MAP.put((byte) 0x85, 70);
		MAP.put((byte) 0x86, 71);
		MAP.put((byte) 0x87, 72);
		MAP.put((byte) 0x88, 73);
		MAP.put((byte) 0x89, 74);
		MAP.put((byte) 0x8a, 75);
		MAP.put((byte) 0x8b, 76);
		MAP.put((byte) 0x8c, 77);
		MAP.put((byte) 0x8d, 78);
		MAP.put((byte) 0x8e, 79);
		MAP.put((byte) 0x8f, 80);
		MAP.put((byte) 0x90, 81);
		MAP.put((byte) 0x91, 82);
		MAP.put((byte) 0x92, 83);
		MAP.put((byte) 0x93, 84);
		MAP.put((byte) 0x94, 85);
		MAP.put((byte) 0x95, 86);
		MAP.put((byte) 0x96, 87);
		MAP.put((byte) 0x97, 88);
		MAP.put((byte) 0x98, 89);
		MAP.put((byte) 0x99, 90);
		MAP.put((byte) 0x9a, 91);
		MAP.put((byte) 0x9b, 92);
		MAP.put((byte) 0x9c, 93);
		MAP.put((byte) 0x9d, 94);
		MAP.put((byte) 0x9e, 95);
		MAP.put((byte) 0x9f, 96);
		MAP.put((byte) 0xa0, 97);
		MAP.put((byte) 0xa1, 98);
		MAP.put((byte) 0xa2, 99);
		MAP.put((byte) 0xa3, 100);
		MAP.put((byte) 0xa4, 101);
		MAP.put((byte) 0xa5, 102);
		MAP.put((byte) 0xa6, 103);
		MAP.put((byte) 0xa7, 104);
		MAP.put((byte) 0xa8, 105);
		MAP.put((byte) 0xa9, 106);
		MAP.put((byte) 0xaa, 107);
		MAP.put((byte) 0xab, 108);
		MAP.put((byte) 0xac, 109);
		MAP.put((byte) 0xad, 110);
		MAP.put((byte) 0xae, 111);
		MAP.put((byte) 0xaf, 112);
		MAP.put((byte) 0xb0, 113);
		MAP.put((byte) 0xb1, 114);
		MAP.put((byte) 0xb2, 115);
		MAP.put((byte) 0xb3, 116);
		MAP.put((byte) 0xb4, 117);
		MAP.put((byte) 0xb5, 118);
		MAP.put((byte) 0xb6, 119);
		MAP.put((byte) 0xb7, 120);
		MAP.put((byte) 0xb8, 121);
		MAP.put((byte) 0xb9, 122);
		MAP.put((byte) 0xba, 123);
		MAP.put((byte) 0xbb, 124);
		MAP.put((byte) 0xbc, 125);
		MAP.put((byte) 0xbd, 126);
		MAP.put((byte) 0xbe, 127);
		MAP.put((byte) 0xbf, 0);
		MAP.put((byte) 0xc0, 0);
		MAP.put((byte) 0xc1, 0);
		MAP.put((byte) 0xc2, 0);
		MAP.put((byte) 0xc3, 0);
		MAP.put((byte) 0xc4, 0);
		MAP.put((byte) 0xc5, 0);
		MAP.put((byte) 0xc6, 0);
		MAP.put((byte) 0xc7, 0);
		MAP.put((byte) 0xc8, 0);
		MAP.put((byte) 0xc9, 0);
		MAP.put((byte) 0xca, 0);
		MAP.put((byte) 0xcb, 0);
		MAP.put((byte) 0xcc, 0);
		MAP.put((byte) 0xcd, 0);
		MAP.put((byte) 0xce, 0);
		MAP.put((byte) 0xcf, 0);
		MAP.put((byte) 0xd0, 0);
		MAP.put((byte) 0xd1, 0);
		MAP.put((byte) 0xd2, 0);
		MAP.put((byte) 0xd3, 0);
		MAP.put((byte) 0xd4, 0);
		MAP.put((byte) 0xd5, 0);
		MAP.put((byte) 0xd6, 0);
		MAP.put((byte) 0xd7, 0);
		MAP.put((byte) 0xd8, 0);
		MAP.put((byte) 0xd9, 0);
		MAP.put((byte) 0xda, 0);
		MAP.put((byte) 0xdb, 0);
		MAP.put((byte) 0xdc, 0);
		MAP.put((byte) 0xdd, 0);
		MAP.put((byte) 0xde, 0);
		MAP.put((byte) 0xdf, 0);
		MAP.put((byte) 0xe0, 0);
		MAP.put((byte) 0xe1, 0);
		MAP.put((byte) 0xe2, 0);
		MAP.put((byte) 0xe3, 0);
		MAP.put((byte) 0xe4, 0);
		MAP.put((byte) 0xe5, 0);
		MAP.put((byte) 0xe6, 0);
		MAP.put((byte) 0xe7, 0);
		MAP.put((byte) 0xe8, 0);
		MAP.put((byte) 0xe9, 0);
		MAP.put((byte) 0xea, 0);
		MAP.put((byte) 0xeb, 0);
		MAP.put((byte) 0xec, 0);
		MAP.put((byte) 0xed, 0);
		MAP.put((byte) 0xee, 0);
		MAP.put((byte) 0xef, 0);
		MAP.put((byte) 0xf0, 2);
		MAP.put((byte) 0xf1, 3);
		MAP.put((byte) 0xf2, 5);
		MAP.put((byte) 0xf3, 9);
		MAP.put((byte) 0xf4, 0);
		MAP.put((byte) 0xf5, 0);
		MAP.put((byte) 0xf6, 0);
		MAP.put((byte) 0xf7, 0);
		MAP.put((byte) 0xf8, 0);
		MAP.put((byte) 0xf9, 0);
		MAP.put((byte) 0xfa, 0);
		MAP.put((byte) 0xfb, 0);
		MAP.put((byte) 0xfc, 0);
		MAP.put((byte) 0xfd, 0);
		MAP.put((byte) 0xfe, 0);
		MAP.put((byte) 0xff, 0);
	}

	private ValueLengthUtil() {
		super();
	}

	public static int get(final byte key) {
		return MAP.get(key);
	}

}
