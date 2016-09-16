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
import java.util.Optional;

/**
 * @author Mark - mark at arangodb.com
 */
public class AqlExecutionExplainResult {

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
		private IndexResult indexes;
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

		public Optional<Long> getDepth() {
			return Optional.ofNullable(depth);
		}

		public Optional<String> getDatabase() {
			return Optional.ofNullable(database);
		}

		public Optional<String> getCollection() {
			return Optional.ofNullable(collection);
		}

		public Optional<ExecutionVariable> getInVariable() {
			return Optional.ofNullable(inVariable);
		}

		public Optional<ExecutionVariable> getOutVariable() {
			return Optional.ofNullable(outVariable);
		}

		public Optional<ExecutionVariable> getConditionVariable() {
			return Optional.ofNullable(conditionVariable);
		}

		public Optional<Boolean> getRandom() {
			return Optional.ofNullable(random);
		}

		public Optional<Long> getOffset() {
			return Optional.ofNullable(offset);
		}

		public Optional<Long> getLimit() {
			return Optional.ofNullable(limit);
		}

		public Optional<Boolean> getFullCount() {
			return Optional.ofNullable(fullCount);
		}

		public Optional<ExecutionNode> getSubquery() {
			return Optional.ofNullable(subquery);
		}

		public Optional<Boolean> getIsConst() {
			return Optional.ofNullable(isConst);
		}

		public Optional<Boolean> getCanThrow() {
			return Optional.ofNullable(canThrow);
		}

		public Optional<String> getExpressionType() {
			return Optional.ofNullable(expressionType);
		}

		public Optional<IndexResult> getIndexes() {
			return Optional.ofNullable(indexes);
		}

		public Optional<ExecutionExpression> getExpression() {
			return Optional.ofNullable(expression);
		}

		public Optional<ExecutionCollection> getCondition() {
			return Optional.ofNullable(condition);
		}

		public Optional<Boolean> getReverse() {
			return Optional.ofNullable(reverse);
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

		public Optional<Object> getValue() {
			return Optional.ofNullable(value);
		}

		public Optional<Boolean> getSorted() {
			return Optional.ofNullable(sorted);
		}

		public Optional<String> getQuantifier() {
			return Optional.ofNullable(quantifier);
		}

		public Optional<Collection<Long>> getLevels() {
			return Optional.ofNullable(levels);
		}

		public Optional<Collection<ExecutionExpression>> getSubNodes() {
			return Optional.ofNullable(subNodes);
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

	public Optional<ExecutionPlan> getPlan() {
		return Optional.ofNullable(plan);
	}

	public Optional<Collection<ExecutionPlan>> getPlans() {
		return Optional.ofNullable(plans);
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
