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

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Mark Vollmary
 *
 */
public class DocumentImportEntity implements Entity {

	private Integer created;
	private Integer errors;
	private Integer empty;
	private Integer updated;
	private Integer ignored;
	private Collection<String> details;

	public DocumentImportEntity() {
		super();
        details = new ArrayList<>();
	}

	/**
	 * @return number of documents imported.
	 */
	public Integer getCreated() {
		return created;
	}

	public void setCreated(final Integer created) {
		this.created = created;
	}

	/**
	 * @return number of documents that were not imported due to an error.
	 */
	public Integer getErrors() {
		return errors;
	}

	public void setErrors(final Integer errors) {
		this.errors = errors;
	}

	/**
	 * @return number of empty lines found in the input (will only contain a value greater zero for types documents or
	 *         auto).
	 */
	public Integer getEmpty() {
		return empty;
	}

	public void setEmpty(final Integer empty) {
		this.empty = empty;
	}

	/**
	 * @return number of updated/replaced documents (in case onDuplicate was set to either update or replace).
	 */
	public Integer getUpdated() {
		return updated;
	}

	public void setUpdated(final Integer updated) {
		this.updated = updated;
	}

	/**
	 * @return number of failed but ignored insert operations (in case onDuplicate was set to ignore).
	 */
	public Integer getIgnored() {
		return ignored;
	}

	public void setIgnored(final Integer ignored) {
		this.ignored = ignored;
	}

	/**
	 * @return if query parameter details is set to true, the result contain details with more detailed information
	 *         about which documents could not be inserted.
	 */
	public Collection<String> getDetails() {
		return details;
	}

	public void setDetails(final Collection<String> details) {
		this.details = details;
	}

}
