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
import com.arangodb.entity.DocumentCreateEntity;
import com.arangodb.velocystream.Request;
import com.arangodb.velocystream.RequestType;
import com.arangodb.velocystream.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Michele Rastelli
 */
public class HeadRequestTest extends BaseJunit5 {

    private static final String COLLECTION_NAME = "HeadRequestTest_collection";

    @BeforeAll
    public static void init() {
        BaseJunit5.initCollections(COLLECTION_NAME);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    public void headRequestShouldNotReturnBody(ArangoDatabase db) {
        DocumentCreateEntity<BaseDocument> createEntity = db.collection(COLLECTION_NAME)
                .insertDocument(new BaseDocument("key-" + UUID.randomUUID()));
        Request request = new Request(
                db.name(),
                RequestType.HEAD,
                "/_api/document/" + createEntity.getId());
        executeAndAssert(db, request);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    public void headRequestIfMatchNonMatchingShouldNotReturnBody(ArangoDatabase db) {
        DocumentCreateEntity<BaseDocument> createEntity = db.collection(COLLECTION_NAME)
                .insertDocument(new BaseDocument("key-" + UUID.randomUUID()));
        Request request = new Request(
                db.name(),
                RequestType.HEAD,
                "/_api/document/" + createEntity.getId());
        request.putHeaderParam("If-Match", "nonMatching");
        executeAndAssert(db, request);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    public void headRequestIfMatchMatchingShouldNotReturnBody(ArangoDatabase db) {
        DocumentCreateEntity<BaseDocument> createEntity = db.collection(COLLECTION_NAME)
                .insertDocument(new BaseDocument("key-" + UUID.randomUUID()));
        Request request = new Request(
                db.name(),
                RequestType.HEAD,
                "/_api/document/" + createEntity.getId());
        request.putHeaderParam("If-Match", createEntity.getRev());
        executeAndAssert(db, request);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    public void headRequestIfNoneMatchMatchingShouldNotReturnBody(ArangoDatabase db) {
        DocumentCreateEntity<BaseDocument> createEntity = db.collection(COLLECTION_NAME)
                .insertDocument(new BaseDocument("key-" + UUID.randomUUID()));
        Request request = new Request(
                db.name(),
                RequestType.HEAD,
                "/_api/document/" + createEntity.getId());
        request.putHeaderParam("If-None-Match", createEntity.getRev());
        executeAndAssert(db, request);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    public void headRequestIfNoneMatchNonMatchingShouldNotReturnBody(ArangoDatabase db) {
        DocumentCreateEntity<BaseDocument> createEntity = db.collection(COLLECTION_NAME)
                .insertDocument(new BaseDocument("key-" + UUID.randomUUID()));
        Request request = new Request(
                db.name(),
                RequestType.HEAD,
                "/_api/document/" + createEntity.getId());
        request.putHeaderParam("If-None-Match", "nonMatching");
        executeAndAssert(db, request);
    }

    private void executeAndAssert(ArangoDatabase db, Request request) {
        try {
            Response response = db.arango().execute(request);
            assertThat(response.getBody()).isNull();
        } catch (ArangoDBException e) {
            assertThat(e.getResponseBody()).isNull();
        }
    }

}
