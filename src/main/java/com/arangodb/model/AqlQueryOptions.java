package com.arangodb.model;

import java.util.Collection;
import java.util.Map;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class AqlQueryOptions {

	private Boolean count;
	private Integer ttl;
	private Integer batchSize;
	private Boolean cache;
	private Map<String, Object> bindVars;
	private String query;
	private Options options;

	public Boolean getCount() {
		return count;
	}

	public AqlQueryOptions count(final Boolean count) {
		this.count = count;
		return this;
	}

	public Integer getTtl() {
		return ttl;
	}

	public AqlQueryOptions ttl(final Integer ttl) {
		this.ttl = ttl;
		return this;
	}

	public Integer getBatchSize() {
		return batchSize;
	}

	public AqlQueryOptions batchSize(final Integer batchSize) {
		this.batchSize = batchSize;
		return this;
	}

	public Boolean getCache() {
		return cache;
	}

	public AqlQueryOptions cache(final Boolean cache) {
		this.cache = cache;
		return this;
	}

	protected Map<String, Object> getBindVars() {
		return bindVars;
	}

	protected AqlQueryOptions bindVars(final Map<String, Object> bindVars) {
		this.bindVars = bindVars;
		return this;
	}

	protected String getQuery() {
		return query;
	}

	protected AqlQueryOptions query(final String query) {
		this.query = query;
		return this;
	}

	public Boolean getProfile() {
		return options != null ? options.profile : null;
	}

	public AqlQueryOptions profile(final Boolean profile) {
		getOptions().profile = profile;
		return this;
	}

	public Boolean getFullCount() {
		return options != null ? options.fullCount : null;
	}

	public AqlQueryOptions fullCount(final Boolean fullCount) {
		getOptions().fullCount = fullCount;
		return this;
	}

	public Integer getMaxPlans() {
		return options != null ? options.maxPlans : null;
	}

	public AqlQueryOptions setMaxPlans(final Integer maxPlans) {
		getOptions().maxPlans = maxPlans;
		return this;
	}

	public Collection<String> getRules() {
		return options != null ? options.optimizer != null ? options.optimizer.rules : null : null;
	}

	public AqlQueryOptions rules(final Collection<String> rules) {
		getOptions().getOptimizer().rules = rules;
		return this;
	}

	protected Options getOptions() {
		if (options != null) {
			options = new Options();
		}
		return options;
	}

	public static class Options {
		private Boolean profile;
		private Optimizer optimizer;
		private Boolean fullCount;
		private Integer maxPlans;

		protected Optimizer getOptimizer() {
			if (optimizer == null) {
				optimizer = new Optimizer();
			}
			return optimizer;
		}

	}

	public static class Optimizer {
		private Collection<String> rules;
	}

}
