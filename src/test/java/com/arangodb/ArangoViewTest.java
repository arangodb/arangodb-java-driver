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
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import static org.junit.Assume.assumeTrue;

/**
 * @author Mark Vollmary
 */
@RunWith(Parameterized.class)
public class ArangoViewTest extends BaseTest {

    @BeforeClass
    public static void init() {
        BaseTest.initDB();
    }

    public ArangoViewTest(final ArangoDB arangoDB) {
        super(arangoDB);
    }

    @Test
    public void exists() {
        String name = "view-" + rnd();
        assumeTrue(isAtLeastVersion(3, 4));
        db.createView(name, ViewType.ARANGO_SEARCH);
        assertThat(db.view(name).exists(), is(true));
    }

    @Test
    public void getInfo() {
        String name = "view-" + rnd();
        assumeTrue(isAtLeastVersion(3, 4));
        db.createView(name, ViewType.ARANGO_SEARCH);
        final ViewEntity info = db.view(name).getInfo();
        assertThat(info, is(not(nullValue())));
        assertThat(info.getId(), is(not(nullValue())));
        assertThat(info.getName(), is(name));
        assertThat(info.getType(), is(ViewType.ARANGO_SEARCH));
    }

    @Test
    public void drop() {
        String name = "view-" + rnd();
        assumeTrue(isAtLeastVersion(3, 4));
        db.createView(name, ViewType.ARANGO_SEARCH);
        final ArangoView view = db.view(name);
        view.drop();
        assertThat(view.exists(), is(false));
    }

    @Test
    public void rename() {
        String oldName = "view-" + rnd();
        String newName = "view-" + rnd();

        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 4));
        db.createView(oldName, ViewType.ARANGO_SEARCH);
        db.view(oldName).rename(newName);
        assertThat(db.view(oldName).exists(), is(false));
        assertThat(db.view(newName).exists(), is(true));
    }

}
