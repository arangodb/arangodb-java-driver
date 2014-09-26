/*
 * Copyright (C) 2012,2013 tamtam180
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package at.orz.arangodb.entity;

import java.util.List;
import java.util.Map;

/**
 * @author tamtam180 - kirscheless at gmail.com
 *
 */
public class ExplainEntity extends BaseEntity {
	
	List<PlanEntity> plan;

	public List<PlanEntity> getPlan() {
		return plan;
	}

	public void setPlan(List<PlanEntity> plan) {
		this.plan = plan;
	}

	public static class PlanEntity {
		long id;
		int loopLevel;
		String type;
		String resultVariable;
		long offset;
		long count;
		ExpressionEntity expression;
		public long getId() {
			return id;
		}
		public int getLoopLevel() {
			return loopLevel;
		}
		public String getType() {
			return type;
		}
		public String getResultVariable() {
			return resultVariable;
		}
		public long getOffset() {
			return offset;
		}
		public long getCount() {
			return count;
		}
		public ExpressionEntity getExpression() {
			return expression;
		}
		public void setId(long id) {
			this.id = id;
		}
		public void setLoopLevel(int loopLevel) {
			this.loopLevel = loopLevel;
		}
		public void setType(String type) {
			this.type = type;
		}
		public void setResultVariable(String resultVariable) {
			this.resultVariable = resultVariable;
		}
		public void setOffset(long offset) {
			this.offset = offset;
		}
		public void setCount(long count) {
			this.count = count;
		}
		public void setExpression(ExpressionEntity expression) {
			this.expression = expression;
		}
		
	}
	
	public static class ExpressionEntity {
		String type;
		String value;
		Map<String, Object> extra;
		public String getType() {
			return type;
		}
		public String getValue() {
			return value;
		}
		public Map<String, Object> getExtra() {
			return extra;
		}
		public void setType(String type) {
			this.type = type;
		}
		public void setValue(String value) {
			this.value = value;
		}
		public void setExtra(Map<String, Object> extra) {
			this.extra = extra;
		}
	}
	
}
