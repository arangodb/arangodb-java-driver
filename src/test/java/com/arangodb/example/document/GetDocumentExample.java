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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Optional;

import org.json.simple.parser.ParseException;
import org.junit.BeforeClass;
import org.junit.Test;

import com.arangodb.entity.BaseDocument;
import com.arangodb.entity.DocumentCreateEntity;
import com.arangodb.example.ExampleBase;
import com.arangodb.velocypack.VPackSlice;

/**
 * @author Mark - mark at arangodb.com
 *
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
		final Optional<TestEntity> doc = collection.getDocument(key, TestEntity.class);
		assertThat(doc.isPresent(), is(true));
		assertThat(doc.get().getFoo(), is("bar"));
	}

	@Test
	public void getAsBaseDocument() {
		final Optional<BaseDocument> doc = collection.getDocument(key, BaseDocument.class);
		assertThat(doc.isPresent(), is(true));
		assertThat(doc.get().getAttribute("foo"), is("bar"));
	}

	@Test
	public void getAsVPack() {
		final Optional<VPackSlice> doc = collection.getDocument(key, VPackSlice.class);
		assertThat(doc.isPresent(), is(true));
		assertThat(doc.get().get("foo").isString(), is(true));
		assertThat(doc.get().get("foo").getAsString(), is("bar"));
	}

	@Test
	public void getAsJson() throws ParseException {
		final Optional<String> doc = collection.getDocument(key, String.class);
		assertThat(doc.isPresent(), is(true));
		assertThat(doc.get().contains("foo"), is(true));
		assertThat(doc.get().contains("bar"), is(true));
	}

}
