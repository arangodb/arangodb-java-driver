package com.arangodb.internal.velocypack;

import java.util.HashMap;
import java.util.Map;

import com.arangodb.entity.BaseDocument;
import com.arangodb.entity.CollectionType;
import com.arangodb.entity.DocumentField;
import com.arangodb.internal.net.velocystream.RequestType;
import com.arangodb.velocypack.VPackSerializer;
import com.arangodb.velocypack.Value;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class VPackSerializers {

	public static final VPackSerializer<RequestType> REQUEST_TYPE = (builder, attribute, value, context) -> builder
			.add(attribute, new Value(value.getType()));

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

}
