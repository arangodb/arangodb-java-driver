package com.arangodb.internal.velocypack;

import java.util.Map;

import com.arangodb.entity.BaseDocument;
import com.arangodb.entity.BaseEdgeDocument;
import com.arangodb.entity.CollectionStatus;
import com.arangodb.entity.CollectionType;
import com.arangodb.internal.net.Response;
import com.arangodb.velocypack.VPackDeserializer;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class VPackDeserializers {

	public static final VPackDeserializer<Response> RESPONSE = (parent, vpack, context) -> {
		final Response response = new Response();
		response.setVersion(vpack.get(0).getAsInt());
		response.setType(vpack.get(1).getAsInt());
		response.setResponseCode(vpack.get(2).getAsInt());
		return response;
	};

	public static final VPackDeserializer<CollectionType> COLLECTION_TYPE = (parent, vpack, context) -> CollectionType
			.fromType(vpack.getAsInt());

	public static final VPackDeserializer<CollectionStatus> COLLECTION_STATUS = (
		parent,
		vpack,
		context) -> CollectionStatus.fromStatus(vpack.getAsInt());

	@SuppressWarnings("unchecked")
	public static final VPackDeserializer<BaseDocument> BASE_DOCUMENT = (parent, vpack, context) -> new BaseDocument(
			context.deserialize(vpack, Map.class));

	@SuppressWarnings("unchecked")
	public static final VPackDeserializer<BaseEdgeDocument> BASE_EDGE_DOCUMENT = (
		parent,
		vpack,
		context) -> new BaseEdgeDocument(context.deserialize(vpack, Map.class));

}
