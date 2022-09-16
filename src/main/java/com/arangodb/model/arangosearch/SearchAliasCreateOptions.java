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

package com.arangodb.model.arangosearch;

import com.arangodb.entity.ViewType;
import com.arangodb.entity.arangosearch.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * @author Michele Rastelli
 * @since ArangoDB 3.10
 */
public class SearchAliasCreateOptions {

    private String name;
    private final ViewType type;

    private final Collection<SearchAliasIndex> indexes = new ArrayList<>();

    public SearchAliasCreateOptions() {
        super();
        type = ViewType.SEARCH_ALIAS;
    }

    protected SearchAliasCreateOptions name(final String name) {
        this.name = name;
        return this;
    }

    /**
     * @param indexes A list of inverted indexes to add to the View.
     * @return options
     */
    public SearchAliasCreateOptions indexes(final SearchAliasIndex... indexes) {
        Collections.addAll(this.indexes, indexes);
        return this;
    }

    public String getName() {
        return name;
    }

    public ViewType getType() {
        return type;
    }

    public Collection<SearchAliasIndex> getIndexes() {
        return indexes;
    }
}
