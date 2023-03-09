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
import com.arangodb.entity.StreamTransactionEntity;
import com.arangodb.model.AqlQueryOptions;
import com.arangodb.model.StreamTransactionOptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;


/**
 * @author Michele Rastelli
 */
public class ConcurrentStreamTransactionsTest extends BaseJunit5 {

    private static final String COLLECTION_NAME = "db_concurrent_stream_transactions_test";

    @BeforeAll
    public static void init() {
        BaseJunit5.initCollections(COLLECTION_NAME);
    }


    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    public void failingAqlFromBTS57(ArangoDatabase db) {
        assumeTrue(isAtLeastVersion(3, 7));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        String key = "key-" + UUID.randomUUID();
        String id = COLLECTION_NAME + "/" + key;
        db.collection(COLLECTION_NAME).insertDocument(new BaseDocument(key));

        StreamTransactionEntity tx = db.beginStreamTransaction(
                new StreamTransactionOptions().writeCollections(COLLECTION_NAME));

        Map<String, Object> params = new HashMap<>();
        params.put("id", id);
        params.put("@col", COLLECTION_NAME);
        ArangoCursor<Boolean> req = db.query("" +
                        "LET d = DOCUMENT(@id)" +
                        "UPDATE d WITH { \"aaa\": \"aaa\" } IN @@col " +
                        "RETURN true",
                params,
                new AqlQueryOptions().streamTransactionId(tx.getId()), Boolean.class);

        db.commitStreamTransaction(tx.getId());
        assertThat(req.next()).isTrue();
    }

}
