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
import com.arangodb.velocypack.VPackBuilder;
import com.arangodb.velocypack.VPackSlice;
import com.arangodb.velocypack.ValueType;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * @author Mark Vollmary
 */
public class InsertDocumentExample extends ExampleBase {

    @Test
    public void insertBean() {
        final DocumentCreateEntity<TestEntity> doc = collection.insertDocument(new TestEntity("bar"));
        assertThat(doc.getKey(), is(notNullValue()));
    }

    @Test
    public void insertBaseDocument() {
        final BaseDocument value = new BaseDocument();
        value.addAttribute("foo", "bar");
        final DocumentCreateEntity<BaseDocument> doc = collection.insertDocument(value);
        assertThat(doc.getKey(), is(notNullValue()));
    }

    @Test
    public void insertVPack() {
        final VPackBuilder builder = new VPackBuilder();
        builder.add(ValueType.OBJECT).add("foo", "bar").close();
        final DocumentCreateEntity<VPackSlice> doc = collection.insertDocument(builder.slice());
        assertThat(doc.getKey(), is(notNullValue()));
    }

    @Test
    public void insertJson() {
        final DocumentCreateEntity<String> doc = collection.insertDocument("{\"foo\":\"bar\"}");
        assertThat(doc.getKey(), is(notNullValue()));
    }

}
