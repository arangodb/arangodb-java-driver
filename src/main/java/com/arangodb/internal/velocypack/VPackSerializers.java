package com.arangodb.internal.velocypack;

import com.arangodb.entity.CollectionType;
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

}
