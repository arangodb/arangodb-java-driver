package resilience.connection;

import com.arangodb.ArangoDB;
import com.arangodb.Protocol;
import com.arangodb.entity.LoadBalancingStrategy;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import resilience.ClusterTest;
import resilience.Endpoint;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class AcquireHostListTest extends ClusterTest {

    @ParameterizedTest(name = "{index}")
    @MethodSource("protocolProvider")
    void acquireHostList(Protocol protocol) {
        ArangoDB adb = new ArangoDB.Builder()
                .host("172.28.0.1", 8529)
                .password("test")
                .acquireHostList(true)
                .protocol(protocol)
                .loadBalancingStrategy(LoadBalancingStrategy.ROUND_ROBIN)
                .build();

        Set<String> serverIds = getEndpoints().stream()
                .map(Endpoint::getServerId)
                .collect(Collectors.toSet());
        Set<String> retrievedIds = new HashSet<>();

        for (int i = 0; i < serverIds.size(); i++) {
            retrievedIds.add(serverIdGET(adb));
        }

        assertThat(retrievedIds).containsExactlyInAnyOrderElementsOf(serverIds);
    }

    @ParameterizedTest(name = "{index}")
    @EnumSource(LoadBalancingStrategy.class)
    void acquireHostListWithLoadBalancingStrategy(LoadBalancingStrategy lb) {
        ArangoDB adb = new ArangoDB.Builder()
                .host("172.28.0.1", 8529)
                .password("test")
                .acquireHostList(true)
                .loadBalancingStrategy(lb)
                .build();

        adb.getVersion();
        adb.getVersion();
        adb.getVersion();
    }

}
