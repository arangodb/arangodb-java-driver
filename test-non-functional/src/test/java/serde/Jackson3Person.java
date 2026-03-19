package serde;

import com.arangodb.serde.jackson3.Key;
import com.fasterxml.jackson.annotation.JsonProperty;

public record Jackson3Person(
        @Key
        String key,
        @JsonProperty("firstName")
        String name,
        int age
) {
}
