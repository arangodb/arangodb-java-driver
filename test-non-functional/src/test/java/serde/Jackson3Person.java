package serde;

import com.arangodb.serde.annotation.*;
import com.fasterxml.jackson.annotation.JsonProperty;

public record Jackson3Person(
        @Key
        String key,
        @JsonProperty("firstName")
        String name,
        int age
) {
}
