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

import com.arangodb.velocypack.VPackSlice;

import java.util.Collection;

/**
 * @author Mark Vollmary
 * @author Michele Rastelli
 * @see <a href="https://www.arangodb.com/docs/stable/http/aql-query.html#explain-an-aql-query">API Documentation</a>
 */
public class AqlQueryExplainOptions {

    private VPackSlice bindVars;
    private String query;
    private Options options;

    public AqlQueryExplainOptions() {
        super();
    }

    protected VPackSlice getBindVars() {
        return bindVars;
    }

    /**
     * @param bindVars key/value pairs representing the bind parameters
     * @return options
     */
    protected AqlQueryExplainOptions bindVars(final VPackSlice bindVars) {
        this.bindVars = bindVars;
        return this;
    }

    protected String getQuery() {
        return query;
    }

    /**
     * @param query the query which you want explained
     * @return options
     */
    protected AqlQueryExplainOptions query(final String query) {
        this.query = query;
        return this;
    }

    public Integer getMaxNumberOfPlans() {
        return getOptions().maxNumberOfPlans;
    }

    /**
     * @param maxNumberOfPlans an optional maximum number of plans that the optimizer is allowed to generate. Setting this attribute
     *                         to a low value allows to put a cap on the amount of work the optimizer does.
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
     * @param allPlans if set to true, all possible execution plans will be returned. The default is false, meaning only the
     *                 optimal plan will be returned.
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
     * @param rules an array of to-be-included or to-be-excluded optimizer rules can be put into this attribute, telling
     *              the optimizer to include or exclude specific rules.
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
