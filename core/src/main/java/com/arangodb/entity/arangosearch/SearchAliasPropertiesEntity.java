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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

/**
 * @author Michele Rastelli
 */
public final class SearchAliasPropertiesEntity extends ViewEntity {

    private final Collection<SearchAliasIndex> indexes = new ArrayList<>();

    /**
     * @return A list of inverted indexes to add to the View.
     */
    public Collection<SearchAliasIndex> getIndexes() {
        return indexes;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof SearchAliasPropertiesEntity)) return false;
        if (!super.equals(o)) return false;
        SearchAliasPropertiesEntity that = (SearchAliasPropertiesEntity) o;
        return Objects.equals(indexes, that.indexes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), indexes);
    }
}
