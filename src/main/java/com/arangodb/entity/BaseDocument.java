package com.arangodb.entity;

import com.google.gson.annotations.SerializedName;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by gschwab on 1/14/15.
 */
public class BaseDocument extends BaseEntity implements DocumentHolder {

    public static final String REV = "_rev";

    public static final String KEY = "_key";

    /**
     * the documents revision number
     */
    @SerializedName("_rev")
    long documentRevision;

    /**
     * the document handle
     */
    @SerializedName("_id")
    String documentHandle;

    /**
     * the document key
     */
    @SerializedName("_key")
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
     * @param documentKey the unique key of the document
     */
    public BaseDocument(String documentKey) {
        this.init();
        this.documentKey = documentKey;
    }

//    /**
//     * @param keyValues a set of key/value pairs containing the attributes for the document.
//     *                  The length has to be even and each even entry has to be of type String.
//     *                  If not an empty document will be created
//     */
//    public BaseDocument(Object ...keyValues) {
//        this(null, keyValues);
//    }
//
//    /**
//     * create a BaseDocument with a given key and attributes defined in keyValues
//     *
//     * @param documentKey the unique key of the document
//     * @param keyValues a set of key/value pairs containing the attributes for the document.
//     *                  The length has to be even and each even entry has to be of type String.
//     *                  If not an empty document will be created
//     */
//    public BaseDocument(String documentKey, Object ...keyValues) {
//        this.init();
//        if (documentKey != null) {
//            this.documentKey = documentKey;
//        }
//        if (checkKeyValues(keyValues)) {
//            for (int i = 0; i < keyValues.length; i = i+2) {
//                if (keyValues[i] == REV) {
//                    this.documentRevision = (Long) keyValues[i+1];
//                } else if (keyValues[i] == KEY && documentKey == null) {
//                    this.documentKey = (String) keyValues[i+1];
//                } else {
//                    this.addAttribute((String) keyValues[i], keyValues[i + 1]);
//                }
//            }
//        }
//    }

    /**
     * create an BaseDocument with given attributes
     *
     * @param properties the attributes (key/value) of the document to be created
     */
    public BaseDocument(Map<String, Object> properties) {
        this(null, properties);
    }

    /**
     * create an BaseDocument with given key and attributes
     *
     * @param documentKey the unique key of the document
     * @param properties the attributes (key/value) of the document to be created
     */
    public BaseDocument(String documentKey, Map<String, Object> properties) {
        this.init();
        if (documentKey != null) {
            this.documentKey = documentKey;
        }
        if (properties.containsKey(REV)) {
            this.documentRevision = (Long) properties.get(REV);
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

    private void init () {
        //this.properties = new HashMap<String, Object>();
    }

    @Override
    public long getDocumentRevision() {
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
    public void setDocumentRevision(long documentRevision) {
        this.documentRevision = documentRevision;
    }

    @Override
    public void setDocumentHandle(String documentHandle) {
        this.documentHandle = documentHandle;
    }

    @Override
    public void setDocumentKey(String documentKey) {
        this.documentKey = documentKey;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }


    /**
     * add an attribute to the document. If the key already exists, the value of the attribute will be replaced,
     *
     * @param key the key of the attribute
     * @param value the value of the attribute
     */
    public void addAttribute(String key, Object value) {
        this.properties.put(key, value);
    }

    /**
     * update the value of the attribute with the given key
     *
     * @param key the key of the attribute
     * @param value the value of the attribute ti replace the old value
     */
    public void updateAttribute (String key, Object value) {
        this.properties.replace(key, value);
    }

//    /**
//     * check the list if it is suitable
//     *
//     * @param keyValues
//     * @return true, if the list has an even number and is an alternating sequence of instances of String and Object.
//     */
//    private boolean checkKeyValues(Object... keyValues) {
//        if (keyValues.length %2 != 0) {
//            return false;
//        }
//        for (int i = 0; i < keyValues.length; i = i+2) {
//            if (! (keyValues[i] instanceof String)) {
//                return false;
//            }
//        }
//        return true;
//    }
}
