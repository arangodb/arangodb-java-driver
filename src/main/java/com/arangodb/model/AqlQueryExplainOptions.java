package com.arangodb.model;

import java.util.Collection;
import java.util.Map;

/**
 * @author Mark - mark at arangodb.com
 * 
 * @see <a href="https://docs.arangodb.com/3.0/HTTP/AqlQuery/index.html#explain-an-aql-query">API Documentation</a>
 */
public class AqlQueryExplainOptions {

	private Map<String, Object> bindVars;
	private String query;
	private Options options;

	protected Map<String, Object> getBindVars() {
		return bindVars;
	}

	protected AqlQueryExplainOptions bindVars(final Map<String, Object> bindVars) {
		this.bindVars = bindVars;
		return this;
	}

	protected String getQuery() {
		return query;
	}

	protected AqlQueryExplainOptions query(final String query) {
		this.query = query;
		return this;
	}

	public Integer getMaxNumberOfPlans() {
		return getOptions().maxNumberOfPlans;
	}

	/**
	 * @param maxNumberOfPlans
	 *            an optional maximum number of plans that the optimizer is allowed to generate. Setting this attribute
	 *            to a low value allows to put a cap on the amount of work the optimizer does.
	 * @return options
	 */
	public AqlQueryExplainOptions maxNumberOfPlans(final Integer maxNumberOfPlans) {
		getOptions().maxNumberOfPlans = maxNumberOfPlans;
		return this;
	}

	public Boolean getAllPlans() {
		return getOptions().allPlans;
	}

	/**
	 * @param allPlans
	 *            if set to true, all possible execution plans will be returned. The default is false, meaning only the
	 *            optimal plan will be returned.
	 * @return options
	 */
	public AqlQueryExplainOptions allPlans(final Boolean allPlans) {
		getOptions().allPlans = allPlans;
		return this;
	}

	public Collection<String> getRules() {
		return getOptions().getOptimizer().rules;
	}

	/**
	 * @param rules
	 *            an array of to-be-included or to-be-excluded optimizer rules can be put into this attribute, telling
	 *            the optimizer to include or exclude specific rules.
	 * @return options
	 */
	public AqlQueryExplainOptions rules(final Collection<String> rules) {
		getOptions().getOptimizer().rules = rules;
		return this;
	}

	private Options getOptions() {
		if (options == null) {
			options = new Options();
		}
		return options;
	}

	private static class Options {
		private Optimizer optimizer;
		private Integer maxNumberOfPlans;
		private Boolean allPlans;

		protected Optimizer getOptimizer() {
			if (optimizer == null) {
				optimizer = new Optimizer();
			}
			return optimizer;
		}
	}

	private static class Optimizer {
		private Collection<String> rules;
	}
}
