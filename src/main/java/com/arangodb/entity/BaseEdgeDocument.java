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

import java.util.Map;

import com.arangodb.entity.DocumentField.Type;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class BaseEdgeDocument extends BaseDocument {

	@DocumentField(Type.FROM)
	private String from;
	@DocumentField(Type.TO)
	private String to;

	public BaseEdgeDocument() {
		super();
	}

	public BaseEdgeDocument(final Map<String, Object> properties) {
		super(properties);
		final Object tmpFrom = properties.remove(DocumentField.Type.FROM.getSerializeName());
		if (tmpFrom != null) {
			from = tmpFrom.toString();
		}
		final Object tmpTo = properties.remove(DocumentField.Type.TO.getSerializeName());
		if (tmpTo != null) {
			to = tmpTo.toString();
		}
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(final String from) {
		this.from = from;
	}

	public String getTo() {
		return to;
	}

	public void setTo(final String to) {
		this.to = to;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("BaseDocument [documentRevision=");
		sb.append(getRevision());
		sb.append(", documentHandle=");
		sb.append(getId());
		sb.append(", documentKey=");
		sb.append(getKey());
		sb.append(", from=");
		sb.append(getFrom());
		sb.append(", to=");
		sb.append(getTo());
		sb.append(", properties=");
		sb.append(getProperties());
		sb.append("]");
		return sb.toString();
	}
}
