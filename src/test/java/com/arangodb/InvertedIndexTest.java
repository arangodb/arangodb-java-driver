package com.arangodb;

import com.arangodb.entity.*;
import com.arangodb.entity.arangosearch.*;
import com.arangodb.entity.arangosearch.analyzer.DelimiterAnalyzer;
import com.arangodb.entity.arangosearch.analyzer.DelimiterAnalyzerProperties;
import com.arangodb.model.InvertedIndexOptions;
import com.arangodb.model.PersistentIndexOptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.*;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class InvertedIndexTest extends BaseJunit5 {

    private static final String COLLECTION_NAME = "InvertedIndexTest_collection";

    private static Stream<Arguments> cols() {
        return dbsStream().map(db -> db.collection(COLLECTION_NAME)).map(Arguments::of);
    }

    @BeforeAll
    static void init() {
        initCollections(COLLECTION_NAME);
    }

    private void createAnalyzer(String analyzerName, ArangoDatabase db) {
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

        db.createSearchAnalyzer(da);
    }

    private InvertedIndexOptions createOptions(String analyzerName) {
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
                .consolidationPolicy(ConsolidationPolicy.of(ConsolidationType.TIER)
                        .segmentsMin(3L)
                        .segmentsMax(44L)
                        .segmentsBytesMax(55555L)
                        .segmentsBytesFloor(666L)
                        .minScore(77L)
                )
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

    @ParameterizedTest(name = "{index}")
    @MethodSource("cols")
    void createAndGetInvertedIndex(ArangoCollection collection) {
        assumeTrue(isAtLeastVersion(3, 10));
        String analyzerName = "delimiter-" + UUID.randomUUID();
        createAnalyzer(analyzerName, collection.db());
        InvertedIndexOptions options = createOptions(analyzerName);
        InvertedIndexEntity created = collection.ensureInvertedIndex(options);
        assertCorrectIndexEntity(created, options);
        InvertedIndexEntity loadedIndex = collection.getInvertedIndex(created.getName());
        assertCorrectIndexEntity(loadedIndex, options);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("cols")
    void getInvertedIndexesShouldNotReturnOtherIndexTypes(ArangoCollection collection) {
        assumeTrue(isAtLeastVersion(3, 10));

        // create persistent index
        collection.ensurePersistentIndex(Collections.singletonList("foo"), new PersistentIndexOptions().name("persistentIndex"));

        // create inverted index
        String analyzerName = "delimiter-" + UUID.randomUUID();
        createAnalyzer(analyzerName, collection.db());
        InvertedIndexOptions options = createOptions(analyzerName);
        InvertedIndexEntity created = collection.ensureInvertedIndex(options);

        Collection<InvertedIndexEntity> loadedIndexes = collection.getInvertedIndexes();
        assertThat(loadedIndexes).map(InvertedIndexEntity::getName)
                .doesNotContain("persistentIndex")
                .contains(created.getName());
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("cols")
    void getIndexesShouldNotReturnInvertedIndexes(ArangoCollection collection) {
        assumeTrue(isAtLeastVersion(3, 10));

        // create persistent index
        collection.ensurePersistentIndex(Collections.singletonList("foo"), new PersistentIndexOptions().name("persistentIndex"));

        // create inverted index
        String analyzerName = "delimiter-" + UUID.randomUUID();
        createAnalyzer(analyzerName, collection.db());
        InvertedIndexOptions options = createOptions(analyzerName);
        InvertedIndexEntity created = collection.ensureInvertedIndex(options);

        Collection<IndexEntity> loadedIndexes = collection.getIndexes();
        assertThat(loadedIndexes).map(IndexEntity::getName)
                .doesNotContain(created.getName())
                .contains("persistentIndex");
    }

}
