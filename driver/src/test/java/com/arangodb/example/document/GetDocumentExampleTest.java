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

package com.arangodb.example.document;

import com.arangodb.entity.BaseDocument;
import com.arangodb.example.ExampleBase;
import com.arangodb.internal.serde.SerdeContextImpl;
import com.arangodb.util.RawBytes;
import com.arangodb.util.RawJson;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * @author Mark Vollmary
 */
class GetDocumentExampleTest extends ExampleBase {

    private static String key = null;

    @BeforeAll
    static void before() {
        final BaseDocument value = new BaseDocument(UUID.randomUUID().toString());
        value.addAttribute("foo", "bar");
        key = collection.insertDocument(value).getKey();
    }

    @Test
    void getAsBean() {
        final TestEntity doc = collection.getDocument(key, TestEntity.class);
        assertThat(doc).isNotNull();
        assertThat(doc.getFoo()).isEqualTo("bar");
    }

    @Test
    void getAsBaseDocument() {
        final BaseDocument doc = collection.getDocument(key, BaseDocument.class);
        assertThat(doc).isNotNull();
        assertThat(doc.getAttribute("foo")).isNotNull();
        assertThat(String.valueOf(doc.getAttribute("foo"))).isEqualTo("bar");
    }

    @Test
    void getAsMap() {
        final Map<String, Object> doc = collection.getDocument(key, Map.class);
        assertThat(doc).isNotNull();
        assertThat(doc.get("foo")).isNotNull();
        assertThat(String.valueOf(doc.get("foo"))).isEqualTo("bar");
    }

    @Test
    void getAsJsonNode() {
        final JsonNode doc = collection.getDocument(key, JsonNode.class);
        assertThat(doc).isNotNull();
        assertThat(doc.get("foo").isTextual()).isTrue();
        assertThat(doc.get("foo").asText()).isEqualTo("bar");
    }

    @Test
    void getAsJson() {
        final RawJson doc = collection.getDocument(key, RawJson.class);
        assertThat(doc.get()).isNotNull()
                .contains("foo")
                .contains("bar");
    }

    @Test
    void getAsBytes() {
        final RawBytes doc = collection.getDocument(key, RawBytes.class);
        assertThat(doc.get()).isNotNull();
        Map<String, Object> mapDoc = collection.getSerde().deserializeUserData(doc.get(), Map.class, SerdeContextImpl.EMPTY);
        assertThat(mapDoc).containsEntry("foo", "bar");
    }

}
