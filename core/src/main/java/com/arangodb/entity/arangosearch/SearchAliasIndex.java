package com.arangodb.entity.arangosearch;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * @author Michele Rastelli
 * @since ArabgoDB 3.10
 */
public final class SearchAliasIndex {
    private final String collection;
    private final String index;
    private final OperationType operation;

    /**
     * @param collection The name of a collection.
     * @param index      The name of an inverted index of the collection.
     */
    public SearchAliasIndex(String collection, String index) {
        this(collection, index, null);
    }

    /**
     * @param collection The name of a collection.
     * @param index      The name of an inverted index of the collection.
     * @param operation  Whether to add or remove the index to the stored indexes property of the View. (default "add")
     */
    @JsonCreator
    public SearchAliasIndex(
            @JsonProperty("collection") String collection,
            @JsonProperty("index") String index,
            @JsonProperty("operation") OperationType operation) {
        this.collection = collection;
        this.index = index;
        this.operation = operation;
    }

    public String getCollection() {
        return collection;
    }

    public String getIndex() {
        return index;
    }

    public OperationType getOperation() {
        return operation;
    }

    public enum OperationType {
        add, del
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof SearchAliasIndex)) return false;
        SearchAliasIndex that = (SearchAliasIndex) o;
        return Objects.equals(collection, that.collection) && Objects.equals(index, that.index) && operation == that.operation;
    }

    @Override
    public int hashCode() {
        return Objects.hash(collection, index, operation);
    }
}
