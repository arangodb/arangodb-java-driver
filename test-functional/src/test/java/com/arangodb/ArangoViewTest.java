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
import com.arangodb.util.TestUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.api.Assumptions.assumeTrue;


/**
 * @author Mark Vollmary
 * @author Michele Rastelli
 */
class ArangoViewTest extends BaseJunit5 {

    @BeforeAll
    static void init() {
        initDB();
    }

    @ParameterizedTest
    @MethodSource("dbs")
    void create(ArangoDatabase db) {
        String name = rndName();
        try {
            db.createView(name, ViewType.ARANGO_SEARCH);
            assertThat(db.view(name).exists()).isTrue();
        } catch (Exception e) {
            System.err.println("Got exception with name: " + TestUtils.unicodeEscape(name));
            throw e;
        }
    }

    @ParameterizedTest
    @MethodSource("dbs")
    void createWithNotNormalizedName(ArangoDatabase db) {
        assumeTrue(supportsExtendedNames());
        final String name = "view-\u006E\u0303\u00f1";
        Throwable thrown = catchThrowable(() -> db.createView(name, ViewType.ARANGO_SEARCH));
        assertThat(thrown)
                .isInstanceOf(ArangoDBException.class)
                .hasMessageContaining("normalized")
                .extracting(it -> ((ArangoDBException) it).getResponseCode()).isEqualTo(400);
    }

    @ParameterizedTest
    @MethodSource("dbs")
    void getInfo(ArangoDatabase db) {
        String name = rndName();
        db.createView(name, ViewType.ARANGO_SEARCH);
        final ViewEntity info = db.view(name).getInfo();
        assertThat(info).isNotNull();
        assertThat(info.getId()).isNotNull();
        assertThat(info.getName()).isEqualTo(name);
        assertThat(info.getType()).isEqualTo(ViewType.ARANGO_SEARCH);
    }

    @ParameterizedTest
    @MethodSource("dbs")
    void getInfoSearchAlias(ArangoDatabase db) {
        String name = rndName();
        db.createView(name, ViewType.SEARCH_ALIAS);
        final ViewEntity info = db.view(name).getInfo();
        assertThat(info).isNotNull();
        assertThat(info.getId()).isNotNull();
        assertThat(info.getName()).isEqualTo(name);
        assertThat(info.getType()).isEqualTo(ViewType.SEARCH_ALIAS);
    }

    @ParameterizedTest
    @MethodSource("dbs")
    void getViews(ArangoDatabase db) {
        String name1 = rndName();
        String name2 = rndName();
        db.createView(name1, ViewType.ARANGO_SEARCH);
        db.createView(name2, ViewType.SEARCH_ALIAS);
        Collection<ViewEntity> views = db.getViews();
        assertThat(views).extracting(ViewEntity::getName).contains(name1, name2);
    }

    @ParameterizedTest
    @MethodSource("dbs")
    void drop(ArangoDatabase db) {
        String name = rndName();
        db.createView(name, ViewType.ARANGO_SEARCH);
        final ArangoView view = db.view(name);
        view.drop();
        assertThat(view.exists()).isFalse();
    }

    @ParameterizedTest
    @MethodSource("dbs")
    void rename(ArangoDatabase db) {
        assumeTrue(isSingleServer());
        String oldName = rndName();
        String newName = rndName();

        db.createView(oldName, ViewType.ARANGO_SEARCH);
        db.view(oldName).rename(newName);
        assertThat(db.view(oldName).exists()).isFalse();
        assertThat(db.view(newName).exists()).isTrue();
    }

}
