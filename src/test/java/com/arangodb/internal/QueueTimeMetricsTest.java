package com.arangodb.internal;

import com.arangodb.model.QueueTimeSample;
import org.junit.Test;

import java.util.Arrays;
import java.util.Random;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class QueueTimeMetricsTest {
    private final static int QSIZE = 1024;
    private final Random rnd = new Random();
    private final QueueTimeMetrics.CircularFifoQueue q =
            new QueueTimeMetrics.CircularFifoQueue(QSIZE);

    @Test
    public void halfSizeTest() {
        testQueue(QSIZE / 2);
    }

    @Test
    public void fullSizeTest() {
        testQueue(QSIZE);
    }

    @Test
    public void emptySizeTest() {
        testQueue(0);
    }

    @Test
    public void overSizeTest() {
        testQueue((int) (QSIZE * 1.2));
        testQueue((int) (QSIZE * 3000.4));
    }

    private void testQueue(int size) {
        q.clear();
        for (int i = 0; i < size; i++) {
            q.add(new QueueTimeSample(i, rnd.nextDouble()));
        }
        QueueTimeSample[] samples = q.getElements();
        assertThat(samples.length, is(Math.min(size, QSIZE)));
        assertThat(q.getAvg(), is(closeTo(getAvg(samples), 1.0E-12)));

        for (int i = 0; i < samples.length; i++) {
            assertThat(samples[i], is(notNullValue()));
            if (i > 0) {
                assertThat(samples[i].timestamp, greaterThan(samples[i - 1].timestamp));
            }
        }
    }

    private double getAvg(QueueTimeSample[] elements) {
        return Arrays.stream(elements).mapToDouble(it -> it.value).average().orElse(0.0);
    }

}