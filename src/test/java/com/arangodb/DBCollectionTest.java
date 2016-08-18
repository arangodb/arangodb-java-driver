package com.arangodb;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.arangodb.data.TestEntity;
import com.arangodb.entity.DocumentCreateResult;
import com.arangodb.entity.IndexResult;
import com.arangodb.entity.IndexType;
import com.arangodb.model.DocumentCreate;
import com.arangodb.model.DocumentRead;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class DBCollectionTest extends BaseTest {

	private static final String COLLECTION_NAME = "db_collection_test";

	@Before
	public void setup() {
		db.createCollection(COLLECTION_NAME, null).execute();
	}

	@After
	public void teardown() {
		db.deleteCollection(COLLECTION_NAME).execute();
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
	public void createDocumentWaitForSync() {
		final DocumentCreate.Options options = new DocumentCreate.Options().waitForSync(true);
		final DocumentCreateResult<TestEntity> doc = db.collection(COLLECTION_NAME)
				.createDocument(new TestEntity(), options).execute();
		assertThat(doc, is(notNullValue()));
		assertThat(doc.getId(), is(notNullValue()));
		assertThat(doc.getKey(), is(notNullValue()));
		assertThat(doc.getRev(), is(notNullValue()));
		assertThat(doc.getNew().isPresent(), is(false));
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
	public void readDocumentIfMatch() {
		final DocumentCreateResult<TestEntity> createResult = db.collection(COLLECTION_NAME)
				.createDocument(new TestEntity(), null).execute();
		assertThat(createResult.getKey(), is(notNullValue()));
		final DocumentRead.Options options = new DocumentRead.Options().ifMatch(createResult.getRev());
		final TestEntity readResult = db.collection(COLLECTION_NAME)
				.readDocument(createResult.getKey(), TestEntity.class, options).execute();
		assertThat(readResult.getKey(), is(createResult.getKey()));
		assertThat(readResult.getId(), is(COLLECTION_NAME + "/" + createResult.getKey()));
	}

	@Test
	public void readDocumentIfMatchFail() {
		final DocumentCreateResult<TestEntity> createResult = db.collection(COLLECTION_NAME)
				.createDocument(new TestEntity(), null).execute();
		assertThat(createResult.getKey(), is(notNullValue()));
		final DocumentRead.Options options = new DocumentRead.Options().ifMatch("no");
		try {
			db.collection(COLLECTION_NAME).readDocument(createResult.getKey(), TestEntity.class, options).execute();
			fail();
		} catch (final ArangoDBException e) {
		}
	}

	@Test
	public void readDocumentIfNoneMatch() {
		final DocumentCreateResult<TestEntity> createResult = db.collection(COLLECTION_NAME)
				.createDocument(new TestEntity(), null).execute();
		assertThat(createResult.getKey(), is(notNullValue()));
		final DocumentRead.Options options = new DocumentRead.Options().ifNoneMatch("no");
		final TestEntity readResult = db.collection(COLLECTION_NAME)
				.readDocument(createResult.getKey(), TestEntity.class, options).execute();
		assertThat(readResult.getKey(), is(createResult.getKey()));
		assertThat(readResult.getId(), is(COLLECTION_NAME + "/" + createResult.getKey()));
	}

	@Test
	public void readDocumentIfNoneMatchFail() {
		final DocumentCreateResult<TestEntity> createResult = db.collection(COLLECTION_NAME)
				.createDocument(new TestEntity(), null).execute();
		assertThat(createResult.getKey(), is(notNullValue()));
		final DocumentRead.Options options = new DocumentRead.Options().ifMatch(createResult.getRev());
		try {
			db.collection(COLLECTION_NAME).readDocument(createResult.getKey(), TestEntity.class, options).execute();
			fail();
		} catch (final ArangoDBException e) {
		}
	}

	@Test
	public void readDocumentAsJson() {
		db.collection(COLLECTION_NAME).createDocument("{\"_key\":\"docRaw\",\"a\":\"test\"}", null).execute();
		final String readResult = db.collection(COLLECTION_NAME).readDocument("docRaw", String.class, null).execute();
		assertThat(readResult.contains("\"_key\":\"docRaw\""), is(true));
		assertThat(readResult.contains("\"_id\":\"db_collection_test\\/docRaw\""), is(true));
	}

	@Test
	public void createHashIndex() {
		final Collection<String> fields = new ArrayList<>();
		fields.add("a");
		fields.add("b");
		final IndexResult indexResult = db.collection(COLLECTION_NAME).createHashIndex(fields, null).execute();
		assertThat(indexResult, is(notNullValue()));
		assertThat(indexResult.getConstraint().isPresent(), is(false));
		assertThat(indexResult.getFields(), hasItem("a"));
		assertThat(indexResult.getFields(), hasItem("b"));
		assertThat(indexResult.getGeoJson().isPresent(), is(false));
		assertThat(indexResult.getId(), startsWith(COLLECTION_NAME));
		assertThat(indexResult.getIsNewlyCreated().isPresent(), is(true));
		assertThat(indexResult.getIsNewlyCreated().get(), is(true));
		assertThat(indexResult.getMinLength().isPresent(), is(false));
		assertThat(indexResult.getSelectivityEstimate().isPresent(), is(true));
		assertThat(indexResult.getSelectivityEstimate().get(), is(1));
		assertThat(indexResult.getSparse().isPresent(), is(true));
		assertThat(indexResult.getSparse().get(), is(false));
		assertThat(indexResult.getType(), is(IndexType.hash));
		assertThat(indexResult.getUnique().isPresent(), is(true));
		assertThat(indexResult.getUnique().get(), is(false));
	}

	@Test
	public void createGeoIndex() {
		final Collection<String> fields = new ArrayList<>();
		fields.add("a");
		final IndexResult indexResult = db.collection(COLLECTION_NAME).createGeoIndex(fields, null).execute();
		assertThat(indexResult, is(notNullValue()));
		assertThat(indexResult.getConstraint().isPresent(), is(true));
		assertThat(indexResult.getConstraint().get(), is(false));
		assertThat(indexResult.getFields(), hasItem("a"));
		assertThat(indexResult.getGeoJson().isPresent(), is(true));
		assertThat(indexResult.getGeoJson().get(), is(false));
		assertThat(indexResult.getId(), startsWith(COLLECTION_NAME));
		assertThat(indexResult.getIsNewlyCreated().isPresent(), is(true));
		assertThat(indexResult.getIsNewlyCreated().get(), is(true));
		assertThat(indexResult.getMinLength().isPresent(), is(false));
		assertThat(indexResult.getSelectivityEstimate().isPresent(), is(false));
		assertThat(indexResult.getSparse().isPresent(), is(true));
		assertThat(indexResult.getSparse().get(), is(true));
		assertThat(indexResult.getType(), is(IndexType.geo1));
		assertThat(indexResult.getUnique().isPresent(), is(true));
		assertThat(indexResult.getUnique().get(), is(false));
	}

	@Test
	public void createGeo2Index() {
		final Collection<String> fields = new ArrayList<>();
		fields.add("a");
		fields.add("b");
		final IndexResult indexResult = db.collection(COLLECTION_NAME).createGeoIndex(fields, null).execute();
		assertThat(indexResult, is(notNullValue()));
		assertThat(indexResult.getConstraint().isPresent(), is(true));
		assertThat(indexResult.getConstraint().get(), is(false));
		assertThat(indexResult.getFields(), hasItem("a"));
		assertThat(indexResult.getFields(), hasItem("b"));
		assertThat(indexResult.getGeoJson().isPresent(), is(false));
		assertThat(indexResult.getId(), startsWith(COLLECTION_NAME));
		assertThat(indexResult.getIsNewlyCreated().isPresent(), is(true));
		assertThat(indexResult.getIsNewlyCreated().get(), is(true));
		assertThat(indexResult.getMinLength().isPresent(), is(false));
		assertThat(indexResult.getSelectivityEstimate().isPresent(), is(false));
		assertThat(indexResult.getSparse().isPresent(), is(true));
		assertThat(indexResult.getSparse().get(), is(true));
		assertThat(indexResult.getType(), is(IndexType.geo2));
		assertThat(indexResult.getUnique().isPresent(), is(true));
		assertThat(indexResult.getUnique().get(), is(false));
	}

	@Test
	public void createSkiplistIndex() {
		final Collection<String> fields = new ArrayList<>();
		fields.add("a");
		fields.add("b");
		final IndexResult indexResult = db.collection(COLLECTION_NAME).createSkiplistIndex(fields, null).execute();
		assertThat(indexResult, is(notNullValue()));
		assertThat(indexResult.getConstraint().isPresent(), is(false));
		assertThat(indexResult.getFields(), hasItem("a"));
		assertThat(indexResult.getFields(), hasItem("b"));
		assertThat(indexResult.getGeoJson().isPresent(), is(false));
		assertThat(indexResult.getId(), startsWith(COLLECTION_NAME));
		assertThat(indexResult.getIsNewlyCreated().isPresent(), is(true));
		assertThat(indexResult.getIsNewlyCreated().get(), is(true));
		assertThat(indexResult.getMinLength().isPresent(), is(false));
		assertThat(indexResult.getSelectivityEstimate().isPresent(), is(false));
		assertThat(indexResult.getSparse().isPresent(), is(true));
		assertThat(indexResult.getSparse().get(), is(false));
		assertThat(indexResult.getType(), is(IndexType.skiplist));
		assertThat(indexResult.getUnique().isPresent(), is(true));
		assertThat(indexResult.getUnique().get(), is(false));
	}

	@Test
	public void createPersistentIndex() {
		final Collection<String> fields = new ArrayList<>();
		fields.add("a");
		fields.add("b");
		final IndexResult indexResult = db.collection(COLLECTION_NAME).createPersistentIndex(fields, null).execute();
		assertThat(indexResult, is(notNullValue()));
		assertThat(indexResult.getConstraint().isPresent(), is(false));
		assertThat(indexResult.getFields(), hasItem("a"));
		assertThat(indexResult.getFields(), hasItem("b"));
		assertThat(indexResult.getGeoJson().isPresent(), is(false));
		assertThat(indexResult.getId(), startsWith(COLLECTION_NAME));
		assertThat(indexResult.getIsNewlyCreated().isPresent(), is(true));
		assertThat(indexResult.getIsNewlyCreated().get(), is(true));
		assertThat(indexResult.getMinLength().isPresent(), is(false));
		assertThat(indexResult.getSelectivityEstimate().isPresent(), is(false));
		assertThat(indexResult.getSparse().isPresent(), is(true));
		assertThat(indexResult.getSparse().get(), is(false));
		assertThat(indexResult.getType(), is(IndexType.persistent));
		assertThat(indexResult.getUnique().isPresent(), is(true));
		assertThat(indexResult.getUnique().get(), is(false));
	}
}
