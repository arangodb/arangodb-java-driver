package com.arangodb.entity.arangosearch;

/**
 * @author Michele Rastelli
 * @since ArabgoDB 3.10
 */
public class SearchAliasIndex {
    private final String collection;
    private final String index;

    public SearchAliasIndex(String collection, String index) {
        this.collection = collection;
        this.index = index;
    }

    public String getCollection() {
        return collection;
    }

    public String getIndex() {
        return index;
    }
}
