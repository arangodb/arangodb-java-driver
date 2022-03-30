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

package com.arangodb.async;

import com.arangodb.ArangoDBException;
import com.arangodb.entity.ViewEntity;
import com.arangodb.entity.ViewType;
import com.arangodb.entity.arangosearch.*;
import com.arangodb.entity.arangosearch.analyzer.*;
import com.arangodb.model.arangosearch.AnalyzerDeleteOptions;
import com.arangodb.model.arangosearch.ArangoSearchCreateOptions;
import com.arangodb.model.arangosearch.ArangoSearchPropertiesOptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * @author Mark Vollmary
 */

class ArangoSearchTest extends BaseTest {

    private static final String VIEW_NAME = "view_test";

    @BeforeAll
    static void setup() throws InterruptedException, ExecutionException {
        if (!isAtLeastVersion(arangoDB, 3, 4))
            return;
        db.createArangoSearch(VIEW_NAME, new ArangoSearchCreateOptions()).get();
    }

    @Test
    void exists() throws InterruptedException, ExecutionException {
        assumeTrue(isAtLeastVersion(3, 4));
        assertThat(db.arangoSearch(VIEW_NAME).exists().get()).isTrue();
    }

    @Test
    void getInfo() throws InterruptedException, ExecutionException {
        assumeTrue(isAtLeastVersion(3, 4));
        final ViewEntity info = db.arangoSearch(VIEW_NAME).getInfo().get();
        assertThat(info).isNotNull();
        assertThat(info.getId()).isNotNull();
        assertThat(info.getName()).isEqualTo(VIEW_NAME);
        assertThat(info.getType()).isEqualTo(ViewType.ARANGO_SEARCH);
    }

    @Test
    void drop() throws InterruptedException, ExecutionException {
        assumeTrue(isAtLeastVersion(3, 4));
        final String name = VIEW_NAME + "_droptest";
        db.createArangoSearch(name, new ArangoSearchCreateOptions()).get();
        final ArangoViewAsync view = db.arangoSearch(name);
        view.drop().get();
        assertThat(view.exists().get()).isFalse();
    }

