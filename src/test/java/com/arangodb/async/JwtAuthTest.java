package com.arangodb.async;

import com.arangodb.ArangoDB;
import com.arangodb.ArangoDBException;
import com.arangodb.util.ArangoSerialization;
import com.arangodb.velocystream.Request;
import com.arangodb.velocystream.RequestType;
import com.arangodb.velocystream.Response;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

public class JwtAuthTest {

    private static String jwt;
    private ArangoDBAsync arangoDB;

    @BeforeClass
    public static void init() {
        ArangoDB arangoDB = new ArangoDB.Builder().build();
        jwt = getJwt(arangoDB);
        arangoDB.shutdown();
    }

    @After
    public void after() {
        if (arangoDB != null)
            arangoDB.shutdown();
    }

    @Test
    public void notAuthenticated() throws InterruptedException {
        arangoDB = getBuilder().build();
        try {
            arangoDB.getVersion().get();
            fail();
        } catch (ExecutionException e) {
            assertThat(e.getCause(), is(instanceOf(ArangoDBException.class)));
            assertThat(((ArangoDBException) e.getCause()).getResponseCode(), is(401));
        }
        arangoDB.shutdown();
    }

    @Test
    public void authenticated() throws ExecutionException, InterruptedException {
        arangoDB = getBuilder()
                .jwt(jwt)
                .build();
        arangoDB.getVersion().get();
        arangoDB.shutdown();
    }

    @Test
    public void updateJwt() throws ExecutionException, InterruptedException {
        arangoDB = getBuilder()
                .jwt(jwt)
                .build();
        arangoDB.getVersion().get();
        arangoDB.updateJwt("bla");
        try {
            arangoDB.getVersion().get();
            fail();
        } catch (ExecutionException e) {
            assertThat(e.getCause(), is(instanceOf(ArangoDBException.class)));
            assertThat(((ArangoDBException) e.getCause()).getResponseCode(), is(401));
        }

        arangoDB.updateJwt(jwt);
        arangoDB.getVersion().get();
        arangoDB.shutdown();
    }

    private ArangoDBAsync.Builder getBuilder() {
        return new ArangoDBAsync.Builder()
                .jwt(null)          // unset credentials from properties file
                .user(null)         // unset credentials from properties file
                .password(null);    // unset credentials from properties file
    }

    private static String getJwt(ArangoDB arangoDB) {
        ArangoSerialization serde = arangoDB.util();
        Map<String, String> reqBody = new HashMap<>();
        reqBody.put("username", "root");
        reqBody.put("password", "test");

        Request req = new Request("_system", RequestType.POST, "/_open/auth");
        req.setBody(serde.serialize(reqBody));

        Response resp = arangoDB.execute(req);
        Map<String, String> respBody = serde.deserialize(resp.getBody(), Map.class);
        return respBody.get("jwt");
    }
}
