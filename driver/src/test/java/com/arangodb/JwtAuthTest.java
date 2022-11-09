package com.arangodb;

import com.arangodb.internal.InternalRequest;
import com.arangodb.internal.InternalResponse;
import com.arangodb.internal.config.FileConfigPropertiesProvider;
import com.arangodb.serde.ArangoSerde;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.api.Assumptions.assumeTrue;


/**
 * @author Michele Rastelli
 */
class JwtAuthTest {

    private volatile static String jwt;

    @BeforeAll
    static void init() {
        ArangoDB arangoDB = new ArangoDB.Builder()
                .loadProperties(new FileConfigPropertiesProvider())
                .build();
        jwt = getJwt(arangoDB);
        arangoDB.shutdown();
    }

    private static String getJwt(ArangoDB arangoDB) {
        ArangoSerde serde = arangoDB.getSerde();
        Map<String, String> reqBody = new HashMap<>();
        reqBody.put("username", "root");
        reqBody.put("password", "test");

        InternalRequest req = new InternalRequest(DbName.SYSTEM, RequestType.POST, "/_open/auth");
        req.setBody(serde.serialize(reqBody));

        InternalResponse resp = arangoDB.execute(req);
        Map<String, String> respBody = serde.deserialize(resp.getBody(), Map.class);
        return respBody.get("jwt");
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
        assumeTrue(protocol != Protocol.VST, "DE-423");
        ArangoDB arangoDB = getBuilder(protocol)
                .jwt(jwt)
                .build();
        arangoDB.getVersion();
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
                .loadProperties(new FileConfigPropertiesProvider())
                .useProtocol(protocol)
                .jwt(null)          // unset credentials from properties file
                .user(null)         // unset credentials from properties file
                .password(null);    // unset credentials from properties file
    }
}
