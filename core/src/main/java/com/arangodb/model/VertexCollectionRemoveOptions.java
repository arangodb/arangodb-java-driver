package com.arangodb.model;

public class VertexCollectionRemoveOptions {
    private Boolean dropCollection;

    public Boolean getDropCollection() {
        return dropCollection;
    }

    /**
     * @param dropCollection Drop the collection as well. Collection will only be dropped if it is not used in other
     *                       graphs.
     * @return this
     */
    public VertexCollectionRemoveOptions dropCollection(Boolean dropCollection) {
        this.dropCollection = dropCollection;
        return this;
    }
}
