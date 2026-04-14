package serde;


import com.arangodb.serde.InternalKey;
import com.arangodb.serde.annotation.Key;

public record InternalSerdePerson(
        @Key
        String key,
        String name,
        int age
) {
}
