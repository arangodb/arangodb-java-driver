package com.arangodb;

import com.arangodb.entity.*;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 * Created by gschwab on 1/14/15.
 */
public class BaseDocumentTest extends BaseGraphTest {

    String graphName = "UnitTestGraph";
    String edgeCollectionName = "edge";

    public BaseDocumentTest(ArangoConfigure configure, ArangoDriver driver) {
        super(configure, driver);
    }
    class Blub {
        public String getBlubStr() {
            return blubStr;
        }

        public void setBlubStr(String blubStr) {
            this.blubStr = blubStr;
        }

        public Object getBlubObj() {
            return blubObj;
        }

        public void setBlubObj(Object blubObj) {
            this.blubObj = blubObj;
        }

        private String blubStr;
        private Object blubObj;

        public Blub (String blubStr, Object blubObj) {
            this.blubStr = blubStr;
            this.blubObj = blubObj;
        }
    }

    @Test
    public void constructor () {
        String myKey = "myKey";
        Map<String, Object> myEmptyMap = new HashMap<String, Object>();

        BaseDocument doc = new BaseDocument();
        assertThat(doc, instanceOf(BaseDocument.class));
        assertEquals(doc.getProperties(), myEmptyMap);

        doc = new BaseDocument(myKey);
        assertThat(doc, instanceOf(BaseDocument.class));
        assertEquals(doc.getProperties(), myEmptyMap);
        assertThat(doc.getDocumentKey(), is(myKey));

        String key1 = "key1";
        String key2 = "key2";
        String key3 = "key3";
        String val1 = "val1";
        int val2 = 2;

        Blub val3 = new Blub("pappnase", 4711);

        Map<String, Object> myMap = new HashMap<String, Object>();
        myMap.put(key1, val1);
        myMap.put(key2, val2);
        myMap.put(key3, val3);

        doc = new BaseDocument(myMap);
        assertThat(doc, instanceOf(BaseDocument.class));
        assertThat(doc.getProperties().size(), is(3));
        assertThat(doc.getProperties().get(key1), instanceOf(String.class));
        assertThat((String) doc.getProperties().get(key1), is(val1));
        assertThat(doc.getProperties().get(key2), instanceOf(Integer.class));
        assertThat((Integer) doc.getProperties().get(key2), is(val2));
        assertThat(doc.getProperties().get(key3), instanceOf(Blub.class));
        assertThat((Blub) doc.getProperties().get(key3), is(val3));

        doc = new BaseDocument(myKey, myMap);
        assertThat(doc, instanceOf(BaseDocument.class));
        assertThat(doc.getDocumentKey(), is(myKey));
        assertThat(doc.getProperties().size(), is(3));
        assertThat(doc.getProperties().get(key1), instanceOf(String.class));
        assertThat((String) doc.getProperties().get(key1), is(val1));
        assertThat(doc.getProperties().get(key2), instanceOf(Integer.class));
        assertThat((Integer) doc.getProperties().get(key2), is(val2));
        assertThat(doc.getProperties().get(key3), instanceOf(Blub.class));
        assertThat((Blub) doc.getProperties().get(key3), is(val3));

    }

    @Test
    public void save_document() throws ArangoException {
        String key1 = "key1";
        String key2 = "key2";
        String key3 = "key3";
        String key4 = "key4";
        String key5 = "key5";
        String key6 = "key6";
        String key7 = "key7";

        String val1 = "val1";
        double val2 = -20;
        Blub val3 = new Blub("pappnase", 4711);
        Object[] val4 = new Object[4];
        val4[0] = 1;
        val4[1] = 2;
        val4[2] = "Hallo";
        val4[3] = new Blub("Hallo", 42);
        Boolean val5 = true;
        Object val6 = null;
        Object val7 = 0;

        Map<String, Object> myMap = new HashMap<String, Object>();
        myMap.put(key1, val1);
        myMap.put(key2, val2);
        myMap.put(key3, val3);
        myMap.put(key4, val4);
        myMap.put(key5, val5);
        myMap.put(key6, val6);
        myMap.put(key7, val7);


        BaseDocument baseDocument = new BaseDocument(myMap);

        driver.createCollection("myCollection");
        driver.createDocument("myCollection", baseDocument);

        driver.createGraph(this.graphName, this.createEdgeDefinitions(), null, true);
        BaseDocument myDoc1 = new BaseDocument("myKeyFROM", myMap);
        Blub myDoc2 = new Blub("myKeyTO", new Blub("blub2", new Blub("blub3", 42)));
        DocumentEntity<BaseDocument> v1
                = driver.graphCreateVertex(this.graphName, "from", myDoc1, null);
        DocumentEntity<Blub> v2
                = driver.graphCreateVertex(this.graphName, "to", myDoc2, null);

        DocumentEntity<BaseDocument> v1DB = driver.getDocument(v1.getDocumentHandle(), BaseDocument.class);
        DocumentEntity<Blub> v2DB = driver.getDocument(v2.getDocumentHandle(), Blub.class);

        assertThat(v1DB.getStatusCode(), is(200));
        assertThat(v1DB.isError(), is(false));
        assertThat(v1DB.getDocumentHandle(), is(notNullValue()));
        assertThat(v1DB.getDocumentRevision(), is(not(0L)));
        assertThat(v1DB.getDocumentKey(), is(notNullValue()));
        Map<String, Object> dbProperties = v1DB.getEntity().getProperties();
        Object blub = dbProperties.get(key2);

        assertThat((String) v1DB.getEntity().getProperties().get(key1), is(val1));
        assertThat((Double) v1DB.getEntity().getProperties().get(key2), is(val2));
        assertThat((Boolean) v1DB.getEntity().getProperties().get(key5), is(val5));

        assertThat(v1DB.getDocumentKey(), is(notNullValue()));

    }


    protected List<EdgeDefinitionEntity> createEdgeDefinitions() {
        List<EdgeDefinitionEntity> edgeDefinitions = new ArrayList<EdgeDefinitionEntity>();
        EdgeDefinitionEntity edgeDefinition = new EdgeDefinitionEntity();
        edgeDefinition.setCollection("edge");
        List<String> from = new ArrayList<String>();
        from.add("from");
        edgeDefinition.setFrom(from);
        List<String> to = new ArrayList<String>();
        to.add("to");
        edgeDefinition.setTo(to);
        edgeDefinitions.add(edgeDefinition);
        return edgeDefinitions;
    }

}
