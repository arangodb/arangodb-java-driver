package com.arangodb.entity.arangosearch;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Michele Rastelli
 * @since ArabgoDB 3.10
 */
public class SearchAliasIndex {
    private final String collection;
    private final String index;

    @JsonCreator
    public SearchAliasIndex(@JsonProperty("collection") String collection, @JsonProperty("index") String index) {
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
