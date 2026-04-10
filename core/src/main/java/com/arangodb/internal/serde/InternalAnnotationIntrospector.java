package com.arangodb.internal.serde;

import tools.jackson.databind.cfg.MapperConfig;
import tools.jackson.databind.introspect.Annotated;
import tools.jackson.databind.introspect.JacksonAnnotationIntrospector;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serial;

class InternalAnnotationIntrospector extends JacksonAnnotationIntrospector {

    private final transient UserDataSerializer userDataSerializer;
    private final transient UserDataDeserializer userDataDeserializer;

    InternalAnnotationIntrospector(
            final UserDataSerializer userDataSerializer,
            final UserDataDeserializer userDataDeserializer
    ) {
        this.userDataSerializer = userDataSerializer;
        this.userDataDeserializer = userDataDeserializer;
    }

    @Override
    public Object findSerializer(MapperConfig<?> config, Annotated a) {
        if (a.getAnnotation(UserData.class) != null) {
            return userDataSerializer;
        } else {
            return super.findSerializer(config, a);
        }
    }

    @Override
    public Object findContentSerializer(MapperConfig<?> config, Annotated a) {
        if (a.getAnnotation(UserDataInside.class) != null) {
            return userDataSerializer;
        } else {
            return super.findContentSerializer(config, a);
        }
    }

    @Override
    public Object findDeserializer(MapperConfig<?> config, Annotated a) {
        if (a.getAnnotation(UserData.class) != null) {
            return userDataDeserializer;
        } else {
            return super.findDeserializer(config, a);
        }
    }

    @Override
    public Object findContentDeserializer(MapperConfig<?> config, Annotated a) {
        if (a.getAnnotation(UserDataInside.class) != null) {
            return userDataDeserializer;
        } else {
            return super.findContentDeserializer(config, a);
        }
    }

    @Serial
    private void writeObject(ObjectOutputStream out) throws IOException {
        throw new IOException("Serialization not allowed");
    }

    @Serial
    private void readObject(ObjectInputStream in) throws IOException {
        throw new IOException("Deserialization not allowed");
    }
}
