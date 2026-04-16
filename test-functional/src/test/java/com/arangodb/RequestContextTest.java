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
import com.arangodb.model.AqlQueryOptions;
import com.arangodb.model.DocumentReadOptions;
import com.arangodb.model.StreamTransactionOptions;
import com.arangodb.serde.ArangoSerde;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.json.JsonMapper;

import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import static com.arangodb.util.TestUtils.TEST_DB;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * NB: excluded from shaded tests
 */
class RequestContextTest {

    private static final String COLLECTION_NAME = "RequestContextTest_collection";

    private static ArangoDB arangoDB;
    private static ArangoDatabase db;
    private static ArangoDatabaseAsync dbAsync;
    private static ArangoCollection collection;
    private static ArangoCollectionAsync collectionAsync;

    @BeforeAll
    static void init() {
        ArangoSerde serde = new ArangoSerde() {
            private final JsonMapper mapper = JsonMapper.builder()
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                    .build();

            @Override
            public byte[] serialize(Object value) {
                throw new UnsupportedOperationException();
            }

            @Override
            public <T> T deserialize(byte[] content, Class<T> clazz, RequestContext ctx) {
                Objects.requireNonNull(ctx);

                if (clazz != Person.class) {
                    throw new UnsupportedOperationException();
                }

                Person res = mapper.readValue(content, Person.class);
                res.txId = ctx.getStreamTransactionId().orElseThrow();
                return (T) res;
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
        dbAsync = arangoDB.async().db(TEST_DB);
        collectionAsync = dbAsync.collection(COLLECTION_NAME);
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

    @Test
    void queryWithinTx() {
        StreamTransactionEntity tx = db.beginStreamTransaction(new StreamTransactionOptions());
        Person res = db.query("""
                RETURN {"name":"foo"}
                """, Person.class, new AqlQueryOptions().streamTransactionId(tx.getId())).next();

        assertThat(res.name).isEqualTo("foo");
        assertThat(res.txId).isEqualTo(tx.getId());

        db.abortStreamTransaction(tx.getId());
    }

    @Test
    void asyncQueryWithinTx() throws ExecutionException, InterruptedException {
        StreamTransactionEntity tx = db.beginStreamTransaction(new StreamTransactionOptions());
        Person res = dbAsync.query("""
                RETURN {"name":"foo"}
                """, Person.class, new AqlQueryOptions().streamTransactionId(tx.getId())).get().getResult().getFirst();

        assertThat(res.name).isEqualTo("foo");
        assertThat(res.txId).isEqualTo(tx.getId());

        db.abortStreamTransaction(tx.getId());
    }

}
