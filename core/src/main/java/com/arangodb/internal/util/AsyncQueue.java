package com.arangodb.internal.util;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;

public class AsyncQueue<T> {
    private final Queue<CompletableFuture<T>> requests = new ArrayDeque<>();
    private final Queue<T> offers = new ArrayDeque<>();

    public synchronized CompletableFuture<T> poll() {
        CompletableFuture<T> r = new CompletableFuture<>();
        T o = offers.poll();
        if (o != null) {
            r.complete(o);
        } else {
            requests.add(r);
        }
        return r;
    }

    public synchronized void offer(T o) {
        CompletableFuture<T> r = requests.poll();
        if (r != null) {
            r.complete(o);
        } else {
            offers.add(o);
        }
    }
}
