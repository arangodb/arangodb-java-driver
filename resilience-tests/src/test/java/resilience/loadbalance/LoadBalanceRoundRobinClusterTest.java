package resilience.loadbalance;

import com.arangodb.ArangoDB;
import com.arangodb.ArangoDBAsync;
import com.arangodb.ArangoDBException;
import com.arangodb.Protocol;
import com.arangodb.entity.LoadBalancingStrategy;
import eu.rekawek.toxiproxy.model.ToxicDirection;
import eu.rekawek.toxiproxy.model.toxic.Latency;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import resilience.ClusterTest;
import resilience.Endpoint;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

public class LoadBalanceRoundRobinClusterTest extends ClusterTest {

    static Stream<ArangoDB> arangoProvider() {
        return Stream.of(
                dbBuilder().loadBalancingStrategy(LoadBalancingStrategy.ROUND_ROBIN).protocol(Protocol.VST).build(),
                dbBuilder().loadBalancingStrategy(LoadBalancingStrategy.ROUND_ROBIN).protocol(Protocol.HTTP_VPACK).build(),
                dbBuilder().loadBalancingStrategy(LoadBalancingStrategy.ROUND_ROBIN).protocol(Protocol.HTTP2_JSON).build()
        );
    }

    static Stream<ArangoDBAsync> asyncArangoProvider() {
        return arangoProvider().map(ArangoDB::async);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("arangoProvider")
    void loadBalancing(ArangoDB arangoDB) {
        List<Endpoint> endpoints = getEndpoints();
        for (Endpoint endpoint : endpoints) {
            assertThat(serverIdGET(arangoDB)).isEqualTo(endpoint.getServerId());
        }
        assertThat(serverIdGET(arangoDB)).isEqualTo(endpoints.get(0).getServerId());
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("asyncArangoProvider")
    void loadBalancingAsync(ArangoDBAsync arangoDB) {
        List<Endpoint> endpoints = getEndpoints();
        for (Endpoint endpoint : endpoints) {
            assertThat(serverIdGET(arangoDB)).isEqualTo(endpoint.getServerId());
        }
        assertThat(serverIdGET(arangoDB)).isEqualTo(endpoints.get(0).getServerId());
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("arangoProvider")
    void failover(ArangoDB arangoDB) throws IOException {
        List<Endpoint> endpoints = getEndpoints();
        endpoints.get(0).getProxy().disable();
        assertThat(serverIdGET(arangoDB)).isEqualTo(endpoints.get(1).getServerId());
        endpoints.get(0).getProxy().enable();
        assertThat(serverIdGET(arangoDB)).isEqualTo(endpoints.get(2).getServerId());
        assertThat(serverIdGET(arangoDB)).isEqualTo(endpoints.get(0).getServerId());
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("asyncArangoProvider")
    void failoverAsync(ArangoDBAsync arangoDB) throws IOException {
        List<Endpoint> endpoints = getEndpoints();
        endpoints.get(0).getProxy().disable();
        assertThat(serverIdGET(arangoDB)).isEqualTo(endpoints.get(1).getServerId());
        endpoints.get(0).getProxy().enable();
        assertThat(serverIdGET(arangoDB)).isEqualTo(endpoints.get(2).getServerId());
        assertThat(serverIdGET(arangoDB)).isEqualTo(endpoints.get(0).getServerId());
    }

    // FIXME: this fails for VST
    @Disabled
    @ParameterizedTest(name = "{index}")
    @MethodSource("arangoProvider")
    void retryGET(ArangoDB arangoDB) throws IOException, InterruptedException {
        List<Endpoint> endpoints = getEndpoints();

        // slow down the driver connection
        Latency toxic = getEndpoints().get(0).getProxy().toxics().latency("latency", ToxicDirection.DOWNSTREAM, 10_000);
        Thread.sleep(100);

        ScheduledExecutorService es = Executors.newSingleThreadScheduledExecutor();
        es.schedule(() -> getEndpoints().get(0).disable(), 300, TimeUnit.MILLISECONDS);

        assertThat(serverIdGET(arangoDB)).isEqualTo(endpoints.get(1).getServerId());
        assertThat(serverIdGET(arangoDB)).isEqualTo(endpoints.get(2).getServerId());

        toxic.remove();
        enableAllEndpoints();

        assertThat(serverIdGET(arangoDB)).isEqualTo(endpoints.get(0).getServerId());

        es.shutdown();
    }

    // FIXME: this fails for VST
    @Disabled
    @ParameterizedTest(name = "{index}")
    @MethodSource("asyncArangoProvider")
    void retryGETAsync(ArangoDBAsync arangoDB) throws IOException, InterruptedException {
        List<Endpoint> endpoints = getEndpoints();

        // slow down the driver connection
        Latency toxic = getEndpoints().get(0).getProxy().toxics().latency("latency", ToxicDirection.DOWNSTREAM, 10_000);
        Thread.sleep(100);

        ScheduledExecutorService es = Executors.newSingleThreadScheduledExecutor();
        es.schedule(() -> getEndpoints().get(0).disable(), 300, TimeUnit.MILLISECONDS);

        assertThat(serverIdGET(arangoDB)).isEqualTo(endpoints.get(1).getServerId());
        assertThat(serverIdGET(arangoDB)).isEqualTo(endpoints.get(2).getServerId());

        toxic.remove();
        enableAllEndpoints();

        assertThat(serverIdGET(arangoDB)).isEqualTo(endpoints.get(0).getServerId());

        es.shutdown();
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("arangoProvider")
    void retryPOST(ArangoDB arangoDB) throws IOException, InterruptedException {
        List<Endpoint> endpoints = getEndpoints();
        for (Endpoint endpoint : endpoints) {
            System.out.println(endpoint.getServerId());
        }

        // slow down the driver connection
        Latency toxic = getEndpoints().get(0).getProxy().toxics().latency("latency", ToxicDirection.DOWNSTREAM, 10_000);
        Thread.sleep(100);

        ScheduledExecutorService es = Executors.newSingleThreadScheduledExecutor();
        es.schedule(() -> getEndpoints().get(0).disable(), 300, TimeUnit.MILLISECONDS);

        Throwable thrown = catchThrowable(() -> serverIdPOST(arangoDB));
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
        assertThat(thrown.getCause()).isInstanceOf(IOException.class);

        assertThat(serverIdPOST(arangoDB)).isEqualTo(endpoints.get(1).getServerId());
        assertThat(serverIdPOST(arangoDB)).isEqualTo(endpoints.get(2).getServerId());

        toxic.remove();
        enableAllEndpoints();

        assertThat(serverIdPOST(arangoDB)).isEqualTo(endpoints.get(0).getServerId());

        es.shutdown();
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("asyncArangoProvider")
    void retryPOSTAsync(ArangoDBAsync arangoDB) throws IOException, InterruptedException {
        List<Endpoint> endpoints = getEndpoints();
        for (Endpoint endpoint : endpoints) {
            System.out.println(endpoint.getServerId());
        }

        // slow down the driver connection
        Latency toxic = getEndpoints().get(0).getProxy().toxics().latency("latency", ToxicDirection.DOWNSTREAM, 10_000);
        Thread.sleep(100);

        ScheduledExecutorService es = Executors.newSingleThreadScheduledExecutor();
        es.schedule(() -> getEndpoints().get(0).disable(), 300, TimeUnit.MILLISECONDS);

        Throwable thrown = catchThrowable(() -> serverIdPOST(arangoDB));
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
        assertThat(thrown.getCause()).isInstanceOf(IOException.class);

        assertThat(serverIdPOST(arangoDB)).isEqualTo(endpoints.get(1).getServerId());
        assertThat(serverIdPOST(arangoDB)).isEqualTo(endpoints.get(2).getServerId());

        toxic.remove();
        enableAllEndpoints();

        assertThat(serverIdPOST(arangoDB)).isEqualTo(endpoints.get(0).getServerId());

        es.shutdown();
    }

}
