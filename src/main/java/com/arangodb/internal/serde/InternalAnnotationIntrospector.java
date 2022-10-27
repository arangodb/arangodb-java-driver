package com.arangodb.internal.serde;

import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

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
    public Object findSerializer(Annotated a) {
        if (a.getAnnotation(UserData.class) != null) {
            return userDataSerializer;
        } else {
            return super.findSerializer(a);
        }
    }

    @Override
    public Object findContentSerializer(Annotated a) {
        if (a.getAnnotation(UserDataInside.class) != null) {
            return userDataSerializer;
        } else {
            return super.findContentSerializer(a);
        }
    }

    @Override
    public Object findDeserializer(Annotated a) {
        if (a.getAnnotation(UserData.class) != null) {
            return userDataDeserializer;
        } else {
            return super.findDeserializer(a);
        }
    }

    @Override
    public Object findContentDeserializer(Annotated a) {
        if (a.getAnnotation(UserDataInside.class) != null) {
            return userDataDeserializer;
        } else {
            return super.findContentDeserializer(a);
        }
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        throw new IOException("Serialization not allowed");
    }

    private void readObject(ObjectInputStream in) throws IOException {
        throw new IOException("Deserialization not allowed");
    }
}
