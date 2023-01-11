package com.arangodb.async;

import com.arangodb.*;
import com.arangodb.config.ConfigUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

class JwtAuthTest {

    private static String jwt;
    private ArangoDBAsync arangoDB;

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

    @AfterEach
    void after() {
        if (arangoDB != null)
            arangoDB.shutdown();
    }

    @Test
    void notAuthenticated() throws InterruptedException {
        arangoDB = getBuilder().build();
        try {
            arangoDB.getVersion().get();
            fail();
        } catch (ExecutionException e) {
            assertThat(e.getCause()).isInstanceOf(ArangoDBException.class);
            assertThat(((ArangoDBException) e.getCause()).getResponseCode()).isEqualTo(401);
        }
        arangoDB.shutdown();
    }

    @Test
    void authenticated() throws ExecutionException, InterruptedException {
        arangoDB = getBuilder()
                .jwt(jwt)
                .build();
        arangoDB.getVersion().get();
        arangoDB.shutdown();
    }

    @Test
    @Disabled("DE-423")
    void updateJwt() throws ExecutionException, InterruptedException {
        arangoDB = getBuilder()
                .jwt(jwt)
                .build();
        arangoDB.getVersion().get();
        arangoDB.updateJwt("bla");
        try {
            arangoDB.getVersion().get();
            fail();
        } catch (ExecutionException e) {
            assertThat(e.getCause()).isInstanceOf(ArangoDBException.class);
            assertThat(((ArangoDBException) e.getCause()).getResponseCode()).isEqualTo(401);
        }

        arangoDB.updateJwt(jwt);
        arangoDB.getVersion().get();
        arangoDB.shutdown();
    }

    private ArangoDBAsync.Builder getBuilder() {
        return new ArangoDBAsync.Builder()
                .loadProperties(ConfigUtils.loadConfig())
                .jwt(null)          // unset credentials from properties file
                .user(null)         // unset credentials from properties file
                .password(null);    // unset credentials from properties file
    }
}
