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
import com.arangodb.util.RawJson;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * @author Mark Vollmary
 */
class InsertDocumentExampleTest extends ExampleBase {

    @Test
    void insertBean() throws ExecutionException, InterruptedException {
        collection.insertDocument(new TestEntity("bar"))
                .whenComplete((doc, ex) -> assertThat(doc.getKey()).isNotNull())
                .get();
    }

    @Test
    void insertBaseDocument() throws ExecutionException, InterruptedException {
        final BaseDocument value = new BaseDocument(UUID.randomUUID().toString());
        value.addAttribute("foo", "bar");
        collection.insertDocument(value)
                .whenComplete((doc, ex) -> assertThat(doc.getKey()).isNotNull())
                .get();
    }

    @Test
    void insertJsonNode() throws ExecutionException, InterruptedException {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode node = mapper.createObjectNode();
        node.put("foo", "bar");
        collection.insertDocument(node)
                .whenComplete((doc, ex) -> assertThat(doc.getKey()).isNotNull())
                .get();
    }

    @Test
    void insertJson() throws ExecutionException, InterruptedException {
        collection.insertDocument(RawJson.of("{\"foo\":\"bar\"}"))
                .whenComplete((doc, ex) -> assertThat(doc.getKey()).isNotNull())
                .get();
    }

}
