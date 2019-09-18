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
import com.arangodb.model.arangosearch.AnalyzerDeleteOptions;
import com.arangodb.model.arangosearch.ArangoSearchCreateOptions;
import com.arangodb.model.arangosearch.ArangoSearchPropertiesOptions;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
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
        options.consolidationIntervalMsec(666666L);

        final ViewEntity info = db.arangoSearch(viewName).create(options);
        assertThat(info, is(not(nullValue())));
        assertThat(info.getId(), is(not(nullValue())));
        assertThat(info.getName(), is(viewName));
        assertThat(info.getType(), is(ViewType.ARANGO_SEARCH));
        assertThat(db.arangoSearch(viewName).exists(), is(true));
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
        assertThat(createdAnalyzer.getProperties(), is(options.getProperties()));

        // getAnalyzer
        AnalyzerEntity gotAnalyzer = db.getAnalyzer(options.getName());
        assertThat(gotAnalyzer.getName(), is(fullyQualifiedName));
        assertThat(gotAnalyzer.getType(), is(options.getType()));
        assertThat(gotAnalyzer.getFeatures(), is(options.getFeatures()));
        assertThat(gotAnalyzer.getProperties(), is(options.getProperties()));

        // getAnalyzers
        @SuppressWarnings("OptionalGetWithoutIsPresent")
        AnalyzerEntity foundAnalyzer = db.getAnalyzers().stream().filter(it -> it.getName().equals(fullyQualifiedName))
                .findFirst().get();

        assertThat(foundAnalyzer.getName(), is(fullyQualifiedName));
        assertThat(foundAnalyzer.getType(), is(options.getType()));
        assertThat(foundAnalyzer.getFeatures(), is(options.getFeatures()));
        assertThat(foundAnalyzer.getProperties(), is(options.getProperties()));

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

}
