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

package com.arangodb.internal;

import com.arangodb.BaseArangoCursor;
import com.arangodb.entity.CursorEntity;

import java.util.List;

/**
 * @author Mark Vollmary
 * @author Michele Rastelli
 */
public abstract class InternalArangoCursor<T> extends ArangoExecuteable implements BaseArangoCursor<T> {

    private static final String PATH_API_CURSOR = "/_api/cursor";

    private final String dbName;
    private final CursorEntity<T> entity;
    private final Class<T> type;
    private final boolean allowRetry;

    protected InternalArangoCursor(
            final ArangoExecuteable executeable,
            final String dbName,
            final CursorEntity<T> entity,
            final Class<T> type,
            final Boolean allowRetry
    ) {
        super(executeable);
        this.dbName = dbName;
        this.entity = entity;
        this.type = type;
        this.allowRetry = Boolean.TRUE.equals(allowRetry);
    }

    @Override
    public String getId() {
        return entity.getId();
    }

    @Override
    public Long getCount() {
        return entity.getCount();
    }

    @Override
    public Boolean isCached() {
        return entity.getCached();
    }

    @Override
    public Boolean hasMore() {
        return entity.getHasMore();
    }

    @Override
    public List<T> getResult() {
        return entity.getResult();
    }

    @Override
    public Boolean isPotentialDirtyRead() {
        return entity.isPotentialDirtyRead();
    }

    @Override
    public String getNextBatchId() {
        return entity.getNextBatchId();
    }

    @Override
    public CursorEntity.Extra getExtra() {
        return entity.getExtra();
    }

    protected boolean allowRetry() {
        return allowRetry;
    }

    protected Class<T> getType() {
        return type;
    }

    protected InternalRequest queryNextRequest() {
        return request(dbName, RequestType.POST, PATH_API_CURSOR, entity.getId(), entity.getNextBatchId());
    }

    protected InternalRequest queryCloseRequest() {
        return request(dbName, RequestType.DELETE, PATH_API_CURSOR, entity.getId());
    }

}
