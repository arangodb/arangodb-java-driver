package com.arangodb.internal;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.arangodb.ArangoDBException;
import com.arangodb.entity.DocumentField;
import com.arangodb.entity.DocumentField.Type;

/**
 * @author Mark - mark at arangodb.com
 *
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
		} catch (IllegalArgumentException | IllegalAccessException e) {
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
		final Class<?> tmp = clazz;
		final Collection<DocumentField.Type> values = new ArrayList<>(Arrays.asList(DocumentField.Type.values()));
		while (tmp != null && tmp != Object.class && values.size() > 0) {
			final Field[] declaredFields = tmp.getDeclaredFields();
			for (int i = 0; i < declaredFields.length && values.size() > 0; i++) {
				findAnnotation(values, fields, declaredFields[i]);
			}
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
