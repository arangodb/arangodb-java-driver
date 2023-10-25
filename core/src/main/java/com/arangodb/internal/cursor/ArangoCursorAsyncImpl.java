package com.arangodb.internal.cursor;

import com.arangodb.ArangoCursorAsync;
import com.arangodb.entity.CursorEntity;
import com.arangodb.internal.ArangoDatabaseAsyncImpl;
import com.arangodb.internal.InternalArangoCursor;
import com.arangodb.internal.net.HostHandle;

import java.util.NoSuchElementException;
import java.util.concurrent.CompletableFuture;

public class ArangoCursorAsyncImpl<T> extends InternalArangoCursor<T> implements ArangoCursorAsync<T> {

    private final ArangoDatabaseAsyncImpl db;
    private final HostHandle hostHandle;

    public ArangoCursorAsyncImpl(
            final ArangoDatabaseAsyncImpl db,
            final CursorEntity<T> entity,
            final Class<T> type,
            final HostHandle hostHandle,
            final Boolean allowRetry
    ) {
        super(db, db.name(), entity, type, allowRetry);
        this.db = db;
        this.hostHandle = hostHandle;
    }

    @Override
    public CompletableFuture<ArangoCursorAsync<T>> nextBatch() {
        if (Boolean.TRUE.equals(hasMore())) {
            return executorAsync().execute(this::queryNextRequest, db.cursorEntityDeserializer(getType()), hostHandle)
                    .thenApply(r -> new ArangoCursorAsyncImpl<>(db, r, getType(), hostHandle, allowRetry()));
        } else {
            CompletableFuture<ArangoCursorAsync<T>> cf = new CompletableFuture<>();
            cf.completeExceptionally(new NoSuchElementException());
            return cf;
        }
    }

    @Override
    public CompletableFuture<Void> close() {
        if (getId() != null && (allowRetry() || Boolean.TRUE.equals(hasMore()))) {
            return executorAsync().execute(this::queryCloseRequest, Void.class, hostHandle);
        } else {
            return CompletableFuture.completedFuture(null);
        }
    }

}
