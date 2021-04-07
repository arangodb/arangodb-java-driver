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
import com.arangodb.entity.arangosearch.AnalyzerEntity;
import com.arangodb.entity.arangosearch.AnalyzerFeature;
import com.arangodb.entity.arangosearch.AnalyzerType;
import com.arangodb.entity.arangosearch.ArangoSearchCompression;
import com.arangodb.entity.arangosearch.ArangoSearchPropertiesEntity;
import com.arangodb.entity.arangosearch.CollectionLink;
import com.arangodb.entity.arangosearch.ConsolidationPolicy;
import com.arangodb.entity.arangosearch.ConsolidationType;
import com.arangodb.entity.arangosearch.FieldLink;
import com.arangodb.entity.arangosearch.PrimarySort;
import com.arangodb.entity.arangosearch.StoreValuesType;
import com.arangodb.entity.arangosearch.StoredValue;
import com.arangodb.entity.arangosearch.analyzer.AQLAnalyzer;
import com.arangodb.entity.arangosearch.analyzer.AQLAnalyzerProperties;
import com.arangodb.entity.arangosearch.analyzer.DelimiterAnalyzer;
import com.arangodb.entity.arangosearch.analyzer.DelimiterAnalyzerProperties;
import com.arangodb.entity.arangosearch.analyzer.EdgeNgram;
import com.arangodb.entity.arangosearch.analyzer.GeoJSONAnalyzer;
import com.arangodb.entity.arangosearch.analyzer.GeoAnalyzerOptions;
import com.arangodb.entity.arangosearch.analyzer.GeoJSONAnalyzerProperties;
import com.arangodb.entity.arangosearch.analyzer.GeoPointAnalyzer;
import com.arangodb.entity.arangosearch.analyzer.GeoPointAnalyzerProperties;
import com.arangodb.entity.arangosearch.analyzer.IdentityAnalyzer;
import com.arangodb.entity.arangosearch.analyzer.NGramAnalyzer;
import com.arangodb.entity.arangosearch.analyzer.NGramAnalyzerProperties;
import com.arangodb.entity.arangosearch.analyzer.NormAnalyzer;
import com.arangodb.entity.arangosearch.analyzer.NormAnalyzerProperties;
import com.arangodb.entity.arangosearch.analyzer.PipelineAnalyzer;
import com.arangodb.entity.arangosearch.analyzer.PipelineAnalyzerProperties;
import com.arangodb.entity.arangosearch.analyzer.SearchAnalyzer;
import com.arangodb.entity.arangosearch.analyzer.SearchAnalyzerCase;
import com.arangodb.entity.arangosearch.analyzer.StemAnalyzer;
import com.arangodb.entity.arangosearch.analyzer.StemAnalyzerProperties;
import com.arangodb.entity.arangosearch.analyzer.StopwordsAnalyzer;
import com.arangodb.entity.arangosearch.analyzer.StopwordsAnalyzerProperties;
import com.arangodb.entity.arangosearch.analyzer.StreamType;
import com.arangodb.entity.arangosearch.analyzer.TextAnalyzer;
import com.arangodb.entity.arangosearch.analyzer.TextAnalyzerProperties;
import com.arangodb.model.arangosearch.AnalyzerDeleteOptions;
import com.arangodb.model.arangosearch.ArangoSearchCreateOptions;
import com.arangodb.model.arangosearch.ArangoSearchPropertiesOptions;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

/**
 * @author Mark Vollmary
 */
@RunWith(Parameterized.class)
public class ArangoSearchTest extends BaseTest {

    private static final String COLL_1 = "ArangoSearchTest_view_replace_prop";
    private static final String COLL_2 = "ArangoSearchTest_view_update_prop";

    @BeforeClass
    public static void init() {
        BaseTest.initCollections(COLL_1, COLL_2);
    }

    public ArangoSearchTest(final ArangoDB arangoDB) {
        super(arangoDB);
    }

    @Test
    public void exists() {
        assumeTrue(isAtLeastVersion(3, 4));
        String viewName = "view-" + rnd();
        db.createArangoSearch(viewName, new ArangoSearchCreateOptions());
        assertThat(db.arangoSearch(viewName).exists(), is(true));
    }

