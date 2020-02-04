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

import com.arangodb.internal.util.DocumentUtil;
import org.junit.Test;

/**
 * @author Mark Vollmary
 */
public class ArangoDocumentUtilTest {

    @Test
    public void validateDocumentKeyValid() {
        checkDocumentKey("1test");
        checkDocumentKey("test1");
        checkDocumentKey("test-1");
        checkDocumentKey("test_1");
        checkDocumentKey("_test");
    }

    @Test(expected = ArangoDBException.class)
    public void validateDocumentKeyInvalidSlash() {
        checkDocumentKey("test/test");
    }

    @Test(expected = ArangoDBException.class)
    public void validateDocumentKeyEmpty() {
        checkDocumentKey("");
    }

    private void checkDocumentKey(final String key) throws ArangoDBException {
        DocumentUtil.validateDocumentKey(key);
    }

    @Test
    public void validateDocumentIdValid() {
        checkDocumentId("1test/1test");
        checkDocumentId("test1/test1");
        checkDocumentId("test-1/test-1");
        checkDocumentId("test_1/test_1");
        checkDocumentId("_test/_test");
    }

    @Test(expected = ArangoDBException.class)
    public void validateDocumentIdInvalidWithoutSlash() {
        checkDocumentId("test");
    }

    @Test(expected = ArangoDBException.class)
    public void validateDocumentIdEmpty() {
        checkDocumentId("");
    }

    private void checkDocumentId(final String id) throws ArangoDBException {
        DocumentUtil.validateDocumentId(id);
    }
}
