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
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

/**
 * @author Mark Vollmary
 * @author Michele Rastelli
 */
class ArangoDocumentUtilTest {

    @Test
    void validateDocumentKeyValid() {
        checkDocumentKey("1test");
        checkDocumentKey("test1");
        checkDocumentKey("test-1");
        checkDocumentKey("test_1");
        checkDocumentKey("_test");
    }

    @Test
    void validateDocumentKeyInvalidSlash() {
        Throwable thrown = catchThrowable(() -> checkDocumentKey("test/test"));
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
    }

    @Test
    void validateDocumentKeyEmpty() {
        Throwable thrown = catchThrowable(() -> checkDocumentKey(""));
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
    }

    private void checkDocumentKey(final String key) throws ArangoDBException {
        DocumentUtil.validateDocumentKey(key);
    }

    @Test
    void validateDocumentIdValid() {
        checkDocumentId("1test/1test");
        checkDocumentId("test1/test1");
        checkDocumentId("test-1/test-1");
        checkDocumentId("test_1/test_1");
        checkDocumentId("_test/_test");
    }

    @Test
    void validateDocumentIdInvalidWithoutSlash() {
        Throwable thrown = catchThrowable(() -> checkDocumentId("test"));
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
    }

    @Test
    void validateDocumentIdEmpty() {
        Throwable thrown = catchThrowable(() -> checkDocumentId(""));
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
    }

    private void checkDocumentId(final String id) throws ArangoDBException {
        DocumentUtil.validateDocumentId(id);
    }
}
