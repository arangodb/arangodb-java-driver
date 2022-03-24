/*
 * DISCLAIMER
 *
 * Copyright 2018 ArangoDB GmbH, Cologne, Germany
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
import com.arangodb.internal.ArangoRequestParam;
import com.arangodb.velocystream.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

/**
 * @author Mark Vollmary
 * @author Michele Rastelli
 */
class ArangoRouteTest extends BaseJunit5 {

    private static final String COLLECTION_NAME = "ArangoRouteTest_collection";

    @BeforeAll
    static void init() {
        initCollections(COLLECTION_NAME);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void get(ArangoDatabase db) {
        final Response res = db.route("/_api/version").get();
        assertThat(res.getBody().get("version").isString()).isTrue();
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void withHeader(ArangoDatabase db) {
        final BaseDocument doc = new BaseDocument();
        db.collection(COLLECTION_NAME).insertDocument(doc);
        Throwable thrown = catchThrowable(() ->
                db.route("/_api/document", doc.getId()).withHeader(ArangoRequestParam.IF_NONE_MATCH, doc.getRevision()).get());
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
        ArangoDBException e = (ArangoDBException) thrown;
        assertThat(e.getResponseCode()).isEqualTo(304);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void withParentHeader(ArangoDatabase db) {
        final BaseDocument doc = new BaseDocument();
        db.collection(COLLECTION_NAME).insertDocument(doc);
        Throwable thrown = catchThrowable(() ->
                db.route("/_api/document").withHeader(ArangoRequestParam.IF_NONE_MATCH, doc.getRevision()).route(doc.getId()).get());
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
        ArangoDBException e = (ArangoDBException) thrown;
        assertThat(e.getResponseCode()).isEqualTo(304);
    }

}
