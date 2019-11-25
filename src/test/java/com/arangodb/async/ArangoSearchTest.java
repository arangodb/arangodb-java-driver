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
import com.arangodb.model.arangosearch.AnalyzerDeleteOptions;
import com.arangodb.model.arangosearch.ArangoSearchCreateOptions;
import com.arangodb.model.arangosearch.ArangoSearchPropertiesOptions;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.ExecutionException;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

/**
 * @author Mark Vollmary
 */

public class ArangoSearchTest extends BaseTest {

    private static final String VIEW_NAME = "view_test";

    @BeforeClass
    public static void setup() throws InterruptedException, ExecutionException {
        if (!isAtLeastVersion(arangoDB, 3, 4))
            return;
        db.createArangoSearch(VIEW_NAME, new ArangoSearchCreateOptions()).get();
    }

    @Test
    public void exists() throws InterruptedException, ExecutionException {
        assumeTrue(isAtLeastVersion(3, 4));
        assertThat(db.arangoSearch(VIEW_NAME).exists().get(), is(true));
    }

    @Test
    public void getInfo() throws InterruptedException, ExecutionException {
        assumeTrue(isAtLeastVersion(3, 4));
        final ViewEntity info = db.arangoSearch(VIEW_NAME).getInfo().get();
        assertThat(info, is(not(nullValue())));
        assertThat(info.getId(), is(not(nullValue())));
        assertThat(info.getName(), is(VIEW_NAME));
        assertThat(info.getType(), is(ViewType.ARANGO_SEARCH));
    }

    @Test
    public void drop() throws InterruptedException, ExecutionException {
        assumeTrue(isAtLeastVersion(3, 4));
        final String name = VIEW_NAME + "_droptest";
        db.createArangoSearch(name, new ArangoSearchCreateOptions()).get();
        final ArangoViewAsync view = db.arangoSearch(name);
        view.drop().get();
        assertThat(view.exists().get(), is(false));
    }

