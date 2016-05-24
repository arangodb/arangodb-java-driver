/*
 * Copyright (C) 2012,2013 tamtam180
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.arangodb;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.arangodb.entity.ImportResultEntity;
import com.arangodb.util.TestUtils;

/**
 * @author tamtam180 - kirscheless at gmail.com
 * 
 */
@SuppressWarnings("unchecked")
public class ArangoDriverImportTest extends BaseTest {

	private static final String UT_IMPORT_TEST = "ut-import-test";

	@Before
	public void setUp() {
		try {
			driver.deleteCollection(UT_IMPORT_TEST);
		} catch (final ArangoException e) {
		}
		try {
			driver.createCollection(UT_IMPORT_TEST);
		} catch (final ArangoException e) {
		}
	}

	@Test
	public void test_import_documents() throws ArangoException, IOException {

		final List<Station> stations = TestUtils.readStations();
		final ImportResultEntity result = driver.importDocuments(UT_IMPORT_TEST, stations);

		assertThat(result.getStatusCode(), is(201));
		assertThat(result.isError(), is(false));
		assertThat(result.getCreated(), is(632));
		assertThat(result.getErrors(), is(0));
		assertThat(result.getEmpty(), is(0));

	}

	@Test
	public void test_import_documents_404() throws ArangoException, IOException {

		try {
			driver.deleteCollection(UT_IMPORT_TEST);
		} catch (final ArangoException e) {
		}

		final List<Station> stations = TestUtils.readStations();
		try {
			driver.importDocuments(UT_IMPORT_TEST, stations);
			fail();
		} catch (final ArangoException e) {
			assertThat(e.getCode(), is(404));
			assertThat(e.getErrorNumber(), is(1203));
		}

	}

	@Test
	public void test_import_xsv() throws ArangoException, IOException {

		final List<List<?>> values = new ArrayList<List<?>>();
		values.add(Arrays.asList("firstName", "lastName", "age", "gender"));
		values.add(Arrays.asList("Joe", "Public", 42, "male"));
		values.add(Arrays.asList("Jane", "Doe", 31, "female"));

		final ImportResultEntity result = driver.importDocumentsByHeaderValues(UT_IMPORT_TEST, values);

		assertThat(result.getStatusCode(), is(201));
		assertThat(result.isError(), is(false));
		assertThat(result.getCreated(), is(2));
		assertThat(result.getErrors(), is(0));
		assertThat(result.getEmpty(), is(0));

	}

	@Test
	public void test_import_xsv_errors() throws ArangoException, IOException {

		final List<List<?>> values = new ArrayList<List<?>>();
		values.add(Arrays.asList("firstName", "lastName", "age", "gender"));
		values.add(Arrays.asList("Joe", "Public", 42, "male", 10)); // error
		values.add(Arrays.asList("Jane", "Doe", 31, "female"));

		final ImportResultEntity result = driver.importDocumentsByHeaderValues(UT_IMPORT_TEST, values);

		assertThat(result.getStatusCode(), is(201));
		assertThat(result.isError(), is(false));
		assertThat(result.getCreated(), is(1));
		assertThat(result.getErrors(), is(1));
		assertThat(result.getEmpty(), is(0));

	}

	@Test
	public void test_import_xsv_404() throws ArangoException, IOException {

		try {
			driver.deleteCollection(UT_IMPORT_TEST);
		} catch (final ArangoException e) {
		}

		final List<List<?>> values = new ArrayList<List<?>>();
		values.add(Arrays.asList("firstName", "lastName", "age", "gender"));
		values.add(Arrays.asList("Joe", "Public", 42, "male", 10)); // error
		values.add(Arrays.asList("Jane", "Doe", 31, "female"));

		try {
			driver.importDocuments(UT_IMPORT_TEST, values);
			fail();
		} catch (final ArangoException e) {
			assertThat(e.getCode(), is(404));
			assertThat(e.getErrorNumber(), is(1203));
		}

	}

}
