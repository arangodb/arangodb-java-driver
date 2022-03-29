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

import com.arangodb.entity.ViewEntity;
import com.arangodb.entity.ViewType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * @author Mark Vollmary
 */

class ArangoViewTest extends BaseTest {

    private static final String VIEW_NAME = "view_test";

    @BeforeAll
    static void setup() throws InterruptedException, ExecutionException {
        if (!isAtLeastVersion(arangoDB, 3, 4))
            return;
        db.createView(VIEW_NAME, ViewType.ARANGO_SEARCH).get();
    }

    @Test
    void exists() throws InterruptedException, ExecutionException {
        assumeTrue(isAtLeastVersion(3, 4));
        assertThat(db.view(VIEW_NAME).exists().get()).isTrue();
    }

    @Test
    void getInfo() throws InterruptedException, ExecutionException {
        assumeTrue(isAtLeastVersion(3, 4));
        final ViewEntity info = db.view(VIEW_NAME).getInfo().get();
        assertThat(info).isNotNull();
        assertThat(info.getId()).isNotNull();
        assertThat(info.getName()).isEqualTo(VIEW_NAME);
        assertThat(info.getType()).isEqualTo(ViewType.ARANGO_SEARCH);
    }

    @Test
    void drop() throws InterruptedException, ExecutionException {
        assumeTrue(isAtLeastVersion(3, 4));
        final String name = VIEW_NAME + "_droptest";
        db.createView(name, ViewType.ARANGO_SEARCH).get();
        final ArangoViewAsync view = db.view(name);
        view.drop().get();
        assertThat(view.exists().get()).isFalse();
    }

    @Test
    void rename() throws InterruptedException, ExecutionException {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 4));
        final String name = VIEW_NAME + "_renametest";
        final String newName = name + "_new";
        db.createView(name, ViewType.ARANGO_SEARCH).get();
        db.view(name).rename(newName).get();
        assertThat(db.view(name).exists().get()).isFalse();
        assertThat(db.view(newName).exists().get()).isTrue();
    }

}