    @Test
    public void getInfo() {
        assumeTrue(isAtLeastVersion(3, 4));
        String viewName = "view-" + rnd();
        db.createArangoSearch(viewName, new ArangoSearchCreateOptions());
        final ViewEntity info = db.arangoSearch(viewName).getInfo();
        assertThat(info, is(not(nullValue())));
        assertThat(info.getId(), is(not(nullValue())));
        assertThat(info.getName(), is(viewName));
        assertThat(info.getType(), is(ViewType.ARANGO_SEARCH));
    }

    @Test
    public void drop() {
        assumeTrue(isAtLeastVersion(3, 4));
        String viewName = "view-" + rnd();
        db.createArangoSearch(viewName, new ArangoSearchCreateOptions());
        final ArangoView view = db.arangoSearch(viewName);
        view.drop();
        assertThat(view.exists(), is(false));
    }

    @Test
    public void rename() {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 4));
        String viewName = "view-" + rnd();
        final String name = viewName + "_new";
        db.createArangoSearch(name, new ArangoSearchCreateOptions());
        db.arangoSearch(name).rename(viewName);
        assertThat(db.arangoSearch(name).exists(), is(false));
        assertThat(db.arangoSearch(viewName).exists(), is(true));
    }

    @Test
    public void create() {
        assumeTrue(isAtLeastVersion(3, 4));
        String viewName = "view-" + rnd();
        final ViewEntity info = db.arangoSearch(viewName).create();
        assertThat(info, is(not(nullValue())));
        assertThat(info.getId(), is(not(nullValue())));
        assertThat(info.getName(), is(viewName));
        assertThat(info.getType(), is(ViewType.ARANGO_SEARCH));
        assertThat(db.arangoSearch(viewName).exists(), is(true));
    }

    @Test
    public void createWithOptions() {
        assumeTrue(isAtLeastVersion(3, 4));
        String viewName = "view-" + rnd();
        final ArangoSearchCreateOptions options = new ArangoSearchCreateOptions();
        final ViewEntity info = db.arangoSearch(viewName).create(options);
        assertThat(info, is(not(nullValue())));
        assertThat(info.getId(), is(not(nullValue())));
        assertThat(info.getName(), is(viewName));
        assertThat(info.getType(), is(ViewType.ARANGO_SEARCH));
        assertThat(db.arangoSearch(viewName).exists(), is(true));
    }

    @Test
    public void createWithPrimarySort() {
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
        assertThat(info, is(not(nullValue())));
        assertThat(info.getId(), is(not(nullValue())));
        assertThat(info.getName(), is(viewName));
        assertThat(info.getType(), is(ViewType.ARANGO_SEARCH));
        assertThat(db.arangoSearch(viewName).exists(), is(true));

        if(isAtLeastVersion(3,7)){
            final ArangoSearchPropertiesEntity properties = view.getProperties();
            assertThat(properties.getPrimarySortCompression(), is(ArangoSearchCompression.none));
            Collection<StoredValue> retrievedStoredValues = properties.getStoredValues();
            assertThat(retrievedStoredValues, is(notNullValue()));
            assertThat(retrievedStoredValues.size(), is(1));
            StoredValue retrievedStoredValue = retrievedStoredValues.iterator().next();
            assertThat(retrievedStoredValue, is(notNullValue()));
            assertThat(retrievedStoredValue.getFields(), is(storedValue.getFields()));
            assertThat(retrievedStoredValue.getCompression(), is(storedValue.getCompression()));
        }
    }

    @Test
    public void createWithCommitIntervalMsec() {
        assumeTrue(isAtLeastVersion(3, 5));
        String viewName = "view-" + rnd();
        final ArangoSearchCreateOptions options = new ArangoSearchCreateOptions();
        options.commitIntervalMsec(666666L);

        final ViewEntity info = db.arangoSearch(viewName).create(options);
        assertThat(info, is(not(nullValue())));
        assertThat(info.getId(), is(not(nullValue())));
        assertThat(info.getName(), is(viewName));
        assertThat(info.getType(), is(ViewType.ARANGO_SEARCH));
        assertThat(db.arangoSearch(viewName).exists(), is(true));

        // check commit interval msec property
        final ArangoSearch view = db.arangoSearch(viewName);
        final ArangoSearchPropertiesEntity properties = view.getProperties();
        assertThat(properties.getCommitIntervalMsec(), is(666666L));
    }

    @Test
    public void getProperties() {
        assumeTrue(isAtLeastVersion(3, 4));
        String viewName = "view-" + rnd();
        final ArangoSearch view = db.arangoSearch(viewName);
        view.create(new ArangoSearchCreateOptions());
        final ArangoSearchPropertiesEntity properties = view.getProperties();
        assertThat(properties, is(not(nullValue())));
        assertThat(properties.getId(), is(not(nullValue())));
        assertThat(properties.getName(), is(viewName));
        assertThat(properties.getType(), is(ViewType.ARANGO_SEARCH));
        assertThat(properties.getConsolidationIntervalMsec(), is(not(nullValue())));
        assertThat(properties.getCleanupIntervalStep(), is(not(nullValue())));
        final ConsolidationPolicy consolidate = properties.getConsolidationPolicy();
        assertThat(consolidate, is(is(not(nullValue()))));
        final Collection<CollectionLink> links = properties.getLinks();
        assertThat(links.isEmpty(), is(true));
    }

    @Test
    public void updateProperties() {
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
        assertThat(properties, is(not(nullValue())));
        assertThat(properties.getCleanupIntervalStep(), is(15L));
        assertThat(properties.getConsolidationIntervalMsec(), is(65000L));
        final ConsolidationPolicy consolidate = properties.getConsolidationPolicy();
        assertThat(consolidate, is(not(nullValue())));
        assertThat(consolidate.getType(), is(ConsolidationType.BYTES_ACCUM));
        assertThat(consolidate.getThreshold(), is(1.));
        assertThat(properties.getLinks().size(), is(1));
        final CollectionLink link = properties.getLinks().iterator().next();
        assertThat(link.getName(), is(COLL_2));
        assertThat(link.getFields().size(), is(1));
        final FieldLink next = link.getFields().iterator().next();
        assertThat(next.getName(), is("value"));
        assertThat(next.getIncludeAllFields(), is(true));
        assertThat(next.getTrackListPositions(), is(true));
        assertThat(next.getStoreValues(), is(StoreValuesType.ID));
    }

    @Test
    public void replaceProperties() {
        assumeTrue(isAtLeastVersion(3, 4));
        String viewName = "view-" + rnd();
        final ArangoSearch view = db.arangoSearch(viewName);
        view.create(new ArangoSearchCreateOptions());
        final ArangoSearchPropertiesOptions options = new ArangoSearchPropertiesOptions();
        options.link(CollectionLink.on(COLL_1)
                .fields(FieldLink.on("value").analyzers("identity")));
        final ArangoSearchPropertiesEntity properties = view.replaceProperties(options);
        assertThat(properties, is(not(nullValue())));
        assertThat(properties.getLinks().size(), is(1));
        final CollectionLink link = properties.getLinks().iterator().next();
        assertThat(link.getName(), is(COLL_1));
        assertThat(link.getFields().size(), is(1));
        assertThat(link.getFields().iterator().next().getName(), is("value"));
    }

    private void createGetAndDeleteAnalyzer(AnalyzerEntity options) {

        String fullyQualifiedName = db.name() + "::" + options.getName();

        // createAnalyzer
        AnalyzerEntity createdAnalyzer = db.createAnalyzer(options);

        assertThat(createdAnalyzer.getName(), is(fullyQualifiedName));
        assertThat(createdAnalyzer.getType(), is(options.getType()));
        assertThat(createdAnalyzer.getFeatures(), is(options.getFeatures()));
        compareProperties(createdAnalyzer.getProperties(), options.getProperties());

        // getAnalyzer
        AnalyzerEntity gotAnalyzer = db.getAnalyzer(options.getName());
        assertThat(gotAnalyzer.getName(), is(fullyQualifiedName));
        assertThat(gotAnalyzer.getType(), is(options.getType()));
        assertThat(gotAnalyzer.getFeatures(), is(options.getFeatures()));
        compareProperties(gotAnalyzer.getProperties(), options.getProperties());

        // getAnalyzers
        @SuppressWarnings("OptionalGetWithoutIsPresent")
        AnalyzerEntity foundAnalyzer = db.getAnalyzers().stream().filter(it -> it.getName().equals(fullyQualifiedName))
                .findFirst().get();

        assertThat(foundAnalyzer.getName(), is(fullyQualifiedName));
        assertThat(foundAnalyzer.getType(), is(options.getType()));
        assertThat(foundAnalyzer.getFeatures(), is(options.getFeatures()));
        compareProperties(foundAnalyzer.getProperties(), options.getProperties());

        AnalyzerDeleteOptions deleteOptions = new AnalyzerDeleteOptions();
        deleteOptions.setForce(true);

        // deleteAnalyzer
        db.deleteAnalyzer(options.getName(), deleteOptions);

        try {
            db.getAnalyzer(options.getName());
            fail("deleted analyzer should not be found!");
        } catch (ArangoDBException e) {
            // ok
        }

    }

    private void createGetAndDeleteTypedAnalyzer(SearchAnalyzer analyzer) {

        String fullyQualifiedName = db.name() + "::" + analyzer.getName();
        analyzer.setName(fullyQualifiedName);

        // createAnalyzer
        SearchAnalyzer createdAnalyzer = db.createSearchAnalyzer(analyzer);
        assertThat(createdAnalyzer, is(analyzer));

        // getAnalyzer
        SearchAnalyzer gotAnalyzer = db.getSearchAnalyzer(analyzer.getName());
        assertThat(gotAnalyzer, is(analyzer));

        // getAnalyzers
        @SuppressWarnings("OptionalGetWithoutIsPresent")
        SearchAnalyzer foundAnalyzer = db.getSearchAnalyzers().stream().filter(it -> it.getName().equals(fullyQualifiedName))
                .findFirst().get();
        assertThat(foundAnalyzer, is(analyzer));

        // deleteAnalyzer
        AnalyzerDeleteOptions deleteOptions = new AnalyzerDeleteOptions();
        deleteOptions.setForce(true);

        db.deleteSearchAnalyzer(analyzer.getName(), deleteOptions);

        try {
            db.getAnalyzer(analyzer.getName());
            fail("deleted analyzer should not be found!");
        } catch (ArangoDBException e) {
            assertThat(e.getResponseCode(), is(404));
            assertThat(e.getErrorNum(), is(1202));
        }

    }

    private void compareProperties(Map<String, Object> actualProperties, Map<String, Object> expectedProperties) {
        expectedProperties.forEach((key, value) -> {
            Object expectedValue = actualProperties.get(key);
            if (value instanceof Map) {
                assertThat(expectedValue, notNullValue());
                assertThat(expectedValue, instanceOf(Map.class));
                compareProperties((Map) value, (Map) expectedValue);
            } else if(value instanceof Number){
                assertThat(Double.valueOf(value.toString()), is(Double.valueOf(expectedValue.toString())));
            }else {
                assertThat(value, is(expectedValue));
            }
        });
    }

    @Test
    public void identityAnalyzer() {
        assumeTrue(isAtLeastVersion(3, 5));

        String name = "test-" + rnd();

        Set<AnalyzerFeature> features = new HashSet<>();
        features.add(AnalyzerFeature.frequency);
        features.add(AnalyzerFeature.norm);
        features.add(AnalyzerFeature.position);

        AnalyzerEntity options = new AnalyzerEntity();
        options.setFeatures(features);
        options.setName(name);
        options.setType(AnalyzerType.identity);
        options.setProperties(Collections.emptyMap());

        createGetAndDeleteAnalyzer(options);
    }

    @Test
    public void identityAnalyzerTyped() {
        assumeTrue(isAtLeastVersion(3, 5));

        String name = "test-" + UUID.randomUUID().toString();

        Set<AnalyzerFeature> features = new HashSet<>();
        features.add(AnalyzerFeature.frequency);
        features.add(AnalyzerFeature.norm);
        features.add(AnalyzerFeature.position);

        IdentityAnalyzer analyzer = new IdentityAnalyzer();
        analyzer.setFeatures(features);
        analyzer.setName(name);

        createGetAndDeleteTypedAnalyzer(analyzer);
    }

    @Test
    public void delimiterAnalyzer() {
        assumeTrue(isAtLeastVersion(3, 5));

        String name = "test-" + rnd();

        Set<AnalyzerFeature> features = new HashSet<>();
        features.add(AnalyzerFeature.frequency);
        features.add(AnalyzerFeature.norm);
        features.add(AnalyzerFeature.position);

        AnalyzerEntity options = new AnalyzerEntity();
        options.setFeatures(features);
        options.setName(name);
        options.setType(AnalyzerType.delimiter);
        options.setProperties(Collections.singletonMap("delimiter", "-"));

        createGetAndDeleteAnalyzer(options);
    }

    @Test
    public void delimiterAnalyzerTyped() {
        assumeTrue(isAtLeastVersion(3, 5));

        String name = "test-" + UUID.randomUUID().toString();

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

        createGetAndDeleteTypedAnalyzer(analyzer);
    }

    @Test
    public void stemAnalyzer() {
        assumeTrue(isAtLeastVersion(3, 5));

        String name = "test-" + rnd();

        Set<AnalyzerFeature> features = new HashSet<>();
        features.add(AnalyzerFeature.frequency);
        features.add(AnalyzerFeature.norm);
        features.add(AnalyzerFeature.position);

        AnalyzerEntity options = new AnalyzerEntity();
        options.setFeatures(features);
        options.setName(name);
        options.setType(AnalyzerType.stem);
        options.setProperties(Collections.singletonMap("locale", "ru.utf-8"));

        createGetAndDeleteAnalyzer(options);
    }

    @Test
    public void stemAnalyzerTyped() {
        assumeTrue(isAtLeastVersion(3, 5));

        String name = "test-" + UUID.randomUUID().toString();

        Set<AnalyzerFeature> features = new HashSet<>();
        features.add(AnalyzerFeature.frequency);
        features.add(AnalyzerFeature.norm);
        features.add(AnalyzerFeature.position);

        StemAnalyzerProperties properties = new StemAnalyzerProperties();
        properties.setLocale("ru.utf-8");

        StemAnalyzer options = new StemAnalyzer();
        options.setFeatures(features);
        options.setName(name);
        options.setProperties(properties);

        createGetAndDeleteTypedAnalyzer(options);
    }

    @Test
    public void normAnalyzer() {
        assumeTrue(isAtLeastVersion(3, 5));

        String name = "test-" + rnd();

        Set<AnalyzerFeature> features = new HashSet<>();
        features.add(AnalyzerFeature.frequency);
        features.add(AnalyzerFeature.norm);
        features.add(AnalyzerFeature.position);

        Map<String, Object> properties = new HashMap<>();
        properties.put("locale", "ru.utf-8");
        properties.put("case", "lower");
        properties.put("accent", true);

        AnalyzerEntity options = new AnalyzerEntity();
        options.setFeatures(features);
        options.setName(name);
        options.setType(AnalyzerType.norm);
        options.setProperties(properties);

        createGetAndDeleteAnalyzer(options);
    }

    @Test
    public void normAnalyzerTyped() {
        assumeTrue(isAtLeastVersion(3, 5));

        String name = "test-" + UUID.randomUUID().toString();

        Set<AnalyzerFeature> features = new HashSet<>();
        features.add(AnalyzerFeature.frequency);
        features.add(AnalyzerFeature.norm);
        features.add(AnalyzerFeature.position);

        NormAnalyzerProperties properties = new NormAnalyzerProperties();
        properties.setLocale("ru.utf-8");
        properties.setAnalyzerCase(SearchAnalyzerCase.lower);
        properties.setAccent(true);

        NormAnalyzer options = new NormAnalyzer();
        options.setFeatures(features);
        options.setName(name);
        options.setProperties(properties);

        createGetAndDeleteTypedAnalyzer(options);
    }

    @Test
    public void ngramAnalyzer() {
        assumeTrue(isAtLeastVersion(3, 5));

        String name = "test-" + rnd();

        Set<AnalyzerFeature> features = new HashSet<>();
        features.add(AnalyzerFeature.frequency);
        features.add(AnalyzerFeature.norm);
        features.add(AnalyzerFeature.position);

        Map<String, Object> properties = new HashMap<>();
        properties.put("max", 6L);
        properties.put("min", 3L);
        properties.put("preserveOriginal", true);

        AnalyzerEntity options = new AnalyzerEntity();
        options.setFeatures(features);
        options.setName(name);
        options.setType(AnalyzerType.ngram);
        options.setProperties(properties);

        createGetAndDeleteAnalyzer(options);
    }

    @Test
    public void ngramAnalyzerTyped() {
        assumeTrue(isAtLeastVersion(3, 5));

        String name = "test-" + UUID.randomUUID().toString();

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

        createGetAndDeleteTypedAnalyzer(analyzer);
    }

    @Test
    public void enhancedNgramAnalyzer() {
        assumeTrue(isAtLeastVersion(3, 6));

        String name = "test-" + UUID.randomUUID().toString();

        Set<AnalyzerFeature> features = new HashSet<>();
        features.add(AnalyzerFeature.frequency);
        features.add(AnalyzerFeature.norm);
        features.add(AnalyzerFeature.position);

        Map<String, Object> properties = new HashMap<>();
        properties.put("max", 6L);
        properties.put("min", 3L);
        properties.put("preserveOriginal", true);
        properties.put("startMarker", "^");
        properties.put("endMarker", "^");
        properties.put("streamType", "utf8");

        AnalyzerEntity options = new AnalyzerEntity();
        options.setFeatures(features);
        options.setName(name);
        options.setType(AnalyzerType.ngram);
        options.setProperties(properties);

        createGetAndDeleteAnalyzer(options);
    }

    @Test
    public void enhancedNgramAnalyzerTyped() {
        assumeTrue(isAtLeastVersion(3, 6));

        String name = "test-" + UUID.randomUUID().toString();

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

        createGetAndDeleteTypedAnalyzer(analyzer);
    }

    @Test
    public void textAnalyzer() {
        assumeTrue(isAtLeastVersion(3, 5));

        String name = "test-" + rnd();

        Set<AnalyzerFeature> features = new HashSet<>();
        features.add(AnalyzerFeature.frequency);
        features.add(AnalyzerFeature.norm);
        features.add(AnalyzerFeature.position);

        Map<String, Object> properties = new HashMap<>();
        properties.put("locale", "ru.utf-8");
        properties.put("case", "lower");
        properties.put("stopwords", Collections.emptyList());
        properties.put("accent", true);
        properties.put("stemming", true);

        AnalyzerEntity options = new AnalyzerEntity();
        options.setFeatures(features);
        options.setName(name);
        options.setType(AnalyzerType.text);
        options.setProperties(properties);

        createGetAndDeleteAnalyzer(options);
    }

    @Test
    public void textAnalyzerTyped() {
        assumeTrue(isAtLeastVersion(3, 5));

        String name = "test-" + UUID.randomUUID().toString();

        Set<AnalyzerFeature> features = new HashSet<>();
        features.add(AnalyzerFeature.frequency);
        features.add(AnalyzerFeature.norm);
        features.add(AnalyzerFeature.position);

        TextAnalyzerProperties properties = new TextAnalyzerProperties();
        properties.setLocale("ru.utf-8");
        properties.setAnalyzerCase(SearchAnalyzerCase.lower);
        properties.setAccent(true);
        properties.setStemming(true);

        TextAnalyzer analyzer = new TextAnalyzer();
        analyzer.setFeatures(features);
        analyzer.setName(name);
        analyzer.setType(AnalyzerType.text);
        analyzer.setProperties(properties);

        createGetAndDeleteTypedAnalyzer(analyzer);
    }

    @Test
    public void enhancedTextAnalyzer() {
        assumeTrue(isAtLeastVersion(3, 6));

        String name = "test-" + UUID.randomUUID().toString();

        Set<AnalyzerFeature> features = new HashSet<>();
        features.add(AnalyzerFeature.frequency);
        features.add(AnalyzerFeature.norm);
        features.add(AnalyzerFeature.position);

        Map<String, Object> edgeNgram = new HashMap<>();
        edgeNgram.put("min", 2L);
        edgeNgram.put("max", 100000L);
        edgeNgram.put("preserveOriginal", true);

        Map<String, Object> properties = new HashMap<>();
        properties.put("locale", "ru.utf-8");
        properties.put("case", "lower");
        properties.put("stopwords", Collections.emptyList());
        properties.put("accent", true);
        properties.put("stemming", true);
        properties.put("edgeNgram", edgeNgram);

        AnalyzerEntity options = new AnalyzerEntity();
        options.setFeatures(features);
        options.setName(name);
        options.setType(AnalyzerType.text);
        options.setProperties(properties);

        createGetAndDeleteAnalyzer(options);
    }

    @Test
    public void enhancedTextAnalyzerTyped() {
        assumeTrue(isAtLeastVersion(3, 6));

        String name = "test-" + UUID.randomUUID().toString();

        Set<AnalyzerFeature> features = new HashSet<>();
        features.add(AnalyzerFeature.frequency);
        features.add(AnalyzerFeature.norm);
        features.add(AnalyzerFeature.position);

        EdgeNgram edgeNgram = new EdgeNgram();
        edgeNgram.setMin(2L);
        edgeNgram.setMax(100000L);
        edgeNgram.setPreserveOriginal(true);

        TextAnalyzerProperties properties = new TextAnalyzerProperties();
        properties.setLocale("ru.utf-8");
        properties.setAnalyzerCase(SearchAnalyzerCase.lower);
        properties.setAccent(true);
        properties.setStemming(true);
        properties.setEdgeNgram(edgeNgram);

        TextAnalyzer analyzer = new TextAnalyzer();
        analyzer.setFeatures(features);
        analyzer.setName(name);
        analyzer.setProperties(properties);

        createGetAndDeleteTypedAnalyzer(analyzer);
    }

    @Test
    public void arangoSearchOptions() {
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
        assertThat(properties, is(not(nullValue())));
        assertThat(properties.getId(), is(not(nullValue())));
        assertThat(properties.getName(), is(viewName));
        assertThat(properties.getType(), is(ViewType.ARANGO_SEARCH));

        CollectionLink link = properties.getLinks().iterator().next();
        assertThat(link.getAnalyzers(), contains("identity"));
        assertThat(link.getName(), is(COLL_1));
        assertThat(link.getIncludeAllFields(), is(true));
        assertThat(link.getStoreValues(), is(StoreValuesType.ID));
        assertThat(link.getTrackListPositions(), is(false));
    }

    @Test
    public void pipelineAnalyzer() {
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
        stemAnalyzerProperties.setLocale("en.utf-8");

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
        pipelineAnalyzer.setName("test-" + UUID.randomUUID().toString());
        pipelineAnalyzer.setProperties(properties);
        pipelineAnalyzer.setFeatures(features);

        createGetAndDeleteTypedAnalyzer(pipelineAnalyzer);
    }

    @Test
    public void stopwordsAnalyzer() {
        assumeTrue(isAtLeastVersion(3, 8));

        Set<AnalyzerFeature> features = new HashSet<>();
        features.add(AnalyzerFeature.frequency);
        features.add(AnalyzerFeature.norm);
        features.add(AnalyzerFeature.position);

        StopwordsAnalyzerProperties properties = new StopwordsAnalyzerProperties()
                .addStopwordAsHex("616e64")
                .addStopwordAsString("the");

        assertThat(properties.getStopwords(), hasItem("616e64"));
        assertThat(properties.getStopwords(), hasItem("746865"));

        StopwordsAnalyzer analyzer = new StopwordsAnalyzer();
        analyzer.setName("test-" + UUID.randomUUID().toString());
        analyzer.setProperties(properties);
        analyzer.setFeatures(features);

        createGetAndDeleteTypedAnalyzer(analyzer);
    }

    @Test
    public void aqlAnalyzer() {
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
        aqlAnalyzer.setName("test-" + UUID.randomUUID().toString());
        aqlAnalyzer.setProperties(properties);
        aqlAnalyzer.setFeatures(features);

        createGetAndDeleteTypedAnalyzer(aqlAnalyzer);
    }

    @Test
    public void geoJsonAnalyzer() {
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
        geoJSONAnalyzer.setName("test-" + UUID.randomUUID().toString());
        geoJSONAnalyzer.setProperties(properties);
        geoJSONAnalyzer.setFeatures(features);

        createGetAndDeleteTypedAnalyzer(geoJSONAnalyzer);
    }


    @Test
    public void geoPointAnalyzer() {
        assumeTrue(isAtLeastVersion(3, 8));

        GeoAnalyzerOptions options = new GeoAnalyzerOptions();
        options.setMaxLevel(10);
        options.setMaxCells(11);
        options.setMinLevel(8);

        GeoPointAnalyzerProperties properties = new GeoPointAnalyzerProperties();
        properties.setLatitude(new String[]{"a", "b","c"});
        properties.setLongitude(new String[]{"d", "e","f"});
        properties.setOptions(options);

        Set<AnalyzerFeature> features = new HashSet<>();
        features.add(AnalyzerFeature.frequency);
        features.add(AnalyzerFeature.norm);
        features.add(AnalyzerFeature.position);

        GeoPointAnalyzer geoPointAnalyzer = new GeoPointAnalyzer();
        geoPointAnalyzer.setName("test-" + UUID.randomUUID().toString());
        geoPointAnalyzer.setProperties(properties);
        geoPointAnalyzer.setFeatures(features);

        createGetAndDeleteTypedAnalyzer(geoPointAnalyzer);
    }


}
