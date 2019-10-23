package cube;

import com.arangodb.ArangoCollection;
import com.arangodb.ArangoDB;
import com.arangodb.ArangoDatabase;
import com.arangodb.entity.BaseDocument;
import com.arangodb.entity.DocumentCreateEntity;
import com.arangodb.model.DocumentCreateOptions;
import org.arquillian.cube.docker.junit.rule.ContainerDslRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author Michele Rastelli
 */
public class ChunkSizeTest {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(ChunkSizeTest.class);
    private static final int CHUNK_SIZE = 16;

    private ArangoDatabase db;
    private ArangoCollection collection;

    @Rule
    public ContainerDslRule server = CubeUtils.arangodbWithChunkSize(CHUNK_SIZE);

    @Before
    public void setup() {
        ArangoDB arangoDB = new ArangoDB.Builder()
                .host(server.getIpAddress(), server.getBindPort(CubeUtils.PORT))
                .chunksize(CHUNK_SIZE)
                .build();

        db = arangoDB.db("chunkSizeTest");
        collection = db.collection("chunkSizeTest");

        if (!db.exists()) {
            db.create();
        }

        if (!collection.exists()) {
            collection.create();
        }
    }

    @After
    public void cleanup() {
        if (db.exists()) {
            db.drop();
        }
    }

    @Test
    public void insertDocument() {
        BaseDocument doc = new BaseDocument();
        doc.addAttribute("blablabla", "blablabla");
        DocumentCreateEntity<BaseDocument> createdDoc = collection.insertDocument(doc, new DocumentCreateOptions().returnNew(true));
        assertThat(createdDoc.getNew().getProperties().get("blablabla"), is("blablabla"));

        log.info("DB logs: \n{}", server.getLog());
    }

}