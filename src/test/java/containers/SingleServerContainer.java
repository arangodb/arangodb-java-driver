package containers;


import com.arangodb.ArangoDB;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.util.function.Supplier;

enum SingleServerContainer implements Supplier<ArangoDB.Builder> {

    INSTANCE;

    private final int PORT = 8529;
    private final String DOCKER_IMAGE = "docker.io/arangodb/arangodb:3.5.1";
    private final String PASSWORD = "test";

    private final GenericContainer container =
            new GenericContainer(DOCKER_IMAGE)
                    .withExposedPorts(PORT)
                    .withEnv("ARANGO_ROOT_PASSWORD", PASSWORD)
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
