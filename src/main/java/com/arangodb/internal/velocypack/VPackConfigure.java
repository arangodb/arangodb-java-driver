package com.arangodb.internal.velocypack;

import com.arangodb.entity.BaseDocument;
import com.arangodb.entity.CollectionStatus;
import com.arangodb.entity.CollectionType;
import com.arangodb.entity.DocumentField;
import com.arangodb.internal.net.velocystream.RequestType;
import com.arangodb.velocypack.VPack;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class VPackConfigure {

	public static void configure(final VPack.Builder builder) {

		builder.fieldNamingStrategy(field -> {
			final DocumentField annotation = field.getAnnotation(DocumentField.class);
			if (annotation != null) {
				return annotation.value().getSerializeName();
			}
			return field.getName();
		});

		builder.registerSerializer(RequestType.class, VPackSerializers.REQUEST_TYPE);
		builder.registerSerializer(CollectionType.class, VPackSerializers.COLLECTION_TYPE);
		builder.registerSerializer(BaseDocument.class, VPackSerializers.BASE_DOCUMENT);

		builder.registerDeserializer(CollectionType.class, VPackDeserializers.COLLECTION_TYPE);
		builder.registerDeserializer(CollectionStatus.class, VPackDeserializers.COLLECTION_STATUS);
		builder.registerDeserializer(BaseDocument.class, VPackDeserializers.BASE_DOCUMENT);
	}

}
