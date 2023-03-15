import com.arangodb.*;
import com.arangodb.entity.BaseDocument;
import com.arangodb.util.RawJson;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

import java.util.Collections;
import java.util.Map;

public class FirstProject {
    private static final ArangoDB arangoDB = new ArangoDB.Builder()
            .host("localhost", 8529)
            .password("test")
            .build();

    private static void cleanup() {
        ArangoDatabase db = arangoDB.db(DbName.of("mydb"));
        if (db.exists()) db.drop();
    }

    public static void main(String[] args) {
        cleanup();

        // Creating a database
        ArangoDatabase db = arangoDB.db(DbName.of("mydb"));
        System.out.println("Creating database...");
        db.create();

        // Creating a collection
        ArangoCollection collection = db.collection("firstCollection");
        System.out.println("Creating collection...");
        collection.create();

        // Creating a document
        String key = "myKey";
        BaseDocument doc = new BaseDocument(key);
        doc.addAttribute("a", "Foo");
        doc.addAttribute("b", 42);
        System.out.println("Inserting document...");
        collection.insertDocument(doc);

        // Read a document
        {
            System.out.println("Reading document...");
            BaseDocument readDocument = collection.getDocument(key, BaseDocument.class);
            System.out.println("Key: " + readDocument.getKey());
            System.out.println("Attribute a: " + readDocument.getAttribute("a"));
            System.out.println("Attribute b: " + readDocument.getAttribute("b"));
        }

        // Creating a document from Jackson JsonNode
        String keyJackson = "myJacksonKey";
        JsonNode jsonNode = JsonNodeFactory.instance.objectNode()
                .put("_key", keyJackson)
                .put("a", "Bar")
                .put("b", 53);
        System.out.println("Inserting document from Jackson JsonNode...");
        collection.insertDocument(jsonNode);

        // Read a document as Jackson JsonNode
        {
            System.out.println("Reading document as Jackson JsonNode...");
            JsonNode readJsonNode = collection.getDocument(keyJackson, JsonNode.class);
            System.out.println("Key: " + readJsonNode.get("_key").textValue());
            System.out.println("Attribute a: " + readJsonNode.get("a").textValue());
            System.out.println("Attribute b: " + readJsonNode.get("b").intValue());
        }

        // Creating a document from JSON String
        String keyJson = "myJsonKey";
        RawJson json = RawJson.of("""
                {"_key":"%s","a":"Baz","b":64}
                """.formatted(keyJson));
        System.out.println("Inserting document from JSON String...");
        collection.insertDocument(json);

        // Read a document as JSON String
        {
            System.out.println("Reading document as JSON String...");
            RawJson readJson = collection.getDocument(keyJson, RawJson.class);
            System.out.println(readJson.getValue());
        }

        // Update a document
        {
            doc.addAttribute("c", "Bar");
            System.out.println("Updating document ...");
            collection.updateDocument(key, doc);
        }

        // Read the document again
        {
            System.out.println("Reading updated document ...");
            BaseDocument updatedDocument = collection.getDocument(key, BaseDocument.class);
            System.out.println("Key: " + updatedDocument.getKey());
            System.out.println("Attribute a: " + updatedDocument.getAttribute("a"));
            System.out.println("Attribute b: " + updatedDocument.getAttribute("b"));
            System.out.println("Attribute c: " + updatedDocument.getAttribute("c"));
        }

        // Delete a document
        {
            System.out.println("Deleting document ...");
            collection.deleteDocument(key);
        }

        // Execute AQL queries
        {
            for (int i = 0; i < 10; i++) {
                BaseDocument value = new BaseDocument(String.valueOf(i));
                value.addAttribute("name", "Homer");
                collection.insertDocument(value);
            }

            String query = "FOR t IN firstCollection FILTER t.name == @name RETURN t";
            Map<String, Object> bindVars = Collections.singletonMap("name", "Homer");
            System.out.println("Executing read query ...");
            ArangoCursor<BaseDocument> cursor = db.query(query, BaseDocument.class, bindVars);
            cursor.forEach(aDocument -> System.out.println("Key: " + aDocument.getKey()));
        }

        // Delete a document with AQL
        {
            String query = "FOR t IN firstCollection FILTER t.name == @name "
                    + "REMOVE t IN firstCollection LET removed = OLD RETURN removed";
            Map<String, Object> bindVars = Collections.singletonMap("name", "Homer");
            System.out.println("Executing delete query ...");
            ArangoCursor<BaseDocument> cursor = db.query(query, BaseDocument.class, bindVars);
            cursor.forEach(aDocument -> System.out.println("Removed document " + aDocument.getKey()));
        }

        arangoDB.shutdown();
    }
}
