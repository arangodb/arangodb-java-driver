package com.arangodb.internal.cursor;

import com.arangodb.ArangoCursorAsync;
import com.arangodb.ArangoDBException;
import com.arangodb.entity.CursorEntity;
import com.arangodb.internal.ArangoDatabaseAsyncImpl;
import com.arangodb.internal.InternalArangoCursor;
import com.arangodb.internal.net.HostHandle;

import java.util.NoSuchElementException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static com.arangodb.internal.ArangoErrors.matches;

public class ArangoCursorAsyncImpl<T> extends InternalArangoCursor<T> implements ArangoCursorAsync<T> {

    private final ArangoDatabaseAsyncImpl db;
    private final HostHandle hostHandle;
    private final CursorEntity<T> entity;

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
        this.entity = entity;
    }

    @Override
    public CompletableFuture<ArangoCursorAsync<T>> nextBatch() {
        if (Boolean.TRUE.equals(hasMore())) {
            return executorAsync().execute(this::queryNextRequest, db.cursorEntityDeserializer(getType()), hostHandle)
                    .thenApply(r -> {
                        // needed because the latest batch does not return the cursor id
                        r.setId(entity.getId());
                        return new ArangoCursorAsyncImpl<>(db, r, getType(), hostHandle, allowRetry());
                    });
        } else {
            CompletableFuture<ArangoCursorAsync<T>> cf = new CompletableFuture<>();
            cf.completeExceptionally(new NoSuchElementException());
            return cf;
        }
    }

    @Override
    public CompletableFuture<Void> close() {
        if (getId() != null && (allowRetry() || Boolean.TRUE.equals(hasMore()))) {
            return executorAsync().execute(this::queryCloseRequest, Void.class, hostHandle)
                    .exceptionally(err -> {
                        Throwable e = err instanceof CompletionException ? err.getCause() : err;
                        if (e instanceof ArangoDBException) {
                            ArangoDBException aEx = (ArangoDBException) e;
                            // ignore errors Response: 404, Error: 1600 - cursor not found
                            if (matches(aEx, 404, 1600)) {
                                return null;
                            }
                        }
                        throw ArangoDBException.of(e);
                    })
                    .thenApply(__ -> null);
        } else {
            return CompletableFuture.completedFuture(null);
        }
    }

}
