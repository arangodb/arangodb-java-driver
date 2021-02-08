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

package com.arangodb.mapping.annotations;

import com.arangodb.velocypack.annotations.SerializedName;

import java.util.Objects;

/**
 * @author Michele Rastelli
 */
public class SerializedNameParameterEntity {

	final static String SERIALIZED_NAME_A = "aSerializedName";
	final static String SERIALIZED_NAME_B = "bSerializedName";
	final static String SERIALIZED_NAME_C = "cSerializedName";

	private String a;
	private String b;
	private String c;

	public SerializedNameParameterEntity(
			@SerializedName(SERIALIZED_NAME_A)
					String a,
			@SerializedName(SERIALIZED_NAME_B)
					String b,
			@SerializedName(SERIALIZED_NAME_C)
					String c) {
		this.a = a;
		this.b = b;
		this.c = c;
	}

	public String getA() {
		return a;
	}

	public String getB() {
		return b;
	}

	public String getC() {
		return c;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		SerializedNameParameterEntity that = (SerializedNameParameterEntity) o;
		return Objects.equals(a, that.a) && Objects.equals(b, that.b) && Objects.equals(c, that.c);
	}

	@Override
	public int hashCode() {
		return Objects.hash(a, b, c);
	}

	@Override
	public String toString() {
		return "SerializedNameParameterEntity{" + "a='" + a + '\'' + ", b='" + b + '\'' + ", c='" + c + '\'' + '}';
	}
}
