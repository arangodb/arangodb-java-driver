package arch;


import com.arangodb.serde.InternalKey;
import com.arangodb.shaded.fasterxml.jackson.annotation.JsonProperty;

public record Person(
        @InternalKey
        String key,
        @JsonProperty("firstName")
        String name,
        int age
) {
}
