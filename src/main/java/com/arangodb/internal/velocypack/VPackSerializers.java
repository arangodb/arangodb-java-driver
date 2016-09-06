package com.arangodb.internal.velocypack;

import java.util.HashMap;
import java.util.Map;

import com.arangodb.entity.BaseDocument;
import com.arangodb.entity.BaseEdgeDocument;
import com.arangodb.entity.CollectionType;
import com.arangodb.entity.DocumentField;
import com.arangodb.internal.net.Request;
import com.arangodb.velocypack.VPackSerializer;
import com.arangodb.velocypack.Value;
import com.arangodb.velocypack.ValueType;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class VPackSerializers {

	public static final VPackSerializer<Request> REQUEST = (builder, attribute, value, context) -> {
		builder.add(attribute, new Value(ValueType.ARRAY));
		builder.add(new Value(value.getVersion()));
		builder.add(new Value(value.getType()));
		builder.add(new Value(value.getDatabase()));
		builder.add(new Value(value.getRequestType().getType()));
		builder.add(new Value(value.getRequest()));
		context.serialize(builder, null, value.getParameter());
		context.serialize(builder, null, value.getMeta());
		builder.close();
	};

	public static final VPackSerializer<CollectionType> COLLECTION_TYPE = (
		builder,
		attribute,
		value,
		context) -> builder.add(attribute, new Value(value.getType()));

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
}
