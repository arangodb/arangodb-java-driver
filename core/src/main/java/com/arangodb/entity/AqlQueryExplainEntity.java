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
import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof AqlQueryExplainEntity)) return false;
        AqlQueryExplainEntity that = (AqlQueryExplainEntity) o;
        return Objects.equals(plan, that.plan) && Objects.equals(plans, that.plans) && Objects.equals(warnings, that.warnings) && Objects.equals(stats, that.stats) && Objects.equals(cacheable, that.cacheable);
    }

    @Override
    public int hashCode() {
        return Objects.hash(plan, plans, warnings, stats, cacheable);
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

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof ExecutionPlan)) return false;
            ExecutionPlan that = (ExecutionPlan) o;
            return Objects.equals(properties, that.properties) && Objects.equals(nodes, that.nodes) && Objects.equals(estimatedCost, that.estimatedCost) && Objects.equals(collections, that.collections) && Objects.equals(rules, that.rules) && Objects.equals(variables, that.variables);
        }

        @Override
        public int hashCode() {
            return Objects.hash(properties, nodes, estimatedCost, collections, rules, variables);
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

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof ExecutionNode)) return false;
            ExecutionNode that = (ExecutionNode) o;
            return Objects.equals(properties, that.properties);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(properties);
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

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof ExecutionVariable)) return false;
            ExecutionVariable that = (ExecutionVariable) o;
            return Objects.equals(properties, that.properties);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(properties);
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

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof ExecutionCollection)) return false;
            ExecutionCollection that = (ExecutionCollection) o;
            return Objects.equals(properties, that.properties);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(properties);
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

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof ExecutionStats)) return false;
            ExecutionStats that = (ExecutionStats) o;
            return Objects.equals(properties, that.properties);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(properties);
        }
    }

}
