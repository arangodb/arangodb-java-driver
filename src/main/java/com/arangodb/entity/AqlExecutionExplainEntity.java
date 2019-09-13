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

import java.util.Collection;

/**
 * @author Mark Vollmary
 * 
 * @see <a href="https://docs.arangodb.com/current/HTTP/AqlQuery/index.html#explain-an-aql-query">API Documentation</a>
 */
public class AqlExecutionExplainEntity implements Entity {

	public static class ExecutionPlan {
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
	}

	public static class ExecutionNode {
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
	}

	public static class ExecutionVariable {
		private Long id;
		private String name;

		public Long getId() {
			return id;
		}

		public String getName() {
			return name;
		}
	}

	public static class ExecutionExpression {
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
	}

	public static class ExecutionCollection {
		private String name;
		private String type;

		public String getName() {
			return name;
		}

		public String getType() {
			return type;
		}
	}

	public static class ExecutionStats {
		private Integer rulesExecuted;
		private Integer rulesSkipped;
		private Integer plansCreated;

		public Integer getRulesExecuted() {
			return rulesExecuted;
		}

		public Integer getRulesSkipped() {
			return rulesSkipped;
		}

		public Integer getPlansCreated() {
			return plansCreated;
		}

	}

	private ExecutionPlan plan;
	private Collection<ExecutionPlan> plans;
	private Collection<String> warnings;
	private ExecutionStats stats;
	private Boolean cacheable;

	public ExecutionPlan getPlan() {
		return plan;
	}

	public Collection<ExecutionPlan> getPlans() {
		return plans;
	}

	public Collection<String> getWarnings() {
		return warnings;
	}

	public ExecutionStats getStats() {
		return stats;
	}

	public Boolean getCacheable() {
		return cacheable;
	}

}
