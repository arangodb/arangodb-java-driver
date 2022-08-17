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

import com.arangodb.entity.ViewEntity;
import com.arangodb.entity.ViewType;
import com.arangodb.entity.arangosearch.*;
import com.arangodb.entity.arangosearch.analyzer.*;
import com.arangodb.model.arangosearch.AnalyzerDeleteOptions;
import com.arangodb.model.arangosearch.ArangoSearchCreateOptions;
import com.arangodb.model.arangosearch.ArangoSearchPropertiesOptions;
import org.junit.jupiter.api.BeforeAll;
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

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void exists(ArangoDatabase db) {
        assumeTrue(isAtLeastVersion(3, 4));
        String viewName = "view-" + rnd();
        db.createArangoSearch(viewName, new ArangoSearchCreateOptions());
        assertThat(db.arangoSearch(viewName).exists()).isTrue();
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void getInfo(ArangoDatabase db) {
        assumeTrue(isAtLeastVersion(3, 4));
        String viewName = "view-" + rnd();
        db.createArangoSearch(viewName, new ArangoSearchCreateOptions());
        final ViewEntity info = db.arangoSearch(viewName).getInfo();
        assertThat(info).isNotNull();
        assertThat(info.getId()).isNotNull();
        assertThat(info.getName()).isEqualTo(viewName);
        assertThat(info.getType()).isEqualTo(ViewType.ARANGO_SEARCH);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void drop(ArangoDatabase db) {
        assumeTrue(isAtLeastVersion(3, 4));
        String viewName = "view-" + rnd();
        db.createArangoSearch(viewName, new ArangoSearchCreateOptions());
        final ArangoView view = db.arangoSearch(viewName);
        view.drop();
        assertThat(view.exists()).isFalse();
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void rename(ArangoDatabase db) {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 4));
        String viewName = "view-" + rnd();
        final String name = viewName + "_new";
        db.createArangoSearch(name, new ArangoSearchCreateOptions());
        db.arangoSearch(name).rename(viewName);
        assertThat(db.arangoSearch(name).exists()).isFalse();
        assertThat(db.arangoSearch(viewName).exists()).isTrue();
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void create(ArangoDatabase db) {
        assumeTrue(isAtLeastVersion(3, 4));
        String viewName = "view-" + rnd();
        final ViewEntity info = db.arangoSearch(viewName).create();
        assertThat(info).isNotNull();
        assertThat(info.getId()).isNotNull();
        assertThat(info.getName()).isEqualTo(viewName);
        assertThat(info.getType()).isEqualTo(ViewType.ARANGO_SEARCH);
        assertThat(db.arangoSearch(viewName).exists()).isTrue();
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void createWithOptions(ArangoDatabase db) {
        assumeTrue(isAtLeastVersion(3, 4));
        String viewName = "view-" + rnd();
        final ArangoSearchCreateOptions options = new ArangoSearchCreateOptions();
        final ViewEntity info = db.arangoSearch(viewName).create(options);
        assertThat(info).isNotNull();
        assertThat(info.getId()).isNotNull();
        assertThat(info.getName()).isEqualTo(viewName);
        assertThat(info.getType()).isEqualTo(ViewType.ARANGO_SEARCH);
        assertThat(db.arangoSearch(viewName).exists()).isTrue();
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void createWithPrimarySort(ArangoDatabase db) {
        assumeTrue(isAtLeastVersion(3, 5));
        String viewName = "view-" + rnd();
        final ArangoSearchCreateOptions options = new ArangoSearchCreateOptions();

        final PrimarySort primarySort = PrimarySort.on("myFieldName");
        primarySort.ascending(true);
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
        }
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void createWithCommitIntervalMsec(ArangoDatabase db) {
        assumeTrue(isAtLeastVersion(3, 5));
        String viewName = "view-" + rnd();
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

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void getProperties(ArangoDatabase db) {
        assumeTrue(isAtLeastVersion(3, 4));
        String viewName = "view-" + rnd();
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

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void updateProperties(ArangoDatabase db) {
        assumeTrue(isAtLeastVersion(3, 4));
        String viewName = "view-" + rnd();
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

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void replaceProperties(ArangoDatabase db) {
        assumeTrue(isAtLeastVersion(3, 4));
        String viewName = "view-" + rnd();
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

    private void createGetAndDeleteTypedAnalyzer(ArangoDatabase db, SearchAnalyzer analyzer) {

        String fullyQualifiedName = db.dbName().get() + "::" + analyzer.getName();
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

    @ParameterizedTest(name = "{index}")
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

    @ParameterizedTest(name = "{index}")
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

    @ParameterizedTest(name = "{index}")
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

    @ParameterizedTest(name = "{index}")
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

    @ParameterizedTest(name = "{index}")
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

    @ParameterizedTest(name = "{index}")
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

    @ParameterizedTest(name = "{index}")
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

    @ParameterizedTest(name = "{index}")
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

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void arangoSearchOptions(ArangoDatabase db) {
        assumeTrue(isAtLeastVersion(3, 4));
        String viewName = "view-" + rnd();
        ArangoSearchCreateOptions options = new ArangoSearchCreateOptions()
                .link(
                        CollectionLink.on(COLL_1)
                                .analyzers("identity")
                                .fields(
                                        FieldLink.on("id")
                                                .analyzers("identity")
                                )
                                .includeAllFields(true)
                                .storeValues(StoreValuesType.ID)
                                .trackListPositions(false)

                );

        final ArangoSearch view = db.arangoSearch(viewName);
        view.create(options);

        final ArangoSearchPropertiesEntity properties = view.getProperties();
        assertThat(properties).isNotNull();
        assertThat(properties.getId()).isNotNull();
        assertThat(properties.getName()).isEqualTo(viewName);
        assertThat(properties.getType()).isEqualTo(ViewType.ARANGO_SEARCH);

        CollectionLink link = properties.getLinks().iterator().next();
        assertThat(link.getAnalyzers()).contains("identity");
        assertThat(link.getName()).isEqualTo(COLL_1);
        assertThat(link.getIncludeAllFields()).isTrue();
        assertThat(link.getStoreValues()).isEqualTo(StoreValuesType.ID);
        assertThat(link.getTrackListPositions()).isFalse();
    }

    @ParameterizedTest(name = "{index}")
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

    @ParameterizedTest(name = "{index}")
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
                        "@aName))",
                Collections.singletonMap("aName", name), Collection.class).next();
        assertThat(res).containsExactly("fox", "dog", "a", "theater");
        db.deleteSearchAnalyzer(name);
    }

    @ParameterizedTest(name = "{index}")
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

    @ParameterizedTest(name = "{index}")
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


    @ParameterizedTest(name = "{index}")
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


    @ParameterizedTest(name = "{index}")
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

    @ParameterizedTest(name = "{index}")
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


}
