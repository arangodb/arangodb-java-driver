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

package com.arangodb.internal;

import com.arangodb.ArangoDBException;
import com.arangodb.ArangoDatabase;
import com.arangodb.ArangoSearch;
import com.arangodb.entity.ViewEntity;
import com.arangodb.entity.arangosearch.ArangoSearchPropertiesEntity;
import com.arangodb.model.arangosearch.ArangoSearchCreateOptions;
import com.arangodb.model.arangosearch.ArangoSearchPropertiesOptions;

import static com.arangodb.internal.ArangoErrors.ERROR_ARANGO_DATA_SOURCE_NOT_FOUND;
import static com.arangodb.internal.ArangoErrors.matches;

/**
 * @author Mark Vollmary
 */
public class ArangoSearchImpl extends InternalArangoSearch implements ArangoSearch {
    private final ArangoDatabase db;

    protected ArangoSearchImpl(final ArangoDatabaseImpl db, final String name) {
        super(db, db.name(), name);
        this.db = db;
    }

    @Override
    public ArangoDatabase db() {
        return db;
    }

    @Override
    public boolean exists() {
        try {
            getInfo();
            return true;
        } catch (final ArangoDBException e) {
            if (matches(e, 404, ERROR_ARANGO_DATA_SOURCE_NOT_FOUND)) {
                return false;
            }
            throw e;
        }
    }

    @Override
    public void drop() {
        executorSync().execute(dropRequest(), Void.class);
    }

    @Override
    public ViewEntity rename(final String newName) {
        return executorSync().execute(renameRequest(newName), ViewEntity.class);
    }

    @Override
    public ViewEntity getInfo() {
        return executorSync().execute(getInfoRequest(), ViewEntity.class);
    }

    @Override
    public ViewEntity create() {
        return create(new ArangoSearchCreateOptions());
    }

    @Override
    public ViewEntity create(final ArangoSearchCreateOptions options) {
        return db().createArangoSearch(name(), options);
    }

    @Override
    public ArangoSearchPropertiesEntity getProperties() {
        return executorSync().execute(getPropertiesRequest(), ArangoSearchPropertiesEntity.class);
    }

    @Override
    public ArangoSearchPropertiesEntity updateProperties(final ArangoSearchPropertiesOptions options) {
        return executorSync().execute(updatePropertiesRequest(options), ArangoSearchPropertiesEntity.class);
    }

    @Override
    public ArangoSearchPropertiesEntity replaceProperties(final ArangoSearchPropertiesOptions options) {
        return executorSync().execute(replacePropertiesRequest(options), ArangoSearchPropertiesEntity.class);
    }

}