    @Test
    public void rename() throws InterruptedException, ExecutionException {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 4));
        final String name = VIEW_NAME + "_renametest";
        final String newName = name + "_new";
        db.createArangoSearch(name, new ArangoSearchCreateOptions()).get();
        db.arangoSearch(name).rename(newName).get();
        assertThat(db.arangoSearch(name).exists().get(), is(false));
        assertThat(db.arangoSearch(newName).exists().get(), is(true));
    }

    @Test
    public void create() throws InterruptedException, ExecutionException {
        assumeTrue(isAtLeastVersion(3, 4));
        final String name = VIEW_NAME + "_createtest";
        final ViewEntity info = db.arangoSearch(name).create().get();
        assertThat(info, is(not(nullValue())));
        assertThat(info.getId(), is(not(nullValue())));
        assertThat(info.getName(), is(name));
        assertThat(info.getType(), is(ViewType.ARANGO_SEARCH));
        assertThat(db.arangoSearch(name).exists().get(), is(true));
    }

    @Test
    public void createWithOptions() throws InterruptedException, ExecutionException {
        assumeTrue(isAtLeastVersion(3, 4));
        final String name = VIEW_NAME + "_createtest_withotpions";
        final ViewEntity info = db.arangoSearch(name).create(new ArangoSearchCreateOptions()).get();
        assertThat(info, is(not(nullValue())));
        assertThat(info.getId(), is(not(nullValue())));
        assertThat(info.getName(), is(name));
        assertThat(info.getType(), is(ViewType.ARANGO_SEARCH));
        assertThat(db.arangoSearch(name).exists().get(), is(true));
    }

    @Test
    public void createWithPrimarySort() throws ExecutionException, InterruptedException {
        assumeTrue(isAtLeastVersion(3, 5));
        final String name = "createWithPrimarySort";
        final ArangoSearchCreateOptions options = new ArangoSearchCreateOptions();

        final PrimarySort primarySort = PrimarySort.on("myFieldName");
        primarySort.ascending(true);
        options.primarySort(primarySort);
        options.consolidationIntervalMsec(666666L);

        final ViewEntity info = db.arangoSearch(name).create(options).get();
        assertThat(info, is(not(nullValue())));
        assertThat(info.getId(), is(not(nullValue())));
        assertThat(info.getName(), is(name));
        assertThat(info.getType(), is(ViewType.ARANGO_SEARCH));
        assertThat(db.arangoSearch(name).exists().get(), is(true));
    }

    @Test
    public void createWithCommitIntervalMsec() throws ExecutionException, InterruptedException {
        assumeTrue(isAtLeastVersion(3, 5));
        final String name = "createWithCommitIntervalMsec";
        final ArangoSearchCreateOptions options = new ArangoSearchCreateOptions();
        options.commitIntervalMsec(666666L);

        final ViewEntity info = db.arangoSearch(name).create(options).get();
        assertThat(info, is(not(nullValue())));
        assertThat(info.getId(), is(not(nullValue())));
        assertThat(info.getName(), is(name));
        assertThat(info.getType(), is(ViewType.ARANGO_SEARCH));
        assertThat(db.arangoSearch(name).exists().get(), is(true));

        // check commit interval msec property
        final ArangoSearchAsync view = db.arangoSearch(name);
        final ArangoSearchPropertiesEntity properties = view.getProperties().get();
        assertThat(properties.getCommitIntervalMsec(), is(666666L));
    }

    @Test
    public void getProperties() throws InterruptedException, ExecutionException {
        assumeTrue(isAtLeastVersion(3, 4));
        final String name = VIEW_NAME + "_getpropertiestest";
        final ArangoSearchAsync view = db.arangoSearch(name);
        view.create(new ArangoSearchCreateOptions()).get();
        final ArangoSearchPropertiesEntity properties = view.getProperties().get();
        assertThat(properties, is(not(nullValue())));
        assertThat(properties.getId(), is(not(nullValue())));
        assertThat(properties.getName(), is(name));
        assertThat(properties.getType(), is(ViewType.ARANGO_SEARCH));
        assertThat(properties.getConsolidationIntervalMsec(), is(not(nullValue())));
        assertThat(properties.getCleanupIntervalStep(), is(not(nullValue())));
        final ConsolidationPolicy consolidate = properties.getConsolidationPolicy();
        assertThat(consolidate, is(is(not(nullValue()))));
        final Collection<CollectionLink> links = properties.getLinks();
        assertThat(links.isEmpty(), is(true));
    }

    @Test
    public void updateProperties() throws InterruptedException, ExecutionException {
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
        assertThat(properties, is(not(nullValue())));
        assertThat(properties.getCleanupIntervalStep(), is(15L));
        assertThat(properties.getConsolidationIntervalMsec(), is(65000L));
        final ConsolidationPolicy consolidate = properties.getConsolidationPolicy();
        assertThat(consolidate, is(not(nullValue())));
        assertThat(consolidate.getType(), is(ConsolidationType.BYTES_ACCUM));
        assertThat(consolidate.getThreshold(), is(1.));
        assertThat(properties.getLinks().size(), is(1));
        final CollectionLink link = properties.getLinks().iterator().next();
        assertThat(link.getName(), is("view_update_prop_test_collection"));
        assertThat(link.getFields().size(), is(1));
        final FieldLink next = link.getFields().iterator().next();
        assertThat(next.getName(), is("value"));
        assertThat(next.getIncludeAllFields(), is(true));
        assertThat(next.getTrackListPositions(), is(true));
        assertThat(next.getStoreValues(), is(StoreValuesType.ID));
    }

    @Test
    public void replaceProperties() throws InterruptedException, ExecutionException {
        assumeTrue(isAtLeastVersion(3, 4));
        db.createCollection("view_replace_prop_test_collection").get();
        final String name = VIEW_NAME + "_replacepropertiestest";
        final ArangoSearchAsync view = db.arangoSearch(name);
        view.create(new ArangoSearchCreateOptions()).get();
        final ArangoSearchPropertiesOptions options = new ArangoSearchPropertiesOptions();
        options.link(
                CollectionLink.on("view_replace_prop_test_collection").fields(FieldLink.on("value").analyzers("identity")));
        final ArangoSearchPropertiesEntity properties = view.replaceProperties(options).get();
        assertThat(properties, is(not(nullValue())));
        assertThat(properties.getLinks().size(), is(1));
        final CollectionLink link = properties.getLinks().iterator().next();
        assertThat(link.getName(), is("view_replace_prop_test_collection"));
        assertThat(link.getFields().size(), is(1));
        assertThat(link.getFields().iterator().next().getName(), is("value"));
    }

    private void createGetAndDeleteAnalyzer(AnalyzerEntity options) throws ExecutionException, InterruptedException {

        String fullyQualifiedName = db.name() + "::" + options.getName();

        // createAnalyzer
        AnalyzerEntity createdAnalyzer = db.createAnalyzer(options).get();

        assertThat(createdAnalyzer.getName(), is(fullyQualifiedName));
        assertThat(createdAnalyzer.getType(), is(options.getType()));
        assertThat(createdAnalyzer.getFeatures(), is(options.getFeatures()));
        compareProperties(createdAnalyzer.getProperties(), options.getProperties());

        // getAnalyzer
        AnalyzerEntity gotAnalyzer = db.getAnalyzer(options.getName()).get();
        assertThat(gotAnalyzer.getName(), is(fullyQualifiedName));
        assertThat(gotAnalyzer.getType(), is(options.getType()));
        assertThat(gotAnalyzer.getFeatures(), is(options.getFeatures()));
        compareProperties(gotAnalyzer.getProperties(), options.getProperties());

        // getAnalyzers
        @SuppressWarnings("OptionalGetWithoutIsPresent")
        AnalyzerEntity foundAnalyzer = db.getAnalyzers().get().stream().filter(it -> it.getName().equals(fullyQualifiedName))
                .findFirst().get();

        assertThat(foundAnalyzer.getName(), is(fullyQualifiedName));
        assertThat(foundAnalyzer.getType(), is(options.getType()));
        assertThat(foundAnalyzer.getFeatures(), is(options.getFeatures()));
        compareProperties(foundAnalyzer.getProperties(), options.getProperties());

        AnalyzerDeleteOptions deleteOptions = new AnalyzerDeleteOptions();
        deleteOptions.setForce(true);

        // deleteAnalyzer
        db.deleteAnalyzer(options.getName(), deleteOptions).get();

        try {
            db.getAnalyzer(options.getName()).get();
            fail();
        } catch (ExecutionException e) {
            assertThat(e.getCause(), instanceOf(ArangoDBException.class));
            assertThat(((ArangoDBException) e.getCause()).getResponseCode(), is(404));
        }
    }

    private void compareProperties(Map<String, Object> actualProperties, Map<String, Object> expectedProperties) {
        expectedProperties.forEach((key, value) -> {
            Object expectedValue = actualProperties.get(key);
            if (value instanceof Map) {
                assertThat(expectedValue, notNullValue());
                assertThat(expectedValue, instanceOf(Map.class));
                compareProperties((Map) value, (Map) expectedValue);
            } else {
                assertThat(value, is(expectedValue));
            }
        });
    }

    @Test
    public void identityAnalyzer() throws ExecutionException, InterruptedException {
        assumeTrue(isAtLeastVersion(3, 5));
        String name = "test-" + UUID.randomUUID().toString();

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
    public void delimiterAnalyzer() throws ExecutionException, InterruptedException {
        assumeTrue(isAtLeastVersion(3, 5));
        String name = "test-" + UUID.randomUUID().toString();

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
    public void stemAnalyzer() throws ExecutionException, InterruptedException {
        assumeTrue(isAtLeastVersion(3, 5));
        String name = "test-" + UUID.randomUUID().toString();

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
    public void normAnalyzer() throws ExecutionException, InterruptedException {
        assumeTrue(isAtLeastVersion(3, 5));
        String name = "test-" + UUID.randomUUID().toString();

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
    public void ngramAnalyzer() throws ExecutionException, InterruptedException {
        assumeTrue(isAtLeastVersion(3, 5));

        String name = "test-" + UUID.randomUUID().toString();

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
    public void textAnalyzer() throws ExecutionException, InterruptedException {
        assumeTrue(isAtLeastVersion(3, 5));

        String name = "test-" + UUID.randomUUID().toString();

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

}
