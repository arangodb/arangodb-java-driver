/*
 * DISCLAIMER
 *
 * Copyright 2018 ArangoDB GmbH, Cologne, Germany
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Copyright holder is ArangoDB GmbH, Cologne, Germany
 */

package com.arangodb;

import com.arangodb.entity.InvertedIndexField;
import com.arangodb.entity.ViewEntity;
import com.arangodb.entity.ViewType;
import com.arangodb.entity.arangosearch.*;
import com.arangodb.entity.arangosearch.analyzer.*;
import com.arangodb.model.InvertedIndexOptions;
import com.arangodb.model.arangosearch.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * @author Mark Vollmary
 * @author Michele Rastelli
 */
class ArangoSearchTest extends BaseJunit5 {

    private static final String COLL_1 = "ArangoSearchTest_view_replace_prop";
    private static final String COLL_2 = "ArangoSearchTest_view_update_prop";

    @BeforeAll
    static void init() {
        initCollections(COLL_1, COLL_2);
    }

    @ParameterizedTest
    @MethodSource("dbs")
    void exists(ArangoDatabase db) {
        assumeTrue(isAtLeastVersion(3, 4));
        String viewName = rndName();
        db.createArangoSearch(viewName, new ArangoSearchCreateOptions());
        assertThat(db.arangoSearch(viewName).exists()).isTrue();
    }

    @ParameterizedTest
    @MethodSource("dbs")
    void createAndExistsSearchAlias(ArangoDatabase db) {
        assumeTrue(isAtLeastVersion(3, 10));
        String viewName = rndName();
        db.createSearchAlias(viewName, new SearchAliasCreateOptions());
        assertThat(db.arangoSearch(viewName).exists()).isTrue();
    }

    @ParameterizedTest
    @MethodSource("dbs")
    void getInfo(ArangoDatabase db) {
        assumeTrue(isAtLeastVersion(3, 4));
        String viewName = rndName();
        db.createArangoSearch(viewName, new ArangoSearchCreateOptions());
        final ViewEntity info = db.arangoSearch(viewName).getInfo();
        assertThat(info).isNotNull();
        assertThat(info.getId()).isNotNull();
        assertThat(info.getName()).isEqualTo(viewName);
        assertThat(info.getType()).isEqualTo(ViewType.ARANGO_SEARCH);
    }

    @ParameterizedTest
    @MethodSource("dbs")
    void drop(ArangoDatabase db) {
        assumeTrue(isAtLeastVersion(3, 4));
        String viewName = rndName();
        db.createArangoSearch(viewName, new ArangoSearchCreateOptions());
        final ArangoView view = db.arangoSearch(viewName);
        view.drop();
        assertThat(view.exists()).isFalse();
    }

