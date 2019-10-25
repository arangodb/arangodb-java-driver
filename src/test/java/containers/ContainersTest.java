package containers;

import com.arangodb.ArangoDB;
import com.arangodb.entity.ArangoDBVersion;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * @author Michele Rastelli
 */

@Ignore
public class ContainersTest {

    private ArangoDB arangoDB;

    @Before
    public void setUp() {
        arangoDB = SingleServerContainer.INSTANCE.get().build();
    }

    @Test
    public void getVersion() {
        ArangoDBVersion version = arangoDB.getVersion();
        assertThat(version.getVersion(), is(notNullValue()));
    }

}