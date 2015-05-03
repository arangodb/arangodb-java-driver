package com.arangodb.util;

import java.util.Map;

public class AqlQueryOptions implements OptionsInterface {

	private Boolean count;
	private Integer batchSize;
	private Boolean fullCount;
	private Integer ttl;

	/**
	 * boolean flag that indicates whether the number of documents in the result
	 * set should be returned in the "count" attribute of the result (optional).
	 * Calculating the "count" attribute might in the future have a performance
	 * impact for some queries so this option is turned off by default, and
	 * "count" is only returned when requested.
	 * 
	 * @return flag that indicates whether the number of documents in the result
	 *         set should be returned
	 */
	public Boolean getCount() {
		return count;
	}

	/**
	 * boolean flag that indicates whether the number of documents in the result
	 * set should be returned in the "count" attribute of the result (optional).
	 * Calculating the "count" attribute might in the future have a performance
	 * impact for some queries so this option is turned off by default, and
	 * "count" is only returned when requested.
	 * 
	 * @param count
	 *            boolean flag
	 * @return this
	 */
	public AqlQueryOptions setCount(Boolean count) {
		this.count = count;
		return this;
	}

	/**
	 * maximum number of result documents to be transferred from the server to
	 * the client in one roundtrip (optional). If this attribute is not set, a
	 * server-controlled default value will be used.
	 * 
	 * @return maximum number of result documents
	 */
	public Integer getBatchSize() {
		return batchSize;
	}

	/**
	 * maximum number of result documents to be transferred from the server to
	 * the client in one roundtrip (optional). If this attribute is not set, a
	 * server-controlled default value will be used.
	 * 
	 * @param batchSize
	 *            maximum number of result documents
	 * @return this
	 */
	public AqlQueryOptions setBatchSize(Integer batchSize) {
		this.batchSize = batchSize;
		return this;
	}

	/**
	 * if set to true and the query contains a LIMIT clause, then the result
	 * will contain an extra attribute extra with a sub-attribute fullCount.
	 * This sub-attribute will contain the number of documents in the result
	 * before the last LIMIT in the query was applied. It can be used to count
	 * the number of documents that match certain filter criteria, but only
	 * return a subset of them, in one go.
	 * 
	 * @return boolean flag
	 */
	public Boolean getFullCount() {
		return fullCount;
	}

	/**
	 * if set to true and the query contains a LIMIT clause, then the result
	 * will contain an extra attribute extra with a sub-attribute fullCount.
	 * This sub-attribute will contain the number of documents in the result
	 * before the last LIMIT in the query was applied. It can be used to count
	 * the number of documents that match certain filter criteria, but only
	 * return a subset of them, in one go.
	 * 
	 * @param fullCount
	 *            boolean flag
	 * @return this
	 */
	public AqlQueryOptions setFullCount(Boolean fullCount) {
		this.fullCount = fullCount;
		return this;
	}

	/**
	 * an optional time-to-live for the cursor (in seconds). The cursor will be
	 * removed on the server automatically after the specified amount of time.
	 * This is useful to ensure garbage collection of cursors that are not fully
	 * fetched by clients. If not set, a server-defined value will be used.
	 * 
	 * @return optional time-to-live
	 */
	public Integer getTtl() {
		return ttl;
	}

	/**
	 * an optional time-to-live for the cursor (in seconds). The cursor will be
	 * removed on the server automatically after the specified amount of time.
	 * This is useful to ensure garbage collection of cursors that are not fully
	 * fetched by clients. If not set, a server-defined value will be used.
	 * 
	 * @param ttl
	 *            optional time-to-live
	 * @return this
	 */
	public AqlQueryOptions setTtl(Integer ttl) {
		this.ttl = ttl;
		return this;
	}

	@Override
	public Map<String, Object> toMap() {
		MapBuilder mp = new MapBuilder();
		if (count != null) {
			mp.put("count", count);
		}
		if (batchSize != null) {
			mp.put("batchSize", batchSize);
		}
		if (ttl != null) {
			mp.put("ttl", ttl);
		}

		MapBuilder optionsMp = new MapBuilder();

		if (fullCount != null) {
			optionsMp.put("fullCount", fullCount);
		}

		// TODO add maxPlans

		// TODO add optimizer.rules

		mp.put("options", optionsMp.get());

		return mp.get();
	}

}
