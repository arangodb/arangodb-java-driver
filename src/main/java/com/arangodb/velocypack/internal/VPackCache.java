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

package com.arangodb.velocypack.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arangodb.velocypack.VPackAnnotationFieldFilter;
import com.arangodb.velocypack.VPackAnnotationFieldNaming;
import com.arangodb.velocypack.VPackFieldNamingStrategy;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class VPackCache {

	public abstract static class FieldInfo {
		private final Type type;
		private final String fieldName;
		private final boolean serialize;
		private final boolean deserialize;

		private FieldInfo(final Type type, final String fieldName, final boolean serialize, final boolean deserialize) {
			super();
			this.type = type;
			this.fieldName = fieldName;
			this.serialize = serialize;
			this.deserialize = deserialize;
		}

		public Type getType() {
			return type;
		}

		public String getFieldName() {
			return fieldName;
		}

		public boolean isSerialize() {
			return serialize;
		}

		public boolean isDeserialize() {
			return deserialize;
		}

		public abstract void set(Object obj, Object value) throws IllegalAccessException;

		public abstract Object get(Object obj) throws IllegalAccessException;
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(VPackCache.class);

	private final Map<Type, Map<String, FieldInfo>> cache;
	private final Comparator<Entry<String, FieldInfo>> fieldComparator;
	private final VPackFieldNamingStrategy fieldNamingStrategy;

	private final Map<Class<? extends Annotation>, VPackAnnotationFieldFilter<? extends Annotation>> annotationFilter;
	private final Map<Class<? extends Annotation>, VPackAnnotationFieldNaming<? extends Annotation>> annotationFieldNaming;

	public VPackCache(final VPackFieldNamingStrategy fieldNamingStrategy,
		final Map<Class<? extends Annotation>, VPackAnnotationFieldFilter<? extends Annotation>> annotationFieldFilter,
		final Map<Class<? extends Annotation>, VPackAnnotationFieldNaming<? extends Annotation>> annotationFieldNaming) {
		super();
		cache = new ConcurrentHashMap<Type, Map<String, FieldInfo>>();
		fieldComparator = new Comparator<Map.Entry<String, FieldInfo>>() {
			@Override
			public int compare(final Entry<String, FieldInfo> o1, final Entry<String, FieldInfo> o2) {
				return o1.getKey().compareTo(o2.getKey());
			}
		};
		this.fieldNamingStrategy = fieldNamingStrategy;
		this.annotationFilter = annotationFieldFilter;
		this.annotationFieldNaming = annotationFieldNaming;
	}

	public Map<String, FieldInfo> getFields(final Type entityClass) {
		Map<String, FieldInfo> fields = cache.get(entityClass);
		if (fields == null) {
			fields = new HashMap<String, VPackCache.FieldInfo>();
			Class<?> tmp = (Class<?>) entityClass;
			while (tmp != null && tmp != Object.class) {
				final Field[] declaredFields = tmp.getDeclaredFields();
				for (final Field field : declaredFields) {
					if (!field.isSynthetic() && !Modifier.isStatic(field.getModifiers())
							&& !Modifier.isTransient(field.getModifiers())) {
						field.setAccessible(true);
						final FieldInfo fieldInfo = createFieldInfo(field);
						if (fieldInfo.serialize || fieldInfo.deserialize) {
							fields.put(fieldInfo.getFieldName(), fieldInfo);
						}
					}
				}
				tmp = tmp.getSuperclass();
			}
			fields = sort(fields.entrySet());
			cache.put(entityClass, fields);
		}
		return fields;
	}

	private Map<String, FieldInfo> sort(final Set<Entry<String, FieldInfo>> entrySet) {
		final Map<String, FieldInfo> sorted = new LinkedHashMap<String, VPackCache.FieldInfo>();
		final List<Entry<String, FieldInfo>> tmp = new ArrayList<Map.Entry<String, FieldInfo>>(entrySet);
		Collections.sort(tmp, fieldComparator);
		for (final Entry<String, FieldInfo> entry : tmp) {
			sorted.put(entry.getKey(), entry.getValue());
		}
		return sorted;
	}

	@SuppressWarnings("unchecked")
	private FieldInfo createFieldInfo(final Field field) {
		String fieldName = field.getName();
		if (fieldNamingStrategy != null) {
			fieldName = fieldNamingStrategy.translateName(field);
		}
		boolean found = false;
		for (final Entry<Class<? extends Annotation>, VPackAnnotationFieldNaming<? extends Annotation>> entry : annotationFieldNaming
				.entrySet()) {
			final Annotation annotation = field.getAnnotation(entry.getKey());
			if (annotation != null) {
				fieldName = ((VPackAnnotationFieldNaming<Annotation>) entry.getValue()).name(annotation);
				if (found) {
					LOGGER.warn(String.format(
						"Found additional annotation %s for field %s. Override previous annotation informations.",
						entry.getKey().getSimpleName(), field.getName()));
				}
				found = true;
			}
		}
		boolean serialize = true;
		boolean deserialize = true;
		found = false;
		for (final Entry<Class<? extends Annotation>, VPackAnnotationFieldFilter<? extends Annotation>> entry : annotationFilter
				.entrySet()) {
			final Annotation annotation = field.getAnnotation(entry.getKey());
			if (annotation != null) {
				final VPackAnnotationFieldFilter<Annotation> filter = (VPackAnnotationFieldFilter<Annotation>) entry
						.getValue();
				serialize = filter.serialize(annotation);
				deserialize = filter.deserialize(annotation);
				if (found) {
					LOGGER.warn(String.format(
						"Found additional annotation %s for field %s. Override previous annotation informations.",
						entry.getKey().getSimpleName(), field.getName()));
				}
				found = true;
			}
		}
		final Class<?> clazz = field.getType();
		final Type type;
		if (Collection.class.isAssignableFrom(clazz) || Map.class.isAssignableFrom(clazz)) {
			type = (ParameterizedType) field.getGenericType();
		} else {
			type = clazz;
		}
		return new FieldInfo(type, fieldName, serialize, deserialize) {
			@Override
			public void set(final Object obj, final Object value) throws IllegalAccessException {
				field.set(obj, value);
			}

			@Override
			public Object get(final Object obj) throws IllegalAccessException {
				return field.get(obj);
			}
		};
	}

}
