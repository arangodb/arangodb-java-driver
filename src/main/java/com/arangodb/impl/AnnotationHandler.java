package com.arangodb.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arangodb.entity.BaseDocument;
import com.google.gson.annotations.SerializedName;

public class AnnotationHandler {

	private static Logger logger = LoggerFactory.getLogger(AnnotationHandler.class);

	static class DocumentAttributes {
		public Field rev; // NOSONAR
		public Field id; // NOSONAR
		public Field key; // NOSONAR
		public Field from; // NOSONAR
		public Field to; // NOSONAR
	}

	static Map<Class<?>, DocumentAttributes> class2DocumentAttributes;

	static {
		class2DocumentAttributes = new HashMap<Class<?>, DocumentAttributes>();
	}

	public AnnotationHandler() {
		// do nothing here
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public synchronized void updateDocumentAttributes(
		final Object o,
		final String rev,
		final String id,
		final String key) {
		if (o != null) {
			if (o instanceof java.util.Map) {
				final java.util.Map m = (java.util.Map) o;
				m.put(BaseDocument.ID, id);
				m.put(BaseDocument.KEY, key);
				m.put(BaseDocument.REV, rev);
			} else {
				final DocumentAttributes documentAttributes = getDocumentAttributes(o);
				setAttribute(documentAttributes.id, o, id);
				setAttribute(documentAttributes.key, o, key);
				setAttribute(documentAttributes.rev, o, rev);
			}
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public synchronized void updateDocumentRev(final Object o, final String rev) {
		if (o != null) {
			if (o instanceof java.util.Map) {
				final java.util.Map m = (java.util.Map) o;
				m.put(BaseDocument.REV, rev);
			} else {
				final DocumentAttributes documentAttributes = getDocumentAttributes(o);
				setAttribute(documentAttributes.rev, o, rev);
			}
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public synchronized void updateEdgeAttributes(
		final Object o,
		final String rev,
		final String id,
		final String key,
		final String from,
		final String to) {
		if (o != null) {
			if (o instanceof java.util.Map) {
				final java.util.Map m = (java.util.Map) o;
				m.put(BaseDocument.ID, id);
				m.put(BaseDocument.KEY, key);
				m.put(BaseDocument.REV, rev);
				if (from != null) {
					m.put(BaseDocument.FROM, from);
				}
				if (to != null) {
					m.put(BaseDocument.TO, to);
				}
			} else {
				final DocumentAttributes documentAttributes = getDocumentAttributes(o);
				setAttribute(documentAttributes.id, o, id);
				setAttribute(documentAttributes.key, o, key);
				setAttribute(documentAttributes.rev, o, rev);
				if (from != null) {
					setAttribute(documentAttributes.from, o, from);
				}
				if (to != null) {
					setAttribute(documentAttributes.to, o, to);
				}
			}
		}
	}

	private void setAttribute(final Field field, final Object o, final Object value) {
		if (field != null) {
			try {
				field.setAccessible(true);
				field.set(o, value);
			} catch (final Exception e) {
				logger.error("could not update document attribute of class " + value.getClass().getCanonicalName(), e);
			}
		}
	}

	private DocumentAttributes getDocumentAttributes(final Object o) {
		final Class<? extends Object> clazz = o.getClass();
		DocumentAttributes documentAttributes = class2DocumentAttributes.get(clazz);

		if (documentAttributes == null) {
			documentAttributes = new DocumentAttributes();
			documentAttributes.id = getFieldByAnnotationValue(clazz, BaseDocument.ID);
			documentAttributes.key = getFieldByAnnotationValue(clazz, BaseDocument.KEY);
			documentAttributes.rev = getFieldByAnnotationValue(clazz, BaseDocument.REV);
			documentAttributes.from = getFieldByAnnotationValue(clazz, BaseDocument.FROM);
			documentAttributes.to = getFieldByAnnotationValue(clazz, BaseDocument.TO);
			class2DocumentAttributes.put(clazz, documentAttributes);
		}

		return documentAttributes;
	}

	private Field getFieldByAnnotationValue(final Class<?> clazz, final String value) {

		final List<Field> fields = getAllDeclaredFields(clazz);
		for (final Field field : fields) {

			final Annotation[] annotations = field.getAnnotations();
			for (final Annotation annotation : annotations) {

				if (annotation instanceof SerializedName && value.equals(((SerializedName) annotation).value())) {
					return field;
				}
			}
		}

		return null;
	}

	private List<Field> getAllDeclaredFields(final Class<?> clazz) {
		final List<Field> result = new ArrayList<Field>();

		Class<?> current = clazz;

		while (current != null) {
			final Field[] fields = current.getDeclaredFields();
			for (final Field field : fields) {
				result.add(field);
			}
			current = current.getSuperclass();
		}
		return result;
	}

}
