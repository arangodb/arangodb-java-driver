package com.arangodb.serde;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Person(
        @Key
        String key,
        @JsonProperty("firstName")
        String name,
        int age
) {
}
