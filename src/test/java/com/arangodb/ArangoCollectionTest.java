package com.arangodb;

import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.arangodb.entity.BaseDocument;
import com.arangodb.entity.CollectionPropertiesResult;
import com.arangodb.entity.CollectionResult;
import com.arangodb.entity.DocumentCreateResult;
import com.arangodb.entity.DocumentDeleteResult;
import com.arangodb.entity.DocumentUpdateResult;
import com.arangodb.entity.IndexResult;
import com.arangodb.entity.IndexType;
import com.arangodb.model.DocumentCreateOptions;
import com.arangodb.model.DocumentDeleteOptions;
import com.arangodb.model.DocumentExistsOptions;
import com.arangodb.model.DocumentReadOptions;
import com.arangodb.model.DocumentReplaceOptions;
import com.arangodb.model.DocumentUpdateOptions;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class ArangoCollectionTest extends BaseTest {

	private static final String COLLECTION_NAME = "db_collection_test";

	@Before
	public void setup() {
		db.createCollection(COLLECTION_NAME, null);
	}

	@After
	public void teardown() {
		db.collection(COLLECTION_NAME).drop();
	}

	@Test
	public void insert() {
		final DocumentCreateResult<BaseDocument> doc = db.collection(COLLECTION_NAME).insert(new BaseDocument(), null);
		assertThat(doc, is(notNullValue()));
		assertThat(doc.getId(), is(notNullValue()));
		assertThat(doc.getKey(), is(notNullValue()));
		assertThat(doc.getRev(), is(notNullValue()));
		assertThat(doc.getNew().isPresent(), is(false));
		assertThat(doc.getId(), is(COLLECTION_NAME + "/" + doc.getKey()));
	}

	@Test
	public void insertAsync() throws InterruptedException, ExecutionException {
		final CompletableFuture<DocumentCreateResult<BaseDocument>> f = db.collection(COLLECTION_NAME)
				.insertAsync(new BaseDocument(), null);
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
	public void insertReturnNew() {
		final DocumentCreateOptions options = new DocumentCreateOptions().returnNew(true);
		final DocumentCreateResult<BaseDocument> doc = db.collection(COLLECTION_NAME).insert(new BaseDocument(),
			options);
		assertThat(doc, is(notNullValue()));
		assertThat(doc.getId(), is(notNullValue()));
		assertThat(doc.getKey(), is(notNullValue()));
		assertThat(doc.getRev(), is(notNullValue()));
		assertThat(doc.getNew().isPresent(), is(true));
	}

	@Test
	public void insertWaitForSync() {
		final DocumentCreateOptions options = new DocumentCreateOptions().waitForSync(true);
		final DocumentCreateResult<BaseDocument> doc = db.collection(COLLECTION_NAME).insert(new BaseDocument(),
			options);
		assertThat(doc, is(notNullValue()));
		assertThat(doc.getId(), is(notNullValue()));
		assertThat(doc.getKey(), is(notNullValue()));
		assertThat(doc.getRev(), is(notNullValue()));
		assertThat(doc.getNew().isPresent(), is(false));
	}

	@Test
	public void insertAsJson() {
		final DocumentCreateResult<String> doc = db.collection(COLLECTION_NAME)
				.insert("{\"_key\":\"docRaw\",\"a\":\"test\"}", null);
		assertThat(doc, is(notNullValue()));
		assertThat(doc.getId(), is(notNullValue()));
		assertThat(doc.getKey(), is(notNullValue()));
		assertThat(doc.getRev(), is(notNullValue()));
	}

	@Test
	public void read() {
		final DocumentCreateResult<BaseDocument> createResult = db.collection(COLLECTION_NAME)
				.insert(new BaseDocument(), null);
		assertThat(createResult.getKey(), is(notNullValue()));
		final BaseDocument readResult = db.collection(COLLECTION_NAME).read(createResult.getKey(), BaseDocument.class,
			null);
		assertThat(readResult.getKey(), is(createResult.getKey()));
		assertThat(readResult.getId(), is(COLLECTION_NAME + "/" + createResult.getKey()));
	}

	@Test
	public void readIfMatch() {
		final DocumentCreateResult<BaseDocument> createResult = db.collection(COLLECTION_NAME)
				.insert(new BaseDocument(), null);
		assertThat(createResult.getKey(), is(notNullValue()));
		final DocumentReadOptions options = new DocumentReadOptions().ifMatch(createResult.getRev());
		final BaseDocument readResult = db.collection(COLLECTION_NAME).read(createResult.getKey(), BaseDocument.class,
			options);
		assertThat(readResult.getKey(), is(createResult.getKey()));
		assertThat(readResult.getId(), is(COLLECTION_NAME + "/" + createResult.getKey()));
	}

	@Test
	public void readIfMatchFail() {
		final DocumentCreateResult<BaseDocument> createResult = db.collection(COLLECTION_NAME)
				.insert(new BaseDocument(), null);
		assertThat(createResult.getKey(), is(notNullValue()));
		final DocumentReadOptions options = new DocumentReadOptions().ifMatch("no");
		try {
			db.collection(COLLECTION_NAME).read(createResult.getKey(), BaseDocument.class, options);
			fail();
		} catch (final ArangoDBException e) {
		}
	}

	@Test
	public void readIfNoneMatch() {
		final DocumentCreateResult<BaseDocument> createResult = db.collection(COLLECTION_NAME)
				.insert(new BaseDocument(), null);
		assertThat(createResult.getKey(), is(notNullValue()));
		final DocumentReadOptions options = new DocumentReadOptions().ifNoneMatch("no");
		final BaseDocument readResult = db.collection(COLLECTION_NAME).read(createResult.getKey(), BaseDocument.class,
			options);
		assertThat(readResult.getKey(), is(createResult.getKey()));
		assertThat(readResult.getId(), is(COLLECTION_NAME + "/" + createResult.getKey()));
	}

	@Test
	public void readIfNoneMatchFail() {
		final DocumentCreateResult<BaseDocument> createResult = db.collection(COLLECTION_NAME)
				.insert(new BaseDocument(), null);
		assertThat(createResult.getKey(), is(notNullValue()));
		final DocumentReadOptions options = new DocumentReadOptions().ifNoneMatch(createResult.getRev());
		try {
			db.collection(COLLECTION_NAME).read(createResult.getKey(), BaseDocument.class, options);
			fail();
		} catch (final ArangoDBException e) {
		}
	}

	@Test
	public void readAsJson() {
		db.collection(COLLECTION_NAME).insert("{\"_key\":\"docRaw\",\"a\":\"test\"}", null);
		final String readResult = db.collection(COLLECTION_NAME).read("docRaw", String.class, null);
		assertThat(readResult.contains("\"_key\":\"docRaw\""), is(true));
		assertThat(readResult.contains("\"_id\":\"db_collection_test\\/docRaw\""), is(true));
	}

	@Test
	public void update() {
		final BaseDocument doc = new BaseDocument();
		doc.addAttribute("a", "test");
		doc.addAttribute("c", "test");
		final DocumentCreateResult<BaseDocument> createResult = db.collection(COLLECTION_NAME).insert(doc, null);
		doc.updateAttribute("a", "test1");
		doc.addAttribute("b", "test");
		doc.updateAttribute("c", null);
		final DocumentUpdateResult<BaseDocument> updateResult = db.collection(COLLECTION_NAME)
				.update(createResult.getKey(), doc, null);
		assertThat(updateResult, is(notNullValue()));
		assertThat(updateResult.getId(), is(createResult.getId()));
		assertThat(updateResult.getNew().isPresent(), is(false));
		assertThat(updateResult.getOld().isPresent(), is(false));
		assertThat(updateResult.getRev(), is(not(updateResult.getOldRev())));
		assertThat(updateResult.getOldRev(), is(createResult.getRev()));

		final BaseDocument readResult = db.collection(COLLECTION_NAME).read(createResult.getKey(), BaseDocument.class,
			null);
		assertThat(readResult.getKey(), is(createResult.getKey()));
		assertThat(readResult.getAttribute("a"), is("test1"));
		assertThat(readResult.getAttribute("b"), is("test"));
		assertThat(readResult.getRevision(), is(updateResult.getRev()));
		assertThat(readResult.getProperties().keySet(), hasItem("c"));
	}

	@Test
	public void updateIfMatch() {
		final BaseDocument doc = new BaseDocument();
		doc.addAttribute("a", "test");
		doc.addAttribute("c", "test");
		final DocumentCreateResult<BaseDocument> createResult = db.collection(COLLECTION_NAME).insert(doc, null);
		doc.updateAttribute("a", "test1");
		doc.addAttribute("b", "test");
		doc.updateAttribute("c", null);
		final DocumentUpdateOptions options = new DocumentUpdateOptions().ifMatch(createResult.getRev());
		final DocumentUpdateResult<BaseDocument> updateResult = db.collection(COLLECTION_NAME)
				.update(createResult.getKey(), doc, options);
		assertThat(updateResult, is(notNullValue()));
		assertThat(updateResult.getId(), is(createResult.getId()));
		assertThat(updateResult.getRev(), is(not(updateResult.getOldRev())));
		assertThat(updateResult.getOldRev(), is(createResult.getRev()));

		final BaseDocument readResult = db.collection(COLLECTION_NAME).read(createResult.getKey(), BaseDocument.class,
			null);
		assertThat(readResult.getKey(), is(createResult.getKey()));
		assertThat(readResult.getAttribute("a"), is("test1"));
		assertThat(readResult.getAttribute("b"), is("test"));
		assertThat(readResult.getRevision(), is(updateResult.getRev()));
		assertThat(readResult.getProperties().keySet(), hasItem("c"));
	}

	@Test
	public void updateIfMatchFail() {
		final BaseDocument doc = new BaseDocument();
		doc.addAttribute("a", "test");
		doc.addAttribute("c", "test");
		final DocumentCreateResult<BaseDocument> createResult = db.collection(COLLECTION_NAME).insert(doc, null);
		doc.updateAttribute("a", "test1");
		doc.addAttribute("b", "test");
		doc.updateAttribute("c", null);
		try {
			final DocumentUpdateOptions options = new DocumentUpdateOptions().ifMatch("no");
			db.collection(COLLECTION_NAME).update(createResult.getKey(), doc, options);
			fail();
		} catch (final ArangoDBException e) {
		}
	}

	@Test
	public void updateReturnNew() {
		final BaseDocument doc = new BaseDocument();
		doc.addAttribute("a", "test");
		final DocumentCreateResult<BaseDocument> createResult = db.collection(COLLECTION_NAME).insert(doc, null);
		doc.updateAttribute("a", "test1");
		doc.addAttribute("b", "test");
		final DocumentUpdateOptions options = new DocumentUpdateOptions().returnNew(true);
		final DocumentUpdateResult<BaseDocument> updateResult = db.collection(COLLECTION_NAME)
				.update(createResult.getKey(), doc, options);
		assertThat(updateResult, is(notNullValue()));
		assertThat(updateResult.getId(), is(createResult.getId()));
		assertThat(updateResult.getOldRev(), is(createResult.getRev()));
		assertThat(updateResult.getNew().isPresent(), is(true));
		assertThat(updateResult.getNew().get().getKey(), is(createResult.getKey()));
		assertThat(updateResult.getNew().get().getRevision(), is(not(createResult.getRev())));
		assertThat(updateResult.getNew().get().getAttribute("a"), is("test1"));
		assertThat(updateResult.getNew().get().getAttribute("b"), is("test"));
	}

	@Test
	public void updateReturnOld() {
		final BaseDocument doc = new BaseDocument();
		doc.addAttribute("a", "test");
		final DocumentCreateResult<BaseDocument> createResult = db.collection(COLLECTION_NAME).insert(doc, null);
		doc.updateAttribute("a", "test1");
		doc.addAttribute("b", "test");
		final DocumentUpdateOptions options = new DocumentUpdateOptions().returnOld(true);
		final DocumentUpdateResult<BaseDocument> updateResult = db.collection(COLLECTION_NAME)
				.update(createResult.getKey(), doc, options);
		assertThat(updateResult, is(notNullValue()));
		assertThat(updateResult.getId(), is(createResult.getId()));
		assertThat(updateResult.getOldRev(), is(createResult.getRev()));
		assertThat(updateResult.getOld().isPresent(), is(true));
		assertThat(updateResult.getOld().get().getKey(), is(createResult.getKey()));
		assertThat(updateResult.getOld().get().getRevision(), is(createResult.getRev()));
		assertThat(updateResult.getOld().get().getAttribute("a"), is("test"));
		assertThat(updateResult.getOld().get().getProperties().keySet(), not(hasItem("b")));
	}

	@Test
	public void updateKeepNullTrue() {
		final BaseDocument doc = new BaseDocument();
		doc.addAttribute("a", "test");
		final DocumentCreateResult<BaseDocument> createResult = db.collection(COLLECTION_NAME).insert(doc, null);
		doc.updateAttribute("a", null);
		final DocumentUpdateOptions options = new DocumentUpdateOptions().keepNull(true);
		final DocumentUpdateResult<BaseDocument> updateResult = db.collection(COLLECTION_NAME)
				.update(createResult.getKey(), doc, options);
		assertThat(updateResult, is(notNullValue()));
		assertThat(updateResult.getId(), is(createResult.getId()));
		assertThat(updateResult.getRev(), is(not(updateResult.getOldRev())));
		assertThat(updateResult.getOldRev(), is(createResult.getRev()));

		final BaseDocument readResult = db.collection(COLLECTION_NAME).read(createResult.getKey(), BaseDocument.class,
			null);
		assertThat(readResult.getKey(), is(createResult.getKey()));
		assertThat(readResult.getProperties().keySet(), hasItem("a"));
	}

	@Test
	public void updateKeepNullFalse() {
		final BaseDocument doc = new BaseDocument();
		doc.addAttribute("a", "test");
		final DocumentCreateResult<BaseDocument> createResult = db.collection(COLLECTION_NAME).insert(doc, null);
		doc.updateAttribute("a", null);
		final DocumentUpdateOptions options = new DocumentUpdateOptions().keepNull(false);
		final DocumentUpdateResult<BaseDocument> updateResult = db.collection(COLLECTION_NAME)
				.update(createResult.getKey(), doc, options);
		assertThat(updateResult, is(notNullValue()));
		assertThat(updateResult.getId(), is(createResult.getId()));
		assertThat(updateResult.getRev(), is(not(updateResult.getOldRev())));
		assertThat(updateResult.getOldRev(), is(createResult.getRev()));

		final BaseDocument readResult = db.collection(COLLECTION_NAME).read(createResult.getKey(), BaseDocument.class,
			null);
		assertThat(readResult.getKey(), is(createResult.getKey()));
		assertThat(readResult.getId(), is(createResult.getId()));
		assertThat(readResult.getRevision(), is(notNullValue()));
		assertThat(readResult.getProperties().keySet(), not(hasItem("a")));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void updateMergeObjectsTrue() {
		final BaseDocument doc = new BaseDocument();
		final Map<String, String> a = new HashMap<>();
		a.put("a", "test");
		doc.addAttribute("a", a);
		final DocumentCreateResult<BaseDocument> createResult = db.collection(COLLECTION_NAME).insert(doc, null);
		a.clear();
		a.put("b", "test");
		doc.updateAttribute("a", a);
		final DocumentUpdateOptions options = new DocumentUpdateOptions().mergeObjects(true);
		final DocumentUpdateResult<BaseDocument> updateResult = db.collection(COLLECTION_NAME)
				.update(createResult.getKey(), doc, options);
		assertThat(updateResult, is(notNullValue()));
		assertThat(updateResult.getId(), is(createResult.getId()));
		assertThat(updateResult.getRev(), is(not(updateResult.getOldRev())));
		assertThat(updateResult.getOldRev(), is(createResult.getRev()));

		final BaseDocument readResult = db.collection(COLLECTION_NAME).read(createResult.getKey(), BaseDocument.class,
			null);
		assertThat(readResult.getKey(), is(createResult.getKey()));
		final Object aResult = readResult.getAttribute("a");
		assertThat(aResult, instanceOf(Map.class));
		final Map<String, String> aMap = (Map<String, String>) aResult;
		assertThat(aMap.keySet(), hasItem("a"));
		assertThat(aMap.keySet(), hasItem("b"));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void updateMergeObjectsFalse() {
		final BaseDocument doc = new BaseDocument();
		final Map<String, String> a = new HashMap<>();
		a.put("a", "test");
		doc.addAttribute("a", a);
		final DocumentCreateResult<BaseDocument> createResult = db.collection(COLLECTION_NAME).insert(doc, null);
		a.clear();
		a.put("b", "test");
		doc.updateAttribute("a", a);
		final DocumentUpdateOptions options = new DocumentUpdateOptions().mergeObjects(false);
		final DocumentUpdateResult<BaseDocument> updateResult = db.collection(COLLECTION_NAME)
				.update(createResult.getKey(), doc, options);
		assertThat(updateResult, is(notNullValue()));
		assertThat(updateResult.getId(), is(createResult.getId()));
		assertThat(updateResult.getRev(), is(not(updateResult.getOldRev())));
		assertThat(updateResult.getOldRev(), is(createResult.getRev()));

		final BaseDocument readResult = db.collection(COLLECTION_NAME).read(createResult.getKey(), BaseDocument.class,
			null);
		assertThat(readResult.getKey(), is(createResult.getKey()));
		final Object aResult = readResult.getAttribute("a");
		assertThat(aResult, instanceOf(Map.class));
		final Map<String, String> aMap = (Map<String, String>) aResult;
		assertThat(aMap.keySet(), not(hasItem("a")));
		assertThat(aMap.keySet(), hasItem("b"));
	}

	@Test
	public void updateIgnoreRevsFalse() {
		final BaseDocument doc = new BaseDocument();
		doc.addAttribute("a", "test");
		final DocumentCreateResult<BaseDocument> createResult = db.collection(COLLECTION_NAME).insert(doc, null);
		doc.setRevision("no");
		doc.updateAttribute("a", "test1");
		try {
			final DocumentUpdateOptions options = new DocumentUpdateOptions().ignoreRevs(false);
			db.collection(COLLECTION_NAME).update(createResult.getKey(), doc, options);
			fail();
		} catch (final ArangoDBException e) {
		}
	}

	@Test
	public void replace() {
		final BaseDocument doc = new BaseDocument();
		doc.addAttribute("a", "test");
		final DocumentCreateResult<BaseDocument> createResult = db.collection(COLLECTION_NAME).insert(doc, null);
		doc.getProperties().clear();
		doc.addAttribute("b", "test");
		final DocumentUpdateResult<BaseDocument> replaceResult = db.collection(COLLECTION_NAME)
				.replace(createResult.getKey(), doc, null);
		assertThat(replaceResult, is(notNullValue()));
		assertThat(replaceResult.getId(), is(createResult.getId()));
		assertThat(replaceResult.getNew().isPresent(), is(false));
		assertThat(replaceResult.getOld().isPresent(), is(false));
		assertThat(replaceResult.getRev(), is(not(replaceResult.getOldRev())));
		assertThat(replaceResult.getOldRev(), is(createResult.getRev()));

		final BaseDocument readResult = db.collection(COLLECTION_NAME).read(createResult.getKey(), BaseDocument.class,
			null);
		assertThat(readResult.getKey(), is(createResult.getKey()));
		assertThat(readResult.getRevision(), is(replaceResult.getRev()));
		assertThat(readResult.getProperties().keySet(), not(hasItem("a")));
		assertThat(readResult.getAttribute("b"), is("test"));
	}

	@Test
	public void replaceIfMatch() {
		final BaseDocument doc = new BaseDocument();
		doc.addAttribute("a", "test");
		final DocumentCreateResult<BaseDocument> createResult = db.collection(COLLECTION_NAME).insert(doc, null);
		doc.getProperties().clear();
		doc.addAttribute("b", "test");
		final DocumentReplaceOptions options = new DocumentReplaceOptions().ifMatch(createResult.getRev());
		final DocumentUpdateResult<BaseDocument> replaceResult = db.collection(COLLECTION_NAME)
				.replace(createResult.getKey(), doc, options);
		assertThat(replaceResult, is(notNullValue()));
		assertThat(replaceResult.getId(), is(createResult.getId()));
		assertThat(replaceResult.getRev(), is(not(replaceResult.getOldRev())));
		assertThat(replaceResult.getOldRev(), is(createResult.getRev()));

		final BaseDocument readResult = db.collection(COLLECTION_NAME).read(createResult.getKey(), BaseDocument.class,
			null);
		assertThat(readResult.getKey(), is(createResult.getKey()));
		assertThat(readResult.getRevision(), is(replaceResult.getRev()));
		assertThat(readResult.getProperties().keySet(), not(hasItem("a")));
		assertThat(readResult.getAttribute("b"), is("test"));
	}

	@Test
	public void replaceIfMatchFail() {
		final BaseDocument doc = new BaseDocument();
		doc.addAttribute("a", "test");
		final DocumentCreateResult<BaseDocument> createResult = db.collection(COLLECTION_NAME).insert(doc, null);
		doc.getProperties().clear();
		doc.addAttribute("b", "test");
		try {
			final DocumentReplaceOptions options = new DocumentReplaceOptions().ifMatch("no");
			db.collection(COLLECTION_NAME).replace(createResult.getKey(), doc, options);
			fail();
		} catch (final ArangoDBException e) {
		}
	}

	@Test
	public void replaceIgnoreRevsFalse() {
		final BaseDocument doc = new BaseDocument();
		doc.addAttribute("a", "test");
		final DocumentCreateResult<BaseDocument> createResult = db.collection(COLLECTION_NAME).insert(doc, null);
		doc.setRevision("no");
		doc.getProperties().clear();
		doc.addAttribute("b", "test");
		try {
			final DocumentReplaceOptions options = new DocumentReplaceOptions().ignoreRevs(false);
			db.collection(COLLECTION_NAME).replace(createResult.getKey(), doc, options);
			fail();
		} catch (final ArangoDBException e) {
		}
	}

	@Test
	public void replaceReturnNew() {
		final BaseDocument doc = new BaseDocument();
		doc.addAttribute("a", "test");
		final DocumentCreateResult<BaseDocument> createResult = db.collection(COLLECTION_NAME).insert(doc, null);
		doc.getProperties().clear();
		doc.addAttribute("b", "test");
		final DocumentReplaceOptions options = new DocumentReplaceOptions().returnNew(true);
		final DocumentUpdateResult<BaseDocument> replaceResult = db.collection(COLLECTION_NAME)
				.replace(createResult.getKey(), doc, options);
		assertThat(replaceResult, is(notNullValue()));
		assertThat(replaceResult.getId(), is(createResult.getId()));
		assertThat(replaceResult.getOldRev(), is(createResult.getRev()));
		assertThat(replaceResult.getNew().isPresent(), is(true));
		assertThat(replaceResult.getNew().get().getKey(), is(createResult.getKey()));
		assertThat(replaceResult.getNew().get().getRevision(), is(not(createResult.getRev())));
		assertThat(replaceResult.getNew().get().getProperties().keySet(), not(hasItem("a")));
		assertThat(replaceResult.getNew().get().getAttribute("b"), is("test"));
	}

	@Test
	public void replaceReturnOld() {
		final BaseDocument doc = new BaseDocument();
		doc.addAttribute("a", "test");
		final DocumentCreateResult<BaseDocument> createResult = db.collection(COLLECTION_NAME).insert(doc, null);
		doc.getProperties().clear();
		doc.addAttribute("b", "test");
		final DocumentReplaceOptions options = new DocumentReplaceOptions().returnOld(true);
		final DocumentUpdateResult<BaseDocument> replaceResult = db.collection(COLLECTION_NAME)
				.replace(createResult.getKey(), doc, options);
		assertThat(replaceResult, is(notNullValue()));
		assertThat(replaceResult.getId(), is(createResult.getId()));
		assertThat(replaceResult.getOldRev(), is(createResult.getRev()));
		assertThat(replaceResult.getOld().isPresent(), is(true));
		assertThat(replaceResult.getOld().get().getKey(), is(createResult.getKey()));
		assertThat(replaceResult.getOld().get().getRevision(), is(createResult.getRev()));
		assertThat(replaceResult.getOld().get().getAttribute("a"), is("test"));
		assertThat(replaceResult.getOld().get().getProperties().keySet(), not(hasItem("b")));
	}

	@Test
	public void delete() {
		final BaseDocument doc = new BaseDocument();
		final DocumentCreateResult<BaseDocument> createResult = db.collection(COLLECTION_NAME).insert(doc, null);
		db.collection(COLLECTION_NAME).delete(createResult.getKey(), null, null);
		try {
			db.collection(COLLECTION_NAME).read(createResult.getKey(), BaseDocument.class, null);
			fail();
		} catch (final ArangoDBException e) {
		}
	}

	@Test
	public void deleteReturnOld() {
		final BaseDocument doc = new BaseDocument();
		doc.addAttribute("a", "test");
		final DocumentCreateResult<BaseDocument> createResult = db.collection(COLLECTION_NAME).insert(doc, null);
		final DocumentDeleteOptions options = new DocumentDeleteOptions().returnOld(true);
		final DocumentDeleteResult<BaseDocument> deleteResult = db.collection(COLLECTION_NAME)
				.delete(createResult.getKey(), BaseDocument.class, options);
		assertThat(deleteResult.getOld().isPresent(), is(true));
		assertThat(deleteResult.getOld().get(), instanceOf(BaseDocument.class));
		assertThat(deleteResult.getOld().get().getAttribute("a"), is("test"));
	}

	@Test
	public void deleteIfMatch() {
		final BaseDocument doc = new BaseDocument();
		final DocumentCreateResult<BaseDocument> createResult = db.collection(COLLECTION_NAME).insert(doc, null);
		final DocumentDeleteOptions options = new DocumentDeleteOptions().ifMatch(createResult.getRev());
		db.collection(COLLECTION_NAME).delete(createResult.getKey(), null, options);
		try {
			db.collection(COLLECTION_NAME).read(createResult.getKey(), BaseDocument.class, null);
			fail();
		} catch (final ArangoDBException e) {
		}
	}

	@Test
	public void deleteIfMatchFail() {
		final BaseDocument doc = new BaseDocument();
		final DocumentCreateResult<BaseDocument> createResult = db.collection(COLLECTION_NAME).insert(doc, null);
		final DocumentDeleteOptions options = new DocumentDeleteOptions().ifMatch("no");
		try {
			db.collection(COLLECTION_NAME).delete(createResult.getKey(), null, options);
			fail();
		} catch (final ArangoDBException e) {
		}
	}

	@Test
	public void createHashIndex() {
		final Collection<String> fields = new ArrayList<>();
		fields.add("a");
		fields.add("b");
		final IndexResult indexResult = db.collection(COLLECTION_NAME).createHashIndex(fields, null);
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
		final IndexResult indexResult = db.collection(COLLECTION_NAME).createGeoIndex(fields, null);
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
		final IndexResult indexResult = db.collection(COLLECTION_NAME).createGeoIndex(fields, null);
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
		final IndexResult indexResult = db.collection(COLLECTION_NAME).createSkiplistIndex(fields, null);
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
		final IndexResult indexResult = db.collection(COLLECTION_NAME).createPersistentIndex(fields, null);
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

	@Test
	public void getIndexes() {
		final Collection<String> fields = new ArrayList<>();
		fields.add("a");
		db.collection(COLLECTION_NAME).createHashIndex(fields, null);
		final Collection<IndexResult> indexes = db.collection(COLLECTION_NAME).getIndexes();
		assertThat(indexes, is(notNullValue()));
		assertThat(indexes.size(), is(2));
		indexes.stream().forEach((i) -> {
			assertThat(i.getType(), anyOf(is(IndexType.primary), is(IndexType.hash)));
			if (i.getType() == IndexType.hash) {
				assertThat(i.getFields().size(), is(1));
				assertThat(i.getFields(), hasItem("a"));
			}
		});
	}

	@Test
	public void truncate() {
		final BaseDocument doc = new BaseDocument();
		db.collection(COLLECTION_NAME).insert(doc, null);
		final BaseDocument readResult = db.collection(COLLECTION_NAME).read(doc.getKey(), BaseDocument.class, null);
		assertThat(readResult.getKey(), is(doc.getKey()));
		final CollectionResult truncateResult = db.collection(COLLECTION_NAME).truncate();
		assertThat(truncateResult, is(notNullValue()));
		assertThat(truncateResult.getId(), is(notNullValue()));
		try {
			db.collection(COLLECTION_NAME).read(doc.getKey(), BaseDocument.class, null);
			fail();
		} catch (final ArangoDBException e) {
		}
	}

	@Test
	public void getCount() {
		final CollectionPropertiesResult countEmpty = db.collection(COLLECTION_NAME).count();
		assertThat(countEmpty, is(notNullValue()));
		assertThat(countEmpty.getCount(), is(0L));
		db.collection(COLLECTION_NAME).insert("{}", null);
		final CollectionPropertiesResult count = db.collection(COLLECTION_NAME).count();
		assertThat(count.getCount(), is(1L));
	}

	@Test
	public void documentExists() {
		final Boolean existsNot = db.collection(COLLECTION_NAME).documentExists("no", null);
		assertThat(existsNot, is(false));
		db.collection(COLLECTION_NAME).insert("{\"_key\":\"abc\"}", null);
		final Boolean exists = db.collection(COLLECTION_NAME).documentExists("abc", null);
		assertThat(exists, is(true));
	}

	@Test
	public void documentExistsAsync() throws Exception {
		final CompletableFuture<Boolean> existsNot = db.collection(COLLECTION_NAME).documentExistsAsync("no", null);
		existsNot.thenAccept(result -> {
			assertThat(result, is(false));
		});
		existsNot.get();
		db.collection(COLLECTION_NAME).insert("{\"_key\":\"abc\"}", null);
		final CompletableFuture<Boolean> exists = db.collection(COLLECTION_NAME).documentExistsAsync("abc", null);
		exists.thenAccept(result -> {
			assertThat(result, is(true));
		});
	}

	@Test
	public void documentExistsIfMatch() {
		final DocumentCreateResult<String> createResult = db.collection(COLLECTION_NAME).insert("{\"_key\":\"abc\"}",
			null);
		final DocumentExistsOptions options = new DocumentExistsOptions().ifMatch(createResult.getRev());
		final Boolean exists = db.collection(COLLECTION_NAME).documentExists("abc", options);
		assertThat(exists, is(true));
	}

	@Test
	public void documentExistsIfMatchFail() {
		db.collection(COLLECTION_NAME).insert("{\"_key\":\"abc\"}", null);
		final DocumentExistsOptions options = new DocumentExistsOptions().ifMatch("no");
		final Boolean exists = db.collection(COLLECTION_NAME).documentExists("abc", options);
		assertThat(exists, is(false));
	}

	@Test
	public void documentExistsIfNoneMatch() {
		db.collection(COLLECTION_NAME).insert("{\"_key\":\"abc\"}", null);
		final DocumentExistsOptions options = new DocumentExistsOptions().ifNoneMatch("no");
		final Boolean exists = db.collection(COLLECTION_NAME).documentExists("abc", options);
		assertThat(exists, is(true));
	}

	@Test
	public void documentExistsIfNoneMatchFail() {
		final DocumentCreateResult<String> createResult = db.collection(COLLECTION_NAME).insert("{\"_key\":\"abc\"}",
			null);
		final DocumentExistsOptions options = new DocumentExistsOptions().ifNoneMatch(createResult.getRev());
		final Boolean exists = db.collection(COLLECTION_NAME).documentExists("abc", options);
		assertThat(exists, is(false));
	}

	@Test
	public void insertMany() {
		final Collection<BaseDocument> values = new ArrayList<>();
		values.add(new BaseDocument());
		values.add(new BaseDocument());
		values.add(new BaseDocument());
		final Collection<DocumentCreateResult<BaseDocument>> docs = db.collection(COLLECTION_NAME).insert(values, null);
		assertThat(docs, is(notNullValue()));
		assertThat(docs.size(), is(3));
	}

	@Test
	public void insertManyOne() {
		final Collection<BaseDocument> values = new ArrayList<>();
		values.add(new BaseDocument());
		final Collection<DocumentCreateResult<BaseDocument>> docs = db.collection(COLLECTION_NAME).insert(values, null);
		assertThat(docs, is(notNullValue()));
		assertThat(docs.size(), is(1));
	}

	@Test
	public void insertManyEmpty() {
		final Collection<BaseDocument> values = new ArrayList<>();
		final Collection<DocumentCreateResult<BaseDocument>> docs = db.collection(COLLECTION_NAME).insert(values, null);
		assertThat(docs, is(notNullValue()));
		assertThat(docs.size(), is(0));
	}

	@Test
	public void insertManyReturnNew() {
		final Collection<BaseDocument> values = new ArrayList<>();
		values.add(new BaseDocument());
		values.add(new BaseDocument());
		values.add(new BaseDocument());
		final DocumentCreateOptions options = new DocumentCreateOptions().returnNew(true);
		final Collection<DocumentCreateResult<BaseDocument>> docs = db.collection(COLLECTION_NAME).insert(values,
			options);
		assertThat(docs, is(notNullValue()));
		assertThat(docs.size(), is(3));
		docs.stream().forEach(doc -> {
			assertThat(doc.getNew().isPresent(), is(true));
			final BaseDocument baseDocument = doc.getNew().get();
			assertThat(baseDocument.getKey(), is(notNullValue()));
		});
	}

	@Test
	public void deleteMany() {
		final Collection<BaseDocument> values = new ArrayList<>();
		{
			final BaseDocument e = new BaseDocument();
			e.setKey("1");
			values.add(e);
		}
		{
			final BaseDocument e = new BaseDocument();
			e.setKey("2");
			values.add(e);
		}
		db.collection(COLLECTION_NAME).insert(values, null);
		final Collection<String> keys = new ArrayList<>();
		keys.add("1");
		keys.add("2");
		final Collection<DocumentDeleteResult<Object>> deleteResult = db.collection(COLLECTION_NAME).delete(keys, null,
			null);
		assertThat(deleteResult, is(notNullValue()));
		assertThat(deleteResult.size(), is(2));
		deleteResult.stream().forEach(i -> {
			assertThat(i.getKey(), anyOf(is("1"), is("2")));
		});
	}

	@Test
	public void deleteManyOne() {
		final Collection<BaseDocument> values = new ArrayList<>();
		{
			final BaseDocument e = new BaseDocument();
			e.setKey("1");
			values.add(e);
		}
		db.collection(COLLECTION_NAME).insert(values, null);
		final Collection<String> keys = new ArrayList<>();
		keys.add("1");
		final Collection<DocumentDeleteResult<Object>> deleteResult = db.collection(COLLECTION_NAME).delete(keys, null,
			null);
		assertThat(deleteResult, is(notNullValue()));
		assertThat(deleteResult.size(), is(1));
		deleteResult.stream().forEach(i -> {
			assertThat(i.getKey(), is("1"));
		});
	}

	@Test
	public void deleteManyEmpty() {
		final Collection<BaseDocument> values = new ArrayList<>();
		db.collection(COLLECTION_NAME).insert(values, null);
		final Collection<String> keys = new ArrayList<>();
		final Collection<DocumentDeleteResult<Object>> deleteResult = db.collection(COLLECTION_NAME).delete(keys, null,
			null);
		assertThat(deleteResult, is(notNullValue()));
		assertThat(deleteResult.size(), is(0));
	}

	@Test
	public void deleteDocumentsNotExisting() {
		final Collection<BaseDocument> values = new ArrayList<>();
		db.collection(COLLECTION_NAME).insert(values, null);
		final Collection<String> keys = new ArrayList<>();
		keys.add("1");
		keys.add("2");
		final Collection<DocumentDeleteResult<Object>> deleteResult = db.collection(COLLECTION_NAME).delete(keys, null,
			null);
		assertThat(deleteResult, is(notNullValue()));
		assertThat(deleteResult.size(), is(2));// TODO expexted 0
	}

	@Test
	public void updateMany() {
		final Collection<BaseDocument> values = new ArrayList<>();
		{
			final BaseDocument e = new BaseDocument();
			e.setKey("1");
			values.add(e);
		}
		{
			final BaseDocument e = new BaseDocument();
			e.setKey("2");
			values.add(e);
		}
		db.collection(COLLECTION_NAME).insert(values, null);
		final Collection<BaseDocument> updatedValues = new ArrayList<>();
		values.stream().forEach(i -> {
			i.addAttribute("a", "test");
			updatedValues.add(i);
		});
		final Collection<DocumentUpdateResult<BaseDocument>> updateResult = db.collection(COLLECTION_NAME)
				.update(updatedValues, null);
		assertThat(updateResult.size(), is(2));
	}

	@Test
	public void updateManyOne() {
		final Collection<BaseDocument> values = new ArrayList<>();
		{
			final BaseDocument e = new BaseDocument();
			e.setKey("1");
			values.add(e);
		}
		db.collection(COLLECTION_NAME).insert(values, null);
		final Collection<BaseDocument> updatedValues = new ArrayList<>();
		final BaseDocument first = values.stream().findFirst().get();
		first.addAttribute("a", "test");
		updatedValues.add(first);
		final Collection<DocumentUpdateResult<BaseDocument>> updateResult = db.collection(COLLECTION_NAME)
				.update(updatedValues, null);
		assertThat(updateResult.size(), is(1));
	}

	@Test
	public void updateManyEmpty() {
		final Collection<BaseDocument> values = new ArrayList<>();
		final Collection<DocumentUpdateResult<BaseDocument>> updateResult = db.collection(COLLECTION_NAME)
				.update(values, null);
		assertThat(updateResult.size(), is(0));
	}

	@Test
	public void updateManyWithoutKey() {
		final Collection<BaseDocument> values = new ArrayList<>();
		{
			final BaseDocument e = new BaseDocument();
			e.setKey("1");
			values.add(e);
		}
		db.collection(COLLECTION_NAME).insert(values, null);
		final Collection<BaseDocument> updatedValues = new ArrayList<>();
		values.stream().forEach(i -> {
			i.addAttribute("a", "test");
			updatedValues.add(i);
		});
		updatedValues.add(new BaseDocument());
		final Collection<DocumentUpdateResult<BaseDocument>> updateResult = db.collection(COLLECTION_NAME)
				.update(updatedValues, null);
		assertThat(updateResult.size(), is(2));// TODO expexted 1
	}

	@Test
	public void replaceMany() {
		final Collection<BaseDocument> values = new ArrayList<>();
		{
			final BaseDocument e = new BaseDocument();
			e.setKey("1");
			values.add(e);
		}
		{
			final BaseDocument e = new BaseDocument();
			e.setKey("2");
			values.add(e);
		}
		db.collection(COLLECTION_NAME).insert(values, null);
		final Collection<BaseDocument> updatedValues = new ArrayList<>();
		values.stream().forEach(i -> {
			i.addAttribute("a", "test");
			updatedValues.add(i);
		});
		final Collection<DocumentUpdateResult<BaseDocument>> updateResult = db.collection(COLLECTION_NAME)
				.replace(updatedValues, null);
		assertThat(updateResult.size(), is(2));
	}

	@Test
	public void replaceManyOne() {
		final Collection<BaseDocument> values = new ArrayList<>();
		{
			final BaseDocument e = new BaseDocument();
			e.setKey("1");
			values.add(e);
		}
		db.collection(COLLECTION_NAME).insert(values, null);
		final Collection<BaseDocument> updatedValues = new ArrayList<>();
		final BaseDocument first = values.stream().findFirst().get();
		first.addAttribute("a", "test");
		updatedValues.add(first);
		final Collection<DocumentUpdateResult<BaseDocument>> updateResult = db.collection(COLLECTION_NAME)
				.update(updatedValues, null);
		assertThat(updateResult.size(), is(1));
	}

	@Test
	public void replaceManyEmpty() {
		final Collection<BaseDocument> values = new ArrayList<>();
		final Collection<DocumentUpdateResult<BaseDocument>> updateResult = db.collection(COLLECTION_NAME)
				.update(values, null);
		assertThat(updateResult.size(), is(0));
	}

	@Test
	public void replaceManyWithoutKey() {
		final Collection<BaseDocument> values = new ArrayList<>();
		{
			final BaseDocument e = new BaseDocument();
			e.setKey("1");
			values.add(e);
		}
		db.collection(COLLECTION_NAME).insert(values, null);
		final Collection<BaseDocument> updatedValues = new ArrayList<>();
		values.stream().forEach(i -> {
			i.addAttribute("a", "test");
			updatedValues.add(i);
		});
		updatedValues.add(new BaseDocument());
		final Collection<DocumentUpdateResult<BaseDocument>> updateResult = db.collection(COLLECTION_NAME)
				.update(updatedValues, null);
		assertThat(updateResult.size(), is(2));// TODO expexted 1
	}

	@Test
	public void load() {
		final CollectionResult result = db.collection(COLLECTION_NAME).load();
		assertThat(result.getName(), is(COLLECTION_NAME));
	}

	@Test
	public void unload() {
		final CollectionResult result = db.collection(COLLECTION_NAME).unload();
		assertThat(result.getName(), is(COLLECTION_NAME));
	}

}
