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

package com.arangodb.internal;

import com.arangodb.entity.BaseDocument;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * @author Mark Vollmary
 */
class DocumentCacheTest {

    @Test
    void setValues() {
        final DocumentCache cache = new DocumentCache();
        final BaseDocument doc = new BaseDocument();

        assertThat(doc.getId()).isNull();
        assertThat(doc.getKey()).isNull();
        assertThat(doc.getRevision()).isNull();

        final Map<String, String> values = new HashMap<>();
        values.put(DocumentFields.ID, "testId");
        values.put(DocumentFields.KEY, "testKey");
        values.put(DocumentFields.REV, "testRev");
        cache.setValues(doc, values);

        assertThat(doc.getId()).isEqualTo("testId");
        assertThat(doc.getKey()).isEqualTo("testKey");
        assertThat(doc.getRevision()).isEqualTo("testRev");
    }

    @Test
    void setValuesMap() {
        final DocumentCache cache = new DocumentCache();
        final Map<String, String> map = new HashMap<>();

        final Map<String, String> values = new HashMap<>();
        values.put(DocumentFields.ID, "testId");
        values.put(DocumentFields.KEY, "testKey");
        values.put(DocumentFields.REV, "testRev");
        cache.setValues(map, values);

        assertThat(map.isEmpty()).isTrue();
    }
}
