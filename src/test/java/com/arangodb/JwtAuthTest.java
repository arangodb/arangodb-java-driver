package com.arangodb;

import com.arangodb.util.ArangoSerialization;
import com.arangodb.velocystream.Request;
import com.arangodb.velocystream.RequestType;
import com.arangodb.velocystream.Response;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

/**
 * @author Michele Rastelli
 */
@RunWith(Parameterized.class)
public class JwtAuthTest {

    private static String jwt;
    private final Protocol protocol;
    private ArangoDB arangoDB;

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

    @Parameterized.Parameters
    public static List<Protocol> builders() {
        return Arrays.asList(
                Protocol.VST,
                Protocol.HTTP_JSON,
                Protocol.HTTP_VPACK
        );
    }

    public JwtAuthTest(Protocol protocol) {
        this.protocol = protocol;
    }

    @Test
    public void notAuthenticated() {
        arangoDB = getBuilder().build();
        try {
            arangoDB.getVersion();
            fail();
        } catch (ArangoDBException e) {
            assertThat(e.getResponseCode(), is(401));
        }
        arangoDB.shutdown();
    }

    @Test
    public void authenticated() {
        arangoDB = getBuilder()
                .jwt(jwt)
                .build();
        arangoDB.getVersion();
        arangoDB.shutdown();
    }

    @Test
    public void updateJwt() {
        arangoDB = getBuilder()
                .jwt(jwt)
                .build();
        arangoDB.getVersion();
        if (protocol == Protocol.VST) {
            arangoDB.shutdown();
        }
        arangoDB.updateJwt("bla");
        try {
            arangoDB.getVersion();
            fail();
        } catch (ArangoDBException e) {
            assertThat(e.getResponseCode(), is(401));
        }

        arangoDB.updateJwt(jwt);
        arangoDB.getVersion();
        arangoDB.shutdown();
    }

    private ArangoDB.Builder getBuilder() {
        return new ArangoDB.Builder()
                .useProtocol(protocol)
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
