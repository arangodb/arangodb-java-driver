package com.arangodb;

import com.arangodb.entity.ArangoDBVersion;
import com.arangodb.util.ProtocolSource;
import org.junit.jupiter.params.ParameterizedTest;

import static org.assertj.core.api.Assertions.assertThat;

public class JwtTest extends BaseJunit5 {

    private final String jwt = getJwt();

    @ParameterizedTest
    @ProtocolSource
    void getVersion(Protocol p) {
        ArangoDB.Builder builder = new ArangoDB.Builder()
                .protocol(p)
                .jwt(jwt);
        config.getHosts().ifPresent(it ->
                it.forEach(h ->
                        builder.host(h.getHost(), h.getPort())));
        ArangoDB adb = builder.build();

        ArangoDBVersion version = adb.getVersion();
        assertThat(version).isNotNull();
        adb.shutdown();
    }

}
