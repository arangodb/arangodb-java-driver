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

package com.arangodb.async.serde;


import com.arangodb.DbName;
import com.arangodb.async.ArangoCollectionAsync;
import com.arangodb.async.ArangoDBAsync;
import com.arangodb.async.ArangoDatabaseAsync;
import com.arangodb.internal.config.FileConfigPropertiesProvider;
import com.arangodb.model.DocumentCreateOptions;
import com.arangodb.ContentType;
import com.arangodb.serde.jackson.JacksonSerde;
import com.arangodb.serde.jackson.JacksonSerdeProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static com.fasterxml.jackson.databind.DeserializationFeature.USE_BIG_INTEGER_FOR_INTS;
import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED;
import static org.assertj.core.api.Assertions.assertThat;


/**
 * @author Michele Rastelli
 */
class CustomSerdeTest {

    private static final String COLLECTION_NAME = "collection";

    private ArangoDatabaseAsync db;
    private ArangoCollectionAsync collection;

    @BeforeEach
    void init() throws ExecutionException, InterruptedException {
        JacksonSerde serde = new JacksonSerdeProvider().of(ContentType.VPACK);
        serde.configure((mapper) -> {
            mapper.configure(WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED, true);
            mapper.configure(USE_BIG_INTEGER_FOR_INTS, true);
        });
        ArangoDBAsync arangoDB = new ArangoDBAsync.Builder()
                .loadProperties(new FileConfigPropertiesProvider())
                .serializer(serde).build();

        DbName TEST_DB = DbName.of("custom-serde-test");
        db = arangoDB.db(TEST_DB);
        if (!db.exists().get()) {
            db.create().get();
        }

        collection = db.collection(COLLECTION_NAME);
        if (!collection.exists().get()) {
            collection.create().get();
        }
    }

    @AfterEach
    void shutdown() throws ExecutionException, InterruptedException {
        db.drop().get();
    }

    @Test
    void aqlSerialization() throws ExecutionException, InterruptedException {
        String key = "test-" + UUID.randomUUID();

        Map<String, Object> doc = new HashMap<>();
        doc.put("_key", key);
        doc.put("arr", Collections.singletonList("hello"));
        doc.put("int", 10);

        HashMap<String, Object> params = new HashMap<>();
        params.put("doc", doc);
        params.put("@collection", COLLECTION_NAME);

        Map<String, Object> result = db.query(
                "INSERT @doc INTO @@collection RETURN NEW",
                params,
                Map.class
        ).get().next();

        assertThat(result.get("arr")).isInstanceOf(String.class);
        assertThat(result.get("arr")).isEqualTo("hello");
        assertThat(result.get("int")).isInstanceOf(BigInteger.class);
        assertThat(result.get("int")).isEqualTo(BigInteger.valueOf(10));
    }

    @Test
    void aqlDeserialization() throws ExecutionException, InterruptedException {
        String key = "test-" + UUID.randomUUID();

        Map<String, Object> doc = new HashMap<>();
        doc.put("_key", key);
        doc.put("arr", Collections.singletonList("hello"));
        doc.put("int", 10);

        collection.insertDocument(doc).get();

        final Map<String, Object> result = db.query(
                "RETURN DOCUMENT(@docId)",
                Collections.singletonMap("docId", COLLECTION_NAME + "/" + key),
                Map.class
        ).get().next();

        assertThat(result.get("arr")).isInstanceOf(String.class);
        assertThat(result.get("arr")).isEqualTo("hello");
        assertThat(result.get("int")).isInstanceOf(BigInteger.class);
        assertThat(result.get("int")).isEqualTo(BigInteger.valueOf(10));
    }

    @Test
    void insertDocument() throws ExecutionException, InterruptedException {
        String key = "test-" + UUID.randomUUID();

        Map<String, Object> doc = new HashMap<>();
        doc.put("_key", key);
        doc.put("arr", Collections.singletonList("hello"));
        doc.put("int", 10);

        Map<String, Object> result = collection.insertDocument(
                doc,
                new DocumentCreateOptions().returnNew(true)
        ).get().getNew();

        assertThat(result.get("arr")).isInstanceOf(String.class);
        assertThat(result.get("arr")).isEqualTo("hello");
        assertThat(result.get("int")).isInstanceOf(BigInteger.class);
        assertThat(result.get("int")).isEqualTo(BigInteger.valueOf(10));
    }

    @Test
    void getDocument() throws ExecutionException, InterruptedException {
        String key = "test-" + UUID.randomUUID();

        Map<String, Object> doc = new HashMap<>();
        doc.put("_key", key);
        doc.put("arr", Collections.singletonList("hello"));
        doc.put("int", 10);

        collection.insertDocument(doc).get();

        final Map<String, Object> result = db.collection(COLLECTION_NAME).getDocument(
                key,
                Map.class,
                null).get();

        assertThat(result.get("arr")).isInstanceOf(String.class);
        assertThat(result.get("arr")).isEqualTo("hello");
        assertThat(result.get("int")).isInstanceOf(BigInteger.class);
        assertThat(result.get("int")).isEqualTo(BigInteger.valueOf(10));
    }

}
