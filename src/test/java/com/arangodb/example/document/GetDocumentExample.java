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
import com.arangodb.entity.DocumentCreateEntity;
import com.arangodb.example.ExampleBase;
import com.arangodb.velocypack.VPackSlice;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * @author Mark Vollmary
 */
public class GetDocumentExample extends ExampleBase {

    private static String key = null;

    @BeforeClass
    public static void before() {
        final BaseDocument value = new BaseDocument();
        value.addAttribute("foo", "bar");
        final DocumentCreateEntity<BaseDocument> doc = collection.insertDocument(value);
        key = doc.getKey();
    }

    @Test
    public void getAsBean() {
        final TestEntity doc = collection.getDocument(key, TestEntity.class);
        assertThat(doc, is(notNullValue()));
        assertThat(doc.getFoo(), is("bar"));
    }

    @Test
    public void getAsBaseDocument() {
        final BaseDocument doc = collection.getDocument(key, BaseDocument.class);
        assertThat(doc, is(notNullValue()));
        assertThat(doc.getAttribute("foo"), is(notNullValue()));
        assertThat(String.valueOf(doc.getAttribute("foo")), is("bar"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void getAsMap() {
        final Map<String, Object> doc = collection.getDocument(key, Map.class);
        assertThat(doc, is(notNullValue()));
        assertThat(doc.get("foo"), is(notNullValue()));
        assertThat(String.valueOf(doc.get("foo")), is("bar"));
    }

    @Test
    public void getAsVPack() {
        final VPackSlice doc = collection.getDocument(key, VPackSlice.class);
        assertThat(doc, is(notNullValue()));
        assertThat(doc.get("foo").isString(), is(true));
        assertThat(doc.get("foo").getAsString(), is("bar"));
    }

    @Test
    public void getAsJson() {
        final String doc = collection.getDocument(key, String.class);
        assertThat(doc, is(notNullValue()));
        assertThat(doc.contains("foo"), is(true));
        assertThat(doc.contains("bar"), is(true));
    }

}
