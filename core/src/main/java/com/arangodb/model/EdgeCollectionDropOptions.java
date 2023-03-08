package com.arangodb.model;

public class EdgeCollectionDropOptions {
    private Boolean waitForSync;
    private Boolean dropCollections;

    public Boolean getWaitForSync() {
        return waitForSync;
    }

    /**
     * @param waitForSync Define if the request should wait until synced to disk.
     * @return this
     */
    public EdgeCollectionDropOptions waitForSync(Boolean waitForSync) {
        this.waitForSync = waitForSync;
        return this;
    }

    public Boolean getDropCollections() {
        return dropCollections;
    }

    /**
     * @param dropCollections Drop the collection as well. Collection will only be dropped if it is not used in other
     *                        graphs.
     * @return this
     */
    public EdgeCollectionDropOptions dropCollections(Boolean dropCollections) {
        this.dropCollections = dropCollections;
        return this;
    }
}
