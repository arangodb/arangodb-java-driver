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
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.UUID;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Michele Rastelli
 */
@RunWith(Parameterized.class)
public class HeadRequestTest extends BaseTest {

    private static final String COLLECTION_NAME = "HeadRequestTest_collection";
    private final ArangoCollection collection;
    private final DocumentCreateEntity<BaseDocument> createEntity;

    public HeadRequestTest(ArangoDB arangoDB) {
        super(arangoDB);
        collection = db.collection(COLLECTION_NAME);
        createEntity = collection.insertDocument(new BaseDocument("key-" + UUID.randomUUID().toString()));
    }

    @BeforeClass
    public static void init() {
        BaseTest.initCollections(COLLECTION_NAME);
    }

    @Test
    public void headRequestShouldNotReturnBody() {
        Request request = new Request(
                db.name(),
                RequestType.HEAD,
                "/_api/document/" + createEntity.getId());
        executeAndAssert(request);
    }

    @Test
    public void headRequestIfMatchNonMatchingShouldNotReturnBody() {
        Request request = new Request(
                db.name(),
                RequestType.HEAD,
                "/_api/document/" + createEntity.getId());
        request.putHeaderParam("If-Match", "nonMatching");
        executeAndAssert(request);
    }

    @Test
    public void headRequestIfMatchMatchingShouldNotReturnBody() {
        Request request = new Request(
                db.name(),
                RequestType.HEAD,
                "/_api/document/" + createEntity.getId());
        request.putHeaderParam("If-Match", createEntity.getRev());
        executeAndAssert(request);
    }

    @Test
    public void headRequestIfNoneMatchMatchingShouldNotReturnBody() {
        Request request = new Request(
                db.name(),
                RequestType.HEAD,
                "/_api/document/" + createEntity.getId());
        request.putHeaderParam("If-None-Match", createEntity.getRev());
        executeAndAssert(request);
    }

    @Test
    public void headRequestIfNoneMatchNonMatchingShouldNotReturnBody() {
        Request request = new Request(
                db.name(),
                RequestType.HEAD,
                "/_api/document/" + createEntity.getId());
        request.putHeaderParam("If-None-Match", "nonMatching");
        executeAndAssert(request);
    }

    private void executeAndAssert(Request request) {
        try {
            Response response = arangoDB.execute(request);
            assertThat(response.getBody(), nullValue());
        } catch (ArangoDBException e) {
            assertThat(e.getResponseBody(), nullValue());
        }
    }

}
