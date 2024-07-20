package serde;


import com.arangodb.serde.InternalKey;

public record InternalSerdePerson(
        @InternalKey
        String key,
        String name,
        int age
) {
}
