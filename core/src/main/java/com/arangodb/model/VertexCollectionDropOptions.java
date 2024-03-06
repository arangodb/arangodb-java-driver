package com.arangodb.model;

/**
 * @deprecated use {@link VertexCollectionRemoveOptions} instead
 */
@Deprecated
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
