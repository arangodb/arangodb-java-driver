package com.arangodb.serde;

import com.arangodb.entity.CollectionStatus;
import com.arangodb.entity.CollectionType;
import com.arangodb.entity.ReplicationFactor;
import com.arangodb.internal.velocystream.internal.AuthenticationRequest;
import com.arangodb.internal.velocystream.internal.JwtAuthenticationRequest;
import com.arangodb.velocypack.VPackSlice;
import com.arangodb.velocystream.Request;
import com.arangodb.velocystream.Response;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.util.function.Supplier;

enum InternalModule implements Supplier<Module> {
    INSTANCE;

    private final SimpleModule module;

    InternalModule() {
        module = new SimpleModule();

        module.addSerializer(VPackSlice.class, InternalSerializers.VPACK_SLICE_JSON_SERIALIZER);
        module.addSerializer(AuthenticationRequest.class, InternalSerializers.AUTHENTICATION_REQUEST);
        module.addSerializer(JwtAuthenticationRequest.class, InternalSerializers.JWT_AUTHENTICATION_REQUEST);
        module.addSerializer(Request.class, InternalSerializers.REQUEST);
        module.addSerializer(CollectionType.class, InternalSerializers.COLLECTION_TYPE);

        module.addDeserializer(CollectionStatus.class, InternalDeserializers.COLLECTION_STATUS);
        module.addDeserializer(CollectionType.class, InternalDeserializers.COLLECTION_TYPE);
        module.addDeserializer(ReplicationFactor.class, InternalDeserializers.REPLICATION_FACTOR);
        module.addDeserializer(Response.class, InternalDeserializers.RESPONSE);
    }

    @Override
    public Module get() {
        return module;
    }

}
