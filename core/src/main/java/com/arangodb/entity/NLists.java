/*
 * DISCLAIMER
 *
 * Copyright 2026 ArangoDB GmbH, Cologne, Germany
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

package com.arangodb.entity;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Number of centroids used by a vector index.
 *
 * @since ArangoDB 3.12.10
 */
public interface NLists {

    /**
     * Creates a fixed centroid configuration.
     *
     * @param value number of centroids
     * @return fixed centroid configuration
     */
    static NumericNLists fixed(final Integer value) {
        if (value == null) {
            return null;
        }
        return new NumericNLists(value);
    }

    /**
     * Creates an empty scaling configuration. The server requires {@code strategy}, {@code multiplier}, and
     * {@code minNLists} when the configuration is sent in a request.
     *
     * @return scaling centroid configuration
     */
    static ObjectNLists scaling() {
        return new ObjectNLists();
    }

    /**
     * The representation sent to, and received from, the server.
     */
    @JsonValue
    Object get();

    /**
     * Legacy fixed number of centroids.
     */
    final class NumericNLists implements NLists {

        private final Integer value;

        public NumericNLists(final Integer value) {
            this.value = value;
        }

        @Override
        public Integer get() {
            return value;
        }

        @Override
        public boolean equals(final Object o) {
            return o instanceof NumericNLists && Objects.equals(value, ((NumericNLists) o).value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }
    }

    /**
     * Scaling configuration for the number of vector-index centroids.
     */
    final class ObjectNLists implements NLists {
        private Strategy strategy;
        private Integer multiplier;
        private Integer minNLists;
        private List<Tier> tiers;

        public ObjectNLists() {
        }

        public ObjectNLists(final Strategy strategy, final Integer multiplier, final Integer minNLists,
                            final Collection<Tier> tiers) {
            this.strategy = strategy;
            this.multiplier = multiplier;
            this.minNLists = minNLists;
            this.tiers = tiers == null ? null : new ArrayList<>(tiers);
        }

        public Strategy getStrategy() {
            return strategy;
        }

        public ObjectNLists strategy(final Strategy strategy) {
            this.strategy = strategy;
            return this;
        }

        public Integer getMultiplier() {
            return multiplier;
        }

        public ObjectNLists multiplier(final Integer multiplier) {
            this.multiplier = multiplier;
            return this;
        }

        public Integer getMinNLists() {
            return minNLists;
        }

        public ObjectNLists minNLists(final Integer minNLists) {
            this.minNLists = minNLists;
            return this;
        }

        public List<Tier> getTiers() {
            return tiers;
        }

        public ObjectNLists tiers(final Collection<Tier> tiers) {
            this.tiers = tiers == null ? null : new ArrayList<>(tiers);
            return this;
        }

        public ObjectNLists tiers(final Tier... tiers) {
            this.tiers = tiers == null ? null : new ArrayList<>(Arrays.asList(tiers));
            return this;
        }

        @Override
        public Object get() {
            Map<String, Object> value = new LinkedHashMap<>();
            if (strategy != null) value.put("strategy", strategy);
            if (multiplier != null) value.put("multiplier", multiplier);
            if (minNLists != null) value.put("minNLists", minNLists);
            if (tiers != null) value.put("tiers", tiers);
            return value;
        }

        @Override
        public boolean equals(final Object o) {
            if (!(o instanceof ObjectNLists)) return false;
            ObjectNLists that = (ObjectNLists) o;
            return strategy == that.strategy && Objects.equals(multiplier, that.multiplier)
                    && Objects.equals(minNLists, that.minNLists) && Objects.equals(tiers, that.tiers);
        }

        @Override
        public int hashCode() {
            return Objects.hash(strategy, multiplier, minNLists, tiers);
        }

        /**
         * Scaling strategy supported by the vector-index API.
         */
        public enum Strategy {
            autoSqrt
        }

        /**
         * A threshold at which a fixed centroid count is used.
         */
        public static final class Tier {
            private Integer threshold;
            private Integer fixedValue;

            public Tier() {
            }

            public Tier(final Integer threshold, final Integer fixedValue) {
                this.threshold = threshold;
                this.fixedValue = fixedValue;
            }

            public Integer getThreshold() {
                return threshold;
            }

            public Tier threshold(final Integer threshold) {
                this.threshold = threshold;
                return this;
            }

            public Integer getFixedValue() {
                return fixedValue;
            }

            public Tier fixedValue(final Integer fixedValue) {
                this.fixedValue = fixedValue;
                return this;
            }

            @Override
            public boolean equals(final Object o) {
                if (!(o instanceof Tier)) return false;
                Tier tier = (Tier) o;
                return Objects.equals(threshold, tier.threshold) && Objects.equals(fixedValue, tier.fixedValue);
            }

            @Override
            public int hashCode() {
                return Objects.hash(threshold, fixedValue);
            }
        }
    }
}
