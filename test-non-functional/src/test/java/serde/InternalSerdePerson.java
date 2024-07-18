package serde;


import com.arangodb.serde.InternalKey;
import com.fasterxml.jackson.annotation.JsonProperty;

public record InternalSerdePerson(
        @InternalKey
        String key,
        @JsonProperty("firstName")
        String name,
        int age
) {
}
