/*
 * DISCLAIMER
 *
 * Copyright 2018 ArangoDB GmbH, Cologne, Germany
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

package com.arangodb.entity.arangosearch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * @author Mark Vollmary
 * @author Heiko Kernbach
 */
public class ArangoSearchProperties {

    private Long consolidationIntervalMsec;
    private Long commitIntervalMsec;
    private Long cleanupIntervalStep;
    private ConsolidationPolicy consolidationPolicy;
    private final Collection<PrimarySort> primarySorts;
    private final Collection<CollectionLink> links;

    public ArangoSearchProperties() {
        super();
        links = new ArrayList<>();
        primarySorts = new ArrayList<>();
    }

    public Long getCommitIntervalMsec() {
        return commitIntervalMsec;
    }

    public void setCommitIntervalMsec(final Long commitIntervalMsec) {
        this.commitIntervalMsec = commitIntervalMsec;
    }

    public Long getConsolidationIntervalMsec() {
        return consolidationIntervalMsec;
    }

    public void setConsolidationIntervalMsec(final Long consolidationIntervalMsec) {
        this.consolidationIntervalMsec = consolidationIntervalMsec;
    }

    public Long getCleanupIntervalStep() {
        return cleanupIntervalStep;
    }

    public void setCleanupIntervalStep(final Long cleanupIntervalStep) {
        this.cleanupIntervalStep = cleanupIntervalStep;
    }

    public ConsolidationPolicy getConsolidationPolicy() {
        return consolidationPolicy;
    }

    public void setConsolidationPolicy(final ConsolidationPolicy consolidationPolicy) {
        this.consolidationPolicy = consolidationPolicy;
    }

    public Collection<CollectionLink> getLinks() {
        return links;
    }

    public void addLink(final CollectionLink... links) {
        this.links.addAll(Arrays.asList(links));
    }

    public Collection<PrimarySort> getPrimarySort() {
        return primarySorts;
    }

    public void addPrimarySort(final PrimarySort... primarySorts) {
        this.primarySorts.addAll(Arrays.asList(primarySorts));
    }
}
