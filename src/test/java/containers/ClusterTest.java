package containers;

import com.arangodb.ArangoDB;
import com.arangodb.entity.ArangoDBVersion;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.testcontainers.containers.GenericContainer;

import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * @author Michele Rastelli
 */

@Ignore
public class ClusterTest {

    private ArangoDB arangoDB;

    @Before
    public void setUp() {
        arangoDB = ClusterContainer.INSTANCE.get().build();
    }

    @Test
    public void getVersion() {
        ArangoDBVersion version = arangoDB.getVersion();
        assertThat(version.getVersion(), is(notNullValue()));
    }

    @Test
    @Ignore
    /**
     * FIXME: hosts are merged to the ones coming from arangodb.properties
     */
    public void acquireHostListWithFailOver() throws InterruptedException {
        List<GenericContainer> coordinators = ClusterContainer.INSTANCE.getCoordinators();
        GenericContainer coord = coordinators.get(0);

        ArangoDB arangoDB = new ArangoDB.Builder()
                .host(coord.getContainerIpAddress(), coord.getFirstMappedPort())
                .acquireHostList(true)
                .acquireHostListInterval(1)
                .timeout(1000)
                .build();

        System.out.println(arangoDB.getVersion().getVersion());
        coord.stop();
        Thread.sleep(2000);
        System.out.println(arangoDB.getVersion().getVersion());
    }

}