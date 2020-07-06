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


import com.arangodb.entity.BaseDocument;
import com.arangodb.entity.LoadBalancingStrategy;
import com.arangodb.entity.ServerRole;
import com.arangodb.model.DocumentUpdateOptions;
import com.arangodb.model.StreamTransactionOptions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.Assume.assumeTrue;

/**
 * @author Michele Rastelli
 */
@RunWith(Parameterized.class)
public class StreamTransactionApiTest {

    private static final String COLLECTION_NAME = "stream_transactions_api_test";

    private final ArangoDB arangoDB;
    private final ArangoDatabase db;
    private final ArangoCollection collection;

    @Parameterized.Parameters
    public static List<ArangoDB> builders() {
        return Arrays.asList(
                new ArangoDB.Builder().acquireHostList(true).loadBalancingStrategy(LoadBalancingStrategy.ROUND_ROBIN).useProtocol(Protocol.VST).build(),
                new ArangoDB.Builder().acquireHostList(true).loadBalancingStrategy(LoadBalancingStrategy.ROUND_ROBIN).useProtocol(Protocol.HTTP_JSON).build(),
                new ArangoDB.Builder().acquireHostList(true).loadBalancingStrategy(LoadBalancingStrategy.ROUND_ROBIN).useProtocol(Protocol.HTTP_VPACK).build()
        );
    }

    public StreamTransactionApiTest(final ArangoDB arangoDB) {
        this.arangoDB = arangoDB;
        db = arangoDB.db();
        collection = db.collection(COLLECTION_NAME);
    }

    @Before
    public void init() {
        if (!collection.exists())
            collection.create();
    }

    @Test
    public void streamTransactionFromDifferentCoordinators() {
        assumeTrue(isCluster());

        String key = "key-" + UUID.randomUUID().toString();
        collection.insertDocument(new BaseDocument(key));

        String transactionId = db.beginStreamTransaction(
                new StreamTransactionOptions().writeCollections(COLLECTION_NAME)).getId();

        collection.updateDocument(key, new BaseDocument(key), new DocumentUpdateOptions().streamTransactionId(transactionId));
    }

    private boolean isCluster() {
        return arangoDB.getRole() == ServerRole.COORDINATOR;
    }

}
