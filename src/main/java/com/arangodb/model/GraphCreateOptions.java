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

import com.arangodb.entity.EdgeDefinition;

import java.util.Arrays;
import java.util.Collection;

/**
 * @author Mark Vollmary
 * @see <a href="https://www.arangodb.com/docs/stable/http/gharial-management.html#create-a-graph">API Documentation</a>
 */
public class GraphCreateOptions {

    private String name;
    private Collection<EdgeDefinition> edgeDefinitions;
    private Collection<String> orphanCollections;
    private Boolean isSmart;
    private SmartOptions options;

    public GraphCreateOptions() {
        super();
    }

    protected String getName() {
        return name;
    }

    /**
     * @param name Name of the graph
     * @return options
     */
    protected GraphCreateOptions name(final String name) {
        this.name = name;
        return this;
    }

    public Collection<EdgeDefinition> getEdgeDefinitions() {
        return edgeDefinitions;
    }

    /**
     * @param edgeDefinitions An array of definitions for the edge
     * @return options
     */
    protected GraphCreateOptions edgeDefinitions(final Collection<EdgeDefinition> edgeDefinitions) {
        this.edgeDefinitions = edgeDefinitions;
        return this;
    }

    public Collection<String> getOrphanCollections() {
        return orphanCollections;
    }

    /**
     * @param orphanCollections Additional vertex collections
     * @return options
     */
    public GraphCreateOptions orphanCollections(final String... orphanCollections) {
        this.orphanCollections = Arrays.asList(orphanCollections);
        return this;
    }

    public Boolean getIsSmart() {
        return isSmart;
    }

    /**
     * @param isSmart Define if the created graph should be smart. This only has effect in Enterprise version.
     * @return options
     */
    public GraphCreateOptions isSmart(final Boolean isSmart) {
        this.isSmart = isSmart;
        return this;
    }

    public Integer getReplicationFactor() {
        return getOptions().getReplicationFactor();
    }

    /**
     * @param replicationFactor (The default is 1): in a cluster, this attribute determines how many copies of each shard are kept on
     *                          different DBServers. The value 1 means that only one copy (no synchronous replication) is kept. A
     *                          value of k means that k-1 replicas are kept. Any two copies reside on different DBServers. Replication
     *                          between them is synchronous, that is, every write operation to the "leader" copy will be replicated to
     *                          all "follower" replicas, before the write operation is reported successful. If a server fails, this is
     *                          detected automatically and one of the servers holding copies take over, usually without an error being
     *                          reported.
     * @return options
     */
    public GraphCreateOptions replicationFactor(final Integer replicationFactor) {
        getOptions().setReplicationFactor(replicationFactor);
        return this;
    }

    public Integer getMinReplicationFactor() {
        return getOptions().getMinReplicationFactor();
    }

    /**
     * @param minReplicationFactor (optional, default is 1): in a cluster, this attribute determines how many desired copies of each
     *                             shard are kept on different DBServers. The value 1 means that only one copy (no synchronous
     *                             replication) is kept. A value of k means that desired k-1 replicas are kept. If in a failover scenario
     *                             a shard of a collection has less than minReplicationFactor many insync followers it will go into
     *                             "read-only" mode and will reject writes until enough followers are insync again. In more detail:
     *                             Having `minReplicationFactor == 1` means as soon as a "master-copy" is available of the data writes
     *                             are allowed. Having `minReplicationFactor > 1` requires additional insync copies on follower servers
     *                             to allow writes.
     * @return options
     */
    public GraphCreateOptions minReplicationFactor(final Integer minReplicationFactor) {
        getOptions().setMinReplicationFactor(minReplicationFactor);
        return this;
    }

    public Integer getNumberOfShards() {
        return getOptions().getNumberOfShards();
    }

    /**
     * @param numberOfShards The number of shards that is used for every collection within this graph. Cannot be modified later.
     * @return options
     */
    public GraphCreateOptions numberOfShards(final Integer numberOfShards) {
        getOptions().setNumberOfShards(numberOfShards);
        return this;
    }

    public String getSmartGraphAttribute() {
        return getOptions().getSmartGraphAttribute();
    }

    /**
     * @param smartGraphAttribute The attribute name that is used to smartly shard the vertices of a graph. Every vertex in this Graph
     *                            has to have this attribute. Cannot be modified later.
     * @return options
     */
    public GraphCreateOptions smartGraphAttribute(final String smartGraphAttribute) {
        getOptions().setSmartGraphAttribute(smartGraphAttribute);
        return this;
    }

    private SmartOptions getOptions() {
        if (options == null) {
            options = new SmartOptions();
        }
        return options;
    }

    public static class SmartOptions {
        private Integer replicationFactor;
        private Integer minReplicationFactor;
        private Integer numberOfShards;
        private String smartGraphAttribute;

        public SmartOptions() {
            super();
        }

        public Integer getReplicationFactor() {
            return replicationFactor;
        }

        public void setReplicationFactor(final Integer replicationFactor) {
            this.replicationFactor = replicationFactor;
        }

        public Integer getMinReplicationFactor() {
            return minReplicationFactor;
        }

        public void setMinReplicationFactor(final Integer minReplicationFactor) {
            this.minReplicationFactor = minReplicationFactor;
        }

        public Integer getNumberOfShards() {
            return numberOfShards;
        }

        public void setNumberOfShards(final Integer numberOfShards) {
            this.numberOfShards = numberOfShards;
        }

        public String getSmartGraphAttribute() {
            return smartGraphAttribute;
        }

        public void setSmartGraphAttribute(final String smartGraphAttribute) {
            this.smartGraphAttribute = smartGraphAttribute;
        }

    }

}
