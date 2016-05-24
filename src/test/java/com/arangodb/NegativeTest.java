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
import static org.junit.Assert.fail;

import org.junit.Ignore;
import org.junit.Test;

import com.arangodb.entity.DefaultEntity;
import com.arangodb.entity.DocumentEntity;
import com.arangodb.entity.EntityFactory;
import com.arangodb.http.HttpManager;
import com.arangodb.http.HttpResponseEntity;

/**
 * @author tamtam180 - kirscheless at gmail.com
 * 
 */
public class NegativeTest extends BaseTest {

	/**
	 * 開発途中にあった命令だけど、今は存在しない。 きとんとエラーになること。
	 * 
	 * @throws ArangoException
	 */
	@Ignore
	@Test
	public void test_collections() throws ArangoException {

		final ArangoConfigure configure = new ArangoConfigure();

		final HttpManager httpManager = new HttpManager(configure);
		httpManager.init();

		// TODO Create configure of common test.
		final HttpResponseEntity res = httpManager.doGet("http://" + configure.getArangoHost().getHost() + ":"
				+ configure.getArangoHost().getPort() + "/_api/collections",
			null);

		final DefaultEntity entity = EntityFactory.createEntity(res.getText(), DefaultEntity.class);
		assertThat(entity.isError(), is(true));
		assertThat(entity.getCode(), is(501));
		assertThat(entity.getErrorNumber(), is(9));

		httpManager.destroy();

	}

	public static class TestComplex {
		private String name;

		public String getName() {
			return name;
		}

		public void setName(final String name) {
			this.name = name;
		}
	}

	@Test
	public void test_issue_35_and_41() throws Exception {

		final ArangoConfigure configure = new ArangoConfigure();
		configure.init();
		final ArangoDriver driver = new ArangoDriver(configure);

		try {
			driver.createCollection("unit_test_issue35");
		} catch (final ArangoException e) {
		}

		final TestComplex value = new TestComplex();
		value.setName("A\"A'@:///A");

		// String value = "AAA";
		final DocumentEntity<?> doc = driver.createDocument("unit_test_issue35", value, true);
		final String documentHandle = doc.getDocumentHandle();
		driver.getDocument(documentHandle, TestComplex.class);

		configure.shutdown();

	}

	@Test
	public void test_primitive() throws Exception {

		final ArangoConfigure configure = new ArangoConfigure();
		configure.init();
		final ArangoDriver driver = new ArangoDriver(configure);

		try {
			driver.createCollection("unit_test_issue35");
		} catch (final ArangoException e) {
		}

		try {
			final String value = "AAA";
			final DocumentEntity<?> doc = driver.createDocument("unit_test_issue35", value, true, true);
			final String documentHandle = doc.getDocumentHandle();
			driver.getDocument(documentHandle, String.class);
			fail();
		} catch (final ArangoException e) {
			assertThat(e.getErrorNumber(), is(ErrorNums.ERROR_ARANGO_DOCUMENT_TYPE_INVALID));
		}

		configure.shutdown();

	}

}