    @Test
    void rename() throws InterruptedException, ExecutionException {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 4));
        final String name = VIEW_NAME + "_renametest";
        final String newName = name + "_new";
        db.createArangoSearch(name, new ArangoSearchCreateOptions()).get();
        db.arangoSearch(name).rename(newName).get();
        assertThat(db.arangoSearch(name).exists().get()).isFalse();
        assertThat(db.arangoSearch(newName).exists().get()).isTrue();
    }

    @Test
    void create() throws InterruptedException, ExecutionException {
        assumeTrue(isAtLeastVersion(3, 4));
        final String name = VIEW_NAME + "_createtest";
        final ViewEntity info = db.arangoSearch(name).create().get();
        assertThat(info).isNotNull();
        assertThat(info.getId()).isNotNull();
        assertThat(info.getName()).isEqualTo(name);
        assertThat(info.getType()).isEqualTo(ViewType.ARANGO_SEARCH);
        assertThat(db.arangoSearch(name).exists().get()).isTrue();
    }

    @Test
    void createWithOptions() throws InterruptedException, ExecutionException {
        assumeTrue(isAtLeastVersion(3, 4));
        final String name = VIEW_NAME + "_createtest_withotpions";
        final ViewEntity info = db.arangoSearch(name).create(new ArangoSearchCreateOptions()).get();
        assertThat(info).isNotNull();
        assertThat(info.getId()).isNotNull();
        assertThat(info.getName()).isEqualTo(name);
        assertThat(info.getType()).isEqualTo(ViewType.ARANGO_SEARCH);
        assertThat(db.arangoSearch(name).exists().get()).isTrue();
    }

    @Test
    void createWithPrimarySort() throws ExecutionException, InterruptedException {
        assumeTrue(isAtLeastVersion(3, 5));
        final String name = "createWithPrimarySort";
        final ArangoSearchCreateOptions options = new ArangoSearchCreateOptions();

        final PrimarySort primarySort = PrimarySort.on("myFieldName");
        primarySort.ascending(true);
        options.primarySort(primarySort);
        options.consolidationIntervalMsec(666666L);

        final ViewEntity info = db.arangoSearch(name).create(options).get();
        assertThat(info).isNotNull();
        assertThat(info.getId()).isNotNull();
        assertThat(info.getName()).isEqualTo(name);
        assertThat(info.getType()).isEqualTo(ViewType.ARANGO_SEARCH);
        assertThat(db.arangoSearch(name).exists().get()).isTrue();
    }

    @Test
    void createWithCommitIntervalMsec() throws ExecutionException, InterruptedException {
        assumeTrue(isAtLeastVersion(3, 5));
        final String name = "createWithCommitIntervalMsec";
        final ArangoSearchCreateOptions options = new ArangoSearchCreateOptions();
        options.commitIntervalMsec(666666L);

        final ViewEntity info = db.arangoSearch(name).create(options).get();
        assertThat(info).isNotNull();
        assertThat(info.getId()).isNotNull();
        assertThat(info.getName()).isEqualTo(name);
        assertThat(info.getType()).isEqualTo(ViewType.ARANGO_SEARCH);
        assertThat(db.arangoSearch(name).exists().get()).isTrue();

        // check commit interval msec property
        final ArangoSearchAsync view = db.arangoSearch(name);
        final ArangoSearchPropertiesEntity properties = view.getProperties().get();
        assertThat(properties.getCommitIntervalMsec()).isEqualTo(666666L);
    }

    @Test
    void getProperties() throws InterruptedException, ExecutionException {
        assumeTrue(isAtLeastVersion(3, 4));
        final String name = VIEW_NAME + "_getpropertiestest";
        final ArangoSearchAsync view = db.arangoSearch(name);
        view.create(new ArangoSearchCreateOptions()).get();
        final ArangoSearchPropertiesEntity properties = view.getProperties().get();
        assertThat(properties).isNotNull();
        assertThat(properties.getId()).isNotNull();
        assertThat(properties.getName()).isEqualTo(name);
        assertThat(properties.getType()).isEqualTo(ViewType.ARANGO_SEARCH);
        assertThat(properties.getConsolidationIntervalMsec()).isNotNull();
        assertThat(properties.getCleanupIntervalStep()).isNotNull();
        final ConsolidationPolicy consolidate = properties.getConsolidationPolicy();
        assertThat(consolidate).isNotNull();
        final Collection<CollectionLink> links = properties.getLinks();
        assertThat(links).isEmpty();
    }

    @Test
    void updateProperties() throws InterruptedException, ExecutionException {
        assumeTrue(isAtLeastVersion(3, 4));
        db.createCollection("view_update_prop_test_collection").get();
        final String name = VIEW_NAME + "_updatepropertiestest";
        final ArangoSearchAsync view = db.arangoSearch(name);
        view.create(new ArangoSearchCreateOptions()).get();
        final ArangoSearchPropertiesOptions options = new ArangoSearchPropertiesOptions();
        options.cleanupIntervalStep(15L);
        options.consolidationIntervalMsec(65000L);
        options.consolidationPolicy(ConsolidationPolicy.of(ConsolidationType.BYTES_ACCUM).threshold(1.));
        options.link(
                CollectionLink.on("view_update_prop_test_collection").fields(FieldLink.on("value").analyzers("identity")
                        .trackListPositions(true).includeAllFields(true).storeValues(StoreValuesType.ID)));
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
        assertThat(link.getName()).isEqualTo("view_update_prop_test_collection");
        assertThat(link.getFields()).hasSize(1);
        final FieldLink next = link.getFields().iterator().next();
        assertThat(next.getName()).isEqualTo("value");
        assertThat(next.getIncludeAllFields()).isTrue();
        assertThat(next.getTrackListPositions()).isTrue();
        assertThat(next.getStoreValues()).isEqualTo(StoreValuesType.ID);
    }

    @Test
    void replaceProperties() throws InterruptedException, ExecutionException {
        assumeTrue(isAtLeastVersion(3, 4));
        db.createCollection("view_replace_prop_test_collection").get();
        final String name = VIEW_NAME + "_replacepropertiestest";
        final ArangoSearchAsync view = db.arangoSearch(name);
        view.create(new ArangoSearchCreateOptions()).get();
        final ArangoSearchPropertiesOptions options = new ArangoSearchPropertiesOptions();
        options.link(
                CollectionLink.on("view_replace_prop_test_collection").fields(FieldLink.on("value").analyzers("identity")));
        final ArangoSearchPropertiesEntity properties = view.replaceProperties(options).get();
        assertThat(properties).isNotNull();
        assertThat(properties.getLinks()).hasSize(1);
        final CollectionLink link = properties.getLinks().iterator().next();
        assertThat(link.getName()).isEqualTo("view_replace_prop_test_collection");
        assertThat(link.getFields()).hasSize(1);
        assertThat(link.getFields().iterator().next().getName()).isEqualTo("value");
    }

    private void createGetAndDeleteTypedAnalyzer(SearchAnalyzer analyzer) throws ExecutionException, InterruptedException {

        String fullyQualifiedName = db.dbName().get() + "::" + analyzer.getName();
        analyzer.setName(fullyQualifiedName);

        // createAnalyzer
        SearchAnalyzer createdAnalyzer = db.createSearchAnalyzer(analyzer).get();
        assertThat(createdAnalyzer).isEqualTo(analyzer);

        // getAnalyzer
        SearchAnalyzer gotAnalyzer = db.getSearchAnalyzer(analyzer.getName()).get();
        assertThat(gotAnalyzer).isEqualTo(analyzer);

        // getAnalyzers
        @SuppressWarnings("OptionalGetWithoutIsPresent")
        SearchAnalyzer foundAnalyzer = db.getSearchAnalyzers().get().stream().filter(it -> it.getName().equals(fullyQualifiedName))
                .findFirst().get();
        assertThat(foundAnalyzer).isEqualTo(analyzer);

        // deleteAnalyzer
        AnalyzerDeleteOptions deleteOptions = new AnalyzerDeleteOptions();
        deleteOptions.setForce(true);

        db.deleteSearchAnalyzer(analyzer.getName(), deleteOptions).get();

        try {
            db.getAnalyzer(analyzer.getName()).get();
            fail("deleted analyzer should not be found!");
        } catch (ExecutionException e) {
            assertThat(e.getCause()).isInstanceOf(ArangoDBException.class);
            assertThat(((ArangoDBException) e.getCause()).getResponseCode()).isEqualTo(404);
            assertThat(((ArangoDBException) e.getCause()).getErrorNum()).isEqualTo(1202);
        }

    }

    private void createGetAndDeleteAnalyzer(AnalyzerEntity options) throws ExecutionException, InterruptedException {

        String fullyQualifiedName = db.dbName().get() + "::" + options.getName();

        // createAnalyzer
        AnalyzerEntity createdAnalyzer = db.createAnalyzer(options).get();

        assertThat(createdAnalyzer.getName()).isEqualTo(fullyQualifiedName);
        assertThat(createdAnalyzer.getType()).isEqualTo(options.getType());
        assertThat(createdAnalyzer.getFeatures()).isEqualTo(options.getFeatures());
        compareProperties(createdAnalyzer.getProperties(), options.getProperties());

        // getAnalyzer
        AnalyzerEntity gotAnalyzer = db.getAnalyzer(options.getName()).get();
        assertThat(gotAnalyzer.getName()).isEqualTo(fullyQualifiedName);
        assertThat(gotAnalyzer.getType()).isEqualTo(options.getType());
        assertThat(gotAnalyzer.getFeatures()).isEqualTo(options.getFeatures());
        compareProperties(gotAnalyzer.getProperties(), options.getProperties());

        // getAnalyzers
        @SuppressWarnings("OptionalGetWithoutIsPresent")
        AnalyzerEntity foundAnalyzer = db.getAnalyzers().get().stream().filter(it -> it.getName().equals(fullyQualifiedName))
                .findFirst().get();

        assertThat(foundAnalyzer.getName()).isEqualTo(fullyQualifiedName);
        assertThat(foundAnalyzer.getType()).isEqualTo(options.getType());
        assertThat(foundAnalyzer.getFeatures()).isEqualTo(options.getFeatures());
        compareProperties(foundAnalyzer.getProperties(), options.getProperties());

        AnalyzerDeleteOptions deleteOptions = new AnalyzerDeleteOptions();
        deleteOptions.setForce(true);

        // deleteAnalyzer
        db.deleteAnalyzer(options.getName(), deleteOptions).get();

        try {
            db.getAnalyzer(options.getName()).get();
            fail();
        } catch (ExecutionException e) {
            assertThat(e.getCause()).isInstanceOf(ArangoDBException.class);
            assertThat(((ArangoDBException) e.getCause()).getResponseCode()).isEqualTo(404);
        }
    }

    private void compareProperties(Map<String, Object> actualProperties, Map<String, Object> expectedProperties) {
        expectedProperties.forEach((key, expectedValue) -> {
            Object actualValue = actualProperties.get(key);
            if (expectedValue instanceof Map) {
                assertThat(actualValue).isNotNull();
                assertThat(actualValue).isInstanceOf(Map.class);
                compareProperties((Map) actualValue, (Map) expectedValue);
            } else if (expectedValue instanceof Number) {
                assertThat(Double.valueOf(actualValue.toString())).isEqualTo(Double.valueOf(expectedValue.toString()));
            } else {
                assertThat(actualValue).isEqualTo(expectedValue);
            }
        });
    }

    @Test
    void identityAnalyzer() throws ExecutionException, InterruptedException {
        assumeTrue(isAtLeastVersion(3, 5));
        String name = "test-" + UUID.randomUUID();

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
    void identityAnalyzerTyped() throws ExecutionException, InterruptedException {
        assumeTrue(isAtLeastVersion(3, 5));

        String name = "test-" + UUID.randomUUID();

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
    void delimiterAnalyzer() throws ExecutionException, InterruptedException {
        assumeTrue(isAtLeastVersion(3, 5));
        String name = "test-" + UUID.randomUUID();

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
    void delimiterAnalyzerTyped() throws ExecutionException, InterruptedException {
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

        createGetAndDeleteTypedAnalyzer(analyzer);
    }

    @Test
    void stemAnalyzer() throws ExecutionException, InterruptedException {
        assumeTrue(isAtLeastVersion(3, 5));
        String name = "test-" + UUID.randomUUID();

        Set<AnalyzerFeature> features = new HashSet<>();
        features.add(AnalyzerFeature.frequency);
        features.add(AnalyzerFeature.norm);
        features.add(AnalyzerFeature.position);

        AnalyzerEntity options = new AnalyzerEntity();
        options.setFeatures(features);
        options.setName(name);
        options.setType(AnalyzerType.stem);
        options.setProperties(Collections.singletonMap("locale", "ru"));

        createGetAndDeleteAnalyzer(options);
    }

    @Test
    void stemAnalyzerTyped() throws ExecutionException, InterruptedException {
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

        createGetAndDeleteTypedAnalyzer(options);
    }

    @Test
    void normAnalyzer() throws ExecutionException, InterruptedException {
        assumeTrue(isAtLeastVersion(3, 5));
        String name = "test-" + UUID.randomUUID();

        Set<AnalyzerFeature> features = new HashSet<>();
        features.add(AnalyzerFeature.frequency);
        features.add(AnalyzerFeature.norm);
        features.add(AnalyzerFeature.position);

        Map<String, Object> properties = new HashMap<>();
        properties.put("locale", "ru");
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
    void normAnalyzerTyped() throws ExecutionException, InterruptedException {
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

        createGetAndDeleteTypedAnalyzer(options);
    }

    @Test
    void ngramAnalyzer() throws ExecutionException, InterruptedException {
        assumeTrue(isAtLeastVersion(3, 5));

        String name = "test-" + UUID.randomUUID();

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
    void ngramAnalyzerTyped() throws ExecutionException, InterruptedException {
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

        createGetAndDeleteTypedAnalyzer(analyzer);
    }

    @Test
    void textAnalyzer() throws ExecutionException, InterruptedException {
        assumeTrue(isAtLeastVersion(3, 5));

        String name = "test-" + UUID.randomUUID();

        Set<AnalyzerFeature> features = new HashSet<>();
        features.add(AnalyzerFeature.frequency);
        features.add(AnalyzerFeature.norm);
        features.add(AnalyzerFeature.position);

        Map<String, Object> properties = new HashMap<>();
        properties.put("locale", "ru");
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
    void textAnalyzerTyped() throws ExecutionException, InterruptedException {
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

        createGetAndDeleteTypedAnalyzer(analyzer);
    }

}
