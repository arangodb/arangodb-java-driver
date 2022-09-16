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

package com.arangodb.entity.arangosearch;

import com.arangodb.entity.ViewEntity;
import com.arangodb.entity.ViewType;

import java.util.Collection;

/**
 * @author Michele Rastelli
 * @see <a href="https://www.arangodb.com/docs/stable/http/views-search-alias.html">API Documentation</a>
 */
public class SearchAliasPropertiesEntity extends ViewEntity {

    private final SearchAliasProperties properties;

    public SearchAliasPropertiesEntity(final String id, final String name, final ViewType type,
                                       final SearchAliasProperties properties) {
        super(id, name, type);
        this.properties = properties;
    }

    /**
     * @return A list of inverted indexes to add to the View.
     */
    public Collection<SearchAliasIndex> getIndexes() {
        return properties.getIndexes();
    }

}
