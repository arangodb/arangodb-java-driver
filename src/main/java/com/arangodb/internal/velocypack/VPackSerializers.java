package com.arangodb.internal.velocypack;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.arangodb.entity.BaseDocument;
import com.arangodb.entity.BaseEdgeDocument;
import com.arangodb.entity.CollectionType;
import com.arangodb.entity.DocumentField;
import com.arangodb.internal.net.AuthenticationRequest;
import com.arangodb.internal.net.Request;
import com.arangodb.model.TraversalOptions;
import com.arangodb.velocypack.VPackSerializer;
import com.arangodb.velocypack.ValueType;

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
		for (final Entry<String, String> entry : value.getParameter().entrySet()) {
			builder.add(entry.getKey(), entry.getValue());
		}
		builder.close();
		builder.add(ValueType.OBJECT);
		for (final Entry<String, String> entry : value.getMeta().entrySet()) {
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
