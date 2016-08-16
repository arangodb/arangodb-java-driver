package com.arangodb.internal.velocypack;

import java.util.Map;

import com.arangodb.entity.BaseDocument;
import com.arangodb.entity.CollectionStatus;
import com.arangodb.entity.CollectionType;
import com.arangodb.velocypack.VPackDeserializer;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class VPackDeserializers {

	public static final VPackDeserializer<CollectionType> COLLECTION_TYPE = (parent, vpack, context) -> CollectionType
			.fromType(vpack.getAsInt());

	public static final VPackDeserializer<CollectionStatus> COLLECTION_STATUS = (
		parent,
		vpack,
		context) -> CollectionStatus.fromStatus(vpack.getAsInt());

	@SuppressWarnings("unchecked")
	public static final VPackDeserializer<BaseDocument> BASE_DOCUMENT = (parent, vpack, context) -> new BaseDocument(
			context.deserialize(vpack, Map.class));
}
