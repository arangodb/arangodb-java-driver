package com.arangodb.vst.internal.utils;

import java.util.concurrent.*;

public class CompletableFutureUtils {

    private CompletableFutureUtils() {
    }

    private static final ScheduledExecutorService timeoutScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = Executors.defaultThreadFactory().newThread(r);
                t.setDaemon(true);
                return t;
            }
    );

    public static <T> CompletableFuture<T> orTimeout(CompletableFuture<T> completableFuture, long timeout, TimeUnit unit) {
        ScheduledFuture<?> timeoutTask = timeoutScheduler.schedule(() ->
                completableFuture.completeExceptionally(new TimeoutException()), timeout, unit);
        completableFuture.whenComplete((v, e) -> timeoutTask.cancel(false));
        return completableFuture;
    }

}
