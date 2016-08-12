package com.arangodb.entity;

import java.util.HashMap;
import java.util.Map;

import com.arangodb.entity.DocumentField.Type;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class BaseDocument {

	@DocumentField(Type.ID)
	private String documentHandle;
	@DocumentField(Type.KEY)
	private String documentKey;
	@DocumentField(Type.REV)
	private String documentRevision;
	private Map<String, Object> properties;

	public BaseDocument() {
		super();
		properties = new HashMap<>();
	}

	public BaseDocument(final Map<String, Object> properties) {
		final Object id = properties.remove(DocumentField.Type.ID.getSerializeName());
		if (id != null) {
			documentHandle = id.toString();
		}
		final Object key = properties.remove(DocumentField.Type.KEY.getSerializeName());
		if (key != null) {
			documentKey = key.toString();
		}
		final Object rev = properties.remove(DocumentField.Type.REV.getSerializeName());
		if (rev != null) {
			documentRevision = rev.toString();
		}
		this.properties = properties;
	}

	public String getDocumentHandle() {
		return documentHandle;
	}

	public void setDocumentHandle(final String documentHandle) {
		this.documentHandle = documentHandle;
	}

	public String getDocumentKey() {
		return documentKey;
	}

	public void setDocumentKey(final String documentKey) {
		this.documentKey = documentKey;
	}

	public String getDocumentRevision() {
		return documentRevision;
	}

	public void setDocumentRevision(final String documentRevision) {
		this.documentRevision = documentRevision;
	}

	public Map<String, Object> getProperties() {
		return properties;
	}

	public void setProperties(final Map<String, Object> properties) {
		this.properties = properties;
	}

	public void addAttribute(final String key, final Object value) {
		properties.put(key, value);
	}

	public void updateAttribute(final String key, final Object value) {
		if (properties.containsKey(key)) {
			properties.put(key, value);
		}
	}

	public Object getAttribute(final String key) {
		return properties.get(key);
	}

	@Override
	public String toString() {
		return "BaseDocument [documentRevision=" + documentRevision + ", documentHandle=" + documentHandle
				+ ", documentKey=" + documentKey + ", properties=" + properties + "]";
	}
}
