package containers;

import com.arangodb.ArangoCollection;
import com.arangodb.ArangoDB;
import com.arangodb.ArangoDBException;
import com.arangodb.ArangoDatabase;
import com.arangodb.entity.BaseDocument;
import com.arangodb.model.DocumentReadOptions;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.testcontainers.containers.GenericContainer;

import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * @author Michele Rastelli
 */
@Ignore
public class ActiveFailoverTest {
    private final String DB_NAME = "failoverTest";
    private final String COLLECTION_NAME = "failoverTest";

    @Before
    public void setUp() {
        ActiveFailoverContainer.INSTANCE.get();
    }

    @Test
    @Ignore
    /**
     * FIXME: hosts are merged to the ones coming from arangodb.properties
     */
    public void ActiveFailoverTest() throws InterruptedException {
        List<GenericContainer> servers = ActiveFailoverContainer.INSTANCE.getServers();
        GenericContainer master = servers.get(0);
        GenericContainer follower = servers.get(1);

        ArangoDB arangoDBMaster = new ArangoDB.Builder()
                .host(master.getContainerIpAddress(), master.getFirstMappedPort())
                .build();

        ArangoDB arangoDBFollower = new ArangoDB.Builder()
                .host(follower.getContainerIpAddress(), follower.getFirstMappedPort())
                .build();

        ArangoDatabase db = arangoDBMaster.db(DB_NAME);
        if (!db.exists())
            db.create();

        ArangoCollection collection = db.collection(COLLECTION_NAME);
        if (!collection.exists())
            collection.create();

        BaseDocument doc = new BaseDocument();
        collection.insertDocument(doc);

        BaseDocument retrievedDoc1 = collection.getDocument(doc.getKey(), BaseDocument.class);
        assertThat(retrievedDoc1, is(notNullValue()));

        Thread.sleep(500);

        BaseDocument retrievedDoc2 = arangoDBFollower.db(DB_NAME).collection(COLLECTION_NAME)
                .getDocument(doc.getKey(), BaseDocument.class, new DocumentReadOptions().allowDirtyRead(true));
        assertThat(retrievedDoc2, is(notNullValue()));

        try {
            arangoDBFollower.db(DB_NAME).collection(COLLECTION_NAME).getDocument(doc.getKey(), BaseDocument.class);
            fail();
        } catch (ArangoDBException e) {
        }
    }

}