package serde;

import com.arangodb.serde.jackson.Key;
import com.fasterxml.jackson.annotation.JsonProperty;

public record JacksonPerson(
        @Key
        String key,
        @JsonProperty("firstName")
        String name,
        int age
) {
}
