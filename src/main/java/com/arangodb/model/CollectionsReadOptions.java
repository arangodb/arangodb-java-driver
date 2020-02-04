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

/**
 * @author Mark Vollmary
 * @see <a href="https://www.arangodb.com/docs/stable/http/collection-getting.html#reads-all-collections">API
 * Documentation</a>
 */
public class CollectionsReadOptions {

    private Boolean excludeSystem;

    public CollectionsReadOptions() {
        super();
    }

    public Boolean getExcludeSystem() {
        return excludeSystem;
    }

    /**
     * @param excludeSystem Whether or not system collections should be excluded from the result.
     * @return options
     */
    public CollectionsReadOptions excludeSystem(final Boolean excludeSystem) {
        this.excludeSystem = excludeSystem;
        return this;
    }

}
