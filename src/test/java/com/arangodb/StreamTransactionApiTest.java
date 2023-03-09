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
import com.arangodb.mapping.ArangoJack;
import com.arangodb.model.DocumentCreateOptions;
import com.arangodb.model.StreamTransactionOptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * @author Michele Rastelli
 */
public class StreamTransactionApiTest extends BaseJunit5 {

    private static final String COLLECTION_NAME = "stream_transactions_api_test";
    private static final List<ArangoDB> adbs = Arrays.stream(Protocol.values())
            .map(p -> new ArangoDB.Builder()
                    .useProtocol(p)
                    .serializer(new ArangoJack())
                    .acquireHostList(true)
                    .loadBalancingStrategy(LoadBalancingStrategy.ROUND_ROBIN)
                    .build())
            .collect(Collectors.toList());

    protected static Stream<ArangoDatabase> dbsStream() {
        return adbs.stream().map(ArangoDB::db);
    }

    protected static Stream<Arguments> dbs() {
        return dbsStream().map(Arguments::of);
    }

    @BeforeEach
    void beforeEach() {
        assumeTrue(isCluster());

        ArangoDatabase db = dbsStream().iterator().next();
        ArangoCollection collection = db.collection(COLLECTION_NAME);
        if (!collection.exists())
            collection.create();
    }

    /**
     * It performs the following steps, using a driver instance configured with loadBalancingStrategy set to
     * {@link LoadBalancingStrategy#ROUND_ROBIN}: - begin a stream transaction <t> with writeCollections: [<c>] - insert
     * a new document into <c> from within <t>
     */
    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    @Timeout(value = 10_000, unit = TimeUnit.MILLISECONDS)
    public void streamTransactionFromDifferentCoordinators(ArangoDatabase db) {
        ArangoCollection collection = db.collection(COLLECTION_NAME);

        String transactionId = db.beginStreamTransaction(
                new StreamTransactionOptions().writeCollections(COLLECTION_NAME)).getId();
        collection.insertDocument(new BaseDocument(), new DocumentCreateOptions().streamTransactionId(transactionId));
        db.abortStreamTransaction(transactionId);
    }

}
