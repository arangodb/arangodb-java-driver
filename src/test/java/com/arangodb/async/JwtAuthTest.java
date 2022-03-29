package com.arangodb.async;

import com.arangodb.ArangoDB;
import com.arangodb.ArangoDBException;
import com.arangodb.DbName;
import com.arangodb.mapping.ArangoJack;
import com.arangodb.util.ArangoSerialization;
import com.arangodb.velocystream.Request;
import com.arangodb.velocystream.RequestType;
import com.arangodb.velocystream.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
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
        ArangoDB arangoDB = new ArangoDB.Builder().serializer(new ArangoJack()).build();
        jwt = getJwt(arangoDB);
        arangoDB.shutdown();
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
