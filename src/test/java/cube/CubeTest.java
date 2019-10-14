package cube;

import com.arangodb.ArangoDB;
import com.arangodb.Protocol;
import com.arangodb.entity.ArangoDBVersion;
import org.arquillian.cube.HostIp;
import org.arquillian.cube.HostPort;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;


/**
 * @author Michele Rastelli
 */

@Ignore
@RunWith(Arquillian.class)
public class CubeTest {

    @HostIp
    private String host;

    @HostPort(containerName = "arangodb", value = 8529)
    int port;

    @Test
    public void getVersion() {
        ArangoDB arangoDB = new ArangoDB.Builder().host(host, port).useProtocol(Protocol.HTTP_JSON).build();
        ArangoDBVersion version = arangoDB.getVersion();
        assertThat(version.getVersion(), is(notNullValue()));
    }

}