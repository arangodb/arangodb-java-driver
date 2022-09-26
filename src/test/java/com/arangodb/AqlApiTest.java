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

import com.arangodb.entity.ArangoDBEngine;
import com.arangodb.entity.BaseDocument;
import com.arangodb.model.AqlQueryOptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * @author Michele Rastelli
 */
public class AqlApiTest extends BaseJunit5 {

    private static final String COLLECTION_NAME = "aql_api_test";

    @BeforeAll
    public static void init() {
        BaseJunit5.initCollections(COLLECTION_NAME);
    }

    /**
     * It performs the following steps: - create 40 documents in the document collection <c> - create a stream AQL
     * cursor which reads all the documents in collection <c>, batch size 10 - read 20 documents from the cursor (2
     * batches) - explicitly close the cursor
     *
     * @see <a
     * href="https://www.arangodb.com/docs/stable/http/aql-query-cursor-accessing-cursors.html#delete-cursor">API
     * Documentation</a>
     */
    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    @Timeout(value = 120_000L, unit = TimeUnit.MILLISECONDS)
    public void explicitlyDestroyStreamCursor(ArangoDatabase db) throws IOException {
        assumeTrue(isAtLeastVersion(3, 7));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        for (int i = 0; i < 40; i++) {
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
