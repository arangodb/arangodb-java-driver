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

package com.arangodb.model.arangosearch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * @author Mark Vollmary
 *
 */
public class ArangoSearchProperties {

	private String locale;
	private Long commitIntervalMsec;
	private Long cleanupIntervalStep;
	private final Collection<ConsolidateThreshold> thresholds;
	private final Collection<CollectionLink> links;

	public ArangoSearchProperties() {
		super();
		thresholds = new ArrayList<ConsolidateThreshold>();
		links = new ArrayList<CollectionLink>();
	}

	public String getLocale() {
		return locale;
	}

	public void setLocale(final String locale) {
		this.locale = locale;
	}

	public Long getCommitIntervalMsec() {
		return commitIntervalMsec;
	}

	public void setCommitIntervalMsec(final Long commitIntervalMsec) {
		this.commitIntervalMsec = commitIntervalMsec;
	}

	public Long getCleanupIntervalStep() {
		return cleanupIntervalStep;
	}

	public void setCleanupIntervalStep(final Long cleanupIntervalStep) {
		this.cleanupIntervalStep = cleanupIntervalStep;
	}

	public Collection<ConsolidateThreshold> getThresholds() {
		return thresholds;
	}

	public void addThreshold(final ConsolidateThreshold... thresholds) {
		this.thresholds.addAll(Arrays.asList(thresholds));
	}

	public Collection<CollectionLink> getLinks() {
		return links;
	}

	public void addLink(final CollectionLink... links) {
		this.links.addAll(Arrays.asList(links));
	}

}
