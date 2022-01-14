package com.arangodb.model;

import java.util.Objects;

/**
 * Represents an observed value of the server queue latency, as returned from the "X-Arango-Queue-Time-Seconds" response
 * header.
 * This header contains the most recent request (de)queuing time (in seconds) as tracked by the server’s scheduler.
 *
 * @author Michele Rastelli
 * @see <a href="https://www.arangodb.com/docs/stable/http/general.html#overload-control">API Documentation</a>
 */
public class QueueTimeSample {
    /**
     * Unix-timestamp in milliseconds, recorded at client side.
     */
    public final long timestamp;

    /**
     * Observed value.
     */
    public final double value;

    public QueueTimeSample(long timestamp, double value) {
        this.timestamp = timestamp;
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QueueTimeSample that = (QueueTimeSample) o;
        return timestamp == that.timestamp && Double.compare(that.value, value) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(timestamp, value);
    }
}
