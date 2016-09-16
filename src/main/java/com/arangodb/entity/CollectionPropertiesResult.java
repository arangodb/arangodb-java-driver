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

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class CollectionPropertiesResult extends CollectionResult {

	private Boolean doCompact;
	private Long journalSize;
	private Integer indexBuckets;
	private KeyOptions keyOptions;
	private Long count;

	public CollectionPropertiesResult() {
		super();
	}

	public Boolean getDoCompact() {
		return doCompact;
	}

	public void setDoCompact(final Boolean doCompact) {
		this.doCompact = doCompact;
	}

	public Long getJournalSize() {
		return journalSize;
	}

	public void setJournalSize(final Long journalSize) {
		this.journalSize = journalSize;
	}

	public Integer getIndexBuckets() {
		return indexBuckets;
	}

	public void setIndexBuckets(final Integer indexBuckets) {
		this.indexBuckets = indexBuckets;
	}

	public KeyOptions getKeyOptions() {
		return keyOptions;
	}

	public void setKeyOptions(final KeyOptions keyOptions) {
		this.keyOptions = keyOptions;
	}

	public Long getCount() {
		return count;
	}

	public void setCount(final Long count) {
		this.count = count;
	}

}
