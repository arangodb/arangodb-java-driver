package com.arangodb.internal.cursor;

import com.arangodb.ArangoCursorAsync;
import com.arangodb.entity.CursorEntity;
import com.arangodb.internal.net.HostHandle;
import com.arangodb.model.AqlQueryOptions;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ArangoCursorAsyncImpl<T> implements ArangoCursorAsync<T> {

    private final CursorEntity<T> entity;
    private final AqlQueryOptions options;
    private final HostHandle hostHandle;

    public ArangoCursorAsyncImpl(
            final CursorEntity<T> entity,
            final AqlQueryOptions options,
            final HostHandle hostHandle
    ) {
        this.entity = entity;
        this.options = options;
        this.hostHandle = hostHandle;
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

    @Override
    public CompletableFuture<ArangoCursorAsync<T>> next() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public CompletableFuture<Void> close() {
        throw new UnsupportedOperationException("TODO");
    }

}
