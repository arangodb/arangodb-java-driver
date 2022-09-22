package com.arangodb.async;

import com.arangodb.entity.*;
import com.arangodb.entity.arangosearch.*;
import com.arangodb.entity.arangosearch.analyzer.DelimiterAnalyzer;
import com.arangodb.entity.arangosearch.analyzer.DelimiterAnalyzerProperties;
import com.arangodb.model.InvertedIndexOptions;
import com.arangodb.model.PersistentIndexOptions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class InvertedIndexTest extends BaseTest {

    private static final String COLLECTION_NAME = "InvertedIndexTestAsync_collection";

    InvertedIndexTest() throws ExecutionException, InterruptedException {
        ArangoCollectionAsync collection = db.collection(COLLECTION_NAME);
        if (!collection.exists().get()) {
            collection.create().get();
        }
    }

    @BeforeAll
    static void setup() throws InterruptedException, ExecutionException {
        db.createCollection(COLLECTION_NAME, null).get();
    }

    @AfterEach
    void teardown() throws InterruptedException, ExecutionException {
        db.collection(COLLECTION_NAME).drop().get();
    }


    private void createAnalyzer(String analyzerName, ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        Set<AnalyzerFeature> features = new HashSet<>();
        features.add(AnalyzerFeature.frequency);
        features.add(AnalyzerFeature.norm);
        features.add(AnalyzerFeature.position);

        DelimiterAnalyzer da = new DelimiterAnalyzer();
        da.setName(analyzerName);
        da.setFeatures(features);
        DelimiterAnalyzerProperties props = new DelimiterAnalyzerProperties();
        props.setDelimiter("-");
        da.setProperties(props);

        db.createSearchAnalyzer(da).get();
    }

    private InvertedIndexOptions createOptions(String analyzerName) throws ExecutionException, InterruptedException {
        InvertedIndexField field = new InvertedIndexField()
                .name("foo")
                .analyzer(AnalyzerType.identity.toString())
                .includeAllFields(true)
                .searchField(false)
                .trackListPositions(false)
                .features(
                        AnalyzerFeature.position,
                        AnalyzerFeature.frequency,
                        AnalyzerFeature.norm,
                        AnalyzerFeature.offset
                );

        if (isEnterprise()) {
            field.nested(
                    new InvertedIndexField()
                            .name("bar")
                            .analyzer(analyzerName)
                            .searchField(true)
                            .features(AnalyzerFeature.position, AnalyzerFeature.frequency)
                            .nested(
                                    new InvertedIndexField()
                                            .name("baz")
                                            .analyzer(AnalyzerType.identity.toString())
                                            .searchField(false)
                                            .features(AnalyzerFeature.frequency)
                            )
            );
        }

        return new InvertedIndexOptions()
                .name("invertedIndex-" + UUID.randomUUID())
                .inBackground(true)
                .parallelism(5)
                .primarySort(new InvertedIndexPrimarySort()
                        .fields(
                                new InvertedIndexPrimarySort.Field("f1", InvertedIndexPrimarySort.Field.Direction.asc),
                                new InvertedIndexPrimarySort.Field("f2", InvertedIndexPrimarySort.Field.Direction.desc)
                        )
                        .compression(ArangoSearchCompression.lz4)
                )
                .storedValues(new StoredValue(Arrays.asList("f3", "f4"), ArangoSearchCompression.none))
                .analyzer(analyzerName)
                .features(AnalyzerFeature.position, AnalyzerFeature.frequency)
                .includeAllFields(false)
                .trackListPositions(true)
                .searchField(true)
                .fields(field)
                .consolidationIntervalMsec(11L)
                .commitIntervalMsec(22L)
                .cleanupIntervalStep(33L)
                .consolidationPolicy(ConsolidationPolicy.of(ConsolidationType.BYTES_ACCUM).threshold(1.))
                .writebufferIdle(44L)
                .writebufferActive(55L)
                .writebufferSizeMax(66L);
    }

    private void assertCorrectIndexEntity(InvertedIndexEntity indexResult, InvertedIndexOptions options) {
        assertThat(indexResult).isNotNull();
        assertThat(indexResult.getId()).isNotNull().isNotEmpty();
        // FIXME: in single server this is null
        // assertThat(indexResult.getIsNewlyCreated()).isTrue();
        assertThat(indexResult.getUnique()).isFalse();
        assertThat(indexResult.getSparse()).isTrue();
        assertThat(indexResult.getVersion()).isNotNull();
        assertThat(indexResult.getCode()).isNotNull();
        assertThat(indexResult.getType()).isEqualTo(IndexType.inverted);
        assertThat(indexResult.getName()).isEqualTo(options.getName());
        assertThat(indexResult.getFields()).containsExactlyElementsOf(options.getFields());
        assertThat(indexResult.getSearchField()).isEqualTo(options.getSearchField());
        assertThat(indexResult.getStoredValues()).containsExactlyElementsOf(options.getStoredValues());
        assertThat(indexResult.getPrimarySort()).isEqualTo(options.getPrimarySort());
        assertThat(indexResult.getAnalyzer()).isEqualTo(options.getAnalyzer());
        assertThat(indexResult.getFeatures()).hasSameElementsAs(options.getFeatures());
        assertThat(indexResult.getIncludeAllFields()).isEqualTo(options.getIncludeAllFields());
        assertThat(indexResult.getTrackListPositions()).isEqualTo(options.getTrackListPositions());
        assertThat(indexResult.getCleanupIntervalStep()).isEqualTo(options.getCleanupIntervalStep());
        assertThat(indexResult.getCommitIntervalMsec()).isEqualTo(options.getCommitIntervalMsec());
        assertThat(indexResult.getConsolidationIntervalMsec()).isEqualTo(options.getConsolidationIntervalMsec());
        assertThat(indexResult.getConsolidationPolicy()).isEqualTo(options.getConsolidationPolicy());
        assertThat(indexResult.getWritebufferIdle()).isEqualTo(options.getWritebufferIdle());
        assertThat(indexResult.getWritebufferActive()).isEqualTo(options.getWritebufferActive());
        assertThat(indexResult.getWritebufferSizeMax()).isEqualTo(options.getWritebufferSizeMax());
    }

   @Test
    void createAndGetInvertedIndex() throws ExecutionException, InterruptedException {
        assumeTrue(isAtLeastVersion(3, 10));

       ArangoCollectionAsync collection = db.collection(COLLECTION_NAME);
        String analyzerName = "delimiter-" + UUID.randomUUID();
        createAnalyzer(analyzerName, collection.db());
        InvertedIndexOptions options = createOptions(analyzerName);
        InvertedIndexEntity created = collection.ensureInvertedIndex(options).get();
        assertCorrectIndexEntity(created, options);
        InvertedIndexEntity loadedIndex = collection.getInvertedIndex(created.getName()).get();
        assertCorrectIndexEntity(loadedIndex, options);
    }

    @Test
    void getInvertedIndexesShouldNotReturnOtherIndexTypes() throws ExecutionException, InterruptedException {
        assumeTrue(isAtLeastVersion(3, 10));

        ArangoCollectionAsync collection = db.collection(COLLECTION_NAME);

        // create persistent index
        collection.ensurePersistentIndex(Collections.singletonList("foo"), new PersistentIndexOptions().name("persistentIndex"));

        // create inverted index
        String analyzerName = "delimiter-" + UUID.randomUUID();
        createAnalyzer(analyzerName, collection.db());
        InvertedIndexOptions options = createOptions(analyzerName);
        InvertedIndexEntity created = collection.ensureInvertedIndex(options).get();

        Collection<InvertedIndexEntity> loadedIndexes = collection.getInvertedIndexes().get();
        assertThat(loadedIndexes).map(InvertedIndexEntity::getName)
                .doesNotContain("persistentIndex")
                .contains(created.getName());
    }

    @Test
    void getIndexesShouldNotReturnInvertedIndexes() throws ExecutionException, InterruptedException {
        assumeTrue(isAtLeastVersion(3, 10));

        ArangoCollectionAsync collection = db.collection(COLLECTION_NAME);

        // create persistent index
        collection.ensurePersistentIndex(Collections.singletonList("foo"), new PersistentIndexOptions().name("persistentIndex"));

        // create inverted index
        String analyzerName = "delimiter-" + UUID.randomUUID();
        createAnalyzer(analyzerName, collection.db());
        InvertedIndexOptions options = createOptions(analyzerName);
        InvertedIndexEntity created = collection.ensureInvertedIndex(options).get();

        Collection<IndexEntity> loadedIndexes = collection.getIndexes().get();
        assertThat(loadedIndexes).map(IndexEntity::getName)
                .doesNotContain(created.getName())
                .contains("persistentIndex");
    }

}
