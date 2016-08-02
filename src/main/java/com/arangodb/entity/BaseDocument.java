package com.arangodb.entity;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.annotations.SerializedName;

/**
 * Created by gschwab on 1/14/15.
 */
public class BaseDocument extends BaseEntity implements DocumentHolder {

	public static final String REV = "_rev";

	public static final String KEY = "_key";

	public static final String ID = "_id";

	public static final String FROM = "_from";

	public static final String TO = "_to";

	/**
	 * the documents revision number
	 */
	@SerializedName(REV)
	String documentRevision;

	/**
	 * the document handle
	 */
	@SerializedName(ID)
	String documentHandle;

	/**
	 * the document key
	 */
	@SerializedName(KEY)
	String documentKey;

	/**
	 * the map containing the key value pairs
	 */
	Map<String, Object> properties = new HashMap<String, Object>();

	/**
	 * create an empty BaseDocument
	 */
	public BaseDocument() {
		this.init();
	}

	/**
	 * create an empty BaseDocument with a given key
	 *
	 * @param documentKey
	 *            the unique key of the document
	 */
	public BaseDocument(final String documentKey) {
		this.init();
		this.documentKey = documentKey;
	}

	/**
	 * create an BaseDocument with given attributes
	 *
	 * @param properties
	 *            the attributes (key/value) of the document to be created
	 */
	public BaseDocument(final Map<String, Object> properties) {
		this(null, properties);
	}

	/**
	 * create an BaseDocument with given key and attributes
	 *
	 * @param documentKey
	 *            the unique key of the document
	 * @param properties
	 *            the attributes (key/value) of the document to be created
	 */
	public BaseDocument(final String documentKey, final Map<String, Object> properties) {
		this.init();
		if (documentKey != null) {
			this.documentKey = documentKey;
		}
		if (properties.containsKey(REV)) {
			this.documentRevision = (String) properties.get(REV);
			properties.remove(REV);
		}
		if (properties.containsKey(KEY)) {
			if (documentKey == null) {
				this.documentKey = (String) properties.get(KEY);
			}
			properties.remove(KEY);
		}
		this.properties = properties;
	}

	private void init() {
		// this.properties = new HashMap<String, Object>();
	}

	@Override
	public String getDocumentRevision() {
		return this.documentRevision;
	}

	@Override
	public String getDocumentHandle() {
		return this.documentHandle;
	}

	@Override
	public String getDocumentKey() {
		return this.documentKey;
	}

	@Override
	public void setDocumentRevision(final String documentRevision) {
		this.documentRevision = documentRevision;
	}

	@Override
	public void setDocumentHandle(final String documentHandle) {
		this.documentHandle = documentHandle;
	}

	@Override
	public void setDocumentKey(final String documentKey) {
		this.documentKey = documentKey;
	}

	public Map<String, Object> getProperties() {
		return properties;
	}

	public void setProperties(final Map<String, Object> properties) {
		this.properties = properties;
	}

	/**
	 * add an attribute to the document. If the key already exists, the value of
	 * the attribute will be replaced,
	 *
	 * @param key
	 *            the key of the attribute
	 * @param value
	 *            the value of the attribute
	 */
	public void addAttribute(final String key, final Object value) {
		this.properties.put(key, value);
	}

	/**
	 * update the value of the attribute with the given key
	 *
	 * @param key
	 *            the key of the attribute
	 * @param value
	 *            the value of the attribute ti replace the old value
	 */
	public void updateAttribute(final String key, final Object value) {
		if (this.properties.containsKey(key)) {
			this.properties.put(key, value);
		}
	}

	/**
	 * get a single attribute of the document
	 *
	 * @param key
	 *            the key of the attribute
	 * @return value of the attribute key
	 */
	public Object getAttribute(final String key) {
		return this.properties.get(key);
	}

	@Override
	public String toString() {
		return "BaseDocument [documentRevision=" + documentRevision + ", documentHandle=" + documentHandle
				+ ", documentKey=" + documentKey + ", properties=" + properties + "]";
	}

}
