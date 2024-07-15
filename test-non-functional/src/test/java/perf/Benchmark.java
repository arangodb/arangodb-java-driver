package perf;

import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class Benchmark {

    private static final int SYNC_THREADS = 128;
    private final CountDownLatch completed = new CountDownLatch(1);
    private volatile Long startTime = null;
    private volatile Long endTime = null;
    private volatile int targetCount = Integer.MAX_VALUE;
    private final AtomicInteger counter = new AtomicInteger();
    private final ExecutorService es = Executors.newFixedThreadPool(SYNC_THREADS);
    private final int warmupDurationSeconds;
    private final int numberOfRequests;

    public Benchmark(int warmupDurationSeconds, int numberOfRequests) {
        this.warmupDurationSeconds = warmupDurationSeconds;
        this.numberOfRequests = numberOfRequests;
    }

    public void run() {
        // warmup
        startBenchmark();

        // start monitor / warmup
        startMonitor();

        // start benchmark
        startMeasuring();
    }

    private void startMonitor() {
        for (int i = 0; i < warmupDurationSeconds; i++) {
            counter.set(0);
            long start = new Date().getTime();
            try {
                Thread.sleep(1_000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            long current = new Date().getTime();
            long elapsed = current - start;
            double reqsPerSec = 1_000.0 * counter.get() / elapsed;
            System.out.println("reqs/s: \t" + reqsPerSec);
        }
    }

    private void startBenchmark() {
        start();
        new Thread(() -> {
            try {
                completed.await();
                // wait graceful shutdown
                Thread.sleep(1_000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // force shutdown
            es.shutdown();
            shutdown();
        }).start();
    }

    private void startMeasuring() {
        counter.set(0);
        targetCount = numberOfRequests;
        startTime = System.currentTimeMillis();
    }

    public long waitComplete() {
        try {
            completed.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return endTime - startTime;
    }

    /**
     * @return req/s
     */
    public long getThroughput() {
        return targetCount * 1000L / (endTime - startTime);
    }

    /**
     * notify the success of #count requests
     *
     * @return whether more requests should be performed
     */
    private boolean success() {
        if (endTime != null) return false;
        if (counter.addAndGet(1) >= targetCount) {
            endTime = System.currentTimeMillis();
            completed.countDown();
            return false;
        }
        return true;
    }

    private void start() {
        for (int i = 0; i < SYNC_THREADS; i++) {
            es.execute(() -> {
                boolean more = true;
                while (more) {
                    sendRequest();
                    more = success();
                }
            });
        }
    }

    protected abstract void sendRequest();

    protected abstract void shutdown();

}
