package com.arangodb;

import com.arangodb.mapping.ArangoJack;
import org.junit.jupiter.params.provider.Arguments;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class BaseJunit5 {
    protected static final DbName TEST_DB = DbName.of("java_driver_test_db");

    protected static final List<ArangoDB> arangos = Arrays.stream(Protocol.values()).map(p ->
            new ArangoDB.Builder()
                    .useProtocol(p)
                    .serializer(new ArangoJack())
                    .build()
    ).collect(Collectors.toList());

    protected static Stream<Arguments> arangos() {
        return arangos.stream().map(Arguments::of);
    }
}
