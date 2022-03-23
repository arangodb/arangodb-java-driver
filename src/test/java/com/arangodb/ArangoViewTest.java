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
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;


/**
 * @author Mark Vollmary
 * @author Michele Rastelli
 */
class ArangoViewTest extends BaseJunit5 {

    private static Stream<Arguments> dbs() {
        return dbsStream().map(Arguments::of);
    }

    @BeforeAll
    static void init() {
        initDB();
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void exists(ArangoDatabase db) {
        String name = "view-" + rnd();
        db.createView(name, ViewType.ARANGO_SEARCH);
        assertThat(db.view(name).exists()).isTrue();
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void getInfo(ArangoDatabase db) {
        String name = "view-" + rnd();
        db.createView(name, ViewType.ARANGO_SEARCH);
        final ViewEntity info = db.view(name).getInfo();
        assertThat(info).isNotNull();
        assertThat(info.getId()).isNotNull();
        assertThat(info.getName()).isEqualTo(name);
        assertThat(info.getType()).isEqualTo(ViewType.ARANGO_SEARCH);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void drop(ArangoDatabase db) {
        String name = "view-" + rnd();
        db.createView(name, ViewType.ARANGO_SEARCH);
        final ArangoView view = db.view(name);
        view.drop();
        assertThat(view.exists()).isFalse();
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void rename(ArangoDatabase db) {
        String oldName = "view-" + rnd();
        String newName = "view-" + rnd();

        assumeTrue(isSingleServer());
        db.createView(oldName, ViewType.ARANGO_SEARCH);
        db.view(oldName).rename(newName);
        assertThat(db.view(oldName).exists()).isFalse();
        assertThat(db.view(newName).exists()).isTrue();
    }

}
