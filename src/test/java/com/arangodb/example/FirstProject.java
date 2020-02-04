package com.arangodb.example;

import com.arangodb.ArangoCollection;
import com.arangodb.ArangoCursor;
import com.arangodb.ArangoDB;
import com.arangodb.ArangoDBException;
import com.arangodb.entity.BaseDocument;
import com.arangodb.entity.CollectionEntity;
import com.arangodb.util.MapBuilder;
import com.arangodb.velocypack.VPackSlice;

import java.util.Map;

public class FirstProject {

    public static void main(final String[] args) {
        final ArangoDB arangoDB = new ArangoDB.Builder().user("root").build();

        // create database
        final String dbName = "mydb";
        try {
            arangoDB.createDatabase(dbName);
            System.out.println("Database created: " + dbName);
        } catch (final ArangoDBException e) {
            System.err.println("Failed to create database: " + dbName + "; " + e.getMessage());
        }

        // create collection
        final String collectionName = "firstCollection";
        try {
            final CollectionEntity myArangoCollection = arangoDB.db(dbName).createCollection(collectionName);
            System.out.println("Collection created: " + myArangoCollection.getName());
        } catch (final ArangoDBException e) {
            System.err.println("Failed to create collection: " + collectionName + "; " + e.getMessage());
        }

        // creating a document
        final BaseDocument myObject = new BaseDocument();
        myObject.setKey("myKey");
        myObject.addAttribute("a", "Foo");
        myObject.addAttribute("b", 42);
        try {
            arangoDB.db(dbName).collection(collectionName).insertDocument(myObject);
            System.out.println("Document created");
        } catch (final ArangoDBException e) {
            System.err.println("Failed to create document. " + e.getMessage());
        }

        // read a document
        try {
            final BaseDocument myDocument = arangoDB.db(dbName).collection(collectionName).getDocument("myKey",
                    BaseDocument.class);
            System.out.println("Key: " + myDocument.getKey());
            System.out.println("Attribute a: " + myDocument.getAttribute("a"));
            System.out.println("Attribute b: " + myDocument.getAttribute("b"));
        } catch (final ArangoDBException e) {
            System.err.println("Failed to get document: myKey; " + e.getMessage());
        }

        // read a document as VPack
        try {
            final VPackSlice myDocument = arangoDB.db(dbName).collection(collectionName).getDocument("myKey",
                    VPackSlice.class);
            System.out.println("Key: " + myDocument.get("_key").getAsString());
            System.out.println("Attribute a: " + myDocument.get("a").getAsString());
            System.out.println("Attribute b: " + myDocument.get("b").getAsInt());
        } catch (final ArangoDBException e) {
            System.err.println("Failed to get document: myKey; " + e.getMessage());
        }

        // update a document
        myObject.addAttribute("c", "Bar");
        try {
            arangoDB.db(dbName).collection(collectionName).updateDocument("myKey", myObject);
        } catch (final ArangoDBException e) {
            System.err.println("Failed to update document. " + e.getMessage());
        }

        // read the document again
        try {
            final BaseDocument myUpdatedDocument = arangoDB.db(dbName).collection(collectionName).getDocument("myKey",
                    BaseDocument.class);
            System.out.println("Key: " + myUpdatedDocument.getKey());
            System.out.println("Attribute a: " + myUpdatedDocument.getAttribute("a"));
            System.out.println("Attribute b: " + myUpdatedDocument.getAttribute("b"));
            System.out.println("Attribute c: " + myUpdatedDocument.getAttribute("c"));
        } catch (final ArangoDBException e) {
            System.err.println("Failed to get document: myKey; " + e.getMessage());
        }

        // delete a document
        try {
            arangoDB.db(dbName).collection(collectionName).deleteDocument("myKey");
        } catch (final ArangoDBException e) {
            System.err.println("Failed to delete document. " + e.getMessage());
        }

        // create some documents for the next step
        final ArangoCollection collection = arangoDB.db(dbName).collection(collectionName);
        for (int i = 0; i < 10; i++) {
            final BaseDocument value = new BaseDocument();
            value.setKey(String.valueOf(i));
            value.addAttribute("name", "Homer");
            collection.insertDocument(value);
        }

        // execute AQL queries
        try {
            final String query = "FOR t IN firstCollection FILTER t.name == @name RETURN t";
            final Map<String, Object> bindVars = new MapBuilder().put("name", "Homer").get();
            final ArangoCursor<BaseDocument> cursor = arangoDB.db(dbName).query(query, bindVars, null,
                    BaseDocument.class);
            for (; cursor.hasNext(); ) {
                System.out.println("Key: " + cursor.next().getKey());
            }
        } catch (final ArangoDBException e) {
            System.err.println("Failed to execute query. " + e.getMessage());
        }

        // delete a document with AQL
        try {
            final String query = "FOR t IN firstCollection FILTER t.name == @name "
                    + "REMOVE t IN firstCollection LET removed = OLD RETURN removed";
            final Map<String, Object> bindVars = new MapBuilder().put("name", "Homer").get();
            final ArangoCursor<BaseDocument> cursor = arangoDB.db(dbName).query(query, bindVars, null,
                    BaseDocument.class);
            for (; cursor.hasNext(); ) {
                System.out.println("Removed document " + cursor.next().getKey());
            }
        } catch (final ArangoDBException e) {
            System.err.println("Failed to execute query. " + e.getMessage());
        }

    }

}
