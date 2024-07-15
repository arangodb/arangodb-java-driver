package com.arangodb.vst;

import com.arangodb.vst.internal.AuthenticationRequest;
import com.arangodb.vst.internal.JwtAuthenticationRequest;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.util.function.Supplier;

enum VstModule implements Supplier<Module> {
    INSTANCE;

    private final SimpleModule module;

    VstModule() {
        module = new SimpleModule();
        module.addSerializer(AuthenticationRequest.class, VstSerializers.AUTHENTICATION_REQUEST);
        module.addSerializer(JwtAuthenticationRequest.class, VstSerializers.JWT_AUTHENTICATION_REQUEST);
    }

    @Override
    public Module get() {
        return module;
    }

}
