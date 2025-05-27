package com.arangodb.internal.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.*;

public class AsyncQueue<T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AsyncQueue.class);
    private final Queue<CompletableFuture<T>> requests = new ConcurrentLinkedQueue<>();
    private final Queue<T> offers = new ArrayDeque<>();

    public synchronized CompletableFuture<T> poll() {
        LOGGER.trace("poll()");
        T o = offers.poll();
        if (o != null) {
            LOGGER.trace("poll(): short-circuit: {}", o);
            return CompletableFuture.completedFuture(o);
        }
        CompletableFuture<T> r = new CompletableFuture<>();
        LOGGER.trace("poll(): enqueue request: {}", r);
        requests.add(r);
        return r;
    }

    public void offer(T o) {
        LOGGER.trace("offer({})", o);
        CompletableFuture<T> r = requests.poll();
        if (r == null) {
            synchronized (this) {
                r = requests.poll();
                if (r == null) {
                    LOGGER.trace("offer({}): enqueue", o);
                    offers.add(o);
                }
            }
        }
        if (r != null) {
            LOGGER.trace("offer({}): short-circuit: {}", o, r);
            r.complete(o);
        }
    }
}
