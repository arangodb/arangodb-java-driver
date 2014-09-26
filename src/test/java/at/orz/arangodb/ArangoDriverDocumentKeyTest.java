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

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.util.Map;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import at.orz.arangodb.annotations.DocumentKey;
import at.orz.arangodb.entity.DocumentEntity;

/**
 * @author tamtam180 - kirscheless at gmail.com
 *
 */
public class ArangoDriverDocumentKeyTest {

	private static ArangoConfigure configure;
	private static ArangoDriver driver;
	
	@BeforeClass
	public static void beforeClass() {
		configure = new ArangoConfigure();
		configure.init();
		driver = new ArangoDriver(configure);
	}
	
	@AfterClass
	public static void afterClass() {
		configure.shutdown();
	}
	
	@Before
	public void before() throws ArangoException {
		
		try {
			driver.deleteCollection("unit_test_arango_001");
		} catch (ArangoException e) {}
		
	}
	
	private static class DocumentKeyTestEntity1 {
		@DocumentKey public String S1;
		public String S2;
		public Integer X;
	}

	private static class DocumentKeyTestEntity2 {
		public String S1;
		public String S2;
		@DocumentKey public Integer X;
	}

	@Test
	public void test_document_key_string() throws ArangoException {
		
		DocumentKeyTestEntity1 obj = new DocumentKeyTestEntity1();
		obj.S1 = "s1";
		obj.S2 = "s2";
		obj.X = 123;
		
		// create
		DocumentEntity<DocumentKeyTestEntity1> doc = driver.createDocument("unit_test_arango_001", obj, true, null);
		assertThat(doc.getDocumentKey(), is("s1"));
		assertThat(doc.getDocumentHandle(), is("unit_test_arango_001/s1"));
		assertThat(doc.getEntity(), is(nullValue()));
		
		// get
		doc = driver.getDocument(doc.getDocumentHandle(), DocumentKeyTestEntity1.class);
		assertThat(doc.getDocumentKey(), is("s1"));
		assertThat(doc.getDocumentHandle(), is("unit_test_arango_001/s1"));
		assertThat(doc.getEntity(), is(notNullValue()));
		assertThat(doc.getEntity().S1, is("s1"));
		assertThat(doc.getEntity().S2, is("s2"));
		assertThat(doc.getEntity().X, is(123));
		
		// get as map
		DocumentEntity<Map<String, Object>> doc2 = driver.getDocument(doc.getDocumentHandle(), Map.class);
		assertThat(doc2.getEntity().get("s1"), is(nullValue())); // s1 is not contains.
		assertThat((String) doc2.getEntity().get("_key"), is("s1"));
		
		System.out.println(doc2.getEntity());
		
	}

	@Test
	public void test_document_key_integer() throws ArangoException {
		
		DocumentKeyTestEntity2 obj = new DocumentKeyTestEntity2();
		obj.S1 = "s1";
		obj.S2 = "s2";
		obj.X = 123;
		
		// create
		try {
			driver.createDocument("unit_test_arango_001", obj, true, null);
			fail();
		} catch (ArangoException e) {
			assertThat(e.getCode(), is(400));
			assertThat(e.getErrorNumber(), is(1221));
			assertThat(e.getErrorMessage(), is("invalid document key"));
		}
		
	}
	
	@Test
	public void test_document_create() throws ArangoException {

		DocumentKeyTestEntity1 obj = new DocumentKeyTestEntity1();
		obj.S1 = "s1";
		obj.S2 = "s2";
		obj.X = 123;

		DocumentEntity<?> doc1 = driver.createDocument("unit_test_arango_001", "mykey1", obj, true, null);
		assertThat(doc1.getStatusCode(), is(202));
		assertThat(doc1.getDocumentKey(), is("mykey1"));
		
	}
	
