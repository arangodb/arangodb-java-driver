package com.arangodb.internal;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.arangodb.data.TestEntity;
import com.arangodb.entity.DocumentField.Type;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class DocumentCacheTest {

	@Test
	public void setValues() {
		final DocumentCache cache = new DocumentCache();
		final TestEntity doc = new TestEntity();

		assertThat(doc.getId(), is(nullValue()));
		assertThat(doc.getKey(), is(nullValue()));
		assertThat(doc.getRev(), is(nullValue()));

		final Map<Type, String> values = new HashMap<>();
		values.put(Type.ID, "testId");
		values.put(Type.KEY, "testKey");
		values.put(Type.REV, "testRev");
		cache.setValues(doc, values);

		assertThat(doc.getId(), is("testId"));
		assertThat(doc.getKey(), is("testKey"));
		assertThat(doc.getRev(), is("testRev"));
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
