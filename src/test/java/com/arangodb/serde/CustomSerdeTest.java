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
import com.arangodb.entity.BaseDocument;
import com.arangodb.model.DocumentCreateOptions;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.math.BigInteger;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static com.fasterxml.jackson.databind.DeserializationFeature.USE_BIG_INTEGER_FOR_INTS;
import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author Michele Rastelli
 */
public class CustomSerdeTest {

    private static String COLLECTION_NAME = "collection";

    private ArangoDatabase db;
    private ArangoCollection collection;

    @Before
    public void init() {
        VelocyJack velocyJack = new VelocyJack();
        velocyJack.configure((mapper) -> {
            mapper.configure(WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED, true);
            mapper.configure(USE_BIG_INTEGER_FOR_INTS, true);
            SimpleModule module = new SimpleModule();
        });
        ArangoDB arangoDB = new ArangoDB.Builder().serializer(velocyJack).build();

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
        db.drop();
    }

    @Test
    public void documentDeserialization() {
        BaseDocument doc = new BaseDocument("test-" + UUID.randomUUID().toString());
        doc.addAttribute("int", 10);

        collection.insertDocument(doc, null);

        final BaseDocument getDocumentResult = db.collection(COLLECTION_NAME).getDocument(
                doc.getKey(),
                BaseDocument.class,
                null);

        assertThat(getDocumentResult.getAttribute("int"), instanceOf(BigInteger.class));
    }

    @Test
    public void aqlDeserialization() {
        BaseDocument doc = new BaseDocument("test-" + UUID.randomUUID().toString());
        doc.addAttribute("int", 10);

        collection.insertDocument(doc, null);

        final BaseDocument queryResult = db.query(
                "RETURN DOCUMENT(@docId)",
                Collections.singletonMap("docId", COLLECTION_NAME + "/" + doc.getKey()),
                BaseDocument.class
        ).first();

        assertThat(queryResult.getAttribute("int"), instanceOf(BigInteger.class));
    }

    @Test
    public void documentSerialization() {
        String key = "test-" + UUID.randomUUID().toString();

        String hello = "hello";
        List<String> arr = Collections.singletonList(hello);

        BaseDocument doc = new BaseDocument(key);
        doc.addAttribute("arr", arr);

        BaseDocument insertedDoc = collection.insertDocument(
                doc,
                new DocumentCreateOptions().returnNew(true)
        ).getNew();

        Object gotArr = insertedDoc.getAttribute("arr");
        assertThat(gotArr, instanceOf(String.class));
        assertThat(gotArr, is(hello));
    }

    @Test
    public void aqlSerialization() {
        String key = "test-" + UUID.randomUUID().toString();

        String hello = "hello";
        List<String> arr = Collections.singletonList(hello);

        BaseDocument doc = new BaseDocument(key);
        doc.addAttribute("arr", arr);

        HashMap<String, Object> params = new HashMap<>();
        params.put("doc", doc);
        params.put("@collection", COLLECTION_NAME);

        BaseDocument insertedQuery = db.query(
                "INSERT @doc INTO @@collection RETURN NEW",
                params,
                BaseDocument.class
        ).first();

        Object gotArr = insertedQuery.getAttribute("arr");
        assertThat(gotArr, instanceOf(String.class));
        assertThat(gotArr, is(hello));
    }

}
