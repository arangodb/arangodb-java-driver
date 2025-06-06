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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.*;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * @author Mark Vollmary
 * @author Michele Rastelli
 */
class ArangoSearchAsyncTest extends BaseJunit5 {

    private static final String COLL_1 = "ArangoSearchTest_view_replace_prop";
    private static final String COLL_2 = "ArangoSearchTest_view_update_prop";

    @BeforeAll
    static void init() {
        initCollections(COLL_1, COLL_2);
    }

    @ParameterizedTest
    @MethodSource("asyncDbs")
    void exists(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        String viewName = rndName();
        db.createArangoSearch(viewName, new ArangoSearchCreateOptions()).get();
        assertThat(db.arangoSearch(viewName).exists().get()).isTrue();
    }

    @ParameterizedTest
    @MethodSource("asyncDbs")
    void createAndExistsSearchAlias(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        String viewName = rndName();
        db.createSearchAlias(viewName, new SearchAliasCreateOptions()).get();
        assertThat(db.arangoSearch(viewName).exists().get()).isTrue();
    }

    @ParameterizedTest
    @MethodSource("asyncDbs")
    void getInfo(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        String viewName = rndName();
        db.createArangoSearch(viewName, new ArangoSearchCreateOptions()).get();
        final ViewEntity info = db.arangoSearch(viewName).getInfo().get();
        assertThat(info).isNotNull();
        assertThat(info.getId()).isNotNull();
        assertThat(info.getName()).isEqualTo(viewName);
        assertThat(info.getType()).isEqualTo(ViewType.ARANGO_SEARCH);
    }

    @ParameterizedTest
    @MethodSource("asyncDbs")
    void drop(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        String viewName = rndName();
        db.createArangoSearch(viewName, new ArangoSearchCreateOptions()).get();
        final ArangoSearchAsync view = db.arangoSearch(viewName);
        view.drop().get();
        assertThat(view.exists().get()).isFalse();
    }

    @ParameterizedTest
    @MethodSource("asyncDbs")
    void rename(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        assumeTrue(isSingleServer());
        String viewName = rndName();
        final String name = viewName + "_new";
        db.createArangoSearch(name, new ArangoSearchCreateOptions()).get();
        db.arangoSearch(name).rename(viewName).get();
        assertThat(db.arangoSearch(name).exists().get()).isFalse();
        assertThat(db.arangoSearch(viewName).exists().get()).isTrue();
    }

    @ParameterizedTest
    @MethodSource("asyncDbs")
    void createArangoSearchView(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        String viewName = rndName();
        final ViewEntity info = db.arangoSearch(viewName).create().get();
        assertThat(info).isNotNull();
        assertThat(info.getId()).isNotNull();
        assertThat(info.getName()).isEqualTo(viewName);
        assertThat(info.getType()).isEqualTo(ViewType.ARANGO_SEARCH);
        assertThat(db.arangoSearch(viewName).exists().get()).isTrue();
    }

    @ParameterizedTest
    @MethodSource("asyncDbs")
    void createSearchAliasView(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        String viewName = rndName();
        final ViewEntity info = db.searchAlias(viewName).create().get();
        assertThat(info).isNotNull();
        assertThat(info.getId()).isNotNull();
        assertThat(info.getName()).isEqualTo(viewName);
        assertThat(info.getType()).isEqualTo(ViewType.SEARCH_ALIAS);
        assertThat(db.searchAlias(viewName).exists().get()).isTrue();
    }

    @ParameterizedTest
    @MethodSource("asyncDbs")
    void createArangoSearchViewWithOptions(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        String viewName = rndName();
        final ArangoSearchCreateOptions options = new ArangoSearchCreateOptions();
        final ViewEntity info = db.arangoSearch(viewName).create(options).get();
        assertThat(info).isNotNull();
        assertThat(info.getId()).isNotNull();
        assertThat(info.getName()).isEqualTo(viewName);
        assertThat(info.getType()).isEqualTo(ViewType.ARANGO_SEARCH);
        assertThat(db.arangoSearch(viewName).exists().get()).isTrue();
    }

