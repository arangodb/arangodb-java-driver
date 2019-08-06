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
 */
public class IndexEntity implements Entity {

	private String id;
	private String name;
	private IndexType type;
	private Collection<String> fields;
	private Double selectivityEstimate;
	private Boolean unique;
	private Boolean sparse;
	private Integer minLength;
	private Boolean isNewlyCreated;
	private Boolean geoJson;
	private Boolean constraint;
	private Boolean deduplicate;
	private Integer expireAfter;
	private Boolean inBackground;

	public IndexEntity() {
		super();
	}

	public String getId() {
		return id;
	}

	public Boolean getInBackground() {
		return inBackground;
	}

	public String getName() {
		return name;
	}

	public IndexType getType() {
		return type;
	}

	public Collection<String> getFields() {
		return fields;
	}

	public Double getSelectivityEstimate() {
		return selectivityEstimate;
	}

	public Boolean getUnique() {
		return unique;
	}

	public Boolean getSparse() {
		return sparse;
	}

	public Integer getMinLength() {
		return minLength;
	}

	public Boolean getIsNewlyCreated() {
		return isNewlyCreated;
	}

	public Boolean getGeoJson() {
		return geoJson;
	}

	public Integer getExpireAfter() {
		return expireAfter;
	}

	public Boolean getConstraint() {
		return constraint;
	}

	public Boolean getDeduplicate() {
		return deduplicate;
	}

}
