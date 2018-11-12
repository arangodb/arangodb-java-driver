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

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.Collection;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import com.arangodb.ArangoDB.Builder;
import com.arangodb.entity.ServerRole;
import com.arangodb.entity.ViewEntity;
import com.arangodb.entity.ViewType;
import com.arangodb.entity.arangosearch.ArangoSearchPropertiesEntity;
import com.arangodb.entity.arangosearch.CollectionLink;
import com.arangodb.entity.arangosearch.ConsolidationPolicy;
import com.arangodb.entity.arangosearch.ConsolidationType;
import com.arangodb.entity.arangosearch.FieldLink;
import com.arangodb.entity.arangosearch.StoreValuesType;
import com.arangodb.model.arangosearch.ArangoSearchCreateOptions;
import com.arangodb.model.arangosearch.ArangoSearchPropertiesOptions;

/**
 * @author Mark Vollmary
 *
 */
@RunWith(Parameterized.class)
public class ArangoSearchTest extends BaseTest {

	private static final String VIEW_NAME = "view_test";

	public ArangoSearchTest(final Builder builder) {
		super(builder);
	}

	@After
	public void teardown() {
		try {
			db.view(VIEW_NAME).drop();
		} catch (final ArangoDBException e) {
		}
	}

	@Test
	public void exists() {
		if (!requireVersion(3, 4)) {
			return;
		}
		db.createArangoSearch(VIEW_NAME, new ArangoSearchCreateOptions());
		assertThat(db.arangoSearch(VIEW_NAME).exists(), is(true));
	}

	@Test
	public void getInfo() {
		if (!requireVersion(3, 4)) {
			return;
		}
		db.createArangoSearch(VIEW_NAME, new ArangoSearchCreateOptions());
		final ViewEntity info = db.arangoSearch(VIEW_NAME).getInfo();
		assertThat(info, is(not(nullValue())));
		assertThat(info.getId(), is(not(nullValue())));
		assertThat(info.getName(), is(VIEW_NAME));
		assertThat(info.getType(), is(ViewType.ARANGO_SEARCH));
	}

	@Test
	public void drop() {
		if (!requireVersion(3, 4)) {
			return;
		}
		db.createArangoSearch(VIEW_NAME, new ArangoSearchCreateOptions());
		final ArangoView view = db.arangoSearch(VIEW_NAME);
		view.drop();
		assertThat(view.exists(), is(false));
	}

	@Test
	public void rename() {
		if (arangoDB.getRole() != ServerRole.SINGLE) {
			return;
		}
		if (!requireVersion(3, 4)) {
			return;
		}
		final String name = VIEW_NAME + "_new";
		db.createArangoSearch(name, new ArangoSearchCreateOptions());
		db.arangoSearch(name).rename(VIEW_NAME);
		assertThat(db.arangoSearch(name).exists(), is(false));
		assertThat(db.arangoSearch(VIEW_NAME).exists(), is(true));
	}

	@Test
	public void create() {
		if (!requireVersion(3, 4)) {
			return;
		}
		final ViewEntity info = db.arangoSearch(VIEW_NAME).create();
		assertThat(info, is(not(nullValue())));
		assertThat(info.getId(), is(not(nullValue())));
		assertThat(info.getName(), is(VIEW_NAME));
		assertThat(info.getType(), is(ViewType.ARANGO_SEARCH));
		assertThat(db.arangoSearch(VIEW_NAME).exists(), is(true));
	}

	@Test
	public void createWithOptions() {
		if (!requireVersion(3, 4)) {
			return;
		}
		final ArangoSearchCreateOptions options = new ArangoSearchCreateOptions();
		final ViewEntity info = db.arangoSearch(VIEW_NAME).create(options);
		assertThat(info, is(not(nullValue())));
		assertThat(info.getId(), is(not(nullValue())));
		assertThat(info.getName(), is(VIEW_NAME));
		assertThat(info.getType(), is(ViewType.ARANGO_SEARCH));
		assertThat(db.arangoSearch(VIEW_NAME).exists(), is(true));
	}

	@Test
	public void getProperties() {
		if (!requireVersion(3, 4)) {
			return;
		}
		final ArangoSearch view = db.arangoSearch(VIEW_NAME);
		view.create(new ArangoSearchCreateOptions());
		final ArangoSearchPropertiesEntity properties = view.getProperties();
		assertThat(properties, is(not(nullValue())));
		assertThat(properties.getId(), is(not(nullValue())));
		assertThat(properties.getName(), is(VIEW_NAME));
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
		if (!requireVersion(3, 4)) {
			return;
		}
		db.createCollection("view_update_prop_test_collection");
		final ArangoSearch view = db.arangoSearch(VIEW_NAME);
		view.create(new ArangoSearchCreateOptions());
		final ArangoSearchPropertiesOptions options = new ArangoSearchPropertiesOptions();
		options.cleanupIntervalStep(15L);
		options.consolidationIntervalMsec(65000L);
		options.consolidationPolicy(ConsolidationPolicy.of(ConsolidationType.BYTES_ACCUM).threshold(1.));
		options.link(
			CollectionLink.on("view_update_prop_test_collection").fields(FieldLink.on("value").analyzers("identity")
					.trackListPositions(true).includeAllFields(true).storeValues(StoreValuesType.ID)));
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
		assertThat(link.getName(), is("view_update_prop_test_collection"));
		assertThat(link.getFields().size(), is(1));
		final FieldLink next = link.getFields().iterator().next();
		assertThat(next.getName(), is("value"));
		assertThat(next.getIncludeAllFields(), is(true));
		assertThat(next.getTrackListPositions(), is(true));
		assertThat(next.getStoreValues(), is(StoreValuesType.ID));
	}

	@Test
	public void replaceProperties() {
		if (!requireVersion(3, 4)) {
			return;
		}
		db.createCollection("view_replace_prop_test_collection");
		final ArangoSearch view = db.arangoSearch(VIEW_NAME);
		view.create(new ArangoSearchCreateOptions());
		final ArangoSearchPropertiesOptions options = new ArangoSearchPropertiesOptions();
		options.link(
			CollectionLink.on("view_replace_prop_test_collection").fields(FieldLink.on("value").analyzers("identity")));
		final ArangoSearchPropertiesEntity properties = view.replaceProperties(options);
		assertThat(properties, is(not(nullValue())));
		assertThat(properties.getLinks().size(), is(1));
		final CollectionLink link = properties.getLinks().iterator().next();
		assertThat(link.getName(), is("view_replace_prop_test_collection"));
		assertThat(link.getFields().size(), is(1));
		assertThat(link.getFields().iterator().next().getName(), is("value"));
	}

}