    @ParameterizedTest
    @MethodSource("asyncDbs")
    void createArangoSearchViewWithPrimarySort(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        String viewName = rndName();
        final ArangoSearchCreateOptions options = new ArangoSearchCreateOptions();

        final PrimarySort primarySort = PrimarySort.on("myFieldName");
        primarySort.ascending(true);
        options.primarySort(primarySort);
        options.primarySortCompression(ArangoSearchCompression.none);
        options.consolidationIntervalMsec(666666L);
        StoredValue storedValue = new StoredValue(Arrays.asList("a", "b"), ArangoSearchCompression.none);
        options.storedValues(storedValue);

        final ArangoSearchAsync view = db.arangoSearch(viewName);
        final ViewEntity info = view.create(options).get();
        assertThat(info).isNotNull();
        assertThat(info.getId()).isNotNull();
        assertThat(info.getName()).isEqualTo(viewName);
        assertThat(info.getType()).isEqualTo(ViewType.ARANGO_SEARCH);
        assertThat(db.arangoSearch(viewName).exists().get()).isTrue();

        final ArangoSearchPropertiesEntity properties = view.getProperties().get();
        assertThat(properties.getPrimarySortCompression()).isEqualTo(ArangoSearchCompression.none);
        Collection<StoredValue> retrievedStoredValues = properties.getStoredValues();
        assertThat(retrievedStoredValues).isNotNull();
        assertThat(retrievedStoredValues).hasSize(1);
        StoredValue retrievedStoredValue = retrievedStoredValues.iterator().next();
        assertThat(retrievedStoredValue).isNotNull();
        assertThat(retrievedStoredValue.getFields()).isEqualTo(storedValue.getFields());
        assertThat(retrievedStoredValue.getCompression()).isEqualTo(storedValue.getCompression());
    }

    @ParameterizedTest
    @MethodSource("asyncDbs")
    void createArangoSearchViewWithCommitIntervalMsec(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        String viewName = rndName();
        final ArangoSearchCreateOptions options = new ArangoSearchCreateOptions();
        options.commitIntervalMsec(666666L);

        final ViewEntity info = db.arangoSearch(viewName).create(options).get();
        assertThat(info).isNotNull();
        assertThat(info.getId()).isNotNull();
        assertThat(info.getName()).isEqualTo(viewName);
        assertThat(info.getType()).isEqualTo(ViewType.ARANGO_SEARCH);
        assertThat(db.arangoSearch(viewName).exists().get()).isTrue();

        // check commit interval msec property
        final ArangoSearchAsync view = db.arangoSearch(viewName);
        final ArangoSearchPropertiesEntity properties = view.getProperties().get();
        assertThat(properties.getCommitIntervalMsec()).isEqualTo(666666L);
    }

    @ParameterizedTest
    @MethodSource("asyncDbs")
    void createSearchAliasViewWithOptions(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        String viewName = rndName();
        final SearchAliasCreateOptions options = new SearchAliasCreateOptions();
        final ViewEntity info = db.searchAlias(viewName).create(options).get();
        assertThat(info).isNotNull();
        assertThat(info.getId()).isNotNull();
        assertThat(info.getName()).isEqualTo(viewName);
        assertThat(info.getType()).isEqualTo(ViewType.SEARCH_ALIAS);
        assertThat(db.searchAlias(viewName).exists().get()).isTrue();
    }

