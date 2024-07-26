package com.arangodb;

import com.arangodb.entity.ArangoDBVersion;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class JwtTest extends BaseJunit5 {

    private final String jwt = getJwt();

    @ParameterizedTest
    @EnumSource(Protocol.class)
    void getVersion(Protocol p) {
        assumeTrue(!p.equals(Protocol.VST) || BaseJunit5.isLessThanVersion(3, 12));
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
