package com.arangodb.entity.arangosearch;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum ConsolidationType {

    /**
     * @deprecated The “bytes_accum” policy type is deprecated and remains in ArangoSearch for backwards compatibility
     * with the older versions. Please make sure to always use the “tier” policy instead.
     */
    @Deprecated
    @JsonProperty("bytes_accum")
    BYTES_ACCUM,

    @JsonProperty("tier")
    TIER

}