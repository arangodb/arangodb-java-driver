package containers;


import com.arangodb.ArangoDB;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

enum ClusterContainer implements Supplier<ArangoDB.Builder> {

    INSTANCE;

    private final org.slf4j.Logger log = LoggerFactory.getLogger(ClusterContainer.class);

    private final String DOCKER_IMAGE = "docker.io/arangodb/arangodb:3.5.1";

    private Network network = Network.newNetwork();

    private final GenericContainer agent1 =
            new GenericContainer(DOCKER_IMAGE)
                    .withExposedPorts(8531)
                    .withNetwork(network)
                    .withNetworkAliases("agent1")
                    .withLogConsumer(new Slf4jLogConsumer(log).withPrefix("[AGENT_1]"))
                    .withCommand("arangodb --cluster.start-dbserver false --cluster.start-coordinator false")
                    .waitingFor(Wait.forHttp("/_api/version").forStatusCode(200));

    private final GenericContainer agent2 =
            new GenericContainer(DOCKER_IMAGE)
                    .withExposedPorts(8531)
                    .withNetwork(network)
                    .withNetworkAliases("agent2")
                    .withLogConsumer(new Slf4jLogConsumer(log).withPrefix("[AGENT_2]"))
                    .withCommand("arangodb --cluster.start-dbserver false --cluster.start-coordinator false --starter.join agent1")
                    .waitingFor(Wait.forHttp("/_api/version").forStatusCode(200));

    private final GenericContainer agent3 =
            new GenericContainer(DOCKER_IMAGE)
                    .withExposedPorts(8531)
                    .withNetwork(network)
                    .withNetworkAliases("agent3")
                    .withLogConsumer(new Slf4jLogConsumer(log).withPrefix("[AGENT_3]"))
                    .withCommand("arangodb --cluster.start-dbserver false --cluster.start-coordinator false --starter.join agent1")
                    .waitingFor(Wait.forHttp("/_api/version").forStatusCode(200));

    private final GenericContainer dbserver1 =
            new GenericContainer(DOCKER_IMAGE)
                    .withExposedPorts(8530)
                    .withNetwork(network)
                    .withNetworkAliases("dbserver1")
                    .withLogConsumer(new Slf4jLogConsumer(log).withPrefix("[DBSERVER_1]"))
                    .withCommand("arangodb --cluster.start-dbserver true --cluster.start-coordinator false --starter.join agent1")
                    .waitingFor(Wait.forHttp("/_api/version").forStatusCode(200));

    private final GenericContainer coordinator1 =
            new GenericContainer(DOCKER_IMAGE)
                    .withExposedPorts(8529)
                    .withNetwork(network)
                    .withNetworkAliases("coordinator1")
                    .withLogConsumer(new Slf4jLogConsumer(log).withPrefix("[COORDINATOR_1]"))
                    .withCommand("arangodb --cluster.start-dbserver false --cluster.start-coordinator true --starter.join agent1")
                    .waitingFor(Wait.forHttp("/_api/version").forStatusCode(200));

    {
        // start agents
        Arrays.asList(
                CompletableFuture.runAsync(agent1::start).thenAccept((v) -> log.info("READY: agent1")),
                CompletableFuture.runAsync(agent2::start).thenAccept((v) -> log.info("READY: agent2")),
                CompletableFuture.runAsync(agent3::start).thenAccept((v) -> log.info("READY: agent3"))
        ).forEach(CompletableFuture::join);

        // start dbservers and coordinators
        Arrays.asList(
                CompletableFuture.runAsync(dbserver1::start).thenAccept((v) -> log.info("READY: dbserver1")),
                CompletableFuture.runAsync(coordinator1::start).thenAccept((v) -> log.info("READY: coordinator1"))
        ).forEach(CompletableFuture::join);

    }

    @Override
    public ArangoDB.Builder get() {
        return new ArangoDB.Builder()
                .host(coordinator1.getContainerIpAddress(), coordinator1.getFirstMappedPort());
    }

}
