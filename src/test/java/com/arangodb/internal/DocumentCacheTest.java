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

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.arangodb.entity.BaseDocument;
import com.arangodb.entity.DocumentField.Type;

/**
 * @author Mark Vollmary
 *
 */
public class DocumentCacheTest {

	@Test
	public void setValues() {
		final DocumentCache cache = new DocumentCache();
		final BaseDocument doc = new BaseDocument();

		assertThat(doc.getId(), is(nullValue()));
		assertThat(doc.getKey(), is(nullValue()));
		assertThat(doc.getRevision(), is(nullValue()));

		final Map<Type, String> values = new HashMap<>();
		values.put(Type.ID, "testId");
		values.put(Type.KEY, "testKey");
		values.put(Type.REV, "testRev");
		cache.setValues(doc, values);

		assertThat(doc.getId(), is("testId"));
		assertThat(doc.getKey(), is("testKey"));
		assertThat(doc.getRevision(), is("testRev"));
	}

	@Test
	public void setValuesMap() {
		final DocumentCache cache = new DocumentCache();
		final Map<String, String> map = new HashMap<>();

		final Map<Type, String> values = new HashMap<>();
		values.put(Type.ID, "testId");
		values.put(Type.KEY, "testKey");
		values.put(Type.REV, "testRev");
		cache.setValues(map, values);

		assertThat(map.isEmpty(), is(true));
	}
}
