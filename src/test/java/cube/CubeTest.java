package cube;

import com.arangodb.ArangoDB;
import com.arangodb.entity.ArangoDBVersion;
import org.arquillian.cube.docker.junit.rule.ContainerDslRule;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * @author Michele Rastelli
 */

@Ignore
public class CubeTest {

    @Rule
    public ContainerDslRule server = CubeUtils.arangodb();

    @Test
    public void getVersion() {
        ArangoDB arangoDB = new ArangoDB.Builder()
                .host(server.getIpAddress(), server.getBindPort(CubeUtils.PORT))
                .build();
        ArangoDBVersion version = arangoDB.getVersion();
        assertThat(version.getVersion(), is(notNullValue()));
    }

}