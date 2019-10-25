package containers;

import com.arangodb.ArangoCollection;
import com.arangodb.ArangoDB;
import com.arangodb.ArangoDatabase;
import com.arangodb.entity.BaseDocument;
import com.arangodb.entity.DocumentCreateEntity;
import com.arangodb.model.DocumentCreateOptions;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author Michele Rastelli
 */
public class ChunkSizeTest {

    private static final int CHUNK_SIZE = 16;

    private ArangoDB arangoDB;
    private ArangoDatabase db;
    private ArangoCollection collection;

    @Before
    public void setUp() {
        arangoDB = SingleServerWithChunkSizeContainer.INSTANCE.get()
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

    @Test
    public void insertDocument() {
        BaseDocument doc = new BaseDocument();
        doc.addAttribute("blablabla", "blablabla");
        DocumentCreateEntity<BaseDocument> createdDoc = collection.insertDocument(doc, new DocumentCreateOptions().returnNew(true));
        assertThat(createdDoc.getNew().getProperties().get("blablabla"), is("blablabla"));
    }

}