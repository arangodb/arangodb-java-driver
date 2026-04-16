package serde;

import com.arangodb.serde.annotation.*;
import com.fasterxml.jackson.annotation.JsonProperty;

public record JacksonPerson(
        @Key
        String key,
        @JsonProperty("firstName")
        String name,
        int age
) {
}
