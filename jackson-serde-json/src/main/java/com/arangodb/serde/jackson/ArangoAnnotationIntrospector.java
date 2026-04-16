package com.arangodb.serde.jackson;

import com.arangodb.serde.annotation.*;
import com.fasterxml.jackson.annotation.JsonInclude;
import tools.jackson.databind.PropertyName;
import tools.jackson.databind.cfg.MapperConfig;
import tools.jackson.databind.introspect.Annotated;
import tools.jackson.databind.introspect.JacksonAnnotationIntrospector;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class ArangoAnnotationIntrospector extends JacksonAnnotationIntrospector {
    private static final Map<Class<? extends Annotation>, String> MAPPINGS;
    private static final Class<? extends Annotation>[] ANNOTATIONS;

    static {
        MAPPINGS = new HashMap<>();
        MAPPINGS.put(Id.class, "_id");
        MAPPINGS.put(Key.class, "_key");
        MAPPINGS.put(Rev.class, "_rev");
        MAPPINGS.put(From.class, "_from");
        MAPPINGS.put(To.class, "_to");
        @SuppressWarnings("unchecked")
        Class<? extends Annotation>[] annotations = MAPPINGS.keySet().toArray(new Class[0]);
        ANNOTATIONS = annotations;
    }

    @Override
    public PropertyName findNameForSerialization(MapperConfig<?> config, Annotated a) {
        return Optional.ofNullable(findMapping(a)).orElseGet(() -> super.findNameForSerialization(config, a));
    }

    @Override
    public PropertyName findNameForDeserialization(MapperConfig<?> config, Annotated a) {
        return Optional.ofNullable(findMapping(a)).orElseGet(() -> super.findNameForDeserialization(config, a));
    }

    private PropertyName findMapping(Annotated a) {
        for (Map.Entry<Class<? extends Annotation>, String> e : MAPPINGS.entrySet()) {
            if (_hasAnnotation(a, e.getKey())) {
                return PropertyName.construct(e.getValue());
            }
        }
        return null;
    }

    @Override
    public JsonInclude.Value findPropertyInclusion(MapperConfig<?> config, Annotated a) {
        if (_hasOneOf(a, ANNOTATIONS)) {
            return JsonInclude.Value.ALL_NON_NULL;
        } else {
            return super.findPropertyInclusion(config, a);
        }
    }
}
