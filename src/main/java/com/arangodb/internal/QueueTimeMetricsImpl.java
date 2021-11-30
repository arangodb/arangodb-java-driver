/*
 * DISCLAIMER
 *
 * Copyright 2016 ArangoDB GmbH, Cologne, Germany
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Copyright holder is ArangoDB GmbH, Cologne, Germany
 */

package com.arangodb.internal;

import com.arangodb.QueueTimeMetrics;
import com.arangodb.model.QueueTimeSample;

import java.util.Arrays;

/**
 * @author Michele Rastelli
 */
public class QueueTimeMetricsImpl implements QueueTimeMetrics {
    private final CircularFifoQueue samples;

    public QueueTimeMetricsImpl(int queueSize) {
        samples = new CircularFifoQueue(queueSize);
    }

    @Override
    public QueueTimeSample[] getValues() {
        return samples.getElements();
    }

    @Override
    public double getAvg() {
        return samples.getAvg();
    }

    void add(double value) {
        add(new QueueTimeSample(System.currentTimeMillis(), value));
    }

    void add(QueueTimeSample value) {
        samples.add(value);
    }

    void clear() {
        samples.clear();
    }

    private static class CircularFifoQueue {
        private final QueueTimeSample[] elements;

        /**
         * Array index of the oldest queue element.
         */
        private int start;

        /**
         * Capacity of the queue.
         */
        private final int size;

        /**
         * Amount of elements in the queue.
         */
        private int count;

        /**
         * Sum of the values in the queue.
         */
        private double sum;

        CircularFifoQueue(final int size) {
            elements = new QueueTimeSample[size];
            this.size = elements.length;
            clear();
        }

        /**
         * @return the average of the values in the queue, 0.0 if the queue is empty.
         */
        synchronized double getAvg() {
            if (count == 0) return 0.0;
            return sum / count;
        }

        synchronized void clear() {
            start = 0;
            count = 0;
            sum = 0.0;
            Arrays.fill(elements, null);
        }

        /**
         * Adds the given element to this queue. If the queue is full, the least recently added
         * element is replaced with the given one.
         *
         * @param element the element to add
         */
        synchronized void add(final QueueTimeSample element) {
            if (count < size) {
                count++;
            }
            QueueTimeSample overridden = elements[start];
            if (overridden != null) {
                sum -= overridden.value;
            }
            elements[start++] = element;
            if (start >= size) {
                start = 0;
            }
            sum += element.value;
        }

        synchronized QueueTimeSample[] getElements() {
            QueueTimeSample[] out = new QueueTimeSample[count];
            if (count < size) {
                System.arraycopy(elements, 0, out, 0, count);
            } else {
                System.arraycopy(elements, start, out, 0, size - start);
                System.arraycopy(elements, 0, out, size - start, start);
            }
            return out;
        }

    }
}
