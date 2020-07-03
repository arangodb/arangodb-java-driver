package com.arangodb;/*
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


import com.arangodb.entity.ArangoDBEngine;
import com.arangodb.entity.BaseDocument;
import com.arangodb.model.AqlQueryOptions;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.util.Collections;

import static org.junit.Assume.assumeTrue;

/**
 * @author Michele Rastelli
 */
@RunWith(Parameterized.class)
public class AqlApiTest extends BaseTest {

    private static final String COLLECTION_NAME = "aql_api_test";

    public AqlApiTest(final ArangoDB arangoDB) {
        super(arangoDB);
    }

    @BeforeClass
    public static void init() {
        BaseTest.initCollections(COLLECTION_NAME);
    }

    /**
     * It performs the following steps:
     * - create 100 documents in the document collection <c>
     * - create a stream AQL cursor which reads all the documents in collection <c>, batch size 10
     * - read 20 documents from the cursor (2 batches)
     * - explicitly close the cursor
     *
     * @see <a href="https://www.arangodb.com/docs/stable/http/aql-query-cursor-accessing-cursors.html#delete-cursor">API
     * Documentation</a>
     */
    @Test
    public void explicitlyDestroyStreamCursor() throws IOException {
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        for (int i = 0; i < 100; i++) {
            db.collection(COLLECTION_NAME).insertDocument(new BaseDocument());
        }

        ArangoCursor<BaseDocument> cursor = db.query(
                "FOR doc IN @@col RETURN doc",
                Collections.singletonMap("@col", COLLECTION_NAME),
                new AqlQueryOptions()
                        .stream(true)
                        .batchSize(10),
                BaseDocument.class);

        for (int i = 0; i < 20; i++) {
            cursor.next();
        }

        cursor.close();
    }

}
