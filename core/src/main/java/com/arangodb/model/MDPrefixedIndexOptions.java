/*
 * DISCLAIMER
 *
 * Copyright 2016 ArangoDB GmbH, Cologne, Germany
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

package com.arangodb.model;

import com.arangodb.entity.IndexType;


/**
 * @author Michele Rastelli
 * @since ArangoDB 3.12
 */
public final class MDPrefixedIndexOptions extends AbstractMDIndexOptions<MDPrefixedIndexOptions> {

    private Iterable<String> prefixFields;

    public MDPrefixedIndexOptions() {
        super();
    }

    public Iterable<String> getPrefixFields() {
        return prefixFields;
    }

    /**
     * @param prefixFields An array of attribute names used as search prefix. Array expansions are not allowed.
     * @return options
     */
    public MDPrefixedIndexOptions prefixFields(final Iterable<String> prefixFields) {
        this.prefixFields = prefixFields;
        return this;
    }

    @Override
    public IndexType getType() {
        return IndexType.mdiPrefixed;
    }

    @Override
    MDPrefixedIndexOptions getThis() {
        return this;
    }

}
