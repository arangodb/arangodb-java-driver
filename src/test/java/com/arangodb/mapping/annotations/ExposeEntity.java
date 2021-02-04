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

import com.arangodb.velocypack.annotations.Expose;

import java.util.Objects;

/**
 * @author Michele Rastelli
 */
public class ExposeEntity {

	@Expose()
	private String readWrite;

	@Expose(deserialize = false)
	private String readOnly;

	@Expose(serialize = false)
	private String writeOnly;

	@Expose(serialize = false,
			deserialize = false)
	private String ignored;

	public ExposeEntity() {
	}

	public String getReadWrite() {
		return readWrite;
	}

	public void setReadWrite(String readWrite) {
		this.readWrite = readWrite;
	}

	public String getReadOnly() {
		return readOnly;
	}

	public void setReadOnly(String readOnly) {
		this.readOnly = readOnly;
	}

	public String getWriteOnly() {
		return writeOnly;
	}

	public void setWriteOnly(String writeOnly) {
		this.writeOnly = writeOnly;
	}

	public String getIgnored() {
		return ignored;
	}

	public void setIgnored(String ignored) {
		this.ignored = ignored;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		ExposeEntity that = (ExposeEntity) o;
		return Objects.equals(readWrite, that.readWrite) && Objects.equals(readOnly, that.readOnly) && Objects
				.equals(writeOnly, that.writeOnly) && Objects.equals(ignored, that.ignored);
	}

	@Override
	public int hashCode() {
		return Objects.hash(readWrite, readOnly, writeOnly, ignored);
	}

	@Override
	public String toString() {
		return "ExposeEntity{" + "readWrite='" + readWrite + '\'' + ", readOnly='" + readOnly + '\'' + ", writeOnly='"
				+ writeOnly + '\'' + ", ignored='" + ignored + '\'' + '}';
	}
}