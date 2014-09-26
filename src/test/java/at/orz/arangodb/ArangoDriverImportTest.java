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

package at.orz.arangodb;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import at.orz.arangodb.entity.ImportResultEntity;
import at.orz.arangodb.util.TestUtils;

/**
 * @author tamtam180 - kirscheless at gmail.com
 * 
 */
public class ArangoDriverImportTest extends BaseTest {

	public ArangoDriverImportTest(ArangoConfigure configure, ArangoDriver driver) {
		super(configure, driver);
	}

	@Test
	public void test_import_documents() throws ArangoException, IOException {

		List<Station> stations = TestUtils.readStations();
		ImportResultEntity result = driver.importDocuments("ut-import-test", true, stations);

		assertThat(result.getStatusCode(), is(201));
		assertThat(result.isError(), is(false));
		assertThat(result.getCreated(), is(632));
		assertThat(result.getErrors(), is(0));
		assertThat(result.getEmpty(), is(0));

	}

	@Test
	public void test_import_documents_404() throws ArangoException, IOException {

		try {
			driver.deleteCollection("ut-import-test");
		} catch (ArangoException e) {
		}

		List<Station> stations = TestUtils.readStations();
		try {
			driver.importDocuments("ut-import-test", false, stations);
			fail();
		} catch (ArangoException e) {
			assertThat(e.getCode(), is(404));
			assertThat(e.getErrorNumber(), is(1203));
		}

	}

	@Test
	public void test_import_xsv() throws ArangoException, IOException {

		List<List<?>> values = new ArrayList<List<?>>();
		values.add(Arrays.asList("firstName", "lastName", "age", "gender"));
		values.add(Arrays.asList("Joe", "Public", 42, "male"));
		values.add(Arrays.asList("Jane", "Doe", 31, "female"));

		ImportResultEntity result = driver.importDocumentsByHeaderValues("ut-import-test", true, values);

		assertThat(result.getStatusCode(), is(201));
		assertThat(result.isError(), is(false));
		assertThat(result.getCreated(), is(2));
		assertThat(result.getErrors(), is(0));
		assertThat(result.getEmpty(), is(0));

	}

	@Test
	public void test_import_xsv_errors() throws ArangoException, IOException {

		List<List<?>> values = new ArrayList<List<?>>();
		values.add(Arrays.asList("firstName", "lastName", "age", "gender"));
		values.add(Arrays.asList("Joe", "Public", 42, "male", 10)); // error
		values.add(Arrays.asList("Jane", "Doe", 31, "female"));

		ImportResultEntity result = driver.importDocumentsByHeaderValues("ut-import-test", true, values);

		assertThat(result.getStatusCode(), is(201));
		assertThat(result.isError(), is(false));
		assertThat(result.getCreated(), is(1));
		assertThat(result.getErrors(), is(1));
		assertThat(result.getEmpty(), is(0));

	}

	@Test
	public void test_import_xsv_404() throws ArangoException, IOException {

		try {
			driver.deleteCollection("ut-import-test");
		} catch (ArangoException e) {
		}

		List<List<?>> values = new ArrayList<List<?>>();
		values.add(Arrays.asList("firstName", "lastName", "age", "gender"));
		values.add(Arrays.asList("Joe", "Public", 42, "male", 10)); // error
		values.add(Arrays.asList("Jane", "Doe", 31, "female"));

		try {
			driver.importDocuments("ut-import-test", false, values);
			fail();
		} catch (ArangoException e) {
			assertThat(e.getCode(), is(404));
			assertThat(e.getErrorNumber(), is(1203));
		}

	}

}
