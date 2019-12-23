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

package com.arangodb.internal;

import com.arangodb.ArangoDBException;
import com.arangodb.entity.DocumentField;
import com.arangodb.entity.DocumentField.Type;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.Map.Entry;

/**
 * @author Mark Vollmary
 */
public class DocumentCache {

    private final Map<Class<?>, Map<DocumentField.Type, Field>> cache;

    public DocumentCache() {
        super();
        cache = new HashMap<>();
    }

    public void setValues(final Object doc, final Map<DocumentField.Type, String> values) throws ArangoDBException {
        try {
            final Map<DocumentField.Type, Field> fields = getFields(doc.getClass());
            for (final Entry<DocumentField.Type, String> value : values.entrySet()) {
                final Field field = fields.get(value.getKey());
                if (field != null) {
                    field.set(doc, value.getValue());
                }
            }
        } catch (final IllegalArgumentException | IllegalAccessException e) {
            throw new ArangoDBException(e);
        }
    }

    private Map<DocumentField.Type, Field> getFields(final Class<?> clazz) {
        Map<DocumentField.Type, Field> fields = new HashMap<>();
        if (!isTypeRestricted(clazz)) {
            fields = cache.get(clazz);
            if (fields == null) {
                fields = createFields(clazz);
                cache.put(clazz, fields);
            }
        }
        return fields;
    }

    private boolean isTypeRestricted(final Class<?> type) {
        return Map.class.isAssignableFrom(type) || Collection.class.isAssignableFrom(type);
    }

    private Map<DocumentField.Type, Field> createFields(final Class<?> clazz) {
        final Map<DocumentField.Type, Field> fields = new HashMap<>();
        Class<?> tmp = clazz;
        final Collection<DocumentField.Type> values = new ArrayList<>(
                Arrays.asList(DocumentField.Type.values()));
        while (tmp != null && tmp != Object.class && values.size() > 0) {
            final Field[] declaredFields = tmp.getDeclaredFields();
            for (int i = 0; i < declaredFields.length && values.size() > 0; i++) {
                findAnnotation(values, fields, declaredFields[i]);
            }
            tmp = tmp.getSuperclass();
        }
        return fields;
    }

    private void findAnnotation(
            final Collection<Type> values,
            final Map<DocumentField.Type, Field> fields,
            final Field field) {
        final DocumentField annotation = field.getAnnotation(DocumentField.class);
        if (annotation != null && !field.isSynthetic() && !Modifier.isStatic(field.getModifiers())
                && String.class.isAssignableFrom(field.getType())) {
            final Type value = annotation.value();
            if (values.contains(value)) {
                field.setAccessible(true);
                fields.put(value, field);
                values.remove(value);
            }
        }
    }
}
