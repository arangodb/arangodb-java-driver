package com.arangodb.serde;

import com.arangodb.entity.CollectionType;
import com.arangodb.internal.velocystream.internal.AuthenticationRequest;
import com.arangodb.internal.velocystream.internal.JwtAuthenticationRequest;
import com.arangodb.velocystream.Request;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.util.function.Supplier;

enum InternalModule implements Supplier<Module> {
    INSTANCE;

    private final SimpleModule module;

    InternalModule() {
        module = new SimpleModule();
        module.addSerializer(AuthenticationRequest.class, InternalSerializers.AUTHENTICATION_REQUEST);
        module.addSerializer(JwtAuthenticationRequest.class, InternalSerializers.JWT_AUTHENTICATION_REQUEST);
        module.addSerializer(Request.class, InternalSerializers.REQUEST);
        module.addSerializer(CollectionType.class, InternalSerializers.COLLECTION_TYPE);
    }

    @Override
    public Module get() {
        return module;
    }

}
