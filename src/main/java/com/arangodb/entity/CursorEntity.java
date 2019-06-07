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
import java.util.Map;

import org.apache.http.protocol.HTTP;

import com.arangodb.velocypack.VPackSlice;

/**
 * @author Mark Vollmary
 * 
 * @see <a href="https://docs.arangodb.com/current/HTTP/AqlQueryCursor/AccessingCursors.html#create-cursor">API
 *      Documentation</a>
 */
public class CursorEntity implements Entity, MetaAware {

	private String id;
	private Integer count;
	private Extras extra;
	private Boolean cached;
	private Boolean hasMore;
	private VPackSlice result;
	
	private Map<String, String> meta;

	public String getId() {
		return id;
	}

	/**
	 * @return the total number of result documents available (only available if the query was executed with the count
	 *         attribute set)
	 */
	public Integer getCount() {
		return count;
	}

	/**
	 * @return an optional object with extra information about the query result contained in its stats sub-attribute.
	 *         For data-modification queries, the extra.stats sub-attribute will contain the number of modified
	 *         documents and the number of documents that could not be modified due to an error (if ignoreErrors query
	 *         option is specified)
	 */
	public Extras getExtra() {
		return extra;
	}

	/**
	 * @return a boolean flag indicating whether the query result was served from the query cache or not. If the query
	 *         result is served from the query cache, the extra return attribute will not contain any stats
	 *         sub-attribute and no profile sub-attribute.
	 */
	public Boolean getCached() {
		return cached;
	}

	/**
	 * @return A boolean indicator whether there are more results available for the cursor on the server
	 */
	public Boolean getHasMore() {
		return hasMore;
	}

	/**
	 * @return an vpack-array of result documents (might be empty if query has no results)
	 */
	public VPackSlice getResult() {
		return result;
	}
	
	public Map<String, String> getMeta() {
		return meta;
	}

	/**
	 * @return remove not allowed (valid storable) meta information
	 */
	public Map<String, String> cleanupMeta(Map<String, String> meta) {
		meta.remove(HTTP.CONTENT_LEN);
		meta.remove(HTTP.TRANSFER_ENCODING);
		return meta;
	}

	public void setMeta(Map<String, String> meta) {
		this.meta = cleanupMeta(meta);
	}

	public static class Warning {

		private Integer code;
		private String message;

		public Integer getCode() {
			return code;
		}

		public String getMessage() {
			return message;
		}

	}

	public static class Extras {
		private Stats stats;
		private Collection<Warning> warnings;

		public Stats getStats() {
			return stats;
		}

		public Collection<Warning> getWarnings() {
			return warnings;
		}

	}

	public static class Stats {
		private Long writesExecuted;
		private Long writesIgnored;
		private Long scannedFull;
		private Long scannedIndex;
		private Long filtered;
		private Long fullCount;
		private Double executionTime;

		public Long getWritesExecuted() {
			return writesExecuted;
		}

		public Long getWritesIgnored() {
			return writesIgnored;
		}

		public Long getScannedFull() {
			return scannedFull;
		}

		public Long getScannedIndex() {
			return scannedIndex;
		}

		public Long getFiltered() {
			return filtered;
		}

		public Long getFullCount() {
			return fullCount;
		}

		public Double getExecutionTime() {
			return executionTime;
		}

	}
}
