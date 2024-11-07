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

package com.arangodb.entity;

import com.fasterxml.jackson.annotation.JsonAnySetter;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public final class AqlQueryExplainEntity {

    private ExecutionPlan plan;
    private Collection<ExecutionPlan> plans;
    private Collection<CursorWarning> warnings;
    private ExecutionStats stats;
    private Boolean cacheable;

    public ExecutionPlan getPlan() {
        return plan;
    }

    public Collection<ExecutionPlan> getPlans() {
        return plans;
    }

    public Collection<CursorWarning> getWarnings() {
        return warnings;
    }

    public ExecutionStats getStats() {
        return stats;
    }

    public Boolean getCacheable() {
        return cacheable;
    }

    public static final class ExecutionPlan {
        private final Map<String, Object> properties = new HashMap<>();
        private Collection<ExecutionNode> nodes;
        private Double estimatedCost;
        private Collection<ExecutionCollection> collections;
        private Collection<String> rules;
        private Collection<ExecutionVariable> variables;

        @JsonAnySetter
        public void add(String key, Object value) {
            properties.put(key, value);
        }

        public Object get(String key) {
            return properties.get(key);
        }

        public Collection<ExecutionNode> getNodes() {
            return nodes;
        }

        public Double getEstimatedCost() {
            return estimatedCost;
        }

        public Collection<ExecutionCollection> getCollections() {
            return collections;
        }

        public Collection<String> getRules() {
            return rules;
        }

        public Collection<ExecutionVariable> getVariables() {
            return variables;
        }
    }

    public static final class ExecutionNode {
        private final Map<String, Object> properties = new HashMap<>();

        @JsonAnySetter
        public void add(String key, Object value) {
            properties.put(key, value);
        }

        public Object get(String key) {
            return properties.get(key);
        }
    }

    public static final class ExecutionVariable {
        private final Map<String, Object> properties = new HashMap<>();

        @JsonAnySetter
        public void add(String key, Object value) {
            properties.put(key, value);
        }

        public Object get(String key) {
            return properties.get(key);
        }
    }

    public static final class ExecutionCollection {
        private final Map<String, Object> properties = new HashMap<>();

        @JsonAnySetter
        public void add(String key, Object value) {
            properties.put(key, value);
        }

        public Object get(String key) {
            return properties.get(key);
        }
    }

    public static final class ExecutionStats {
        private final Map<String, Object> properties = new HashMap<>();

        @JsonAnySetter
        public void add(String key, Object value) {
            properties.put(key, value);
        }

        public Object get(String key) {
            return properties.get(key);
        }
    }

}