    @ParameterizedTest
    @MethodSource("dbs")
    void rename(ArangoDatabase db) {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 4));
        String viewName = rndName();
        final String name = viewName + "_new";
        db.createArangoSearch(name, new ArangoSearchCreateOptions());
        db.arangoSearch(name).rename(viewName);
        assertThat(db.arangoSearch(name).exists()).isFalse();
        assertThat(db.arangoSearch(viewName).exists()).isTrue();
    }

    @ParameterizedTest
    @MethodSource("dbs")
    void createArangoSearchView(ArangoDatabase db) {
        assumeTrue(isAtLeastVersion(3, 4));
        String viewName = rndName();
        final ViewEntity info = db.arangoSearch(viewName).create();
        assertThat(info).isNotNull();
        assertThat(info.getId()).isNotNull();
        assertThat(info.getName()).isEqualTo(viewName);
        assertThat(info.getType()).isEqualTo(ViewType.ARANGO_SEARCH);
        assertThat(db.arangoSearch(viewName).exists()).isTrue();
    }

    @ParameterizedTest
    @MethodSource("dbs")
    void createSearchAliasView(ArangoDatabase db) {
        assumeTrue(isAtLeastVersion(3, 10));
        String viewName = rndName();
        final ViewEntity info = db.searchAlias(viewName).create();
        assertThat(info).isNotNull();
        assertThat(info.getId()).isNotNull();
        assertThat(info.getName()).isEqualTo(viewName);
        assertThat(info.getType()).isEqualTo(ViewType.SEARCH_ALIAS);
        assertThat(db.searchAlias(viewName).exists()).isTrue();
    }

    @ParameterizedTest
    @MethodSource("dbs")
    void createArangoSearchViewWithOptions(ArangoDatabase db) {
        assumeTrue(isAtLeastVersion(3, 4));
        String viewName = rndName();
        final ArangoSearchCreateOptions options = new ArangoSearchCreateOptions();
        final ViewEntity info = db.arangoSearch(viewName).create(options);
        assertThat(info).isNotNull();
        assertThat(info.getId()).isNotNull();
        assertThat(info.getName()).isEqualTo(viewName);
        assertThat(info.getType()).isEqualTo(ViewType.ARANGO_SEARCH);
        assertThat(db.arangoSearch(viewName).exists()).isTrue();
    }

    @ParameterizedTest
    @MethodSource("dbs")
    void createArangoSearchViewWithPrimarySort(ArangoDatabase db) {
        assumeTrue(isAtLeastVersion(3, 5));
        String viewName = rndName();
        final ArangoSearchCreateOptions options = new ArangoSearchCreateOptions();

        final PrimarySort primarySort = PrimarySort.on("myFieldName");
        primarySort.ascending(false);
        options.primarySort(primarySort);
        options.primarySortCompression(ArangoSearchCompression.none);
        options.consolidationIntervalMsec(666666L);
        StoredValue storedValue = new StoredValue(Arrays.asList("a", "b"), ArangoSearchCompression.none);
        options.storedValues(storedValue);

        final ArangoSearch view = db.arangoSearch(viewName);
        final ViewEntity info = view.create(options);
        assertThat(info).isNotNull();
        assertThat(info.getId()).isNotNull();
        assertThat(info.getName()).isEqualTo(viewName);
        assertThat(info.getType()).isEqualTo(ViewType.ARANGO_SEARCH);
        assertThat(db.arangoSearch(viewName).exists()).isTrue();

        if (isAtLeastVersion(3, 7)) {
            final ArangoSearchPropertiesEntity properties = view.getProperties();
            assertThat(properties.getPrimarySortCompression()).isEqualTo(ArangoSearchCompression.none);
            Collection<StoredValue> retrievedStoredValues = properties.getStoredValues();
            assertThat(retrievedStoredValues).isNotNull();
            assertThat(retrievedStoredValues).hasSize(1);
            StoredValue retrievedStoredValue = retrievedStoredValues.iterator().next();
            assertThat(retrievedStoredValue).isNotNull();
            assertThat(retrievedStoredValue.getFields()).isEqualTo(storedValue.getFields());
            assertThat(retrievedStoredValue.getCompression()).isEqualTo(storedValue.getCompression());
            assertThat(properties.getPrimarySort())
                    .hasSize(1)
                    .allSatisfy(ps -> {
                        assertThat(ps).isNotNull();
                        assertThat(ps.getField()).isEqualTo(primarySort.getField());
                        assertThat(ps.getAscending()).isEqualTo(primarySort.getAscending());
                    });
        }
    }

    @ParameterizedTest
    @MethodSource("dbs")
    void createArangoSearchViewWithCommitIntervalMsec(ArangoDatabase db) {
        assumeTrue(isAtLeastVersion(3, 5));
        String viewName = rndName();
        final ArangoSearchCreateOptions options = new ArangoSearchCreateOptions();
        options.commitIntervalMsec(666666L);

        final ViewEntity info = db.arangoSearch(viewName).create(options);
        assertThat(info).isNotNull();
        assertThat(info.getId()).isNotNull();
        assertThat(info.getName()).isEqualTo(viewName);
        assertThat(info.getType()).isEqualTo(ViewType.ARANGO_SEARCH);
        assertThat(db.arangoSearch(viewName).exists()).isTrue();

        // check commit interval msec property
        final ArangoSearch view = db.arangoSearch(viewName);
        final ArangoSearchPropertiesEntity properties = view.getProperties();
        assertThat(properties.getCommitIntervalMsec()).isEqualTo(666666L);
    }

    @ParameterizedTest
    @MethodSource("dbs")
    void createSearchAliasViewWithOptions(ArangoDatabase db) {
        assumeTrue(isAtLeastVersion(3, 10));
        String viewName = rndName();
        final SearchAliasCreateOptions options = new SearchAliasCreateOptions();
        final ViewEntity info = db.searchAlias(viewName).create(options);
        assertThat(info).isNotNull();
        assertThat(info.getId()).isNotNull();
        assertThat(info.getName()).isEqualTo(viewName);
        assertThat(info.getType()).isEqualTo(ViewType.SEARCH_ALIAS);
        assertThat(db.searchAlias(viewName).exists()).isTrue();
    }

    @ParameterizedTest
    @MethodSource("dbs")
    void createSearchAliasViewWithIndexesAndGetProperties(ArangoDatabase db) {
        assumeTrue(isAtLeastVersion(3, 10));
        ArangoCollection col = db.collection(COLL_1);
        String idxName1 = rndName();
        col.ensureInvertedIndex(new InvertedIndexOptions()
                .name(idxName1)
                .fields(new InvertedIndexField().name("a" + rnd())));

        String idxName2 = rndName();
        col.ensureInvertedIndex(new InvertedIndexOptions()
                .name(idxName2)
                .fields(new InvertedIndexField().name("a" + rnd())));

        String viewName = rndName();
        final SearchAliasCreateOptions options = new SearchAliasCreateOptions()
                .indexes(
                        new SearchAliasIndex(COLL_1, idxName1, SearchAliasIndex.OperationType.add),
                        new SearchAliasIndex(COLL_1, idxName2, SearchAliasIndex.OperationType.add),
                        new SearchAliasIndex(COLL_1, idxName2, SearchAliasIndex.OperationType.del)
                );
        final ViewEntity info = db.searchAlias(viewName).create(options);
        assertThat(info).isNotNull();
        assertThat(info.getId()).isNotNull();
        assertThat(info.getName()).isEqualTo(viewName);
        assertThat(info.getType()).isEqualTo(ViewType.SEARCH_ALIAS);

        final SearchAliasPropertiesEntity properties = db.searchAlias(viewName).getProperties();
        assertThat(properties).isNotNull();
        assertThat(properties.getId()).isNotNull();
        assertThat(properties.getName()).isEqualTo(viewName);
        assertThat(properties.getType()).isEqualTo(ViewType.SEARCH_ALIAS);
        assertThat(properties.getIndexes())
                .isNotNull()
                .isNotEmpty()
                .anyMatch(i -> i.getCollection().equals(COLL_1) && i.getIndex().equals(idxName1));
    }

    @ParameterizedTest
    @MethodSource("dbs")
    void getArangoSearchViewProperties(ArangoDatabase db) {
        assumeTrue(isAtLeastVersion(3, 4));
        String viewName = rndName();
        final ArangoSearch view = db.arangoSearch(viewName);
        view.create(new ArangoSearchCreateOptions());
        final ArangoSearchPropertiesEntity properties = view.getProperties();
        assertThat(properties).isNotNull();
        assertThat(properties.getId()).isNotNull();
        assertThat(properties.getName()).isEqualTo(viewName);
        assertThat(properties.getType()).isEqualTo(ViewType.ARANGO_SEARCH);
        assertThat(properties.getConsolidationIntervalMsec()).isNotNull();
        assertThat(properties.getCleanupIntervalStep()).isNotNull();
        final ConsolidationPolicy consolidate = properties.getConsolidationPolicy();
        assertThat(consolidate).isNotNull();
        final Collection<CollectionLink> links = properties.getLinks();
        assertThat(links).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("dbs")
    void updateArangoSearchViewProperties(ArangoDatabase db) {
        assumeTrue(isAtLeastVersion(3, 4));
        String viewName = rndName();
        final ArangoSearch view = db.arangoSearch(viewName);
        view.create(new ArangoSearchCreateOptions());
        final ArangoSearchPropertiesOptions options = new ArangoSearchPropertiesOptions();
        options.cleanupIntervalStep(15L);
        options.consolidationIntervalMsec(65000L);
        options.consolidationPolicy(ConsolidationPolicy.of(ConsolidationType.BYTES_ACCUM).threshold(1.));
        options.link(CollectionLink.on(COLL_2)
                .fields(FieldLink.on("value").analyzers("identity").trackListPositions(true).includeAllFields(true)
                        .storeValues(StoreValuesType.ID)));
        final ArangoSearchPropertiesEntity properties = view.updateProperties(options);
        assertThat(properties).isNotNull();
        assertThat(properties.getCleanupIntervalStep()).isEqualTo(15L);
        assertThat(properties.getConsolidationIntervalMsec()).isEqualTo(65000L);
        final ConsolidationPolicy consolidate = properties.getConsolidationPolicy();
        assertThat(consolidate).isNotNull();
        assertThat(consolidate.getType()).isEqualTo(ConsolidationType.BYTES_ACCUM);
        assertThat(consolidate.getThreshold()).isEqualTo(1.);
        assertThat(properties.getLinks()).hasSize(1);
        final CollectionLink link = properties.getLinks().iterator().next();
        assertThat(link.getName()).isEqualTo(COLL_2);
        assertThat(link.getFields()).hasSize(1);
        final FieldLink next = link.getFields().iterator().next();
        assertThat(next.getName()).isEqualTo("value");
        assertThat(next.getIncludeAllFields()).isTrue();
        assertThat(next.getTrackListPositions()).isTrue();
        assertThat(next.getStoreValues()).isEqualTo(StoreValuesType.ID);
    }

    @ParameterizedTest
    @MethodSource("dbs")
    void updateSearchAliasViewWithIndexesAndGetProperties(ArangoDatabase db) {
        assumeTrue(isAtLeastVersion(3, 10));
        ArangoCollection col = db.collection(COLL_1);
        String idxName = rndName();
        col.ensureInvertedIndex(new InvertedIndexOptions()
                .name(idxName)
                .fields(new InvertedIndexField().name("a" + rnd())));
        ArangoCollection col2 = db.collection(COLL_2);
        String idxName2 = rndName();
        col2.ensureInvertedIndex(new InvertedIndexOptions()
                .name(idxName2)
                .fields(new InvertedIndexField().name("a" + rnd())));

        String viewName = rndName();
        final SearchAliasCreateOptions options = new SearchAliasCreateOptions()
                .indexes(new SearchAliasIndex(COLL_1, idxName));
        final ViewEntity info = db.searchAlias(viewName).create(options);
        db.searchAlias(viewName).updateProperties(new SearchAliasPropertiesOptions()
                .indexes(new SearchAliasIndex(COLL_2, idxName2)));

        assertThat(info).isNotNull();
        assertThat(info.getId()).isNotNull();
        assertThat(info.getName()).isEqualTo(viewName);
        assertThat(info.getType()).isEqualTo(ViewType.SEARCH_ALIAS);

        final SearchAliasPropertiesEntity properties = db.searchAlias(viewName).getProperties();
        assertThat(properties).isNotNull();
        assertThat(properties.getId()).isNotNull();
        assertThat(properties.getName()).isEqualTo(viewName);
        assertThat(properties.getType()).isEqualTo(ViewType.SEARCH_ALIAS);
        assertThat(properties.getIndexes())
                .isNotNull()
                .isNotEmpty()
                .hasSize(2)
                .anyMatch(i -> i.getCollection().equals(COLL_1) && i.getIndex().equals(idxName))
                .anyMatch(i -> i.getCollection().equals(COLL_2) && i.getIndex().equals(idxName2));
    }

    @ParameterizedTest
    @MethodSource("dbs")
    void replaceArangoSearchViewProperties(ArangoDatabase db) {
        assumeTrue(isAtLeastVersion(3, 4));
        String viewName = rndName();
        final ArangoSearch view = db.arangoSearch(viewName);
        view.create(new ArangoSearchCreateOptions());
        final ArangoSearchPropertiesOptions options = new ArangoSearchPropertiesOptions();
        options.link(CollectionLink.on(COLL_1)
                .fields(FieldLink.on("value").analyzers("identity")));
        final ArangoSearchPropertiesEntity properties = view.replaceProperties(options);
        assertThat(properties).isNotNull();
        assertThat(properties.getLinks()).hasSize(1);
        final CollectionLink link = properties.getLinks().iterator().next();
        assertThat(link.getName()).isEqualTo(COLL_1);
        assertThat(link.getFields()).hasSize(1);
        assertThat(link.getFields().iterator().next().getName()).isEqualTo("value");
    }

    @ParameterizedTest
    @MethodSource("dbs")
    void replaceSearchAliasViewWithIndexesAndGetProperties(ArangoDatabase db) {
        assumeTrue(isAtLeastVersion(3, 10));
        ArangoCollection col = db.collection(COLL_1);
        String idxName = rndName();
        col.ensureInvertedIndex(new InvertedIndexOptions()
                .name(idxName)
                .fields(new InvertedIndexField().name("a" + rnd())));
        ArangoCollection col2 = db.collection(COLL_2);
        String idxName2 = rndName();
        col2.ensureInvertedIndex(new InvertedIndexOptions()
                .name(idxName2)
                .fields(new InvertedIndexField().name("a" + rnd())));

        String viewName = rndName();
        final SearchAliasCreateOptions options = new SearchAliasCreateOptions()
                .indexes(new SearchAliasIndex(COLL_1, idxName));
        final ViewEntity info = db.searchAlias(viewName).create(options);
        db.searchAlias(viewName).replaceProperties(new SearchAliasPropertiesOptions()
                .indexes(new SearchAliasIndex(COLL_2, idxName2)));

        assertThat(info).isNotNull();
        assertThat(info.getId()).isNotNull();
        assertThat(info.getName()).isEqualTo(viewName);
        assertThat(info.getType()).isEqualTo(ViewType.SEARCH_ALIAS);

        final SearchAliasPropertiesEntity properties = db.searchAlias(viewName).getProperties();
        assertThat(properties).isNotNull();
        assertThat(properties.getId()).isNotNull();
        assertThat(properties.getName()).isEqualTo(viewName);
        assertThat(properties.getType()).isEqualTo(ViewType.SEARCH_ALIAS);
        assertThat(properties.getIndexes())
                .isNotNull()
                .isNotEmpty()
                .hasSize(1)
                .anyMatch(i -> i.getCollection().equals(COLL_2) && i.getIndex().equals(idxName2));
    }

    private void createGetAndDeleteTypedAnalyzer(ArangoDatabase db, SearchAnalyzer analyzer) {

        String fullyQualifiedName = db.name() + "::" + analyzer.getName();
        analyzer.setName(fullyQualifiedName);

        // createAnalyzer
        SearchAnalyzer createdAnalyzer = db.createSearchAnalyzer(analyzer);
        assertThat(createdAnalyzer).isEqualTo(analyzer);

        // getAnalyzer
        SearchAnalyzer gotAnalyzer = db.getSearchAnalyzer(analyzer.getName());
        assertThat(gotAnalyzer).isEqualTo(analyzer);

        // getAnalyzers
        SearchAnalyzer foundAnalyzer =
                db.getSearchAnalyzers().stream().filter(it -> it.getName().equals(fullyQualifiedName))
                        .findFirst().get();
        assertThat(foundAnalyzer).isEqualTo(analyzer);

        // deleteAnalyzer
        AnalyzerDeleteOptions deleteOptions = new AnalyzerDeleteOptions();
        deleteOptions.setForce(true);

        db.deleteSearchAnalyzer(analyzer.getName(), deleteOptions);

        Throwable thrown = catchThrowable(() -> db.getSearchAnalyzer(analyzer.getName()));
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
        ArangoDBException e = (ArangoDBException) thrown;
        assertThat(e.getResponseCode()).isEqualTo(404);
        assertThat(e.getErrorNum()).isEqualTo(1202);
    }

    @ParameterizedTest
    @MethodSource("dbs")
    void identityAnalyzerTyped(ArangoDatabase db) {
        assumeTrue(isAtLeastVersion(3, 5));

        String name = "test-" + UUID.randomUUID();

        Set<AnalyzerFeature> features = new HashSet<>();
        features.add(AnalyzerFeature.frequency);
        features.add(AnalyzerFeature.norm);
        features.add(AnalyzerFeature.position);

        IdentityAnalyzer analyzer = new IdentityAnalyzer();
        analyzer.setFeatures(features);
        analyzer.setName(name);

        createGetAndDeleteTypedAnalyzer(db, analyzer);
    }

    @ParameterizedTest
    @MethodSource("dbs")
    void delimiterAnalyzerTyped(ArangoDatabase db) {
        assumeTrue(isAtLeastVersion(3, 5));

        String name = "test-" + UUID.randomUUID();

        Set<AnalyzerFeature> features = new HashSet<>();
        features.add(AnalyzerFeature.frequency);
        features.add(AnalyzerFeature.norm);
        features.add(AnalyzerFeature.position);

        DelimiterAnalyzerProperties properties = new DelimiterAnalyzerProperties();
        properties.setDelimiter("-");

        DelimiterAnalyzer analyzer = new DelimiterAnalyzer();
        analyzer.setFeatures(features);
        analyzer.setName(name);
        analyzer.setProperties(properties);

        createGetAndDeleteTypedAnalyzer(db, analyzer);
    }

    @ParameterizedTest
    @MethodSource("dbs")
    void multiDelimiterAnalyzerTyped(ArangoDatabase db) {
        assumeTrue(isAtLeastVersion(3, 12));

        String name = "test-" + UUID.randomUUID();

        Set<AnalyzerFeature> features = new HashSet<>();
        features.add(AnalyzerFeature.frequency);
        features.add(AnalyzerFeature.norm);
        features.add(AnalyzerFeature.position);

        MultiDelimiterAnalyzerProperties properties = new MultiDelimiterAnalyzerProperties();
        properties.setDelimiters("-", ",", "...");

        MultiDelimiterAnalyzer analyzer = new MultiDelimiterAnalyzer();
        analyzer.setFeatures(features);
        analyzer.setName(name);
        analyzer.setProperties(properties);

        createGetAndDeleteTypedAnalyzer(db, analyzer);
    }

    @ParameterizedTest
    @MethodSource("dbs")
    void stemAnalyzerTyped(ArangoDatabase db) {
        assumeTrue(isAtLeastVersion(3, 5));

        String name = "test-" + UUID.randomUUID();

        Set<AnalyzerFeature> features = new HashSet<>();
        features.add(AnalyzerFeature.frequency);
        features.add(AnalyzerFeature.norm);
        features.add(AnalyzerFeature.position);

        StemAnalyzerProperties properties = new StemAnalyzerProperties();
        properties.setLocale("ru");

        StemAnalyzer options = new StemAnalyzer();
        options.setFeatures(features);
        options.setName(name);
        options.setProperties(properties);

        createGetAndDeleteTypedAnalyzer(db, options);
    }

    @ParameterizedTest
    @MethodSource("dbs")
    void normAnalyzerTyped(ArangoDatabase db) {
        assumeTrue(isAtLeastVersion(3, 5));

        String name = "test-" + UUID.randomUUID();

        Set<AnalyzerFeature> features = new HashSet<>();
        features.add(AnalyzerFeature.frequency);
        features.add(AnalyzerFeature.norm);
        features.add(AnalyzerFeature.position);

        NormAnalyzerProperties properties = new NormAnalyzerProperties();
        properties.setLocale("ru");
        properties.setAnalyzerCase(SearchAnalyzerCase.lower);
        properties.setAccent(true);

        NormAnalyzer options = new NormAnalyzer();
        options.setFeatures(features);
        options.setName(name);
        options.setProperties(properties);

        createGetAndDeleteTypedAnalyzer(db, options);
    }

    @ParameterizedTest
    @MethodSource("dbs")
    void ngramAnalyzerTyped(ArangoDatabase db) {
        assumeTrue(isAtLeastVersion(3, 5));

        String name = "test-" + UUID.randomUUID();

        Set<AnalyzerFeature> features = new HashSet<>();
        features.add(AnalyzerFeature.frequency);
        features.add(AnalyzerFeature.norm);
        features.add(AnalyzerFeature.position);

        NGramAnalyzerProperties properties = new NGramAnalyzerProperties();
        properties.setMax(6L);
        properties.setMin(3L);
        properties.setPreserveOriginal(true);

        NGramAnalyzer analyzer = new NGramAnalyzer();
        analyzer.setFeatures(features);
        analyzer.setName(name);
        analyzer.setType(AnalyzerType.ngram);
        analyzer.setProperties(properties);

        createGetAndDeleteTypedAnalyzer(db, analyzer);
    }

    @ParameterizedTest
    @MethodSource("dbs")
    void enhancedNgramAnalyzerTyped(ArangoDatabase db) {
        assumeTrue(isAtLeastVersion(3, 6));

        String name = "test-" + UUID.randomUUID();

        Set<AnalyzerFeature> features = new HashSet<>();
        features.add(AnalyzerFeature.frequency);
        features.add(AnalyzerFeature.norm);
        features.add(AnalyzerFeature.position);

        NGramAnalyzerProperties properties = new NGramAnalyzerProperties();
        properties.setMax(6L);
        properties.setMin(3L);
        properties.setPreserveOriginal(true);
        properties.setStartMarker("^");
        properties.setEndMarker("^");
        properties.setStreamType(StreamType.utf8);

        NGramAnalyzer analyzer = new NGramAnalyzer();
        analyzer.setFeatures(features);
        analyzer.setName(name);
        analyzer.setProperties(properties);

        createGetAndDeleteTypedAnalyzer(db, analyzer);
    }

    @ParameterizedTest
    @MethodSource("dbs")
    void textAnalyzerTyped(ArangoDatabase db) {
        assumeTrue(isAtLeastVersion(3, 5));

        String name = "test-" + UUID.randomUUID();

        Set<AnalyzerFeature> features = new HashSet<>();
        features.add(AnalyzerFeature.frequency);
        features.add(AnalyzerFeature.norm);
        features.add(AnalyzerFeature.position);

        TextAnalyzerProperties properties = new TextAnalyzerProperties();
        properties.setLocale("ru");
        properties.setAnalyzerCase(SearchAnalyzerCase.lower);
        properties.setAccent(true);
        properties.setStemming(true);

        TextAnalyzer analyzer = new TextAnalyzer();
        analyzer.setFeatures(features);
        analyzer.setName(name);
        analyzer.setType(AnalyzerType.text);
        analyzer.setProperties(properties);

        createGetAndDeleteTypedAnalyzer(db, analyzer);
    }

    @ParameterizedTest
    @MethodSource("dbs")
    void enhancedTextAnalyzerTyped(ArangoDatabase db) {
        assumeTrue(isAtLeastVersion(3, 6));

        String name = "test-" + UUID.randomUUID();

        Set<AnalyzerFeature> features = new HashSet<>();
        features.add(AnalyzerFeature.frequency);
        features.add(AnalyzerFeature.norm);
        features.add(AnalyzerFeature.position);

        EdgeNgram edgeNgram = new EdgeNgram();
        edgeNgram.setMin(2L);
        edgeNgram.setMax(100000L);
        edgeNgram.setPreserveOriginal(true);

        TextAnalyzerProperties properties = new TextAnalyzerProperties();
        properties.setLocale("ru");
        properties.setAnalyzerCase(SearchAnalyzerCase.lower);
        properties.setAccent(true);
        properties.setStemming(true);
        properties.setEdgeNgram(edgeNgram);

        TextAnalyzer analyzer = new TextAnalyzer();
        analyzer.setFeatures(features);
        analyzer.setName(name);
        analyzer.setProperties(properties);

        createGetAndDeleteTypedAnalyzer(db, analyzer);
    }

    @ParameterizedTest
    @MethodSource("dbs")
    void arangoSearchOptions(ArangoDatabase db) {
        assumeTrue(isAtLeastVersion(3, 4));
        String viewName = rndName();
        FieldLink field = FieldLink.on("f1")
                .inBackground(true)
                .cache(false);
        if (isEnterprise()) {
            field.nested(FieldLink.on("f2"));
        }
        CollectionLink link = CollectionLink.on(COLL_1)
                .analyzers("identity")
                .fields(field)
                .includeAllFields(true)
                .storeValues(StoreValuesType.ID)
                .trackListPositions(false)
                .inBackground(true)
                .cache(true);

        if (isEnterprise()) {
            link.nested(FieldLink.on("f3"));
        }
        ArangoSearchCreateOptions options = new ArangoSearchCreateOptions()
                .link(link)
                .primarySortCache(true)
                .primaryKeyCache(true);
        StoredValue storedValue = new StoredValue(Arrays.asList("a", "b"), ArangoSearchCompression.none, true);
        options.storedValues(storedValue);
        String[] optimizeTopK = new String[]{"BM25(@doc) DESC", "TFIDF(@doc) DESC"};
        options.optimizeTopK(optimizeTopK);

        final ArangoSearch view = db.arangoSearch(viewName);
        view.create(options);

        final ArangoSearchPropertiesEntity properties = view.getProperties();
        assertThat(properties).isNotNull();
        assertThat(properties.getId()).isNotNull();
        assertThat(properties.getName()).isEqualTo(viewName);
        assertThat(properties.getType()).isEqualTo(ViewType.ARANGO_SEARCH);
        assertThat(properties.getLinks()).isNotEmpty();

        CollectionLink createdLink = properties.getLinks().iterator().next();
        assertThat(createdLink.getName()).isEqualTo(COLL_1);
        assertThat(createdLink.getAnalyzers()).contains("identity");
        assertThat(createdLink.getIncludeAllFields()).isTrue();
        assertThat(createdLink.getStoreValues()).isEqualTo(StoreValuesType.ID);
        assertThat(createdLink.getTrackListPositions()).isFalse();

        FieldLink fieldLink = createdLink.getFields().iterator().next();
        if (isEnterprise()) {
            assertThat(createdLink.getCache()).isTrue();
            assertThat(fieldLink.getCache()).isFalse();
            assertThat(properties.getPrimaryKeyCache()).isTrue();
            assertThat(properties.getPrimarySortCache()).isTrue();
            assertThat(properties.getStoredValues())
                    .isNotEmpty()
                    .allSatisfy(it -> assertThat(it.getCache()).isTrue());
        }

        if (isEnterprise() && isAtLeastVersion(3, 10)) {
            assertThat(createdLink.getNested()).isNotEmpty();
            FieldLink nested = createdLink.getNested().iterator().next();
            assertThat(nested.getName()).isEqualTo("f3");
        }

        assertThat(fieldLink.getName()).isEqualTo("f1");
        if (isEnterprise() && isAtLeastVersion(3, 10)) {
            assertThat(fieldLink.getNested()).isNotEmpty();
            FieldLink nested = fieldLink.getNested().iterator().next();
            assertThat(nested.getName()).isEqualTo("f2");
        }

        if (isEnterprise() && isAtLeastVersion(3, 12)) {
            assertThat(properties.getOptimizeTopK()).containsExactly(optimizeTopK);
        }

    }

    @ParameterizedTest
    @MethodSource("dbs")
    void pipelineAnalyzer(ArangoDatabase db) {
        assumeTrue(isAtLeastVersion(3, 8));

        // comma delimiter
        DelimiterAnalyzerProperties commaDelimiterProperties = new DelimiterAnalyzerProperties();
        commaDelimiterProperties.setDelimiter(",");

        DelimiterAnalyzer commaDelimiter = new DelimiterAnalyzer();
        commaDelimiter.setProperties(commaDelimiterProperties);

        // semicolon delimiter
        DelimiterAnalyzerProperties semicolonDelimiterProperties = new DelimiterAnalyzerProperties();
        semicolonDelimiterProperties.setDelimiter(",");

        DelimiterAnalyzer semicolonDelimiter = new DelimiterAnalyzer();
        semicolonDelimiter.setProperties(semicolonDelimiterProperties);

        // stem
        StemAnalyzerProperties stemAnalyzerProperties = new StemAnalyzerProperties();
        stemAnalyzerProperties.setLocale("en");

        StemAnalyzer stemAnalyzer = new StemAnalyzer();
        stemAnalyzer.setProperties(stemAnalyzerProperties);

        // pipeline analyzer
        Set<AnalyzerFeature> features = new HashSet<>();
        features.add(AnalyzerFeature.frequency);
        features.add(AnalyzerFeature.norm);
        features.add(AnalyzerFeature.position);

        PipelineAnalyzerProperties properties = new PipelineAnalyzerProperties()
                .addAnalyzer(commaDelimiter)
                .addAnalyzer(semicolonDelimiter)
                .addAnalyzer(stemAnalyzer);

        PipelineAnalyzer pipelineAnalyzer = new PipelineAnalyzer();
        pipelineAnalyzer.setName("test-" + UUID.randomUUID());
        pipelineAnalyzer.setProperties(properties);
        pipelineAnalyzer.setFeatures(features);

        createGetAndDeleteTypedAnalyzer(db, pipelineAnalyzer);
    }

    @ParameterizedTest
    @MethodSource("dbs")
    void stopwordsAnalyzer(ArangoDatabase db) {
        assumeTrue(isAtLeastVersion(3, 8));

        Set<AnalyzerFeature> features = new HashSet<>();
        features.add(AnalyzerFeature.frequency);
        features.add(AnalyzerFeature.norm);
        features.add(AnalyzerFeature.position);

        StopwordsAnalyzerProperties properties = new StopwordsAnalyzerProperties()
                .addStopwordAsHex("616e64")
                .addStopwordAsString("the");

        assertThat(properties.getStopwordsAsStringList()).contains("and");
        assertThat(properties.getStopwordsAsHexList()).contains("746865");

        StopwordsAnalyzer analyzer = new StopwordsAnalyzer();
        String name = "test-" + UUID.randomUUID();
        analyzer.setName(name);
        analyzer.setProperties(properties);
        analyzer.setFeatures(features);

        createGetAndDeleteTypedAnalyzer(db, analyzer);
        db.createSearchAnalyzer(analyzer);
        Collection<String> res = db.query("RETURN FLATTEN(TOKENS(SPLIT('the fox and the dog and a theater', ' '), " +
                        "@aName))", Collection.class,
                Collections.singletonMap("aName", name)).next();
        assertThat(res).containsExactly("fox", "dog", "a", "theater");
        db.deleteSearchAnalyzer(name);
    }

    @ParameterizedTest
    @MethodSource("dbs")
    void aqlAnalyzer(ArangoDatabase db) {
        assumeTrue(isAtLeastVersion(3, 8));

        AQLAnalyzerProperties properties = new AQLAnalyzerProperties();
        properties.setBatchSize(2);
        properties.setCollapsePositions(true);
        properties.setKeepNull(false);
        properties.setMemoryLimit(2200L);
        properties.setQueryString("RETURN SOUNDEX(@param)");
        properties.setReturnType(AQLAnalyzerProperties.ReturnType.string);

        Set<AnalyzerFeature> features = new HashSet<>();
        features.add(AnalyzerFeature.frequency);
        features.add(AnalyzerFeature.norm);
        features.add(AnalyzerFeature.position);

        AQLAnalyzer aqlAnalyzer = new AQLAnalyzer();
        aqlAnalyzer.setName("test-" + UUID.randomUUID());
        aqlAnalyzer.setProperties(properties);
        aqlAnalyzer.setFeatures(features);

        createGetAndDeleteTypedAnalyzer(db, aqlAnalyzer);
    }

    @ParameterizedTest
    @MethodSource("dbs")
    void geoJsonAnalyzer(ArangoDatabase db) {
        assumeTrue(isAtLeastVersion(3, 8));

        GeoAnalyzerOptions options = new GeoAnalyzerOptions();
        options.setMaxLevel(10);
        options.setMaxCells(11);
        options.setMinLevel(8);

        GeoJSONAnalyzerProperties properties = new GeoJSONAnalyzerProperties();
        properties.setOptions(options);
        properties.setType(GeoJSONAnalyzerProperties.GeoJSONAnalyzerType.point);
        properties.setLegacy(true);

        Set<AnalyzerFeature> features = new HashSet<>();
        features.add(AnalyzerFeature.frequency);
        features.add(AnalyzerFeature.norm);
        features.add(AnalyzerFeature.position);

        GeoJSONAnalyzer geoJSONAnalyzer = new GeoJSONAnalyzer();
        geoJSONAnalyzer.setName("test-" + UUID.randomUUID());
        geoJSONAnalyzer.setProperties(properties);
        geoJSONAnalyzer.setFeatures(features);

        createGetAndDeleteTypedAnalyzer(db, geoJSONAnalyzer);
    }


    @ParameterizedTest
    @MethodSource("dbs")
    void geoS2Analyzer(ArangoDatabase db) {
        assumeTrue(isEnterprise());
        assumeTrue(isAtLeastVersion(3, 10, 5));

        GeoAnalyzerOptions options = new GeoAnalyzerOptions();
        options.setMaxLevel(10);
        options.setMaxCells(11);
        options.setMinLevel(8);

        GeoS2AnalyzerProperties properties = new GeoS2AnalyzerProperties();
        properties.setOptions(options);
        properties.setType(GeoS2AnalyzerProperties.GeoS2AnalyzerType.point);
        properties.setFormat(GeoS2AnalyzerProperties.GeoS2Format.s2Point);

        Set<AnalyzerFeature> features = new HashSet<>();
        features.add(AnalyzerFeature.frequency);
        features.add(AnalyzerFeature.norm);
        features.add(AnalyzerFeature.position);

        GeoS2Analyzer geoS2Analyzer = new GeoS2Analyzer();
        geoS2Analyzer.setName("test-" + UUID.randomUUID());
        geoS2Analyzer.setProperties(properties);
        geoS2Analyzer.setFeatures(features);

        createGetAndDeleteTypedAnalyzer(db, geoS2Analyzer);
    }


    @ParameterizedTest
    @MethodSource("dbs")
    void geoPointAnalyzer(ArangoDatabase db) {
        assumeTrue(isAtLeastVersion(3, 8));

        GeoAnalyzerOptions options = new GeoAnalyzerOptions();
        options.setMaxLevel(10);
        options.setMaxCells(11);
        options.setMinLevel(8);

        GeoPointAnalyzerProperties properties = new GeoPointAnalyzerProperties();
        properties.setLatitude(new String[]{"a", "b", "c"});
        properties.setLongitude(new String[]{"d", "e", "f"});
        properties.setOptions(options);

        Set<AnalyzerFeature> features = new HashSet<>();
        features.add(AnalyzerFeature.frequency);
        features.add(AnalyzerFeature.norm);
        features.add(AnalyzerFeature.position);

        GeoPointAnalyzer geoPointAnalyzer = new GeoPointAnalyzer();
        geoPointAnalyzer.setName("test-" + UUID.randomUUID());
        geoPointAnalyzer.setProperties(properties);
        geoPointAnalyzer.setFeatures(features);

        createGetAndDeleteTypedAnalyzer(db, geoPointAnalyzer);
    }


    @ParameterizedTest
    @MethodSource("dbs")
    void segmentationAnalyzer(ArangoDatabase db) {
        assumeTrue(isAtLeastVersion(3, 9));

        SegmentationAnalyzerProperties properties = new SegmentationAnalyzerProperties();
        properties.setBreakMode(SegmentationAnalyzerProperties.BreakMode.graphic);
        properties.setAnalyzerCase(SearchAnalyzerCase.upper);

        Set<AnalyzerFeature> features = new HashSet<>();
        features.add(AnalyzerFeature.frequency);
        features.add(AnalyzerFeature.norm);
        features.add(AnalyzerFeature.position);

        SegmentationAnalyzer segmentationAnalyzer = new SegmentationAnalyzer();
        segmentationAnalyzer.setName("test-" + UUID.randomUUID());
        segmentationAnalyzer.setProperties(properties);
        segmentationAnalyzer.setFeatures(features);

        createGetAndDeleteTypedAnalyzer(db, segmentationAnalyzer);
    }

    @ParameterizedTest
    @MethodSource("dbs")
    void collationAnalyzer(ArangoDatabase db) {
        assumeTrue(isAtLeastVersion(3, 9));

        CollationAnalyzerProperties properties = new CollationAnalyzerProperties();
        properties.setLocale("ru");

        Set<AnalyzerFeature> features = new HashSet<>();
        features.add(AnalyzerFeature.frequency);
        features.add(AnalyzerFeature.norm);
        features.add(AnalyzerFeature.position);

        CollationAnalyzer collationAnalyzer = new CollationAnalyzer();
        collationAnalyzer.setName("test-" + UUID.randomUUID());
        collationAnalyzer.setProperties(properties);
        collationAnalyzer.setFeatures(features);

        createGetAndDeleteTypedAnalyzer(db, collationAnalyzer);
    }


    @DisabledIfSystemProperty(named = "skipStatefulTests", matches = "^(|true|1)$", disabledReason = "Test requires server with analyzer model located at `/tmp/foo.bin`")
    @ParameterizedTest
    @MethodSource("dbs")
    void classificationAnalyzer(ArangoDatabase db) {
        assumeTrue(isAtLeastVersion(3, 10));
        assumeTrue(isEnterprise());

        ClassificationAnalyzerProperties properties = new ClassificationAnalyzerProperties();
        properties.setModelLocation("/tmp/foo.bin");
        properties.setTopK(2);
        properties.setThreshold(.5);

        Set<AnalyzerFeature> features = new HashSet<>();
        features.add(AnalyzerFeature.frequency);
        features.add(AnalyzerFeature.norm);
        features.add(AnalyzerFeature.position);

        ClassificationAnalyzer analyzer = new ClassificationAnalyzer();
        analyzer.setName("test-" + UUID.randomUUID());
        analyzer.setProperties(properties);
        analyzer.setFeatures(features);

        createGetAndDeleteTypedAnalyzer(db, analyzer);
    }

    @DisabledIfSystemProperty(named = "skipStatefulTests", matches = "^(|true|1)$", disabledReason = "Test requires server with analyzer model located at `/tmp/foo.bin`")
    @ParameterizedTest
    @MethodSource("dbs")
    void nearestNeighborsAnalyzer(ArangoDatabase db) {
        assumeTrue(isAtLeastVersion(3, 10));
        assumeTrue(isEnterprise());

        NearestNeighborsAnalyzerProperties properties = new NearestNeighborsAnalyzerProperties();
        properties.setModelLocation("/tmp/foo.bin");
        properties.setTopK(2);

        Set<AnalyzerFeature> features = new HashSet<>();
        features.add(AnalyzerFeature.frequency);
        features.add(AnalyzerFeature.norm);
        features.add(AnalyzerFeature.position);

        NearestNeighborsAnalyzer analyzer = new NearestNeighborsAnalyzer();
        analyzer.setName("test-" + UUID.randomUUID());
        analyzer.setProperties(properties);
        analyzer.setFeatures(features);

        createGetAndDeleteTypedAnalyzer(db, analyzer);
    }

    @ParameterizedTest
    @MethodSource("dbs")
    void MinHashAnalyzer(ArangoDatabase db) {
        assumeTrue(isAtLeastVersion(3, 10));
        assumeTrue(isEnterprise());

        SegmentationAnalyzerProperties segProperties = new SegmentationAnalyzerProperties();
        segProperties.setBreakMode(SegmentationAnalyzerProperties.BreakMode.alpha);
        segProperties.setAnalyzerCase(SearchAnalyzerCase.lower);

        SegmentationAnalyzer segAnalyzer = new SegmentationAnalyzer();
        segAnalyzer.setProperties(segProperties);

        MinHashAnalyzerProperties properties = new MinHashAnalyzerProperties();
        properties.setAnalyzer(segAnalyzer);
        properties.setNumHashes(2);

        Set<AnalyzerFeature> features = new HashSet<>();
        features.add(AnalyzerFeature.frequency);
        features.add(AnalyzerFeature.norm);
        features.add(AnalyzerFeature.position);

        MinHashAnalyzer analyzer = new MinHashAnalyzer();
        analyzer.setName("test-" + UUID.randomUUID());
        analyzer.setProperties(properties);
        analyzer.setFeatures(features);

        createGetAndDeleteTypedAnalyzer(db, analyzer);
    }

    @ParameterizedTest
    @MethodSource("dbs")
    void WildcardAnalyzer(ArangoDatabase db) {
        assumeTrue(isAtLeastVersion(3, 12));

        NormAnalyzerProperties properties = new NormAnalyzerProperties();
        properties.setLocale("ru");
        properties.setAnalyzerCase(SearchAnalyzerCase.lower);
        properties.setAccent(true);

        NormAnalyzer normAnalyzer = new NormAnalyzer();
        normAnalyzer.setProperties(properties);

        WildcardAnalyzerProperties wildcardProperties = new WildcardAnalyzerProperties();
        wildcardProperties.setNgramSize(3);
        wildcardProperties.setAnalyzer(normAnalyzer);

        Set<AnalyzerFeature> features = new HashSet<>();
        features.add(AnalyzerFeature.frequency);
        features.add(AnalyzerFeature.position);

        WildcardAnalyzer wildcardAnalyzer = new WildcardAnalyzer();
        wildcardAnalyzer.setName("test-" + UUID.randomUUID());
        wildcardAnalyzer.setProperties(wildcardProperties);
        wildcardAnalyzer.setFeatures(features);

        createGetAndDeleteTypedAnalyzer(db, wildcardAnalyzer);
    }

    @ParameterizedTest
    @MethodSource("dbs")
    void offsetFeature(ArangoDatabase db) {
        assumeTrue(isAtLeastVersion(3, 10));

        String name = "test-" + UUID.randomUUID();

        Set<AnalyzerFeature> features = new HashSet<>();
        features.add(AnalyzerFeature.frequency);
        features.add(AnalyzerFeature.norm);
        features.add(AnalyzerFeature.position);
        features.add(AnalyzerFeature.offset);

        IdentityAnalyzer analyzer = new IdentityAnalyzer();
        analyzer.setFeatures(features);
        analyzer.setName(name);

        createGetAndDeleteTypedAnalyzer(db, analyzer);
    }

}
