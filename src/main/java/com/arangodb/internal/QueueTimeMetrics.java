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

import java.util.Arrays;

/**
 * @author Michele Rastelli
 */
public class QueueTimeMetrics {
    private final CircularFifoQueue samples;

    public QueueTimeMetrics() {
        super();
        samples = new CircularFifoQueue(8192);
    }

    public void addSample(double value) {
        samples.add(new Sample(System.currentTimeMillis(), value));
    }

    public void clear() {
        samples.clear();
    }

    public static class Sample {
        public final long timestamp;
        public final double value;

        public Sample(long timestamp, double value) {
            this.timestamp = timestamp;
            this.value = value;
        }
    }

    static class CircularFifoQueue {
        private final Sample[] elements;

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

        CircularFifoQueue(final int size) {
            elements = new Sample[size];
            this.size = elements.length;
            start = 0;
            count = 0;
        }

        synchronized void clear() {
            start = 0;
            count = 0;
            Arrays.fill(elements, null);
        }

        /**
         * Adds the given element to this queue. If the queue is full, the least recently added
         * element is replaced with the given one.
         *
         * @param element the element to add
         */
        synchronized void add(final Sample element) {
            if (count < size) {
                count++;
            }
            elements[start++] = element;
            if (start >= size) {
                start = 0;
            }
        }

        synchronized Sample[] getElements() {
            Sample[] out = new Sample[count];
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
