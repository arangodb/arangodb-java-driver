package com.arangodb;

import com.arangodb.mapping.ArangoJack;
import com.arangodb.util.ArangoSerialization;
import com.arangodb.velocystream.Request;
import com.arangodb.velocystream.RequestType;
import com.arangodb.velocystream.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;


/**
 * @author Michele Rastelli
 */
class JwtAuthTest {

    private volatile static String jwt;

    @BeforeAll
    static void init() {
        ArangoDB arangoDB = new ArangoDB.Builder().serializer(new ArangoJack()).build();
        jwt = getJwt(arangoDB);
        arangoDB.shutdown();
    }

    @ParameterizedTest
    @EnumSource(Protocol.class)
    void notAuthenticated(Protocol protocol) {
        ArangoDB arangoDB = getBuilder(protocol).build();
        Throwable thrown = catchThrowable(arangoDB::getVersion);
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
        ArangoDBException e = (ArangoDBException) thrown;
        assertThat(e.getResponseCode()).isEqualTo(401);
        arangoDB.shutdown();
    }

    @ParameterizedTest
    @EnumSource(Protocol.class)
    void authenticated(Protocol protocol) {
        ArangoDB arangoDB = getBuilder(protocol)
                .jwt(jwt)
                .build();
        arangoDB.getVersion();
        arangoDB.shutdown();
    }

    @ParameterizedTest
    @EnumSource(Protocol.class)
    void updateJwt(Protocol protocol) {
        ArangoDB arangoDB = getBuilder(protocol)
                .jwt(jwt)
                .build();
        arangoDB.getVersion();
        if (protocol == Protocol.VST) {
            arangoDB.shutdown();
        }
        arangoDB.updateJwt("bla");

        Throwable thrown = catchThrowable(arangoDB::getVersion);
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
        ArangoDBException e = (ArangoDBException) thrown;
        assertThat(e.getResponseCode()).isEqualTo(401);

        arangoDB.updateJwt(jwt);
        arangoDB.getVersion();
        arangoDB.shutdown();
    }

    private ArangoDB.Builder getBuilder(Protocol protocol) {
        return new ArangoDB.Builder()
                .useProtocol(protocol)
                .serializer(new ArangoJack())
                .jwt(null)          // unset credentials from properties file
                .user(null)         // unset credentials from properties file
                .password(null);    // unset credentials from properties file
    }

    private static String getJwt(ArangoDB arangoDB) {
        ArangoSerialization serde = arangoDB.util();
        Map<String, String> reqBody = new HashMap<>();
        reqBody.put("username", "root");
        reqBody.put("password", "test");

        Request req = new Request(DbName.SYSTEM, RequestType.POST, "/_open/auth");
        req.setBody(serde.serialize(reqBody));

        Response resp = arangoDB.execute(req);
        Map<String, String> respBody = serde.deserialize(resp.getBody(), Map.class);
        return respBody.get("jwt");
    }
}
