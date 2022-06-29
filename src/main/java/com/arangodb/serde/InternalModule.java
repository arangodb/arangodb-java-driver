package com.arangodb.serde;

import com.arangodb.internal.velocystream.internal.AuthenticationRequest;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.util.function.Supplier;

enum InternalModule implements Supplier<Module> {
    INSTANCE;

    private final SimpleModule module;

    InternalModule() {
        module = new SimpleModule();
        module.addSerializer(AuthenticationRequest.class, InternalSerializers.AUTHENTICATION_REQUEST);
    }

    @Override
    public Module get() {
        return module;
    }

}
