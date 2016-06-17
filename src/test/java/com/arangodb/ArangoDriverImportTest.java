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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.arangodb.entity.BaseDocument;
import com.arangodb.entity.CollectionOptions;
import com.arangodb.entity.CollectionType;
import com.arangodb.entity.ImportResultEntity;
import com.arangodb.util.ImportOptions;
import com.arangodb.util.ImportOptions.OnDuplicate;
import com.arangodb.util.ImportOptionsJson;
import com.arangodb.util.ImportOptionsRaw;
import com.arangodb.util.ImportOptionsRaw.ImportType;
import com.arangodb.util.TestUtils;

/**
 * @author tamtam180 - kirscheless at gmail.com
 * 
 */
@SuppressWarnings("unchecked")
public class ArangoDriverImportTest extends BaseTest {

	private static final String UT_IMPORT_TEST = "ut-import-test";
	private static final String UT_IMPORT_TEST_EDGE = "ut-import-test-edge";

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
		try {
			driver.deleteCollection(UT_IMPORT_TEST_EDGE);
		} catch (final ArangoException e) {
		}
		try {
			CollectionOptions options = new CollectionOptions();
			options.setType(CollectionType.EDGE);
			driver.createCollection(UT_IMPORT_TEST_EDGE, options);
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

	@Test
	public void test_import_updateOnDuplicate() throws ArangoException, IOException {

		Collection<BaseDocument> docs = new ArrayList<BaseDocument>();
		for (int i = 0; i < 100; i++) {
			BaseDocument doc = new BaseDocument();
			doc.setDocumentKey(String.valueOf(i));
			docs.add(doc);
		}
		ImportOptionsJson options = new ImportOptionsJson();
		{
			final ImportResultEntity result = driver.importDocuments(UT_IMPORT_TEST, docs, options);
			assertThat(result.getStatusCode(), is(201));
			assertThat(result.isError(), is(false));
			assertThat(result.getCreated(), is(docs.size()));
		}
		options.setOnDuplicate(OnDuplicate.UPDATE);
		{
			final ImportResultEntity result = driver.importDocuments(UT_IMPORT_TEST, docs, options);
			assertThat(result.getStatusCode(), is(201));
			assertThat(result.isError(), is(false));
			assertThat(result.getCreated(), is(0));
			assertThat(result.getUpdated(), is(docs.size()));
		}
	}

	@Test
	public void test_import_ignoreOnDuplicate() throws ArangoException, IOException {

		Collection<BaseDocument> docs = new ArrayList<BaseDocument>();
		for (int i = 0; i < 100; i++) {
			BaseDocument doc = new BaseDocument();
			doc.setDocumentKey(String.valueOf(i));
			docs.add(doc);
		}
		ImportOptionsJson options = new ImportOptionsJson();
		{
			final ImportResultEntity result = driver.importDocuments(UT_IMPORT_TEST, docs, options);
			assertThat(result.getStatusCode(), is(201));
			assertThat(result.isError(), is(false));
			assertThat(result.getCreated(), is(docs.size()));
		}
		options.setOnDuplicate(OnDuplicate.IGNORE);
		{
			final ImportResultEntity result = driver.importDocuments(UT_IMPORT_TEST, docs, options);
			assertThat(result.getStatusCode(), is(201));
			assertThat(result.isError(), is(false));
			assertThat(result.getCreated(), is(0));
			assertThat(result.getIgnored(), is(docs.size()));
		}
	}

	@Test
	public void test_import_replaceOnDuplicate() throws ArangoException, IOException {

		Collection<BaseDocument> docs = new ArrayList<BaseDocument>();
		for (int i = 0; i < 100; i++) {
			BaseDocument doc = new BaseDocument();
			doc.setDocumentKey(String.valueOf(i));
			docs.add(doc);
		}
		ImportOptionsJson options = new ImportOptionsJson();
		{
			final ImportResultEntity result = driver.importDocuments(UT_IMPORT_TEST, docs, options);
			assertThat(result.getStatusCode(), is(201));
			assertThat(result.isError(), is(false));
			assertThat(result.getCreated(), is(docs.size()));
		}
		options.setOnDuplicate(OnDuplicate.REPLACE);
		{
			final ImportResultEntity result = driver.importDocuments(UT_IMPORT_TEST, docs, options);
			assertThat(result.getStatusCode(), is(201));
			assertThat(result.isError(), is(false));
			assertThat(result.getCreated(), is(0));
			assertThat(result.getUpdated(), is(docs.size()));
		}
	}

	@Test
	public void test_import_errorOnDuplicate() throws ArangoException, IOException {

		Collection<BaseDocument> docs = new ArrayList<BaseDocument>();
		for (int i = 0; i < 100; i++) {
			BaseDocument doc = new BaseDocument();
			doc.setDocumentKey(String.valueOf(i));
			docs.add(doc);
		}
		ImportOptionsJson options = new ImportOptionsJson();
		{
			final ImportResultEntity result = driver.importDocuments(UT_IMPORT_TEST, docs, options);
			assertThat(result.getStatusCode(), is(201));
			assertThat(result.isError(), is(false));
			assertThat(result.getCreated(), is(docs.size()));
		}
		options.setOnDuplicate(OnDuplicate.ERROR);
		{
			final ImportResultEntity result = driver.importDocuments(UT_IMPORT_TEST, docs, options);
			assertThat(result.getStatusCode(), is(201));
			assertThat(result.isError(), is(false));
			assertThat(result.getCreated(), is(0));
			assertThat(result.getErrors(), is(docs.size()));
		}
	}

	@Test
	public void test_import_overwrite() throws ArangoException, IOException {
		ImportOptionsJson options = new ImportOptionsJson();
		{
			Collection<BaseDocument> docs = new ArrayList<BaseDocument>();
			for (int i = 0; i < 100; i++) {
				BaseDocument doc = new BaseDocument();
				doc.setDocumentKey(String.valueOf(i));
				docs.add(doc);
			}
			{
				final ImportResultEntity result = driver.importDocuments(UT_IMPORT_TEST, docs, options);
				assertThat(result.getStatusCode(), is(201));
				assertThat(result.isError(), is(false));
				assertThat(result.getCreated(), is(docs.size()));
				assertThat(docs.size(), is(driver.getDocuments(UT_IMPORT_TEST).size()));
			}
		}
		{
			Collection<BaseDocument> docs2 = new ArrayList<BaseDocument>();
			for (int i = 0; i < 50; i++) {
				BaseDocument doc = new BaseDocument();
				doc.setDocumentKey(String.valueOf(-i));
				docs2.add(doc);
			}
			options.setOverwrite(true);
			{
				final ImportResultEntity result = driver.importDocuments(UT_IMPORT_TEST, docs2, options);
				assertThat(result.getStatusCode(), is(201));
				assertThat(result.isError(), is(false));
				assertThat(result.getCreated(), is(docs2.size()));
				assertThat(docs2.size(), is(driver.getDocuments(UT_IMPORT_TEST).size()));
			}
		}
	}

	@Test
	public void test_import_from_to_Prefix() throws ArangoException, IOException {
		ImportOptionsJson options = new ImportOptionsJson();
		{
			Collection<BaseDocument> docs = new ArrayList<BaseDocument>();
			for (int i = 0; i < 100; i++) {
				BaseDocument doc = new BaseDocument();
				doc.setDocumentKey(String.valueOf(i));
				docs.add(doc);
			}
			{
				final ImportResultEntity result = driver.importDocuments(UT_IMPORT_TEST, docs, options);
				assertThat(result.getStatusCode(), is(201));
				assertThat(result.isError(), is(false));
				assertThat(result.getCreated(), is(docs.size()));
			}
		}
		{
			Collection<Map<String, Object>> edgeDocs = new ArrayList<Map<String, Object>>();
			for (int i = 0; i < 100; i++) {
				final HashMap<String, Object> doc = new HashMap<String, Object>();
				doc.put(BaseDocument.KEY, String.valueOf(i));
				doc.put(BaseDocument.FROM, String.valueOf(i));
				doc.put(BaseDocument.TO, String.valueOf(i));
				edgeDocs.add(doc);
			}
			options.setFromPrefix(UT_IMPORT_TEST);
			options.setToPrefix(UT_IMPORT_TEST);
			final ImportResultEntity result = driver.importDocuments(UT_IMPORT_TEST_EDGE, edgeDocs, options);
			assertThat(result.getStatusCode(), is(201));
			assertThat(result.isError(), is(false));
			assertThat(result.getCreated(), is(edgeDocs.size()));
		}
	}

	@Test
	public void test_import_from_to_Prefix_with_errors_details() throws ArangoException, IOException {
		ImportOptionsJson options = new ImportOptionsJson();
		{
			Collection<BaseDocument> docs = new ArrayList<BaseDocument>();
			for (int i = 0; i < 100; i++) {
				BaseDocument doc = new BaseDocument();
				doc.setDocumentKey(String.valueOf(i));
				docs.add(doc);
			}
			{
				final ImportResultEntity result = driver.importDocuments(UT_IMPORT_TEST, docs, options);
				assertThat(result.getStatusCode(), is(201));
				assertThat(result.isError(), is(false));
				assertThat(result.getCreated(), is(docs.size()));
			}
		}
		{
			Collection<Map<String, Object>> edgeDocs = new ArrayList<Map<String, Object>>();
			for (int i = 0; i < 100; i++) {
				final HashMap<String, Object> doc = new HashMap<String, Object>();
				doc.put(BaseDocument.KEY, String.valueOf(i));
				// doc.put(BaseDocument.FROM, String.valueOf(i));
				doc.put(BaseDocument.TO, String.valueOf(i));
				edgeDocs.add(doc);
			}
			options.setFromPrefix(UT_IMPORT_TEST);
			options.setToPrefix(UT_IMPORT_TEST);
			options.setDetails(true);
			final ImportResultEntity result = driver.importDocuments(UT_IMPORT_TEST_EDGE, edgeDocs, options);
			assertThat(result.getStatusCode(), is(201));
			assertThat(result.isError(), is(false));
			assertThat(result.getErrors(), is(edgeDocs.size()));
			assertThat(result.getDetails().size(), is(edgeDocs.size()));
		}
	}

	@Test
	public void test_import_rawList() throws ArangoException {
		String values = "[{\"_key\":\"a\"},{\"_key\":\"b\"}]";
		ImportOptionsRaw importOptionsRaw = new ImportOptionsRaw(ImportType.LIST);
		final ImportResultEntity result = driver.importDocumentsRaw(UT_IMPORT_TEST, values, importOptionsRaw);
		assertThat(result.getStatusCode(), is(201));
		assertThat(result.isError(), is(false));
		assertThat(result.getCreated(), is(2));
	}

	@Test
	public void test_import_rawDocuments() throws ArangoException {
		String values = "{\"_key\":\"a\"}\n{\"_key\":\"b\"}";
		ImportOptionsRaw importOptionsRaw = new ImportOptionsRaw(ImportType.DOCUMENTS);
		final ImportResultEntity result = driver.importDocumentsRaw(UT_IMPORT_TEST, values, importOptionsRaw);
		assertThat(result.getStatusCode(), is(201));
		assertThat(result.isError(), is(false));
		assertThat(result.getCreated(), is(2));
	}

	@Test
	public void test_import_rawAutoList() throws ArangoException {
		String values = "[{\"_key\":\"a\"},{\"_key\":\"b\"}]";
		ImportOptionsRaw importOptionsRaw = new ImportOptionsRaw(ImportType.LIST);
		final ImportResultEntity result = driver.importDocumentsRaw(UT_IMPORT_TEST, values, importOptionsRaw);
		assertThat(result.getStatusCode(), is(201));
		assertThat(result.isError(), is(false));
		assertThat(result.getCreated(), is(2));
	}

	@Test
	public void test_import_rawAutoDocuments() throws ArangoException {
		String values = "{\"_key\":\"a\"}\n{\"_key\":\"b\"}";
		ImportOptionsRaw importOptionsRaw = new ImportOptionsRaw(ImportType.AUTO);
		final ImportResultEntity result = driver.importDocumentsRaw(UT_IMPORT_TEST, values, importOptionsRaw);
		assertThat(result.getStatusCode(), is(201));
		assertThat(result.isError(), is(false));
		assertThat(result.getCreated(), is(2));
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
		assertThat(result.getUpdated(), is(0));
		assertThat(result.getIgnored(), is(0));
		assertThat(result.getDetails().size(), is(0));
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
		assertThat(result.getUpdated(), is(0));
		assertThat(result.getIgnored(), is(0));
		assertThat(result.getDetails().size(), is(0));
	}

	@Test
	public void test_import_xsv_raw() throws ArangoException, IOException {

		final String values = "[\"firstName\",\"lastName\",\"age\",\"gender\"]\n[\"Joe\",\"Public\",42,\"male\"]\n[\"Jane\",\"Doe\",31,\"female\"]";

		final ImportResultEntity result = driver.importDocumentsByHeaderValuesRaw(UT_IMPORT_TEST, values,
			new ImportOptions());

		assertThat(result.getStatusCode(), is(201));
		assertThat(result.isError(), is(false));
		assertThat(result.getCreated(), is(2));
	}

}
