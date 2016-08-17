package com.arangodb;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.arangodb.data.TestEntity;
import com.arangodb.entity.DocumentCreateResult;
import com.arangodb.model.DocumentCreate;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class DBCollectionTest extends BaseTest {

	private static final String COLLECTION_NAME = "db_collection_test";

	@Before
	public void setup() {
		try {
			db.createCollection(COLLECTION_NAME, null).execute();
		} catch (final ArangoDBException e) {
		}
	}

	@After
	public void teardown() {
		try {
			db.deleteCollection(COLLECTION_NAME).execute();
		} catch (final ArangoDBException e) {
		}
	}

	@Test
	public void createDocument() {
		final DocumentCreateResult<TestEntity> doc = db.collection(COLLECTION_NAME)
				.createDocument(new TestEntity(), null).execute();
		assertThat(doc, is(notNullValue()));
		assertThat(doc.getId(), is(notNullValue()));
		assertThat(doc.getKey(), is(notNullValue()));
		assertThat(doc.getRev(), is(notNullValue()));
		assertThat(doc.getNew().isPresent(), is(false));
		assertThat(doc.getId(), is(COLLECTION_NAME + "/" + doc.getKey()));
	}

	@Test
	public void createDocumentAsync() throws InterruptedException, ExecutionException {
		final CompletableFuture<DocumentCreateResult<TestEntity>> f = db.collection(COLLECTION_NAME)
				.createDocument(new TestEntity(), null).executeAsync();
		assertThat(f, is(notNullValue()));
		f.whenComplete((doc, ex) -> {
			assertThat(ex, is(nullValue()));
			assertThat(doc.getId(), is(notNullValue()));
			assertThat(doc.getKey(), is(notNullValue()));
			assertThat(doc.getRev(), is(notNullValue()));
			assertThat(doc.getNew().isPresent(), is(false));
		});
		f.get();
	}

	@Test
	public void createDocumentReturnNew() {
		final DocumentCreate.Options options = new DocumentCreate.Options().returnNew(true);
		final DocumentCreateResult<TestEntity> doc = db.collection(COLLECTION_NAME)
				.createDocument(new TestEntity(), options).execute();
		assertThat(doc, is(notNullValue()));
		assertThat(doc.getId(), is(notNullValue()));
		assertThat(doc.getKey(), is(notNullValue()));
		assertThat(doc.getRev(), is(notNullValue()));
		assertThat(doc.getNew().isPresent(), is(true));
	}

	@Test
	public void createDocumentAsJson() {
		final DocumentCreateResult<String> doc = db.collection(COLLECTION_NAME)
				.createDocument("{\"_key\":\"docRaw\",\"a\":\"test\"}", null).execute();
		assertThat(doc, is(notNullValue()));
		assertThat(doc.getId(), is(notNullValue()));
		assertThat(doc.getKey(), is(notNullValue()));
		assertThat(doc.getRev(), is(notNullValue()));
	}

	@Test
	public void readDocument() {
		final DocumentCreateResult<TestEntity> createResult = db.collection(COLLECTION_NAME)
				.createDocument(new TestEntity(), null).execute();
		assertThat(createResult.getKey(), is(notNullValue()));
		final TestEntity readResult = db.collection(COLLECTION_NAME)
				.readDocument(createResult.getKey(), TestEntity.class, null).execute();
		assertThat(readResult.getKey(), is(createResult.getKey()));
		assertThat(readResult.getId(), is(COLLECTION_NAME + "/" + createResult.getKey()));
	}

	@Test
	public void readDocumentAsJson() {
		db.collection(COLLECTION_NAME).createDocument("{\"_key\":\"docRaw\",\"a\":\"test\"}", null).execute();
		final String readResult = db.collection(COLLECTION_NAME).readDocument("docRaw", String.class, null).execute();
		System.out.println(readResult);
		assertThat(readResult.contains("\"_key\":\"docRaw\""), is(true));
		assertThat(readResult.contains("\"_id\":\"db_collection_test\\/docRaw\""), is(true));
	}

}
