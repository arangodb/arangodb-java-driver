package com.arangodb.internal.velocypack;

import java.util.HashMap;
import java.util.Map;

import com.arangodb.entity.BaseDocument;
import com.arangodb.entity.CollectionType;
import com.arangodb.entity.DocumentField;
import com.arangodb.internal.net.Request;
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
		doc.put(DocumentField.Type.ID.getSerializeName(), value.getDocumentHandle());
		doc.put(DocumentField.Type.KEY.getSerializeName(), value.getDocumentKey());
		doc.put(DocumentField.Type.REV.getSerializeName(), value.getDocumentRevision());
		context.serialize(builder, attribute, doc);
	};
	@Deprecated
	public static final VPackSerializer<Request> REQUEST = (builder, attribute, value, context) -> {
		final Map<String, Object> parameter = value.getParameter();
		parameter.entrySet().stream().forEach(entry -> {
			if (entry.getValue() != null) {
				entry.setValue(entry.getValue().toString());
			}
		});
		final Map<String, Object> doc = new HashMap<>();
		doc.put("version", value.getVersion());
		doc.put("type", value.getType());
		doc.put("database", value.getDatabase());
		doc.put("requestType", value.getRequestType().getType());
		doc.put("request", value.getRequest());
		doc.put("parameter", parameter);
		doc.put("meta", value.getMeta());
		context.serialize(builder, attribute, doc);
	};
}
