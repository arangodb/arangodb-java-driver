/*
 * DISCLAIMER
 *
 * Copyright 2018 ArangoDB GmbH, Cologne, Germany
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

package com.arangodb.entity.arangosearch;

import java.util.Map;
import java.util.Set;

/**
 * @author Michele Rastelli
 */
public class AnalyzerEntity {

	private Set<AnalyzerFeature> features;
	private AnalyzerType type;
	private String name;
	private Map<String, Object> properties;

	public AnalyzerEntity() {
	}

	public Set<AnalyzerFeature> getFeatures() {
		return features;
	}

	public void setFeatures(Set<AnalyzerFeature> features) {
		this.features = features;
	}

	public AnalyzerType getType() {
		return type;
	}

	public void setType(AnalyzerType type) {
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Map<String, Object> getProperties() {
		return properties;
	}

	public void setProperties(Map<String, Object> properties) {
		this.properties = properties;
	}

}
