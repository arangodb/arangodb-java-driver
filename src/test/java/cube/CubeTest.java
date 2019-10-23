package cube;

import com.arangodb.ArangoDB;
import com.arangodb.entity.ArangoDBVersion;
import org.arquillian.cube.docker.impl.client.containerobject.dsl.Container;
import org.arquillian.cube.docker.impl.client.containerobject.dsl.DockerContainer;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * @author Michele Rastelli
 */

@RunWith(Arquillian.class)
public class CubeTest {

    @DockerContainer
    Container server = CubeUtils.arangodb();

    @Test
    public void getVersion() {
        ArangoDB arangoDB = new ArangoDB.Builder()
                .host(server.getIpAddress(), server.getBindPort(CubeUtils.PORT))
                .build();
        ArangoDBVersion version = arangoDB.getVersion();
        assertThat(version.getVersion(), is(notNullValue()));
    }

}