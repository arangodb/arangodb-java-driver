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
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.concurrent.ExecutionException;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * @author Mark Vollmary
 */
public class GetDocumentExample extends ExampleBase {

    private static String key = null;

    @BeforeClass
    public static void before() throws InterruptedException, ExecutionException {
        final BaseDocument value = new BaseDocument();
        value.addAttribute("foo", "bar");
        final DocumentCreateEntity<BaseDocument> doc = collection.insertDocument(value).get();
        key = doc.getKey();
    }

    @Test
    public void getAsBean() throws InterruptedException, ExecutionException {
        collection.getDocument(key, TestEntity.class)
                .whenComplete((doc, ex) -> {
                    assertThat(doc, is(notNullValue()));
                    assertThat(doc.getFoo(), is("bar"));
                })
                .get();
    }

    @Test
    public void getAsBaseDocument() throws InterruptedException, ExecutionException {
        collection.getDocument(key, BaseDocument.class)
                .whenComplete((doc, ex) -> {
                    assertThat(doc, is(notNullValue()));
                    assertThat(doc.getAttribute("foo"), is(notNullValue()));
                    assertThat(String.valueOf(doc.getAttribute("foo")), is("bar"));
                })
                .get();
    }

    @Test
    public void getAsVPack() throws InterruptedException, ExecutionException {
        collection.getDocument(key, VPackSlice.class)
                .whenComplete((doc, ex) -> {
                    assertThat(doc, is(notNullValue()));
                    assertThat(doc.get("foo").isString(), is(true));
                    assertThat(doc.get("foo").getAsString(), is("bar"));
                })
                .get();
    }

    @Test
    public void getAsJson() throws InterruptedException, ExecutionException {
        collection.getDocument(key, String.class)
                .whenComplete((doc, ex) -> {
                    assertThat(doc, is(notNullValue()));
                    assertThat(doc.contains("foo"), is(true));
                    assertThat(doc.contains("bar"), is(true));
                })
                .get();
    }

}
