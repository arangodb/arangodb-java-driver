package com.arangodb.async.internal.utils;

import java.util.concurrent.*;

public class CompletableFutureUtils {

    private static final ScheduledExecutorService timeoutScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = Executors.defaultThreadFactory().newThread(r);
                t.setDaemon(true);
                return t;
            }
    );

    private CompletableFutureUtils() {
    }

    public static <T> CompletableFuture<T> orTimeout(CompletableFuture<T> completableFuture, long timeout, TimeUnit unit) {
        ScheduledFuture<?> timeoutTask = timeoutScheduler.schedule(() ->
                completableFuture.completeExceptionally(new TimeoutException()), timeout, unit);
        completableFuture.whenComplete((v, e) -> timeoutTask.cancel(false));
        return completableFuture;
    }

}