	@Test
	public void test_document_replace() throws ArangoException {

		DocumentKeyTestEntity1 obj = new DocumentKeyTestEntity1();
		obj.S1 = "s1";
		obj.S2 = "s2";
		obj.X = 123;

		DocumentEntity<?> doc1 = driver.createDocument("unit_test_arango_001", "mykey1", obj, true, null);
		assertThat(doc1.getStatusCode(), is(202));
		assertThat(doc1.getDocumentKey(), is("mykey1"));

		// replace
		obj.S1 = "s3";
		obj.X = 456;
		DocumentEntity<?> doc2 = driver.replaceDocument("unit_test_arango_001", "mykey1", obj);
		assertThat(doc1.getStatusCode(), is(202));
		assertThat(doc1.getDocumentKey(), is("mykey1"));
		
		DocumentEntity<DocumentKeyTestEntity1> doc3 = driver.getDocument(doc2.getDocumentHandle(), DocumentKeyTestEntity1.class);
		assertThat(doc3.getEntity().S1, is("mykey1"));
		assertThat(doc3.getEntity().S2, is("s2"));
		assertThat(doc3.getEntity().X, is(456));
		
	}
	
	@Test
	public void test_document_update() throws ArangoException {
		
		DocumentKeyTestEntity1 obj = new DocumentKeyTestEntity1();
		obj.S1 = "s1";
		obj.S2 = "s2";
		obj.X = 123;

		DocumentEntity<?> doc1 = driver.createDocument("unit_test_arango_001", "mykey1", obj, true, null);
		assertThat(doc1.getStatusCode(), is(202));
		assertThat(doc1.getDocumentKey(), is("mykey1"));

		// update
		obj.S1 = "s3";
		obj.X = 456;
		obj.S2 = null;
		DocumentEntity<?> doc2 = driver.updateDocument("unit_test_arango_001", "mykey1", obj, false);
		assertThat(doc1.getStatusCode(), is(202));
		assertThat(doc1.getDocumentKey(), is("mykey1"));
		
		DocumentEntity<DocumentKeyTestEntity1> doc3 = driver.getDocument(doc2.getDocumentHandle(), DocumentKeyTestEntity1.class);
		assertThat(doc3.getEntity().S1, is("mykey1"));
		assertThat(doc3.getEntity().S2, is(nullValue()));
		assertThat(doc3.getEntity().X, is(456));

	}

	@Test
	public void test_document_delete() throws ArangoException {
		
		DocumentKeyTestEntity1 obj = new DocumentKeyTestEntity1();
		obj.S1 = "s1";
		obj.S2 = "s2";
		obj.X = 123;

		DocumentEntity<?> doc1 = driver.createDocument("unit_test_arango_001", "mykey1", obj, true, null);
		assertThat(doc1.getStatusCode(), is(202));
		assertThat(doc1.getDocumentKey(), is("mykey1"));

		// delete
		obj.S1 = "s3";
		obj.X = 456;
		obj.S2 = null;
		DocumentEntity<?> doc2 = driver.deleteDocument("unit_test_arango_001", "mykey1");
		assertThat(doc1.getStatusCode(), is(202));
		assertThat(doc1.getDocumentKey(), is("mykey1"));
		
		try {
			DocumentEntity<DocumentKeyTestEntity1> doc3 = driver.getDocument(doc2.getDocumentHandle(), DocumentKeyTestEntity1.class);
			fail();
		} catch (ArangoException e) {
			assertThat(e.getCode(), is(404));
			assertThat(e.getErrorNumber(), is(1202));
			assertThat(e.isNotFound(), is(true));
		}

	}

	@Test
	public void test_document_check() throws ArangoException {
		
		DocumentKeyTestEntity1 obj = new DocumentKeyTestEntity1();
		obj.S1 = "s1";
		obj.S2 = "s2";
		obj.X = 123;

		DocumentEntity<?> doc1 = driver.createDocument("unit_test_arango_001", "mykey1", obj, true, null);
		assertThat(doc1.getStatusCode(), is(202));
		assertThat(doc1.getDocumentKey(), is("mykey1"));

		// check
		long rev = driver.checkDocument(doc1.getDocumentHandle());
		assertThat(rev, is(doc1.getDocumentRevision()));

	}
	
	
}
