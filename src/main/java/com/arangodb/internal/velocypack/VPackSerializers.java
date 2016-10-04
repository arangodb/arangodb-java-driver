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

package com.arangodb.internal.velocypack;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.arangodb.entity.BaseDocument;
import com.arangodb.entity.BaseEdgeDocument;
import com.arangodb.entity.CollectionType;
import com.arangodb.entity.DocumentField;
import com.arangodb.internal.velocystream.AuthenticationRequest;
import com.arangodb.model.TraversalOptions;
import com.arangodb.velocypack.VPackSerializer;
import com.arangodb.velocypack.ValueType;
import com.arangodb.velocystream.Request;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class VPackSerializers {

	public static final VPackSerializer<Request> REQUEST = (builder, attribute, value, context) -> {
		builder.add(attribute, ValueType.ARRAY);
		builder.add(value.getVersion());
		builder.add(value.getType());
		builder.add(value.getDatabase());
		builder.add(value.getRequestType().getType());
		builder.add(value.getRequest());
		builder.add(ValueType.OBJECT);
		for (final Entry<String, String> entry : value.getQueryParam().entrySet()) {
			builder.add(entry.getKey(), entry.getValue());
		}
		builder.close();
		builder.add(ValueType.OBJECT);
		for (final Entry<String, String> entry : value.getHeaderParam().entrySet()) {
			builder.add(entry.getKey(), entry.getValue());
		}
		builder.close();
		builder.close();
	};

	public static final VPackSerializer<AuthenticationRequest> AUTH_REQUEST = (builder, attribute, value, context) -> {
		builder.add(attribute, ValueType.ARRAY);
		builder.add(value.getVersion());
		builder.add(value.getType());
		builder.add(value.getEncryption());
		builder.add(value.getUser());
		builder.add(value.getPassword());
		builder.close();
	};

	public static final VPackSerializer<CollectionType> COLLECTION_TYPE = (
		builder,
		attribute,
		value,
		context) -> builder.add(attribute, value.getType());

	public static final VPackSerializer<BaseDocument> BASE_DOCUMENT = (builder, attribute, value, context) -> {
		final Map<String, Object> doc = new HashMap<>();
		doc.putAll(value.getProperties());
		doc.put(DocumentField.Type.ID.getSerializeName(), value.getId());
		doc.put(DocumentField.Type.KEY.getSerializeName(), value.getKey());
		doc.put(DocumentField.Type.REV.getSerializeName(), value.getRevision());
		context.serialize(builder, attribute, doc);
	};

	public static final VPackSerializer<BaseEdgeDocument> BASE_EDGE_DOCUMENT = (builder, attribute, value, context) -> {
		final Map<String, Object> doc = new HashMap<>();
		doc.putAll(value.getProperties());
		doc.put(DocumentField.Type.ID.getSerializeName(), value.getId());
		doc.put(DocumentField.Type.KEY.getSerializeName(), value.getKey());
		doc.put(DocumentField.Type.REV.getSerializeName(), value.getRevision());
		doc.put(DocumentField.Type.FROM.getSerializeName(), value.getFrom());
		doc.put(DocumentField.Type.TO.getSerializeName(), value.getTo());
		context.serialize(builder, attribute, doc);
	};

	public static final VPackSerializer<TraversalOptions.Order> TRAVERSAL_ORDER = (
		builder,
		attribute,
		value,
		context) -> {
		if (TraversalOptions.Order.preorder_expander == value) {
			builder.add(attribute, "preorder-expander");
		} else {
			builder.add(attribute, value.name());
		}
	};
}
