package com.arangodb.entity.arangosearch;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum ConsolidationType {

    @JsonProperty("bytes_accum")
    BYTES_ACCUM,

    @JsonProperty("tier")
    TIER

}