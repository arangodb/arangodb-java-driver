package com.arangodb.internal.velocypack;

import com.arangodb.entity.CollectionStatus;
import com.arangodb.entity.CollectionType;
import com.arangodb.internal.net.velocystream.RequestType;
import com.arangodb.velocypack.VPack;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class VPackConfigure {

	public static void configure(final VPack vpack) {
		vpack.registerSerializer(RequestType.class, VPackSerializers.REQUEST_TYPE)
				.registerSerializer(CollectionType.class, VPackSerializers.COLLECTION_TYPE);

		vpack.registerDeserializer(CollectionType.class, VPackDeserializers.COLLECTION_TYPE)
				.registerDeserializer(CollectionStatus.class, VPackDeserializers.COLLECTION_STATUS);
	}

}
