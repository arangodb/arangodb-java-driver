package com.arangodb.internal.cursor;

import com.arangodb.ArangoCursorAsync;
import com.arangodb.entity.CursorEntity;
import com.arangodb.internal.ArangoDatabaseAsyncImpl;
import com.arangodb.internal.InternalArangoCursor;
import com.arangodb.internal.net.HostHandle;
import com.arangodb.model.AqlQueryOptions;

import java.util.concurrent.CompletableFuture;

public class ArangoCursorAsyncImpl<T> extends InternalArangoCursor<T> implements ArangoCursorAsync<T> {

    private final HostHandle hostHandle;

    public ArangoCursorAsyncImpl(
            final ArangoDatabaseAsyncImpl db,
            final CursorEntity<T> entity,
            final AqlQueryOptions options,
            final HostHandle hostHandle
    ) {
        super(db, db.name(), entity, options);
        this.hostHandle = hostHandle;
    }

    @Override
    public CompletableFuture<ArangoCursorAsync<T>> next() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public CompletableFuture<Void> close() {
        if (getId() != null && (allowRetry() || Boolean.TRUE.equals(hasMore()))) {
            return executorAsync().execute(queryCloseRequest(), Void.class, hostHandle);
        } else {
            return CompletableFuture.completedFuture(null);
        }
    }

}
