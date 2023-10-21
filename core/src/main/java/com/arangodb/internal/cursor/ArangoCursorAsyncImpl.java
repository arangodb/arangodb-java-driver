package com.arangodb.internal.cursor;

import com.arangodb.ArangoCursorAsync;
import com.arangodb.entity.CursorEntity;
import com.arangodb.internal.ArangoDatabaseAsyncImpl;
import com.arangodb.internal.InternalArangoCursor;
import com.arangodb.internal.InternalRequest;
import com.arangodb.internal.net.HostHandle;
import com.arangodb.model.AqlQueryOptions;

import java.util.NoSuchElementException;
import java.util.concurrent.CompletableFuture;

public class ArangoCursorAsyncImpl<T> extends InternalArangoCursor<T> implements ArangoCursorAsync<T> {

    private final ArangoDatabaseAsyncImpl db;
    private final HostHandle hostHandle;

    public ArangoCursorAsyncImpl(
            final ArangoDatabaseAsyncImpl db,
            final CursorEntity<T> entity,
            final Class<T> type,
            final AqlQueryOptions options,
            final HostHandle hostHandle
    ) {
        super(db, db.name(), entity, type, options);
        this.db = db;
        this.hostHandle = hostHandle;
    }

    @Override
    public CompletableFuture<ArangoCursorAsync<T>> nextBatch() {
        if (Boolean.TRUE.equals(hasMore())) {
            InternalRequest request = getNextBatchId() == null ? queryNextRequest() : queryNextByBatchIdRequest();
            return executorAsync().execute(request, db.cursorEntityDeserializer(getType()), hostHandle)
                    .thenApply(r -> new ArangoCursorAsyncImpl<>(db, r, getType(), getOptions(), hostHandle));
        } else {
            return CompletableFuture.supplyAsync(() -> {
                throw new NoSuchElementException();
            });
        }
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
