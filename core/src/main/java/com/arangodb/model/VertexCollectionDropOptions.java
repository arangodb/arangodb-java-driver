package com.arangodb.model;

/**
 * @see <a href="https://www.arangodb.com/docs/stable/http/gharial-management.html#remove-vertex-collection">API
 * Documentation</a>
 */
public class VertexCollectionDropOptions {
    private Boolean dropCollection;

    public Boolean getDropCollection() {
        return dropCollection;
    }

    /**
     * @param dropCollection Drop the collection as well. Collection will only be dropped if it is not used in other
     *                       graphs.
     * @return this
     */
    public VertexCollectionDropOptions dropCollection(Boolean dropCollection) {
        this.dropCollection = dropCollection;
        return this;
    }
}
