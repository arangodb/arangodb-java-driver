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

package com.arangodb;

import com.arangodb.config.ConfigUtils;
import com.arangodb.entity.BaseDocument;
import com.arangodb.entity.DocumentCreateEntity;
import com.arangodb.entity.StreamTransactionEntity;
import com.arangodb.model.DocumentReadOptions;
import com.arangodb.model.StreamTransactionOptions;
import com.arangodb.serde.jackson.JacksonSerde;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.ExecutionException;

import static com.arangodb.util.TestUtils.TEST_DB;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * NB: excluded from shaded tests
 */
class JacksonRequestContextTest {

    private static final String COLLECTION_NAME = "JacksonRequestContextTest_collection";

    private static ArangoDB arangoDB;
    private static ArangoDatabase db;
    private static ArangoCollection collection;
    private static ArangoCollectionAsync collectionAsync;

    @BeforeAll
    static void init() {
        JacksonSerde serde = JacksonSerde.of(ContentType.JSON)
                .configure((mapper) -> {
                    SimpleModule module = new SimpleModule("PersonModule");
                    module.addDeserializer(Person.class, new PersonDeserializer());
                    mapper.registerModule(module);
                });
        arangoDB = new ArangoDB.Builder()
                .loadProperties(ConfigUtils.loadConfig())
                .serde(serde).build();

        db = arangoDB.db(TEST_DB);
        if (!db.exists()) {
            db.create();
        }

        collection = db.collection(COLLECTION_NAME);
        collectionAsync = arangoDB.async().db(TEST_DB).collection(COLLECTION_NAME);
        if (!collection.exists()) {
            collection.create();
        }
    }

    @AfterAll
    static void shutdown() {
        if (db.exists()) {
            db.drop();
        }
        arangoDB.shutdown();
    }

    static class PersonDeserializer extends JsonDeserializer<Person> {
        @Override
        public Person deserialize(JsonParser parser, DeserializationContext ctx) throws IOException {
            JsonNode rootNode = parser.getCodec().readTree(parser);
            Person person = new Person(rootNode.get("name").asText());
            person.txId = JacksonSerde.getRequestContext(ctx).getStreamTransactionId().get();
            return person;
        }
    }

    static class Person {
        String name;
        String txId;

        Person(String name) {
            this.name = name;
        }
    }

    @Test
    void getDocumentWithinTx() {
        DocumentCreateEntity<?> doc = collection.insertDocument(
                new BaseDocument(Collections.singletonMap("name", "foo")), null);

        StreamTransactionEntity tx = db
                .beginStreamTransaction(new StreamTransactionOptions().readCollections(COLLECTION_NAME));

        Person read = collection.getDocument(doc.getKey(), Person.class,
                new DocumentReadOptions().streamTransactionId(tx.getId()));

        assertThat(read.name).isEqualTo("foo");
        assertThat(read.txId).isEqualTo(tx.getId());

        db.abortStreamTransaction(tx.getId());
    }

    @Test
    void asyncGetDocumentWithinTx() throws ExecutionException, InterruptedException {
        DocumentCreateEntity<?> doc = collection.insertDocument(
                new BaseDocument(Collections.singletonMap("name", "foo")), null);

        StreamTransactionEntity tx = db
                .beginStreamTransaction(new StreamTransactionOptions().readCollections(COLLECTION_NAME));

        Person read = collectionAsync.getDocument(doc.getKey(), Person.class,
                        new DocumentReadOptions().streamTransactionId(tx.getId()))
                .get();

        assertThat(read.name).isEqualTo("foo");
        assertThat(read.txId).isEqualTo(tx.getId());

        db.abortStreamTransaction(tx.getId());
    }

}
