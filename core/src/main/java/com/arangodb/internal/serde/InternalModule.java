package com.arangodb.internal.serde;

import com.arangodb.entity.CollectionStatus;
import com.arangodb.entity.CollectionType;
import com.arangodb.entity.InvertedIndexPrimarySort;
import com.arangodb.entity.MultiDocumentEntity;
import com.arangodb.entity.ReplicationFactor;
import com.arangodb.util.RawBytes;
import com.arangodb.util.RawJson;
import com.arangodb.internal.InternalRequest;
import com.arangodb.internal.InternalResponse;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.module.SimpleModule;

class InternalModule {

    static Module get(InternalSerde serde) {
        SimpleModule module = new SimpleModule();

        module.addDeserializer(MultiDocumentEntity.class, new MultiDocumentEntityDeserializer(serde));

        module.addSerializer(RawJson.class, InternalSerializers.RAW_JSON_SERIALIZER);
        module.addSerializer(InternalRequest.class, InternalSerializers.REQUEST);
        module.addSerializer(CollectionType.class, InternalSerializers.COLLECTION_TYPE);

        module.addDeserializer(RawJson.class, InternalDeserializers.RAW_JSON_DESERIALIZER);
        module.addDeserializer(RawBytes.class, InternalDeserializers.RAW_BYTES_DESERIALIZER);
        module.addDeserializer(CollectionStatus.class, InternalDeserializers.COLLECTION_STATUS);
        module.addDeserializer(CollectionType.class, InternalDeserializers.COLLECTION_TYPE);
        module.addDeserializer(ReplicationFactor.class, InternalDeserializers.REPLICATION_FACTOR);
        module.addDeserializer(InternalResponse.class, InternalDeserializers.RESPONSE);
        module.addDeserializer(InvertedIndexPrimarySort.Field.class, InternalDeserializers.INVERTED_INDEX_PRIMARY_SORT_FIELD);

        return module;
    }
}
