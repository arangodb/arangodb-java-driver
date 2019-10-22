package cube;

import com.arangodb.ArangoDB;
import com.arangodb.entity.ArangoDBVersion;
import org.arquillian.cube.containerobject.ConnectionMode;
import org.arquillian.cube.docker.impl.client.containerobject.dsl.Container;
import org.arquillian.cube.docker.impl.client.containerobject.dsl.DockerContainer;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

import static cube.CubeUtils.arangoAwaitStrategy;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * @author Michele Rastelli
 */

@RunWith(Arquillian.class)
public class CubeTest {

    @DockerContainer
    Container server = Container.withContainerName("arangodb")
            .fromImage("docker.io/arangodb/arangodb:3.5.1")
            .withPortBinding(8529)
            .withAwaitStrategy(arangoAwaitStrategy())
            .withEnvironment("ARANGO_ROOT_PASSWORD", "test")
            .withConnectionMode(ConnectionMode.START_AND_STOP_AROUND_CLASS)
            .build();

    @Test
    public void getVersion() {
        ArangoDB arangoDB = new ArangoDB.Builder()
                .host(server.getIpAddress(), server.getBindPort(8529))
                .build();
        ArangoDBVersion version = arangoDB.getVersion();
        assertThat(version.getVersion(), is(notNullValue()));
    }

}