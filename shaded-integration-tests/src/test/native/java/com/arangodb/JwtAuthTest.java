package com.arangodb;

import com.arangodb.config.ConfigUtils;
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
                .loadProperties(ConfigUtils.loadConfig())
                .build();
        jwt = getJwt(arangoDB);
        arangoDB.shutdown();
    }

    private static String getJwt(ArangoDB arangoDB) {
        Map<String, String> reqBody = new HashMap<>();
        reqBody.put("username", "root");
        reqBody.put("password", "test");

        Request<?> req = Request.builder()
                .db(DbName.SYSTEM)
                .method(Request.Method.POST)
                .path("/_open/auth")
                .body(reqBody)
                .build();

        Response<Map> resp = arangoDB.execute(req, Map.class);
        return (String) resp.getBody().get("jwt");
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
                .loadProperties(ConfigUtils.loadConfig())
                .useProtocol(protocol)
                .jwt(null)          // unset credentials from properties file
                .user(null)         // unset credentials from properties file
                .password(null);    // unset credentials from properties file
    }
}
