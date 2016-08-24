package com.arangodb.internal.velocypack;

import org.json.simple.JSONValue;

import com.arangodb.entity.BaseDocument;
import com.arangodb.entity.CollectionStatus;
import com.arangodb.entity.CollectionType;
import com.arangodb.entity.DocumentField;
import com.arangodb.internal.CollectionCache;
import com.arangodb.internal.net.Request;
import com.arangodb.internal.net.Response;
import com.arangodb.velocypack.VPack;
import com.arangodb.velocypack.VPackParser;
import com.arangodb.velocypack.VPackSlice;
import com.arangodb.velocypack.ValueType;
import com.arangodb.velocypack.internal.util.NumberUtil;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class VPackConfigure {

	private static final String ID = "_id";

	public static void configure(
		final VPack.Builder builder,
		final VPackParser vpackParser,
		final CollectionCache cache) {

		builder.fieldNamingStrategy(field -> {
			final DocumentField annotation = field.getAnnotation(DocumentField.class);
			if (annotation != null) {
				return annotation.value().getSerializeName();
			}
			return field.getName();
		});
		builder.registerDeserializer(ID, String.class, (parent, vpack, context) -> {
			final String id;
			if (vpack.isCustom()) {
				final long idLong = NumberUtil.toLong(vpack.getVpack(), vpack.getStart() + 1, vpack.getByteSize() - 1);
				final String collectionName = cache.getCollectionName(idLong);
				if (collectionName != null) {
					final VPackSlice key = parent.get("_key");
					id = String.format("%s/%s", collectionName, key.getAsString());
				} else {
					id = null;
				}
			} else {
				id = vpack.getAsString();
			}
			return id;
		});
		vpackParser.registerDeserializer(ID, ValueType.CUSTOM, (parent, attribute, vpack, json) -> {
			final String id;
			final long idLong = NumberUtil.toLong(vpack.getVpack(), vpack.getStart() + 1, vpack.getByteSize() - 1);
			final String collectionName = cache.getCollectionName(idLong);
			if (collectionName != null) {
				final VPackSlice key = parent.get("_key");
				id = String.format("%s/%s", collectionName, key.getAsString());
			} else {
				id = null;
			}
			json.append(JSONValue.toJSONString(id));
		});

		builder.registerSerializer(Request.class, VPackSerializers.REQUEST);
		builder.registerSerializer(CollectionType.class, VPackSerializers.COLLECTION_TYPE);
		builder.registerSerializer(BaseDocument.class, VPackSerializers.BASE_DOCUMENT);

		builder.registerDeserializer(Response.class, VPackDeserializers.RESPONSE);
		builder.registerDeserializer(CollectionType.class, VPackDeserializers.COLLECTION_TYPE);
		builder.registerDeserializer(CollectionStatus.class, VPackDeserializers.COLLECTION_STATUS);
		builder.registerDeserializer(BaseDocument.class, VPackDeserializers.BASE_DOCUMENT);
	}

}