    @ParameterizedTest
    @MethodSource("asyncDbs")
    void createSearchAliasViewWithIndexesAndGetProperties(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        ArangoCollectionAsync col = db.collection(COLL_1);
        String idxName1 = rndName();
        col.ensureInvertedIndex(new InvertedIndexOptions()
                .name(idxName1)
                .fields(new InvertedIndexField().name("a" + rnd()))).get();

        String idxName2 = rndName();
        col.ensureInvertedIndex(new InvertedIndexOptions()
                .name(idxName2)
                .fields(new InvertedIndexField().name("a" + rnd()))).get();

        String viewName = rndName();
        final SearchAliasCreateOptions options = new SearchAliasCreateOptions()
                .indexes(
                        new SearchAliasIndex(COLL_1, idxName1, SearchAliasIndex.OperationType.add),
                        new SearchAliasIndex(COLL_1, idxName2, SearchAliasIndex.OperationType.add),
                        new SearchAliasIndex(COLL_1, idxName2, SearchAliasIndex.OperationType.del)
                );
        final ViewEntity info = db.searchAlias(viewName).create(options).get();
        assertThat(info).isNotNull();
        assertThat(info.getId()).isNotNull();
        assertThat(info.getName()).isEqualTo(viewName);
        assertThat(info.getType()).isEqualTo(ViewType.SEARCH_ALIAS);

        final SearchAliasPropertiesEntity properties = db.searchAlias(viewName).getProperties().get();
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
    @MethodSource("asyncDbs")
    void getArangoSearchViewProperties(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        String viewName = rndName();
        final ArangoSearchAsync view = db.arangoSearch(viewName);
        view.create(new ArangoSearchCreateOptions()).get();
        final ArangoSearchPropertiesEntity properties = view.getProperties().get();
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
    @MethodSource("asyncDbs")
    void updateArangoSearchViewProperties(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        String viewName = rndName();
        final ArangoSearchAsync view = db.arangoSearch(viewName);
        view.create(new ArangoSearchCreateOptions()).get();
        final ArangoSearchPropertiesOptions options = new ArangoSearchPropertiesOptions();
        options.cleanupIntervalStep(15L);
        options.consolidationIntervalMsec(65000L);
        options.consolidationPolicy(ConsolidationPolicy.of(ConsolidationType.BYTES_ACCUM).threshold(1.));
        options.link(CollectionLink.on(COLL_2)
                .fields(FieldLink.on("value").analyzers("identity").trackListPositions(true).includeAllFields(true)
                        .storeValues(StoreValuesType.ID)));
        final ArangoSearchPropertiesEntity properties = view.updateProperties(options).get();
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
    @MethodSource("asyncDbs")
    void updateSearchAliasViewWithIndexesAndGetProperties(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        ArangoCollectionAsync col = db.collection(COLL_1);
        String idxName = rndName();
        col.ensureInvertedIndex(new InvertedIndexOptions()
                .name(idxName)
                .fields(new InvertedIndexField().name("a" + rnd()))).get();
        ArangoCollectionAsync col2 = db.collection(COLL_2);
        String idxName2 = rndName();
        col2.ensureInvertedIndex(new InvertedIndexOptions()
                .name(idxName2)
                .fields(new InvertedIndexField().name("a" + rnd()))).get();

        String viewName = rndName();
        final SearchAliasCreateOptions options = new SearchAliasCreateOptions()
                .indexes(new SearchAliasIndex(COLL_1, idxName));
        final ViewEntity info = db.searchAlias(viewName).create(options).get();
        db.searchAlias(viewName).updateProperties(new SearchAliasPropertiesOptions()
                .indexes(new SearchAliasIndex(COLL_2, idxName2))).get();

        assertThat(info).isNotNull();
        assertThat(info.getId()).isNotNull();
        assertThat(info.getName()).isEqualTo(viewName);
        assertThat(info.getType()).isEqualTo(ViewType.SEARCH_ALIAS);

        final SearchAliasPropertiesEntity properties = db.searchAlias(viewName).getProperties().get();
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
    @MethodSource("asyncDbs")
    void replaceArangoSearchViewProperties(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        String viewName = rndName();
        final ArangoSearchAsync view = db.arangoSearch(viewName);
        view.create(new ArangoSearchCreateOptions()).get();
        final ArangoSearchPropertiesOptions options = new ArangoSearchPropertiesOptions();
        options.link(CollectionLink.on(COLL_1)
                .fields(FieldLink.on("value").analyzers("identity")));
        final ArangoSearchPropertiesEntity properties = view.replaceProperties(options).get();
        assertThat(properties).isNotNull();
        assertThat(properties.getLinks()).hasSize(1);
        final CollectionLink link = properties.getLinks().iterator().next();
        assertThat(link.getName()).isEqualTo(COLL_1);
        assertThat(link.getFields()).hasSize(1);
        assertThat(link.getFields().iterator().next().getName()).isEqualTo("value");
    }

    @ParameterizedTest
    @MethodSource("asyncDbs")
    void replaceSearchAliasViewWithIndexesAndGetProperties(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        ArangoCollectionAsync col = db.collection(COLL_1);
        String idxName = rndName();
        col.ensureInvertedIndex(new InvertedIndexOptions()
                .name(idxName)
                .fields(new InvertedIndexField().name("a" + rnd()))).get();
        ArangoCollectionAsync col2 = db.collection(COLL_2);
        String idxName2 = rndName();
        col2.ensureInvertedIndex(new InvertedIndexOptions()
                .name(idxName2)
                .fields(new InvertedIndexField().name("a" + rnd()))).get();

        String viewName = rndName();
        final SearchAliasCreateOptions options = new SearchAliasCreateOptions()
                .indexes(new SearchAliasIndex(COLL_1, idxName));
        final ViewEntity info = db.searchAlias(viewName).create(options).get();
        db.searchAlias(viewName).replaceProperties(new SearchAliasPropertiesOptions()
                .indexes(new SearchAliasIndex(COLL_2, idxName2))).get();

        assertThat(info).isNotNull();
        assertThat(info.getId()).isNotNull();
        assertThat(info.getName()).isEqualTo(viewName);
        assertThat(info.getType()).isEqualTo(ViewType.SEARCH_ALIAS);

        final SearchAliasPropertiesEntity properties = db.searchAlias(viewName).getProperties().get();
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

    private void createGetAndDeleteTypedAnalyzer(ArangoDatabaseAsync db, SearchAnalyzer analyzer) throws ExecutionException, InterruptedException {

        String fullyQualifiedName = db.name() + "::" + analyzer.getName();
        analyzer.setName(fullyQualifiedName);

        // createAnalyzer
        SearchAnalyzer createdAnalyzer = db.createSearchAnalyzer(analyzer).get();
        assertThat(createdAnalyzer).isEqualTo(analyzer);

        // getAnalyzer
        SearchAnalyzer gotAnalyzer = db.getSearchAnalyzer(analyzer.getName()).get();
        assertThat(gotAnalyzer).isEqualTo(analyzer);

        // getAnalyzers
        SearchAnalyzer foundAnalyzer =
                db.getSearchAnalyzers().get().stream().filter(it -> it.getName().equals(fullyQualifiedName))
                        .findFirst().get();
        assertThat(foundAnalyzer).isEqualTo(analyzer);

        // deleteAnalyzer
        AnalyzerDeleteOptions deleteOptions = new AnalyzerDeleteOptions();
        deleteOptions.setForce(true);

        db.deleteSearchAnalyzer(analyzer.getName(), deleteOptions).get();

        Throwable thrown = catchThrowable(() -> db.getSearchAnalyzer(analyzer.getName()).get()).getCause();
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
        ArangoDBException e = (ArangoDBException) thrown;
        assertThat(e.getResponseCode()).isEqualTo(404);
        assertThat(e.getErrorNum()).isEqualTo(1202);
    }

    @ParameterizedTest
    @MethodSource("asyncDbs")
    void identityAnalyzerTyped(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
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
    @MethodSource("asyncDbs")
    void delimiterAnalyzerTyped(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
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
    @MethodSource("asyncDbs")
    void multiDelimiterAnalyzerTyped(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
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
    @MethodSource("asyncDbs")
    void stemAnalyzerTyped(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
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
    @MethodSource("asyncDbs")
    void normAnalyzerTyped(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
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
    @MethodSource("asyncDbs")
    void ngramAnalyzerTyped(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
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
    @MethodSource("asyncDbs")
    void enhancedNgramAnalyzerTyped(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
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
    @MethodSource("asyncDbs")
    void textAnalyzerTyped(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
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
    @MethodSource("asyncDbs")
    void enhancedTextAnalyzerTyped(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
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
    @MethodSource("asyncDbs")
    void arangoSearchOptions(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        String viewName = rndName();
        FieldLink field = FieldLink.on("f1")
                .inBackground(true)
                .cache(false);
        field.nested(FieldLink.on("f2"));
        CollectionLink link = CollectionLink.on(COLL_1)
                .analyzers("identity")
                .fields(field)
                .includeAllFields(true)
                .storeValues(StoreValuesType.ID)
                .trackListPositions(false)
                .inBackground(true)
                .cache(true);
        link.nested(FieldLink.on("f3"));
        ArangoSearchCreateOptions options = new ArangoSearchCreateOptions()
                .link(link)
                .primarySortCache(true)
                .primaryKeyCache(true);
        StoredValue storedValue = new StoredValue(Arrays.asList("a", "b"), ArangoSearchCompression.none, true);
        options.storedValues(storedValue);
        String[] optimizeTopK = new String[]{"BM25(@doc) DESC", "TFIDF(@doc) DESC"};
        options.optimizeTopK(optimizeTopK);

        final ArangoSearchAsync view = db.arangoSearch(viewName);
        view.create(options).get();

        final ArangoSearchPropertiesEntity properties = view.getProperties().get();
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
        assertThat(createdLink.getCache()).isTrue();
        assertThat(fieldLink.getCache()).isFalse();
        assertThat(properties.getPrimaryKeyCache()).isTrue();
        assertThat(properties.getPrimarySortCache()).isTrue();
        assertThat(properties.getStoredValues())
                .isNotEmpty()
                .allSatisfy(it -> assertThat(it.getCache()).isTrue());
        assertThat(createdLink.getNested()).isNotEmpty();
        FieldLink nested = createdLink.getNested().iterator().next();
        assertThat(nested.getName()).isEqualTo("f3");

        assertThat(fieldLink.getName()).isEqualTo("f1");
        assertThat(fieldLink.getNested()).isNotEmpty();
        FieldLink nested2 = fieldLink.getNested().iterator().next();
        assertThat(nested2.getName()).isEqualTo("f2");
        assertThat(properties.getOptimizeTopK()).containsExactly(optimizeTopK);
    }

    @ParameterizedTest
    @MethodSource("asyncDbs")
    void pipelineAnalyzer(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
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
    @MethodSource("asyncDbs")
    void stopwordsAnalyzer(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
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
        db.createSearchAnalyzer(analyzer).get();
        Collection<String> res = db.query("RETURN FLATTEN(TOKENS(SPLIT('the fox and the dog and a theater', ' '), " +
                        "@aName))", Collection.class,
                Collections.singletonMap("aName", name)).get().getResult().get(0);
        assertThat(res).containsExactly("fox", "dog", "a", "theater");
        db.deleteSearchAnalyzer(name).get();
    }

    @ParameterizedTest
    @MethodSource("asyncDbs")
    void aqlAnalyzer(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
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
    @MethodSource("asyncDbs")
    void geoJsonAnalyzer(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
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
    @MethodSource("asyncDbs")
    void geoS2Analyzer(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
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
    @MethodSource("asyncDbs")
    void geoPointAnalyzer(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
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
    @MethodSource("asyncDbs")
    void segmentationAnalyzer(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
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
    @MethodSource("asyncDbs")
    void collationAnalyzer(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
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


    @ParameterizedTest
    @MethodSource("asyncDbs")
    void classificationAnalyzer(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
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

    @ParameterizedTest
    @MethodSource("asyncDbs")
    void nearestNeighborsAnalyzer(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
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
    @MethodSource("asyncDbs")
    void MinHashAnalyzer(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
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
    @MethodSource("asyncDbs")
    void WildcardAnalyzer(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
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
    @MethodSource("asyncDbs")
    void offsetFeature(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
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
