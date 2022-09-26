package com.arangodb.entity.arangosearch;

/**
 * @author Michele Rastelli
 * @since ArabgoDB 3.10
 */
public class SearchAliasIndex {
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
    public SearchAliasIndex(String collection, String index, OperationType operation) {
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
}
