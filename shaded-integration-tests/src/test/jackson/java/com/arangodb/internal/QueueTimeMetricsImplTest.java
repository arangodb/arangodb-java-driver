package com.arangodb.internal;

import com.arangodb.model.QueueTimeSample;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;


class QueueTimeMetricsImplTest {
    private final static int QSIZE = 1024;
    private final Random rnd = new Random();
    private final QueueTimeMetricsImpl q = new QueueTimeMetricsImpl(QSIZE);

    @Test
    void halfSizeTest() {
        testQueue(QSIZE / 2);
    }

    @Test
    void fullSizeTest() {
        testQueue(QSIZE);
    }

    @Test
    void emptySizeTest() {
        testQueue(0);
    }

    @Test
    void overSizeTest() {
        testQueue((int) (QSIZE * 1.2));
        testQueue((int) (QSIZE * 3000.4));
    }

    private void testQueue(int size) {
        q.clear();
        for (int i = 0; i < size; i++) {
            q.add(new QueueTimeSample(i, rnd.nextDouble()));
        }
        QueueTimeSample[] values = q.getValues();
        assertThat(values).hasSize(Math.min(size, QSIZE));
        assertThat(q.getAvg()).isEqualTo(getAvg(values), within(1.0E-12));
        assertThat(q.getAvg()).isGreaterThanOrEqualTo(0.0);

        for (int i = 0; i < values.length; i++) {
            assertThat(values[i]).isNotNull();
            if (i > 0) {
                assertThat(values[i].timestamp).isGreaterThan(values[i - 1].timestamp);
            }
        }
    }

    private double getAvg(QueueTimeSample[] elements) {
        return Arrays.stream(elements).mapToDouble(it -> it.value).average().orElse(0.0);
    }

}