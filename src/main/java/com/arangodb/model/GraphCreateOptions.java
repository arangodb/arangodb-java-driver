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
import com.arangodb.entity.ReplicationFactor;

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

    public Boolean getIsDisjoint() {
        return getOptions().getIsDisjoint();
    }

    /**
     * @param isDisjoint If set to true, a Disjoint SmartGraph will be created. This flag is not editable after
     *                   creation. Default: false.
     * @return options
     * @since ArangoDB 3.7
     */
    public GraphCreateOptions isDisjoint(final Boolean isDisjoint) {
        getOptions().setIsDisjoint(isDisjoint);
        return this;
    }

    public Integer getReplicationFactor() {
        return getOptions().replicationFactor.getReplicationFactor();
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
        getOptions().replicationFactor.setReplicationFactor(replicationFactor);
        return this;
    }

    public Boolean getSatellite() {
        return getOptions().replicationFactor.getSatellite();
    }

    /**
     * @param satellite If the true the graph is created as a satellite graph. In this case
     *                  {@link #replicationFactor(Integer)} is ignored.
     * @return options
     * @since ArangoDB 3.7
     */
    public GraphCreateOptions satellite(final Boolean satellite) {
        getOptions().replicationFactor.setSatellite(satellite);
        return this;
    }

    public Integer getWriteConcern() {
        return getOptions().getWriteConcern();
    }

    /**
     * @param writeConcern Write concern for new collections in the graph.
     *                     It determines how many copies of each shard are required to be in sync on the different
     *                     DB-Servers. If there are less then these many copies in the cluster a shard will refuse to
     *                     write. Writes to shards with enough up-to-date copies will succeed at the same time however.
     *                     The value of writeConcern can not be larger than replicationFactor. (cluster only)
     * @return options
     */
    public GraphCreateOptions writeConcern(final Integer writeConcern) {
        getOptions().setWriteConcern(writeConcern);
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

    public Collection<String> getSatellites() {
        return getOptions().getSatellites();
    }

    /**
     * @param satellites collection names that will be used to create SatelliteCollections
     *                   for a Hybrid (Disjoint) SmartGraph (Enterprise Edition only). Each array element
     *                   must be a valid collection name. The collection type cannot be modified later.
     * @return options
     * @since ArangoDB 3.9.0
     */
    public GraphCreateOptions satellites(final String... satellites) {
        getOptions().setSatellites(satellites);
        return this;
    }

    private SmartOptions getOptions() {
        if (options == null) {
            options = new SmartOptions();
        }
        return options;
    }

    public static class SmartOptions {
        private ReplicationFactor replicationFactor;
        private Integer writeConcern;
        private Integer numberOfShards;
        private String smartGraphAttribute;
        private Boolean isDisjoint;
        private Collection<String> satellites;

        public SmartOptions() {
            super();
            replicationFactor = new ReplicationFactor();
        }

        public Integer getReplicationFactor() {
            return replicationFactor.getReplicationFactor();
        }

        public void setReplicationFactor(final Integer replicationFactor) {
            this.replicationFactor.setReplicationFactor(replicationFactor);
        }

        public Boolean getSatellite() {
            return replicationFactor.getSatellite();
        }

        public void setSatellite(final Boolean satellite) {
            replicationFactor.setSatellite(satellite);
        }

        public Integer getWriteConcern() {
            return writeConcern;
        }

        public void setWriteConcern(final Integer writeConcern) {
            this.writeConcern = writeConcern;
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

        public Boolean getIsDisjoint() {
            return isDisjoint;
        }

        public void setIsDisjoint(final Boolean isDisjoint) {
            this.isDisjoint = isDisjoint;
        }

        public Collection<String> getSatellites() {
            return satellites;
        }

        public void setSatellites(final String... satellites) {
            this.satellites = Arrays.asList(satellites);
        }
    }

}
