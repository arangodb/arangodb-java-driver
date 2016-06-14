package com.arangodb;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.arangodb.entity.BaseDocument;
import com.arangodb.entity.CollectionOptions;
import com.arangodb.entity.CollectionType;
import com.arangodb.entity.DocumentEntity;
import com.arangodb.entity.EdgeEntity;

/**
 * @author Mark - mark@arangodb.com
 *
 */
public class ArangoDriverEdgeTest extends BaseTest {

	final String collectionName = "unit_test_edge_collection_EdgeTest";
	final String collectionName2 = "unit_test_normal_collection_EdgeTest";

	@Before
	public void before() throws ArangoException {
		try {
			driver.deleteCollection(collectionName);
		} catch (final ArangoException e) {
		}
		try {
			final CollectionOptions collectionOptions = new CollectionOptions();
			collectionOptions.setType(CollectionType.EDGE);
			driver.createCollection(collectionName, collectionOptions);
		} catch (final ArangoException e) {
		}
		try {
			driver.deleteCollection(collectionName2);
		} catch (final ArangoException e) {
		}
		try {
			driver.createCollection(collectionName2);
		} catch (final ArangoException e) {
		}
	}

	@After
	public void after() throws ArangoException {
		try {
			driver.deleteCollection(collectionName);
		} catch (final ArangoException e) {
		}
		try {
			driver.deleteCollection(collectionName2);
		} catch (final ArangoException e) {
		}
	}

	@Test
	public void test_createDocument() throws ArangoException {

		final TestComplexEntity01 value = new TestComplexEntity01("user", "desc", 42);
		final DocumentEntity<TestComplexEntity01> fromDoc = driver.createDocument(collectionName2, value, true);
		final DocumentEntity<TestComplexEntity01> toDoc = driver.createDocument(collectionName2, value, true);

		final BaseDocument baseDocument = new BaseDocument();
		baseDocument.addAttribute(BaseDocument.FROM, fromDoc.getDocumentHandle());
		baseDocument.addAttribute(BaseDocument.TO, toDoc.getDocumentHandle());

		final DocumentEntity<BaseDocument> doc = driver.createDocument(collectionName, baseDocument, true);

		Assert.assertNotNull(doc.getDocumentKey());
		Assert.assertEquals(collectionName + "/" + doc.getDocumentKey(), doc.getDocumentHandle());
		Assert.assertNotEquals(0L, doc.getDocumentRevision());
		Assert.assertEquals(fromDoc.getDocumentHandle(), doc.getEntity().getAttribute(BaseDocument.FROM));
		Assert.assertEquals(toDoc.getDocumentHandle(), doc.getEntity().getAttribute(BaseDocument.TO));

	}

	@Test
	public void test_createEdge() throws ArangoException {

		TestComplexEntity01 value = new TestComplexEntity01("user", "desc", 42);
		DocumentEntity<TestComplexEntity01> fromDoc = driver.createDocument(collectionName2, value, true);
		DocumentEntity<TestComplexEntity01> toDoc = driver.createDocument(collectionName2, value, true);

		EdgeEntity<TestComplexEntity01> doc = driver.createEdge(collectionName, value, fromDoc.getDocumentHandle(),
			toDoc.getDocumentHandle(), true);

		Assert.assertNotNull(doc.getDocumentKey());
		Assert.assertEquals(collectionName + "/" + doc.getDocumentKey(), doc.getDocumentHandle());
		Assert.assertNotEquals(0L, doc.getDocumentRevision());
		Assert.assertEquals(fromDoc.getDocumentHandle(), doc.getFromVertexHandle());
		Assert.assertEquals(toDoc.getDocumentHandle(), doc.getToVertexHandle());
	}

	@Test
	public void test_createEdgeWithKey() throws ArangoException {

		final TestComplexEntity01 value = new TestComplexEntity01("user", "desc", 42);
		final DocumentEntity<TestComplexEntity01> fromDoc = driver.createDocument(collectionName2, value, true);
		final DocumentEntity<TestComplexEntity01> toDoc = driver.createDocument(collectionName2, value, true);

		final String documentKey = ArangoDriverEdgeTest.class.getSimpleName() + "_test_createEdgeWithKey";
		EdgeEntity<TestComplexEntity01> doc = driver.createEdge(collectionName, documentKey, value,
			fromDoc.getDocumentHandle(), toDoc.getDocumentHandle(), true);

		Assert.assertNotNull(doc.getDocumentKey());
		Assert.assertEquals(collectionName + "/" + documentKey, doc.getDocumentHandle());
		Assert.assertNotEquals(0L, doc.getDocumentRevision());
		Assert.assertEquals(fromDoc.getDocumentHandle(), doc.getFromVertexHandle());
		Assert.assertEquals(toDoc.getDocumentHandle(), doc.getToVertexHandle());
	}

}