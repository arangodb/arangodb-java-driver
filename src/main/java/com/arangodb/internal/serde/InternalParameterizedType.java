package com.arangodb.internal.serde;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

class InternalParameterizedType implements ParameterizedType {

    private final Class<?> rawType;
    private final Type[] actualRawArguments;

    InternalParameterizedType(final Class<?> rawType, final Type[] actualRawArguments) {
        this.rawType = rawType;
        this.actualRawArguments = actualRawArguments;
    }

    @Override
    public Type getRawType() {
        return rawType;
    }

    @Override
    public Type[] getActualTypeArguments() {
        return actualRawArguments;
    }

    @Override
    public Type getOwnerType() {
        return null;
    }

}
