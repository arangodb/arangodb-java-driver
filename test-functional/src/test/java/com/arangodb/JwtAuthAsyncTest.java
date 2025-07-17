package com.arangodb;

import com.arangodb.config.ArangoConfigProperties;
import com.arangodb.config.ConfigUtils;
import com.arangodb.internal.ArangoRequestParam;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.api.Assumptions.assumeTrue;


/**
 * @author Michele Rastelli
 */
class JwtAuthAsyncTest {

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
        ArangoConfigProperties conf = ConfigUtils.loadConfig();
        Map<String, String> reqBody = new HashMap<>();
        reqBody.put("username", conf.getUser().orElse("root"));
        reqBody.put("password", conf.getPassword().orElse(null));

        Request<?> req = Request.builder()
                .db(ArangoRequestParam.SYSTEM)
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
        assumeTrue(!protocol.equals(Protocol.VST) || BaseJunit5.isLessThanVersion(3, 12));

        ArangoDBAsync arangoDB = getBuilder(protocol).acquireHostList(false).build().async();
        Throwable thrown = catchThrowable(() -> arangoDB.getVersion().get()).getCause();
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
        ArangoDBException e = (ArangoDBException) thrown;
        assertThat(e.getResponseCode()).isEqualTo(401);
        arangoDB.shutdown();
    }

    @ParameterizedTest
    @EnumSource(Protocol.class)
    void authenticated(Protocol protocol) throws ExecutionException, InterruptedException {
        assumeTrue(!protocol.equals(Protocol.VST) || BaseJunit5.isLessThanVersion(3, 12));

        ArangoDBAsync arangoDB = getBuilder(protocol)
                .jwt(jwt)
                .build()
                .async();
        arangoDB.getVersion().get();
        arangoDB.shutdown();
    }

    @ParameterizedTest
    @EnumSource(Protocol.class)
    void updateJwt(Protocol protocol) throws ExecutionException, InterruptedException {
        assumeTrue(protocol != Protocol.VST, "DE-423");
        ArangoDBAsync arangoDB = getBuilder(protocol)
                .jwt(jwt)
                .build()
                .async();
        arangoDB.getVersion().get();
        arangoDB.updateJwt("bla");

        Throwable thrown = catchThrowable(() -> arangoDB.getVersion().get()).getCause();
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
        ArangoDBException e = (ArangoDBException) thrown;
        assertThat(e.getResponseCode()).isEqualTo(401);

        arangoDB.updateJwt(jwt);
        arangoDB.getVersion().get();
        arangoDB.shutdown();
    }

    private ArangoDB.Builder getBuilder(Protocol protocol) {
        return new ArangoDB.Builder()
                .loadProperties(ConfigUtils.loadConfig())
                .protocol(protocol)
                .jwt(null)          // unset credentials from properties file
                .user(null)         // unset credentials from properties file
                .password(null);    // unset credentials from properties file
    }
}
