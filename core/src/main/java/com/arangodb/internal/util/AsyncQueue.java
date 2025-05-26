package com.arangodb.internal.util;

import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;

public class AsyncQueue<T> {
    private final Queue<CompletableFuture<T>> requests = new ConcurrentLinkedQueue<>();
    private final Queue<T> offers = new ConcurrentLinkedQueue<>();

    public synchronized CompletableFuture<T> poll() {
        T o = offers.poll();
        if (o != null) {
            return CompletableFuture.completedFuture(o);
        } else {
            CompletableFuture<T> res = new CompletableFuture<>();
            requests.offer(res);
            update();
            return res;
        }
    }

    public void offer(T o) {
        CompletableFuture<T> r = requests.poll();
        if (r != null) {
            r.complete(o);
        } else {
            offers.offer(o);
            update();
        }
    }

    private void update() {
        CompletableFuture<T> r;
        T o;
        synchronized (this) {
            if (offers.isEmpty()) return;
            r = requests.poll();
            if (r == null) return;
            o = offers.poll();
        }
        r.complete(o);
    }
}
