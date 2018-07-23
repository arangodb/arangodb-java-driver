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

/**
 * @author Mark Vollmary
 *
 */
public class ArangoSearchPropertiesOptions {

	private final ArangoSearchProperties properties;

	public ArangoSearchPropertiesOptions() {
		super();
		properties = new ArangoSearchProperties();
	}

	/**
	 * @param locale
	 *            The default locale used for queries on analyzed string values (default: C).
	 * @return options
	 */
	public ArangoSearchPropertiesOptions locale(final String locale) {
		properties.setLocale(locale);
		return this;
	}

	/**
	 * @param commitIntervalMsec
	 *            Wait at least this many milliseconds between committing index data changes and making them visible to
	 *            queries (default: 60000, to disable use: 0). For the case where there are a lot of inserts/updates, a
	 *            lower value, until commit, will cause the index not to account for them and memory usage would
	 *            continue to grow. For the case where there are a few inserts/updates, a higher value will impact
	 *            performance and waste disk space for each commit call without any added benefits.
	 * @return options
	 */
	public ArangoSearchPropertiesOptions commitIntervalMsec(final Long commitIntervalMsec) {
		properties.setCommitIntervalMsec(commitIntervalMsec);
		return this;
	}

	/**
	 * @param cleanupIntervalStep
	 *            Wait at least this many commits between removing unused files in data directory (default: 10, to
	 *            disable use: 0). For the case where the consolidation policies merge segments often (i.e. a lot of
	 *            commit+consolidate), a lower value will cause a lot of disk space to be wasted. For the case where the
	 *            consolidation policies rarely merge segments (i.e. few inserts/deletes), a higher value will impact
	 *            performance without any added benefits.
	 * @return options
	 */
	public ArangoSearchPropertiesOptions cleanupIntervalStep(final Long cleanupIntervalStep) {
		properties.setCleanupIntervalStep(cleanupIntervalStep);
		return this;
	}

	/**
	 * @param thresholds
	 *            A list of consolidate thresholds
	 * @return options
	 */
	public ArangoSearchPropertiesOptions threshold(final ConsolidateThreshold... thresholds) {
		properties.addThreshold(thresholds);
		return this;
	}

	/**
	 * @param links
	 *            A list of linked collections
	 * @return options
	 */
	public ArangoSearchPropertiesOptions link(final CollectionLink... links) {
		properties.addLink(links);
		return this;
	}

}
