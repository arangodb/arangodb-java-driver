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

import com.arangodb.entity.DocumentField;

import java.util.Objects;

/**
 * @author Michele Rastelli
 */
public class DocumentFieldEntity {

	@DocumentField(DocumentField.Type.ID)
	private String id;

	@DocumentField(DocumentField.Type.KEY)
	private String key;

	@DocumentField(DocumentField.Type.REV)
	private String rev;

	@DocumentField(DocumentField.Type.FROM)
	private String from;

	@DocumentField(DocumentField.Type.TO)
	private String to;

	public DocumentFieldEntity() {
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getRev() {
		return rev;
	}

	public void setRev(String rev) {
		this.rev = rev;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		DocumentFieldEntity that = (DocumentFieldEntity) o;
		return Objects.equals(id, that.id) && Objects.equals(key, that.key) && Objects.equals(rev, that.rev) && Objects
				.equals(from, that.from) && Objects.equals(to, that.to);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, key, rev, from, to);
	}

	@Override
	public String toString() {
		return "AnnotatedEntity{" + "idField='" + id + '\'' + ", keyField='" + key + '\'' + ", revField='" + rev + '\''
				+ ", fromField='" + from + '\'' + ", toField='" + to + '\'' + '}';
	}

}
