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

import com.arangodb.ArangoDB.Builder;
import com.arangodb.entity.BaseDocument;
import com.arangodb.internal.ArangoRequestParam;
import com.arangodb.velocystream.Response;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * @author Mark Vollmary
 */
@RunWith(Parameterized.class)
public class ArangoRouteTest extends BaseTest {

    public ArangoRouteTest(final Builder builder) {
        super(builder);
    }

    @Test
    public void get() {
        final Response res = db.route("/_api/version").get();
        assertThat(res.getBody().get("version").isString(), is(true));
    }

    @Test
    public void withHeader() {
        final ArangoCollection collection = db.collection("route-test-col");
        try {
            collection.create();
            final BaseDocument doc = new BaseDocument();
            collection.insertDocument(doc);
            db.route("/_api/document", doc.getId()).withHeader(ArangoRequestParam.IF_NONE_MATCH, doc.getRevision())
                    .get();
            fail();
        } catch (final ArangoDBException e) {
            assertThat(e.getResponseCode(), is(304));
        } finally {
            collection.drop();
        }
    }

    @Test
    public void withParentHeader() {
        final ArangoCollection collection = db.collection("route-test-col");
        try {
            collection.create();
            final BaseDocument doc = new BaseDocument();
            collection.insertDocument(doc);
            db.route("/_api/document").withHeader(ArangoRequestParam.IF_NONE_MATCH, doc.getRevision())
                    .route(doc.getId()).get();
            fail();
        } catch (final ArangoDBException e) {
            assertThat(e.getResponseCode(), is(304));
        } finally {
            collection.drop();
        }
    }

}
