package com.arangodb.entity;

import java.util.Collection;

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

		public void setStats(final Stats stats) {
			this.stats = stats;
		}

		public Collection<Warning> getWarnings() {
			return warnings;
		}

		public void setWarnings(final Collection<Warning> warnings) {
			this.warnings = warnings;
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

		public void setWritesExecuted(final Long writesExecuted) {
			this.writesExecuted = writesExecuted;
		}

		public Long getWritesIgnored() {
			return writesIgnored;
		}

		public void setWritesIgnored(final Long writesIgnored) {
			this.writesIgnored = writesIgnored;
		}

		public Long getScannedFull() {
			return scannedFull;
		}

		public void setScannedFull(final Long scannedFull) {
			this.scannedFull = scannedFull;
		}

		public Long getScannedIndex() {
			return scannedIndex;
		}

		public void setScannedIndex(final Long scannedIndex) {
			this.scannedIndex = scannedIndex;
		}

		public Long getFiltered() {
			return filtered;
		}

		public void setFiltered(final Long filtered) {
			this.filtered = filtered;
		}

		public Long getFullCount() {
			return fullCount;
		}

		public void setFullCount(final Long fullCount) {
			this.fullCount = fullCount;
		}

		public Double getExecutionTime() {
			return executionTime;
		}

		public void setExecutionTime(final Double executionTime) {
			this.executionTime = executionTime;
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
