/*
 * Copyright (C) 2012 tamtam180
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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.arangodb.entity.AqlFunctionsEntity;
import com.arangodb.entity.BaseEntity;
import com.arangodb.entity.DefaultEntity;
import com.arangodb.entity.DocumentEntity;

/**
 * @author tamtam180 - kirscheless at gmail.com
 *
 */
public class ArangoDriverBatchTest extends BaseTest {

	private static final String COLLECTION_NAME = "unit_test_batchTest";

	@Before
	public void before() throws ArangoException {
		try {
			driver.cancelBatchMode();
		} catch (final ArangoException e) {
		}
		try {
			driver.deleteCollection(COLLECTION_NAME);
		} catch (final ArangoException e) {
		}
		try {
			driver.createCollection(COLLECTION_NAME);
		} catch (final Exception e) {
			e.printStackTrace();
		}

	}

	@After
	public void after() {
		try {
			driver.cancelBatchMode();
		} catch (final ArangoException e) {
		}
		try {
			driver.deleteCollection(COLLECTION_NAME);
		} catch (final ArangoException e) {
		}
	}

	@Test
	public void test_StartCancelExecuteBatchMode() throws ArangoException {

		driver.startBatchMode();
		String msg = "";
		try {
			driver.startBatchMode();
		} catch (final ArangoException e) {
			msg = e.getErrorMessage();
		}
		assertThat(msg, is("BatchMode is already active."));

		driver.cancelBatchMode();
		msg = "";
		try {
			driver.cancelBatchMode();
		} catch (final ArangoException e) {
			msg = e.getErrorMessage();
		}
		assertThat(msg, is("BatchMode is not active."));

		msg = "";
		try {
			driver.executeBatch();
		} catch (final ArangoException e) {
			msg = e.getErrorMessage();
		}
		assertThat(msg, is("BatchMode is not active."));

	}

	@Test
	public void test_execBatchMode() throws ArangoException {

		try {
			driver.truncateCollection("_aqlfunctions");
		} catch (final Exception e) {
			e.printStackTrace();
		}

		driver.startBatchMode();

		BaseEntity res = driver.createAqlFunction("someNamespace::testCode",
			"function (celsius) { return celsius * 2.8 + 32; }");

		assertThat(res.getStatusCode(), is(206));
		assertThat(res.getRequestId(), is("request1"));

		res = driver.createAqlFunction("someNamespace::testC&&&&&&&&&&de",
			"function (celsius) { return celsius * 2.8 + 32; }");

		assertThat(res.getStatusCode(), is(206));
		assertThat(res.getRequestId(), is("request2"));

		res = driver.getAqlFunctions(null);
		assertThat(res.getStatusCode(), is(206));
		assertThat(res.getRequestId(), is("request3"));

		for (int i = 0; i < 10; i++) {
			final TestComplexEntity01 value = new TestComplexEntity01("user-" + i, "data:" + i, i);
			res = driver.createDocument(COLLECTION_NAME, value, true, false);

			assertThat(res.getStatusCode(), is(206));
			assertThat(res.getRequestId(), is("request" + (4 + i)));
		}

		driver.getDocuments(COLLECTION_NAME);

		driver.executeBatch();
		final DefaultEntity created = driver.getBatchResponseByRequestId("request1");
		assertThat(created.getStatusCode(), is(201));
		final AqlFunctionsEntity functions = driver.getBatchResponseByRequestId("request3");
		assertThat(functions.getStatusCode(), is(200));
		assertThat(String.valueOf(functions.getAqlFunctions().keySet().toArray()[0]), is("someNamespace::testCode"));
		for (int i = 0; i < 10; i++) {
			final DocumentEntity<TestComplexEntity01> resultComplex = driver
					.getBatchResponseByRequestId("request" + (4 + i));
			assertThat(resultComplex.getStatusCode(), is(202));
		}

		final List<String> documents = driver.getBatchResponseByRequestId("request14");
		assertThat(documents.size(), is(10));

		try {
			driver.truncateCollection("_aqlfunctions");
		} catch (final Exception e) {
			e.printStackTrace();
		}

	}

	@Test
	public void test_execBatchMode_twice() throws ArangoException {

		driver.startBatchMode();

		BaseEntity res;

		for (int i = 0; i < 10; i++) {
			final TestComplexEntity01 value = new TestComplexEntity01("user-" + i, "data:" + i, i);
			res = driver.createDocument(COLLECTION_NAME, value, true, false);
			assertThat(res.getRequestId(), is("request" + (i + 1)));
		}

		driver.executeBatch();

		assertThat(driver.getDocuments(COLLECTION_NAME).size(), is(10));

		driver.startBatchMode();

		for (int i = 20; i < 30; i++) {
			final TestComplexEntity01 value = new TestComplexEntity01("user-" + i, "data:" + i, i);
			res = driver.createDocument(COLLECTION_NAME, value, true, false);
			assertThat(res.getRequestId(), is("request" + (i + 1 - 20)));
		}

		driver.executeBatch();

		assertThat(driver.getDocuments(COLLECTION_NAME).size(), is(20));

	}

}
