package com.arangodb.entity;

import java.util.Map;

import com.arangodb.velocypack.VPackSlice;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class CursorResult {

	private String id;
	private Integer count;
	private Map<String, Object> extra;
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

	public Map<String, Object> getExtra() {
		return extra;
	}

	public void setExtra(final Map<String, Object> extra) {
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
