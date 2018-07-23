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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import com.arangodb.ArangoDB.Builder;
import com.arangodb.entity.ViewEntity;
import com.arangodb.entity.ViewType;
import com.arangodb.entity.arangosearch.ArangoSearchPropertiesEntity;
import com.arangodb.model.arangosearch.ArangoSearchCreateOptions;
import com.arangodb.model.arangosearch.ArangoSearchPropertiesOptions;
import com.arangodb.model.arangosearch.CollectionLink;
import com.arangodb.model.arangosearch.ConsolidateThreshold;
import com.arangodb.model.arangosearch.ConsolidateType;
import com.arangodb.model.arangosearch.FieldLink;

/**
 * @author Mark Vollmary
 *
 */
@RunWith(Parameterized.class)
public class ArangoSearchTest extends BaseTest {

	private static final String VIEW_NAME = "view_test";

	public ArangoSearchTest(final Builder builder) {
		super(builder);
		db.createArangoSearch(VIEW_NAME, new ArangoSearchCreateOptions());
	}

	@Test
	public void exists() {
		assertThat(db.arangoSearch(VIEW_NAME).exists(), is(true));
	}

	@Test
	public void getInfo() {
		final ViewEntity info = db.arangoSearch(VIEW_NAME).getInfo();
		assertThat(info, is(not(nullValue())));
		assertThat(info.getId(), is(not(nullValue())));
		assertThat(info.getName(), is(VIEW_NAME));
		assertThat(info.getType(), is(ViewType.ARANGO_SEARCH));
	}

	@Test
	public void drop() {
		final String name = VIEW_NAME + "_droptest";
		db.createArangoSearch(name, new ArangoSearchCreateOptions());
		final ArangoView view = db.arangoSearch(name);
		view.drop();
		assertThat(view.exists(), is(false));
	}

	@Test
	public void rename() {
		final String name = VIEW_NAME + "_renametest";
		final String newName = name + "_new";
		db.createArangoSearch(name, new ArangoSearchCreateOptions());
		db.arangoSearch(name).rename(newName);
		assertThat(db.arangoSearch(name).exists(), is(false));
		assertThat(db.arangoSearch(newName).exists(), is(true));
	}

	@Test
	public void create() {
		final String name = VIEW_NAME + "_createtest";
		final ViewEntity info = db.arangoSearch(name).create();
		assertThat(info, is(not(nullValue())));
		assertThat(info.getId(), is(not(nullValue())));
		assertThat(info.getName(), is(name));
		assertThat(info.getType(), is(ViewType.ARANGO_SEARCH));
		assertThat(db.arangoSearch(name).exists(), is(true));
	}

	@Test
	public void createWithOptions() {
		final String name = VIEW_NAME + "_createtest";
		final ViewEntity info = db.arangoSearch(name).create(new ArangoSearchCreateOptions());
		assertThat(info, is(not(nullValue())));
		assertThat(info.getId(), is(not(nullValue())));
		assertThat(info.getName(), is(name));
		assertThat(info.getType(), is(ViewType.ARANGO_SEARCH));
		assertThat(db.arangoSearch(name).exists(), is(true));
	}

	@Test
	public void getProperties() {
		final String name = VIEW_NAME + "_getpropertiestest";
		final ArangoSearch view = db.arangoSearch(name);
		view.create(new ArangoSearchCreateOptions());
		final ArangoSearchPropertiesEntity properties = view.getProperties();
		assertThat(properties, is(not(nullValue())));
		assertThat(properties.getId(), is(not(nullValue())));
		assertThat(properties.getName(), is(name));
		assertThat(properties.getType(), is(ViewType.ARANGO_SEARCH));
		assertThat(properties.getLocale(), is(not(nullValue())));
		assertThat(properties.getCommitIntervalMsec(), is(not(nullValue())));
		assertThat(properties.getCleanupIntervalStep(), is(not(nullValue())));
		final Collection<ConsolidateThreshold> thresholds = properties.getThresholds();
		assertThat(thresholds.size(), is(4));
		final Collection<CollectionLink> links = properties.getLinks();
		assertThat(links.isEmpty(), is(true));
	}

	@Test
	public void updateProperties() {
		db.createCollection("view_update_prop_test_collection");
		final String name = VIEW_NAME + "_updatepropertiestest";
		final ArangoSearch view = db.arangoSearch(name);
		view.create(new ArangoSearchCreateOptions());
		final ArangoSearchPropertiesOptions options = new ArangoSearchPropertiesOptions();
		options.cleanupIntervalStep(15L);
		options.commitIntervalMsec(65000L);
		options.threshold(ConsolidateThreshold.of(ConsolidateType.COUNT).threshold(1.));
		options.link(
			CollectionLink.on("view_update_prop_test_collection").fields(FieldLink.on("value").analyzers("identity")));
		final ArangoSearchPropertiesEntity properties = view.updateProperties(options);
		assertThat(properties, is(not(nullValue())));
		assertThat(properties.getCleanupIntervalStep(), is(15L));
		assertThat(properties.getCommitIntervalMsec(), is(65000L));
		assertThat(properties.getThresholds().size() >= 1, is(true));
		for (final ConsolidateThreshold t : properties.getThresholds()) {
			if (t.getType() == ConsolidateType.COUNT) {
				assertThat(t.getThreshold(), is(1.));
			}
		}
		assertThat(properties.getLinks().size(), is(1));
		final CollectionLink link = properties.getLinks().iterator().next();
		assertThat(link.getName(), is("view_update_prop_test_collection"));
		assertThat(link.getFields().size(), is(1));
		assertThat(link.getFields().iterator().next().getName(), is("value"));
	}

	@Test
	public void replaceProperties() {
		db.createCollection("view_replace_prop_test_collection");
		final String name = VIEW_NAME + "_updatepropertiestest";
		final ArangoSearch view = db.arangoSearch(name);
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
