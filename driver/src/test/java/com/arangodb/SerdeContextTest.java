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
import com.arangodb.serde.ArangoSerde;
import com.arangodb.serde.SerdeContext;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.Objects;

import static com.arangodb.util.TestUtils.TEST_DB;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Michele Rastelli
 */
class SerdeContextTest {

    private static final String COLLECTION_NAME = "SerdeContextTest_collection";

    private static ArangoDB arangoDB;
    private static ArangoDatabase db;
    private static ArangoCollection collection;

    @BeforeAll
    static void init() {
        ArangoSerde serde = new ArangoSerde() {
            private ObjectMapper mapper = new ObjectMapper()
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            @Override
            public byte[] serialize(Object value) {
                throw new UnsupportedOperationException();
            }

            @Override
            public <T> T deserialize(byte[] content, Class<T> clazz) {
                throw new UnsupportedOperationException();
            }

            @Override
            public <T> T deserialize(byte[] content, Class<T> clazz, SerdeContext ctx) {
                Objects.requireNonNull(ctx);

                if (clazz != Person.class) {
                    throw new UnsupportedOperationException();
                }

                try {
                    Person res = mapper.readValue(content, Person.class);
                    res.txId = ctx.getStreamTransactionId().get();
                    return (T) res;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        };

        arangoDB = new ArangoDB.Builder()
                .loadProperties(ConfigUtils.loadConfig())
                .serde(serde).build();

        db = arangoDB.db(TEST_DB);
        if (!db.exists()) {
            db.create();
        }

        collection = db.collection(COLLECTION_NAME);
        if (!collection.exists()) {
            collection.create();
        }
    }

    @AfterAll
    static void shutdown() {
        if (db.exists())
            db.drop();
    }

    static class Person {
        String name;
        String txId;

        Person(@JsonProperty("name") String name) {
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

}
