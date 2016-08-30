package com.arangodb.velocypack.internal;

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
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.arangodb.velocypack.VPackFieldNamingStrategy;
import com.arangodb.velocypack.annotations.Expose;
import com.arangodb.velocypack.annotations.SerializedName;

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

	private final Map<Type, Map<String, FieldInfo>> cache;
	private final Comparator<Entry<String, FieldInfo>> fieldComparator;
	private final Optional<VPackFieldNamingStrategy> fieldNamingStrategy;

	public VPackCache(final VPackFieldNamingStrategy fieldNamingStrategy) {
		super();
		cache = new ConcurrentHashMap<>();
		fieldComparator = new Comparator<Map.Entry<String, FieldInfo>>() {
			@Override
			public int compare(final Entry<String, FieldInfo> o1, final Entry<String, FieldInfo> o2) {
				return o1.getKey().compareTo(o2.getKey());
			}
		};
		this.fieldNamingStrategy = Optional.ofNullable(fieldNamingStrategy);
	}

	public Map<String, FieldInfo> getFields(final Type entityClass) {
		Map<String, FieldInfo> fields = cache.get(entityClass);
		if (fields == null) {
			fields = new HashMap<>();
			Class<?> tmp = (Class<?>) entityClass;
			while (tmp != null && tmp != Object.class) {
				final Field[] declaredFields = tmp.getDeclaredFields();
				for (final Field field : declaredFields) {
					if (!field.isSynthetic() && !Modifier.isStatic(field.getModifiers())) {
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
		final Map<String, FieldInfo> sorted = new LinkedHashMap<>();
		final List<Entry<String, FieldInfo>> tmp = new ArrayList<>(entrySet);
		Collections.sort(tmp, fieldComparator);
		for (final Entry<String, FieldInfo> entry : tmp) {
			sorted.put(entry.getKey(), entry.getValue());
		}
		return sorted;
	}

	private FieldInfo createFieldInfo(final Field field) {
		String fieldName = field.getName();
		if (fieldNamingStrategy.isPresent()) {
			fieldName = fieldNamingStrategy.get().translateName(field);
		}
		final SerializedName annotationName = field.getAnnotation(SerializedName.class);
		if (annotationName != null) {
			fieldName = annotationName.value();
		}
		final Expose expose = field.getAnnotation(Expose.class);
		final boolean serialize = expose != null ? expose.serialize() : true;
		final boolean deserialize = expose != null ? expose.deserialize() : true;
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
