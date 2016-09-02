package com.arangodb.entity;

import java.util.Collection;
import java.util.Map;

import com.arangodb.velocypack.VPackSlice;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class CursorResult {

	public static class Warning {

		private Integer code;
		private String message;

		public Integer getCode() {
			return code;
		}

		public void setCode(Integer code) {
			this.code = code;
		}

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}

	}

	public static class Extras {
		private Map<String, Object> stats;

		private Collection<Warning> warnings;

		public Map<String, Object> getStats() {
			return stats;
		}

		public void setStats(Map<String, Object> stats) {
			this.stats = stats;
		}

		public Collection<Warning> getWarnings() {
			return warnings;
		}

		public void setWarnings(Collection<Warning> warnings) {
			this.warnings = warnings;
		}
	}

	private String id;
	private Integer count;
	private Extras extra;
	private Boolean cached;
	private Boolean hasMore;
	private VPackSlice result;

	public String getId() {
		return id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public Integer getCount() {
		return count;
	}

	public void setCount(final Integer count) {
		this.count = count;
	}

	public Extras getExtra() {
		return extra;
	}

	public void setExtra(final Extras extra) {
		this.extra = extra;
	}

	public Boolean getCached() {
		return cached;
	}

	public void setCached(final Boolean cached) {
		this.cached = cached;
	}

	public Boolean getHasMore() {
		return hasMore;
	}

	public void setHasMore(final Boolean hasMore) {
		this.hasMore = hasMore;
	}

	public VPackSlice getResult() {
		return result;
	}

	public void setResult(final VPackSlice result) {
		this.result = result;
	}

}
