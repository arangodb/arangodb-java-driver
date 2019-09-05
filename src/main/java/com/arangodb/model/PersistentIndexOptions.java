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

package com.arangodb.model;

import com.arangodb.entity.IndexType;

/**
 * @author Mark Vollmary
 * 
 * @see <a href="https://docs.arangodb.com/current/HTTP/Indexes/Persistent.html#create-a-persistent-index">API
 *      Documentation</a>
 */
public class PersistentIndexOptions extends IndexOptions<PersistentIndexOptions> {

	private Iterable<String> fields;
	protected final IndexType type = IndexType.persistent;
	private Boolean unique;
	private Boolean sparse;

	public PersistentIndexOptions() {
		super();
	}

    @Override
    protected PersistentIndexOptions getThis() {
        return null;
    }

	protected Iterable<String> getFields() {
		return fields;
	}

	/**
	 * @param fields
	 *            A list of attribute paths
	 * @return options
	 */
	protected PersistentIndexOptions fields(final Iterable<String> fields) {
		this.fields = fields;
		return this;
	}

	protected IndexType getType() {
		return type;
	}

	public Boolean getUnique() {
		return unique;
	}

	/**
	 * @param unique
	 *            if true, then create a unique index
	 * @return options
	 */
	public PersistentIndexOptions unique(final Boolean unique) {
		this.unique = unique;
		return this;
	}

	public Boolean getSparse() {
		return sparse;
	}

	/**
	 * @param sparse
	 *            if true, then create a sparse index
	 * @return options
	 */
	public PersistentIndexOptions sparse(final Boolean sparse) {
		this.sparse = sparse;
		return this;
	}

}
