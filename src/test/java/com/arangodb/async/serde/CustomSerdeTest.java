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
import com.arangodb.entity.BaseDocument;
import com.arangodb.mapping.ArangoJack;
import com.arangodb.model.DocumentCreateOptions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import java.math.BigInteger;
import java.util.Collections;
import java.util.HashMap;
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
        ArangoJack arangoJack = new ArangoJack();
        arangoJack.configure((mapper) -> {
            mapper.configure(WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED, true);
            mapper.configure(USE_BIG_INTEGER_FOR_INTS, true);
        });
        ArangoDBAsync arangoDB = new ArangoDBAsync.Builder().serializer(arangoJack).build();

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

        BaseDocument doc = new BaseDocument(key);
        doc.addAttribute("arr", Collections.singletonList("hello"));
        doc.addAttribute("int", 10);

        HashMap<String, Object> params = new HashMap<>();
        params.put("doc", doc);
        params.put("@collection", COLLECTION_NAME);

        BaseDocument result = db.query(
                "INSERT @doc INTO @@collection RETURN NEW",
                params,
                BaseDocument.class
        ).get().next();

        assertThat(result.getAttribute("arr")).isInstanceOf(String.class);
        assertThat(result.getAttribute("arr")).isEqualTo("hello");
        assertThat(result.getAttribute("int")).isInstanceOf(BigInteger.class);
        assertThat(result.getAttribute("int")).isEqualTo(BigInteger.valueOf(10));
    }

    @Test
    void aqlDeserialization() throws ExecutionException, InterruptedException {
        String key = "test-" + UUID.randomUUID();

        BaseDocument doc = new BaseDocument(key);
        doc.addAttribute("arr", Collections.singletonList("hello"));
        doc.addAttribute("int", 10);

        collection.insertDocument(doc, null).get();

        final BaseDocument result = db.query(
                "RETURN DOCUMENT(@docId)",
                Collections.singletonMap("docId", COLLECTION_NAME + "/" + key),
                BaseDocument.class
        ).get().next();

        assertThat(result.getAttribute("arr")).isInstanceOf(String.class);
        assertThat(result.getAttribute("arr")).isEqualTo("hello");
        assertThat(result.getAttribute("int")).isInstanceOf(BigInteger.class);
        assertThat(result.getAttribute("int")).isEqualTo(BigInteger.valueOf(10));
    }

    @Test
    void insertDocument() throws ExecutionException, InterruptedException {
        String key = "test-" + UUID.randomUUID();

        BaseDocument doc = new BaseDocument(key);
        doc.addAttribute("arr", Collections.singletonList("hello"));
        doc.addAttribute("int", 10);

        BaseDocument result = collection.insertDocument(
                doc,
                new DocumentCreateOptions().returnNew(true)
        ).get().getNew();

        assertThat(result.getAttribute("arr")).isInstanceOf(String.class);
        assertThat(result.getAttribute("arr")).isEqualTo("hello");
        assertThat(result.getAttribute("int")).isInstanceOf(BigInteger.class);
        assertThat(result.getAttribute("int")).isEqualTo(BigInteger.valueOf(10));
    }

    @Test
    void getDocument() throws ExecutionException, InterruptedException {
        String key = "test-" + UUID.randomUUID();

        BaseDocument doc = new BaseDocument(key);
        doc.addAttribute("arr", Collections.singletonList("hello"));
        doc.addAttribute("int", 10);

        collection.insertDocument(doc, null).get();

        final BaseDocument result = db.collection(COLLECTION_NAME).getDocument(
                key,
                BaseDocument.class,
                null).get();

        assertThat(result.getAttribute("arr")).isInstanceOf(String.class);
        assertThat(result.getAttribute("arr")).isEqualTo("hello");
        assertThat(result.getAttribute("int")).isInstanceOf(BigInteger.class);
        assertThat(result.getAttribute("int")).isEqualTo(BigInteger.valueOf(10));
    }

}
