package com.arangodb;

import org.junit.jupiter.params.provider.Arguments;

import java.util.Arrays;
import java.util.stream.Stream;

public class BaseTest {
    protected static final DbName TEST_DB = DbName.of("java_driver_integration_tests");
    protected static final String HOST = "172.28.0.1";
    protected static final int PORT = 8529;
    protected static final String PASSWD = "test";

    protected static ArangoDB createAdb() {
        return new ArangoDB.Builder()
                .host(HOST, PORT)
                .password(PASSWD)
                .build();
    }

    protected static ArangoDB createAdb(ContentType contentType) {
        Protocol protocol = contentType == ContentType.VPACK ? Protocol.HTTP2_VPACK : Protocol.HTTP2_JSON;
        return new ArangoDB.Builder()
                .host(HOST, PORT)
                .password(PASSWD)
                .useProtocol(protocol)
                .build();
    }

    protected static ArangoDB createAdb(Protocol protocol) {
        return new ArangoDB.Builder()
                .host(HOST, PORT)
                .password(PASSWD)
                .useProtocol(protocol)
                .build();
    }

    protected static Stream<Arguments> adbByProtocol() {
        return Arrays.stream(Protocol.values())
                .map(BaseTest::createAdb)
                .map(Arguments::of);
    }

    protected static Stream<Arguments> adbByContentType() {
        return Arrays.stream(ContentType.values())
                .map(BaseTest::createAdb)
                .map(Arguments::of);
    }

}
