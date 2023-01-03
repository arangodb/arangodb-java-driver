package arch;


import com.arangodb.shaded.fasterxml.jackson.annotation.JsonProperty;

public record Person(
        @JsonProperty("_key")
        String key,
        @JsonProperty("firstName")
        String name,
        int age
) {
}
