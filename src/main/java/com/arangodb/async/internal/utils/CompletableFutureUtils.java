package com.arangodb.async.internal.utils;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.BiConsumer;

public class CompletableFutureUtils {
    /**
     * Singleton delay scheduler, used only for starting and cancelling tasks.
     */
    static final class Delayer {
        static ScheduledFuture<?> delay(Runnable command, long delay,
                                        TimeUnit unit) {
            return delayer.schedule(command, delay, unit);
        }

        static final class DaemonThreadFactory implements ThreadFactory {
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r);
                t.setDaemon(true);
                t.setName("CompletableFutureDelayScheduler");
                return t;
            }
        }

        static final ScheduledThreadPoolExecutor delayer;

        static {
            (delayer = new ScheduledThreadPoolExecutor(
                    1, new DaemonThreadFactory())).
                    setRemoveOnCancelPolicy(true);
        }
    }

    /**
     * Action to cancel unneeded timeouts
     */
    static final class Canceller implements BiConsumer<Object, Throwable> {
        final Future<?> f;

        Canceller(Future<?> f) {
            this.f = f;
        }

        public void accept(Object ignore, Throwable ex) {
            if (ex == null && f != null && !f.isDone())
                f.cancel(false);
        }
    }

    /**
     * Action to completeExceptionally on timeout
     */
    static final class Timeout implements Runnable {
        final CompletableFuture<?> f;

        Timeout(CompletableFuture<?> f) {
            this.f = f;
        }

        public void run() {
            if (f != null && !f.isDone())
                f.completeExceptionally(new TimeoutException());
        }
    }

    /**
     * Exceptionally completes {@code completableFuture} with a {@link TimeoutException} if not otherwise completed
     * before the given timeout.
     *
     * @param completableFuture
     *         the original CompletableFuture
     * @param timeout
     *         how long to wait before completing exceptionally with a TimeoutException, in units of {@code unit}
     * @param unit
     *         a {@code TimeUnit} determining how to interpret the {@code timeout} parameter
     * @return this CompletableFuture
     *
     * @since 9
     */
    public static <T> CompletableFuture<T> orTimeout(CompletableFuture<T> completableFuture, long timeout, TimeUnit unit) {
        if (unit == null)
            throw new NullPointerException();
        if (!completableFuture.isDone())
            completableFuture.whenComplete(new Canceller(Delayer.delay(new Timeout(completableFuture),
                    timeout, unit)));
        return completableFuture;
    }


}
