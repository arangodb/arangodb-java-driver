package containers;


import com.arangodb.ArangoDB;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.util.function.Supplier;

enum SingleServerWithChunkSizeContainer implements Supplier<ArangoDB.Builder> {

    INSTANCE;

    private final org.slf4j.Logger log = LoggerFactory.getLogger(SingleServerWithChunkSizeContainer.class);

    private final int PORT = 8529;
    private final String DOCKER_IMAGE = "docker.io/arangodb/arangodb:3.5.1";
    private final String PASSWORD = "test";
    private final int CHUNK_SIZE = 16;

    private final GenericContainer container =
            new GenericContainer(DOCKER_IMAGE)
                    .withExposedPorts(PORT)
                    .withEnv("ARANGO_ROOT_PASSWORD", PASSWORD)
                    .withCommand("arangod --log.level communication=trace --log.level requests=trace --log.foreground-tty --vst.maxsize " + CHUNK_SIZE)
                    .withLogConsumer(new Slf4jLogConsumer(log).withPrefix("[DB_LOG]"))
                    .waitingFor(Wait.forHttp("/_api/version")
                            .withBasicCredentials("root", "test")
                            .forStatusCode(200));

    {
        container.start();
    }

    @Override
    public ArangoDB.Builder get() {
        return new ArangoDB.Builder()
                .host(container.getContainerIpAddress(), container.getFirstMappedPort())
                .password(PASSWORD);
    }

}
