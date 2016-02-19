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

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public synchronized void updateDocumentAttributes(Object o, long rev, String id, String key) {
		if (o != null) {
			if (o instanceof java.util.Map) {
				java.util.Map m = (java.util.Map) o;
				m.put(BaseDocument.ID, id);
				m.put(BaseDocument.KEY, key);
				m.put(BaseDocument.REV, rev);
			} else {
				DocumentAttributes documentAttributes = getDocumentAttributes(o);
				setAttribute(documentAttributes.id, o, id);
				setAttribute(documentAttributes.key, o, key);
				setAttribute(documentAttributes.rev, o, rev);
			}
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public synchronized void updateDocumentRev(Object o, long rev) {
		if (o != null) {
			if (o instanceof java.util.Map) {
				java.util.Map m = (java.util.Map) o;
				m.put(BaseDocument.REV, rev);
			} else {
				DocumentAttributes documentAttributes = getDocumentAttributes(o);
				setAttribute(documentAttributes.rev, o, rev);
			}
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public synchronized void updateEdgeAttributes(Object o, long rev, String id, String key, String from, String to) {
		if (o != null) {
			if (o instanceof java.util.Map) {
				java.util.Map m = (java.util.Map) o;
				m.put(BaseDocument.ID, id);
				m.put(BaseDocument.KEY, key);
				m.put(BaseDocument.REV, rev);
				m.put(BaseDocument.FROM, from);
				m.put(BaseDocument.TO, to);
			} else {
				DocumentAttributes documentAttributes = getDocumentAttributes(o);
				setAttribute(documentAttributes.id, o, id);
				setAttribute(documentAttributes.key, o, key);
				setAttribute(documentAttributes.rev, o, rev);
				setAttribute(documentAttributes.from, o, from);
				setAttribute(documentAttributes.to, o, to);
			}
		}
	}

	private void setAttribute(Field field, Object o, Object value) {
		if (field != null) {
			try {
				field.setAccessible(true);
				field.set(o, value);
			} catch (Exception e) {
				logger.error("could not update document attribute of class " + value.getClass().getCanonicalName(), e);
			}
		}
	}

	private DocumentAttributes getDocumentAttributes(Object o) {
		Class<? extends Object> clazz = o.getClass();
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

	private Field getFieldByAnnotationValue(Class<?> clazz, String value) {

		List<Field> fields = getAllDeclaredFields(clazz);
		for (Field field : fields) {

			Annotation[] annotations = field.getAnnotations();
			for (Annotation annotation : annotations) {
				if (value.equals(getAnnotaionValue(annotation))) {
					return field;
				}
			}
		}

		return null;
	}

	private String getAnnotaionValue(Annotation annotation) {
		if (annotation instanceof SerializedName) {
			SerializedName sn = (SerializedName) annotation;
			return sn.value();
		}
		return null;
	}

	private List<Field> getAllDeclaredFields(Class<?> clazz) {
		List<Field> result = new ArrayList<Field>();

		Class<?> current = clazz;

		while (current != null) {
			Field[] fields = current.getDeclaredFields();
			for (Field field : fields) {
				result.add(field);
			}
			current = current.getSuperclass();
		}
		return result;
	}

}
