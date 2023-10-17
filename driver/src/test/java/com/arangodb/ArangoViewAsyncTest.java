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
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collection;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.api.Assumptions.assumeTrue;


/**
 * @author Mark Vollmary
 * @author Michele Rastelli
 */
class ArangoViewAsyncTest extends BaseJunit5 {

    @BeforeAll
    static void init() {
        initDB();
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("asyncDbs")
    void create(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        String name = rndName();
        db.createView(name, ViewType.ARANGO_SEARCH).get();
        assertThat(db.view(name).exists().get()).isTrue();
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("asyncDbs")
    void createWithNotNormalizedName(ArangoDatabaseAsync db) {
        assumeTrue(supportsExtendedNames());
        final String name = "view-\u006E\u0303\u00f1";
        Throwable thrown = catchThrowable(() -> db.createView(name, ViewType.ARANGO_SEARCH));
        assertThat(thrown)
                .isInstanceOf(ArangoDBException.class)
                .hasMessageContaining("normalized")
                .extracting(it -> ((ArangoDBException) it).getResponseCode()).isEqualTo(400);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("asyncDbs")
    void getInfo(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        String name = rndName();
        db.createView(name, ViewType.ARANGO_SEARCH).get();
        final ViewEntity info = db.view(name).getInfo().get();
        assertThat(info).isNotNull();
        assertThat(info.getId()).isNotNull();
        assertThat(info.getName()).isEqualTo(name);
        assertThat(info.getType()).isEqualTo(ViewType.ARANGO_SEARCH);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("asyncDbs")
    void getInfoSearchAlias(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        assumeTrue(isAtLeastVersion(3, 10));
        String name = rndName();
        db.createView(name, ViewType.SEARCH_ALIAS).get();
        final ViewEntity info = db.view(name).getInfo().get();
        assertThat(info).isNotNull();
        assertThat(info.getId()).isNotNull();
        assertThat(info.getName()).isEqualTo(name);
        assertThat(info.getType()).isEqualTo(ViewType.SEARCH_ALIAS);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("asyncDbs")
    void getViews(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        assumeTrue(isAtLeastVersion(3, 10));
        String name1 = rndName();
        String name2 = rndName();
        db.createView(name1, ViewType.ARANGO_SEARCH).get();
        db.createView(name2, ViewType.SEARCH_ALIAS).get();
        Collection<ViewEntity> views = db.getViews().get();
        assertThat(views).extracting(ViewEntity::getName).contains(name1, name2);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("asyncDbs")
    void drop(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        String name = rndName();
        db.createView(name, ViewType.ARANGO_SEARCH).get();
        final ArangoViewAsync view = db.view(name);
        view.drop().get();
        assertThat(view.exists().get()).isFalse();
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("asyncDbs")
    void rename(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        assumeTrue(isSingleServer());
        String oldName = rndName();
        String newName = rndName();

        db.createView(oldName, ViewType.ARANGO_SEARCH);
        db.view(oldName).rename(newName).get();
        assertThat(db.view(oldName).exists().get()).isFalse();
        assertThat(db.view(newName).exists().get()).isTrue();
    }

}
