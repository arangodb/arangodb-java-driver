/*
 * DISCLAIMER
 *
 * Copyright 2016 ArangoDB GmbH, Cologne, Germany
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Copyright holder is ArangoDB GmbH, Cologne, Germany
 */

package com.arangodb.serde;


import com.arangodb.ArangoCollection;
import com.arangodb.ArangoDB;
import com.arangodb.ArangoDatabase;
import com.arangodb.VelocyJack;
import com.arangodb.model.DocumentCreateOptions;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author Michele Rastelli
 */
public class CustomTypeHintTest {

    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "type")
    public interface Animal {
        String getName();
    }

    public static class Gorilla implements Animal {
        private String name;

        @Override
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public static class Zoo {

        @JsonProperty("_key")
        private String key;

        private Animal animal;

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public Animal getAnimal() {
            return animal;
        }

        public void setAnimal(Animal animal) {
            this.animal = animal;
        }
    }

    private static final String COLLECTION_NAME = "collection";

    private ArangoDatabase db;
    private ArangoCollection collection;

    @Before
    public void init() {
        ArangoDB arangoDB = new ArangoDB.Builder()
                .serializer(new VelocyJack())
                .build();

        String TEST_DB = "custom-serde-test";
        db = arangoDB.db(TEST_DB);
        if (!db.exists()) {
            db.create();
        }

        collection = db.collection(COLLECTION_NAME);
        if (!collection.exists()) {
            collection.create();
        }
    }

    @After
    public void shutdown() {
        if (db.exists())
            db.drop();
    }

    @Test
    public void insertDocument() {
        Gorilla gorilla = new Gorilla();
        gorilla.setName("kingKong");

        Zoo doc = new Zoo();
        doc.setAnimal(gorilla);

        Zoo insertedDoc = collection.insertDocument(
                doc,
                new DocumentCreateOptions().returnNew(true)
        ).getNew();

        assertThat((insertedDoc.getAnimal().getName()), is("kingKong"));

        String key = insertedDoc.getKey();

        // in the db a document like this is created:
        //        {
        //            "animal": {
        //                  "type": "com.arangodb.serde.CustomTypeHintTest$Gorilla",
        //                      "name": "kingKong"
        //            }
        //        }

        final Zoo readDoc = collection.getDocument(
                key,
                Zoo.class,
                null);

        assertThat((readDoc.getAnimal().getName()), is("kingKong"));
    }
}
