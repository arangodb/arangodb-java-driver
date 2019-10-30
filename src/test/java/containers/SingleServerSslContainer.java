package containers;


import com.arangodb.ArangoDB;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.nio.file.Paths;
import java.util.function.Supplier;

enum SingleServerSslContainer implements Supplier<ArangoDB.Builder> {

    INSTANCE;

    private final int PORT = 8529;
    private final String DOCKER_IMAGE = "docker.io/arangodb/arangodb:3.5.1";
    private final String PASSWORD = "test";
    private String SSL_CERT_PATH = Paths.get("docker/server.pem").toAbsolutePath().toString();

    public final GenericContainer container =
            new GenericContainer(DOCKER_IMAGE)
                    .withExposedPorts(PORT)
                    .withEnv("ARANGO_ROOT_PASSWORD", PASSWORD)
                    .withFileSystemBind(SSL_CERT_PATH, "/server.pem", BindMode.READ_ONLY)
                    .withCommand("arangod --ssl.keyfile /server.pem --server.endpoint ssl://0.0.0.0:8529")
                    .waitingFor(Wait.forLogMessage(".*ready for business.*", 1));

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
