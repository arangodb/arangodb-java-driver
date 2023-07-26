package com.arangodb.serde.jackson.internal;

import com.arangodb.serde.jackson.Key;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;

class ArangoSerdeAnnotationIntrospector extends JacksonAnnotationIntrospector {
    private static final JsonInclude JSON_INCLUDE_NON_NULL = JsonIncludeNonNull.class.getAnnotation(JsonInclude.class);

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private static class JsonIncludeNonNull {
    }

    @Override
    public PropertyName findNameForSerialization(Annotated a) {
        Key kann = _findAnnotation(a, Key.class);
        if (kann != null) {
            return PropertyName.construct("_key");
        }
        return super.findNameForSerialization(a);
    }

    @Override
    public PropertyName findNameForDeserialization(Annotated a) {
        Key kann = _findAnnotation(a, Key.class);
        if (kann != null) {
            return PropertyName.construct("_key");
        } else {
            return super.findNameForDeserialization(a);
        }
    }

    @Override
    public JsonInclude.Value findPropertyInclusion(Annotated a) {
        Key kann = _findAnnotation(a, Key.class);
        if (kann != null) {
            return new JsonInclude.Value(JSON_INCLUDE_NON_NULL);
        } else {
            return super.findPropertyInclusion(a);
        }
    }
}
