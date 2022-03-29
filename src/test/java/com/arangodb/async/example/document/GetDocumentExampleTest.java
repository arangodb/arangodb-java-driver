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

package com.arangodb.async.example.document;

import com.arangodb.async.example.ExampleBase;
import com.arangodb.entity.BaseDocument;
import com.arangodb.entity.DocumentCreateEntity;
import com.arangodb.velocypack.VPackSlice;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;


import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * @author Mark Vollmary
 */
class GetDocumentExampleTest extends ExampleBase {

    private static String key = null;

    @BeforeAll
    static void before() throws InterruptedException, ExecutionException {
        final BaseDocument value = new BaseDocument();
        value.addAttribute("foo", "bar");
        final DocumentCreateEntity<BaseDocument> doc = collection.insertDocument(value).get();
        key = doc.getKey();
    }

    @Test
    void getAsBean() throws InterruptedException, ExecutionException {
        collection.getDocument(key, TestEntity.class)
                .whenComplete((doc, ex) -> {
                    assertThat(doc).isNotNull();
                    assertThat(doc.getFoo()).isEqualTo("bar");
                })
                .get();
    }

    @Test
    void getAsBaseDocument() throws InterruptedException, ExecutionException {
        collection.getDocument(key, BaseDocument.class)
                .whenComplete((doc, ex) -> {
                    assertThat(doc).isNotNull();
                    assertThat(doc.getAttribute("foo")).isNotNull();
                    assertThat(String.valueOf(doc.getAttribute("foo"))).isEqualTo("bar");
                })
                .get();
    }

    @Test
    void getAsVPack() throws InterruptedException, ExecutionException {
        collection.getDocument(key, VPackSlice.class)
                .whenComplete((doc, ex) -> {
                    assertThat(doc).isNotNull();
                    assertThat(doc.get("foo").isString()).isEqualTo(true);
                    assertThat(doc.get("foo").getAsString()).isEqualTo("bar");
                })
                .get();
    }

    @Test
    void getAsJson() throws InterruptedException, ExecutionException {
        collection.getDocument(key, String.class)
                .whenComplete((doc, ex) -> {
                    assertThat(doc).isNotNull();
                    assertThat(doc.contains("foo")).isEqualTo(true);
                    assertThat(doc.contains("bar")).isEqualTo(true);
                })
                .get();
    }

}
