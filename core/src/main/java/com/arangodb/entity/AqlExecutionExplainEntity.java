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

import com.arangodb.ArangoDatabase;
import com.arangodb.model.ExplainAqlQueryOptions;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

/**
 * @author Mark Vollmary
 * @deprecated for removal, use {@link ArangoDatabase#explainAqlQuery(String, Map, ExplainAqlQueryOptions)} instead
 */
@Deprecated
public final class AqlExecutionExplainEntity {

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
        if (!(o instanceof AqlExecutionExplainEntity)) return false;
        AqlExecutionExplainEntity that = (AqlExecutionExplainEntity) o;
        return Objects.equals(plan, that.plan) && Objects.equals(plans, that.plans) && Objects.equals(warnings, that.warnings) && Objects.equals(stats, that.stats) && Objects.equals(cacheable, that.cacheable);
    }

    @Override
    public int hashCode() {
        return Objects.hash(plan, plans, warnings, stats, cacheable);
    }

    public static final class ExecutionPlan {
        private Collection<ExecutionNode> nodes;
        private Collection<String> rules;
        private Collection<ExecutionCollection> collections;
        private Collection<ExecutionVariable> variables;
        private Integer estimatedCost;
        private Integer estimatedNrItems;

        public Collection<ExecutionNode> getNodes() {
            return nodes;
        }

        public Collection<String> getRules() {
            return rules;
        }

        public Collection<ExecutionCollection> getCollections() {
            return collections;
        }

        public Collection<ExecutionVariable> getVariables() {
            return variables;
        }

        public Integer getEstimatedCost() {
            return estimatedCost;
        }

        public Integer getEstimatedNrItems() {
            return estimatedNrItems;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof ExecutionPlan)) return false;
            ExecutionPlan that = (ExecutionPlan) o;
            return Objects.equals(nodes, that.nodes) && Objects.equals(rules, that.rules) && Objects.equals(collections, that.collections) && Objects.equals(variables, that.variables) && Objects.equals(estimatedCost, that.estimatedCost) && Objects.equals(estimatedNrItems, that.estimatedNrItems);
        }

        @Override
        public int hashCode() {
            return Objects.hash(nodes, rules, collections, variables, estimatedCost, estimatedNrItems);
        }
    }

    public static final class ExecutionNode {
        private String type;
        private Collection<Long> dependencies;
        private Long id;
        private Integer estimatedCost;
        private Integer estimatedNrItems;
        private Long depth;
        private String database;
        private String collection;
        private ExecutionVariable inVariable;
        private ExecutionVariable outVariable;
        private ExecutionVariable conditionVariable;
        private Boolean random;
        private Long offset;
        private Long limit;
        private Boolean fullCount;
        private ExecutionNode subquery;
        private Boolean isConst;
        private Boolean canThrow;
        private String expressionType;
        private Collection<IndexEntity> indexes;
        private ExecutionExpression expression;
        private ExecutionCollection condition;
        private Boolean reverse;

        public String getType() {
            return type;
        }

        public Collection<Long> getDependencies() {
            return dependencies;
        }

        public Long getId() {
            return id;
        }

        public Integer getEstimatedCost() {
            return estimatedCost;
        }

        public Integer getEstimatedNrItems() {
            return estimatedNrItems;
        }

        public Long getDepth() {
            return depth;
        }

        public String getDatabase() {
            return database;
        }

        public String getCollection() {
            return collection;
        }

        public ExecutionVariable getInVariable() {
            return inVariable;
        }

        public ExecutionVariable getOutVariable() {
            return outVariable;
        }

        public ExecutionVariable getConditionVariable() {
            return conditionVariable;
        }

        public Boolean getRandom() {
            return random;
        }

        public Long getOffset() {
            return offset;
        }

        public Long getLimit() {
            return limit;
        }

        public Boolean getFullCount() {
            return fullCount;
        }

        public ExecutionNode getSubquery() {
            return subquery;
        }

        public Boolean getIsConst() {
            return isConst;
        }

        public Boolean getCanThrow() {
            return canThrow;
        }

        public String getExpressionType() {
            return expressionType;
        }

        public Collection<IndexEntity> getIndexes() {
            return indexes;
        }

        public ExecutionExpression getExpression() {
            return expression;
        }

        public ExecutionCollection getCondition() {
            return condition;
        }

        public Boolean getReverse() {
            return reverse;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof ExecutionNode)) return false;
            ExecutionNode that = (ExecutionNode) o;
            return Objects.equals(type, that.type) && Objects.equals(dependencies, that.dependencies) && Objects.equals(id, that.id) && Objects.equals(estimatedCost, that.estimatedCost) && Objects.equals(estimatedNrItems, that.estimatedNrItems) && Objects.equals(depth, that.depth) && Objects.equals(database, that.database) && Objects.equals(collection, that.collection) && Objects.equals(inVariable, that.inVariable) && Objects.equals(outVariable, that.outVariable) && Objects.equals(conditionVariable, that.conditionVariable) && Objects.equals(random, that.random) && Objects.equals(offset, that.offset) && Objects.equals(limit, that.limit) && Objects.equals(fullCount, that.fullCount) && Objects.equals(subquery, that.subquery) && Objects.equals(isConst, that.isConst) && Objects.equals(canThrow, that.canThrow) && Objects.equals(expressionType, that.expressionType) && Objects.equals(indexes, that.indexes) && Objects.equals(expression, that.expression) && Objects.equals(condition, that.condition) && Objects.equals(reverse, that.reverse);
        }

        @Override
        public int hashCode() {
            return Objects.hash(type, dependencies, id, estimatedCost, estimatedNrItems, depth, database, collection, inVariable, outVariable, conditionVariable, random, offset, limit, fullCount, subquery, isConst, canThrow, expressionType, indexes, expression, condition, reverse);
        }
    }

    public static final class ExecutionVariable {
        private Long id;
        private String name;

        public Long getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof ExecutionVariable)) return false;
            ExecutionVariable that = (ExecutionVariable) o;
            return Objects.equals(id, that.id) && Objects.equals(name, that.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, name);
        }
    }

    public static final class ExecutionExpression {
        private String type;
        private String name;
        private Long id;
        private Object value;
        private Boolean sorted;
        private String quantifier;
        private Collection<Long> levels;
        private Collection<ExecutionExpression> subNodes;

        public String getType() {
            return type;
        }

        public String getName() {
            return name;
        }

        public Long getId() {
            return id;
        }

        public Object getValue() {
            return value;
        }

        public Boolean getSorted() {
            return sorted;
        }

        public String getQuantifier() {
            return quantifier;
        }

        public Collection<Long> getLevels() {
            return levels;
        }

        public Collection<ExecutionExpression> getSubNodes() {
            return subNodes;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof ExecutionExpression)) return false;
            ExecutionExpression that = (ExecutionExpression) o;
            return Objects.equals(type, that.type) && Objects.equals(name, that.name) && Objects.equals(id, that.id) && Objects.equals(value, that.value) && Objects.equals(sorted, that.sorted) && Objects.equals(quantifier, that.quantifier) && Objects.equals(levels, that.levels) && Objects.equals(subNodes, that.subNodes);
        }

        @Override
        public int hashCode() {
            return Objects.hash(type, name, id, value, sorted, quantifier, levels, subNodes);
        }
    }

    public static final class ExecutionCollection {
        private String name;
        private String type;

        public String getName() {
            return name;
        }

        public String getType() {
            return type;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof ExecutionCollection)) return false;
            ExecutionCollection that = (ExecutionCollection) o;
            return Objects.equals(name, that.name) && Objects.equals(type, that.type);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, type);
        }
    }

    public static final class ExecutionStats {
        private Integer rulesExecuted;
        private Integer rulesSkipped;
        private Integer plansCreated;
        private Long peakMemoryUsage;
        private Double executionTime;

        public Integer getRulesExecuted() {
            return rulesExecuted;
        }

        public Integer getRulesSkipped() {
            return rulesSkipped;
        }

        public Integer getPlansCreated() {
            return plansCreated;
        }

        public Long getPeakMemoryUsage() {
            return peakMemoryUsage;
        }

        public Double getExecutionTime() {
            return executionTime;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof ExecutionStats)) return false;
            ExecutionStats that = (ExecutionStats) o;
            return Objects.equals(rulesExecuted, that.rulesExecuted) && Objects.equals(rulesSkipped, that.rulesSkipped) && Objects.equals(plansCreated, that.plansCreated) && Objects.equals(peakMemoryUsage, that.peakMemoryUsage) && Objects.equals(executionTime, that.executionTime);
        }

        @Override
        public int hashCode() {
            return Objects.hash(rulesExecuted, rulesSkipped, plansCreated, peakMemoryUsage, executionTime);
        }
    }

}
