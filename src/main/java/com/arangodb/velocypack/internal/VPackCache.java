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
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.arangodb.velocypack.annotations.Expose;
import com.arangodb.velocypack.annotations.SerializedName;

/**
 * @author Mark - mark@arangodb.com
 *
 */
public class VPackCache {

	public abstract static class FieldInfo {
		private final Class<?> type;
		private final String fieldName;
		private final boolean serialize;
		private final boolean deserialize;
		private final Class<?>[] parameterizedTypes;

		private FieldInfo(final Class<?> type, final String fieldName, final boolean serialize,
			final boolean deserialize, final Class<?>[] parameterizedTypes) {
			super();
			this.type = type;
			this.fieldName = fieldName;
			this.serialize = serialize;
			this.deserialize = deserialize;
			this.parameterizedTypes = parameterizedTypes;
		}

		public Class<?> getType() {
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

		public Class<?>[] getParameterizedTypes() {
			return parameterizedTypes;
		}

		public abstract void set(Object obj, Object value) throws IllegalAccessException;

		public abstract Object get(Object obj) throws IllegalAccessException;
	}

	private final Map<Class<?>, Map<String, FieldInfo>> cache;
	private final Comparator<Entry<String, FieldInfo>> fieldComparator;

	public VPackCache() {
		super();
		cache = new ConcurrentHashMap<Class<?>, Map<String, FieldInfo>>();
		fieldComparator = new Comparator<Map.Entry<String, FieldInfo>>() {
			@Override
			public int compare(final Entry<String, FieldInfo> o1, final Entry<String, FieldInfo> o2) {
				return o1.getKey().compareTo(o2.getKey());
			}
		};
	}

	public Map<String, FieldInfo> getFields(final Class<?> entityClass) {
		Map<String, FieldInfo> fields = cache.get(entityClass);
		if (fields == null) {
			fields = new HashMap<String, VPackCache.FieldInfo>();
			Class<?> tmp = entityClass;
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
		final Map<String, FieldInfo> sorted = new LinkedHashMap<String, VPackCache.FieldInfo>();
		final List<Entry<String, FieldInfo>> tmp = new ArrayList<Entry<String, FieldInfo>>(entrySet);
		Collections.sort(tmp, fieldComparator);
		for (final Entry<String, FieldInfo> entry : tmp) {
			sorted.put(entry.getKey(), entry.getValue());
		}
		return sorted;
	}

	private FieldInfo createFieldInfo(final Field field) {
		final SerializedName annotationName = field.getAnnotation(SerializedName.class);
		final String fieldName = annotationName != null ? annotationName.value() : field.getName();
		final Expose expose = field.getAnnotation(Expose.class);
		final boolean serialize = expose != null ? expose.serialize() : true;
		final boolean deserialize = expose != null ? expose.deserialize() : true;
		final Class<?> type = field.getType();
		Class<?>[] parameterizedTypes = null;
		if (type.isArray()) {
			parameterizedTypes = new Class<?>[] { type.getComponentType() };
		} else if (Collection.class.isAssignableFrom(type)) {
			final ParameterizedType genericType = (ParameterizedType) field.getGenericType();
			parameterizedTypes = new Class<?>[] { getParameterizedType(genericType.getActualTypeArguments()[0]) };
		} else if (Map.class.isAssignableFrom(type)) {
			final ParameterizedType genericType = (ParameterizedType) field.getGenericType();
			final Class<?> key = getParameterizedType(genericType.getActualTypeArguments()[0]);
			final Class<?> value = getParameterizedType(genericType.getActualTypeArguments()[1]);
			parameterizedTypes = new Class<?>[] { key, value };
		}
		return new FieldInfo(field.getType(), fieldName, serialize, deserialize, parameterizedTypes) {
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

	private Class<?> getParameterizedType(final Type argType) {
		return (Class<?>) (ParameterizedType.class.isAssignableFrom(argType.getClass())
				? ParameterizedType.class.cast(argType).getRawType() : argType);
	}

}
