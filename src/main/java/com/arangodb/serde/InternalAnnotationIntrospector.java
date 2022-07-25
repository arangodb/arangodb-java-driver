package com.arangodb.serde;

import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;

class InternalAnnotationIntrospector extends JacksonAnnotationIntrospector {

    private final UserDataSerializer userDataSerializer;

    InternalAnnotationIntrospector(final UserDataSerializer userDataSerializer) {
        this.userDataSerializer = userDataSerializer;
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

}
