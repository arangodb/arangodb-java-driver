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

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import com.arangodb.ArangoDB.Builder;
import com.arangodb.entity.ServerRole;
import com.arangodb.entity.ViewEntity;
import com.arangodb.entity.ViewType;

/**
 * @author Mark Vollmary
 *
 */
@RunWith(Parameterized.class)
public class ArangoViewTest extends BaseTest {

	private static final String VIEW_NAME = "view_test";

	public ArangoViewTest(final Builder builder) {
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
		db.createView(VIEW_NAME, ViewType.ARANGO_SEARCH);
		assertThat(db.view(VIEW_NAME).exists(), is(true));
	}

	@Test
	public void getInfo() {
		if (!requireVersion(3, 4)) {
			return;
		}
		db.createView(VIEW_NAME, ViewType.ARANGO_SEARCH);
		final ViewEntity info = db.view(VIEW_NAME).getInfo();
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
		db.createView(VIEW_NAME, ViewType.ARANGO_SEARCH);
		final ArangoView view = db.view(VIEW_NAME);
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
		db.createView(name, ViewType.ARANGO_SEARCH);
		db.view(name).rename(VIEW_NAME);
		assertThat(db.view(name).exists(), is(false));
		assertThat(db.view(VIEW_NAME).exists(), is(true));
	}

}
