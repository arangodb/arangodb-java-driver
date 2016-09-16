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

package com.arangodb.entity;

import java.util.HashMap;
import java.util.Map;

import com.arangodb.entity.DocumentField.Type;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class BaseDocument {

	@DocumentField(Type.ID)
	private String id;
	@DocumentField(Type.KEY)
	private String key;
	@DocumentField(Type.REV)
	private String revision;
	private Map<String, Object> properties;

	public BaseDocument() {
		super();
		properties = new HashMap<>();
	}

	public BaseDocument(final Map<String, Object> properties) {
		final Object tmpId = properties.remove(DocumentField.Type.ID.getSerializeName());
		if (tmpId != null) {
			id = tmpId.toString();
		}
		final Object tmpKey = properties.remove(DocumentField.Type.KEY.getSerializeName());
		if (tmpKey != null) {
			key = tmpKey.toString();
		}
		final Object tmpRev = properties.remove(DocumentField.Type.REV.getSerializeName());
		if (tmpRev != null) {
			revision = tmpRev.toString();
		}
		this.properties = properties;
	}

	public String getId() {
		return id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public String getKey() {
		return key;
	}

	public void setKey(final String key) {
		this.key = key;
	}

	public String getRevision() {
		return revision;
	}

	public void setRevision(final String revision) {
		this.revision = revision;
	}

	public Map<String, Object> getProperties() {
		return properties;
	}

	public void setProperties(final Map<String, Object> properties) {
		this.properties = properties;
	}

	public void addAttribute(final String key, final Object value) {
		properties.put(key, value);
	}

	public void updateAttribute(final String key, final Object value) {
		if (properties.containsKey(key)) {
			properties.put(key, value);
		}
	}

	public Object getAttribute(final String key) {
		return properties.get(key);
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("BaseDocument [documentRevision=");
		sb.append(revision);
		sb.append(", documentHandle=");
		sb.append(id);
		sb.append(", documentKey=");
		sb.append(key);
		sb.append(", properties=");
		sb.append(properties);
		sb.append("]");
		return sb.toString();
	}
}
