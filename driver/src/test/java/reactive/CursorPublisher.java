package reactive;

import com.arangodb.ArangoCursorAsync;
import com.arangodb.ArangoDB;
import com.arangodb.config.ArangoConfigProperties;
import com.arangodb.model.AqlQueryOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class CursorPublisher {
    private final static Logger LOGGER = LoggerFactory.getLogger(CursorPublisherAsyncVerifierTest.class);

    public static Flux<Integer> createFluxAsync(long elements) {
        Flux<List<Integer>> y = Flux.create(sink -> {
            AtomicBoolean pendingRequest = new AtomicBoolean(false);
            AtomicReference<CompletableFuture<ArangoCursorAsync<Integer>>> x = new AtomicReference<>(createInitialState(elements));
            sink.onDispose(() -> x.get().thenAccept(c -> {
                if (c != null) c.close();
            }));
            sink.onRequest(count -> {
                LOGGER.info("sink.onRequest()");
                if (count > 1) {
                    throw new IllegalArgumentException();
                }
                if (!pendingRequest.compareAndSet(false, true)) {
                    throw new IllegalStateException();
                }
                CompletableFuture<ArangoCursorAsync<Integer>> currentX = x.get();
                CompletableFuture<ArangoCursorAsync<Integer>> nextX = currentX
                        .thenCompose(c -> {
                            if (!pendingRequest.compareAndSet(true, false)) {
                                throw new IllegalStateException();
                            }

                            if (c == null) {
                                return CompletableFuture.completedFuture(null);
                            }

                            LOGGER.info("sink.next()");
                            sink.next(c.getResult());

                            if (c.hasMore()) {
                                return c.nextBatch();
                            } else {
                                sink.complete();
                                c.close();
                                return CompletableFuture.completedFuture(null);
                            }
                        });
                x.compareAndSet(currentX, nextX);
            });
        }, FluxSink.OverflowStrategy.ERROR);
        return y.concatMapIterable(i -> i, 1);
    }


    public static Flux<Integer> createFluxSequential(long elements) {
        return Flux.<Mono<List<Integer>>, CompletableFuture<ArangoCursorAsync<Integer>>>generate(
                        () -> createInitialState(elements),
                        (i, sink) -> {
                            sink.next(Mono.fromFuture(i.thenApply(c -> {
                                if (c == null) {
                                    return Collections.emptyList();
                                } else {
                                    return c.getResult();
                                }
                            })));
                            return i.thenCompose(c -> {
                                if (c == null) {
                                    return CompletableFuture.completedFuture(null);
                                } else if (c.hasMore()) {
                                    return c.nextBatch();
                                } else {
                                    sink.complete();
                                    c.close();
                                    return CompletableFuture.completedFuture(null);
                                }
                            });
                        },
                        i -> i.thenAccept(c -> {
                            if (c != null) c.close();
                        }))
                .log()
                .concatMap(o -> o, 0)
                .concatMapIterable(i -> i, 1);
    }

    private static CompletableFuture<ArangoCursorAsync<Integer>> createInitialState(long elements) {
        return new ArangoDB.Builder()
                .loadProperties(ArangoConfigProperties.fromFile())
                .build()
                .async()
                .db()
                .query(
                        "FOR i IN 1..@elements RETURN i",
                        Integer.class,
                        Collections.singletonMap("elements", elements),
                        new AqlQueryOptions()
                                .batchSize(2)
                                .stream(true)
                                .allowRetry(true));
    }

}
