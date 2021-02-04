/*
 * DISCLAIMER
 *
 * Copyright 2016 ArangoDB GmbH, Cologne, Germany
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Copyright holder is ArangoDB GmbH, Cologne, Germany
 */

package com.arangodb.internal.mapping;


import com.arangodb.entity.DocumentField;
import com.arangodb.velocypack.annotations.Expose;
import com.arangodb.velocypack.annotations.SerializedName;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.AnnotatedParameter;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;

/**
 * @author Michele Rastelli
 */
public class ArangoAnnotationIntrospector extends JacksonAnnotationIntrospector {

    @Override
    public JsonProperty.Access findPropertyAccess(Annotated m) {
        if (!(m instanceof AnnotatedMember)) {
            return super.findPropertyAccess(m);
        }

        final Expose expose = m.getAnnotation(Expose.class);
        if (expose != null) {
            final boolean serialize = expose.serialize();
            final boolean deserialize = expose.deserialize();

            if (serialize && deserialize) {
                return JsonProperty.Access.READ_WRITE;
            } else if (serialize) {
                return JsonProperty.Access.READ_ONLY;
            } else if (deserialize) {
                return JsonProperty.Access.WRITE_ONLY;
            }
        }

        return super.findPropertyAccess(m);
    }

    @Override
    public boolean hasIgnoreMarker(AnnotatedMember m) {
        final Expose expose = m.getAnnotation(Expose.class);
        if (expose != null && !expose.serialize() && !expose.deserialize()) {
            return true;
        }
        return super.hasIgnoreMarker(m);
    }

    @Override
    public PropertyName findNameForSerialization(Annotated a) {
        PropertyName name = findPropertyName(a);
        if (name != null) {
            return name;
        } else {
            return super.findNameForSerialization(a);
        }
    }

    @Override
    public PropertyName findNameForDeserialization(Annotated a) {
        PropertyName name = findPropertyName(a);
        if (name != null) {
            return name;
        } else {
            return super.findNameForDeserialization(a);
        }
    }

    @Override
    public String findImplicitPropertyName(AnnotatedMember member) {
        String name = findParameterName(member);
        if (name != null) {
            return name;
        } else {
            return super.findImplicitPropertyName(member);
        }
    }

    private String findParameterName(Annotated a) {
        if (!(a instanceof AnnotatedParameter)) {
            return null;
        }

        final SerializedName serializedName = a.getAnnotation(SerializedName.class);
        if (serializedName != null) {
            return serializedName.value();
        }

        return null;
    }

    private PropertyName findPropertyName(Annotated a) {
        if (!(a instanceof AnnotatedMember)) {
            return null;
        }

        final DocumentField documentField = a.getAnnotation(DocumentField.class);
        if (documentField != null) {
            return PropertyName.construct(documentField.value().getSerializeName());
        }

        final SerializedName serializedName = a.getAnnotation(SerializedName.class);
        if (serializedName != null) {
            return PropertyName.construct(serializedName.value());
        }

        return null;
    }

}

