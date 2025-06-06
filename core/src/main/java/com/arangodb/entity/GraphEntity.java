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

package com.arangodb.entity;

import java.util.Collection;
import java.util.Objects;

/**
 * @author Mark Vollmary
 */
public final class GraphEntity {

    private String name;
    private Collection<EdgeDefinition> edgeDefinitions;
    private Collection<String> orphanCollections;
    private Integer numberOfShards;
    private String _id;
    private String _rev;
    private ReplicationFactor replicationFactor;
    private Integer writeConcern;
    private Boolean isSmart;
    private Boolean isDisjoint;
    private String smartGraphAttribute;
    private Boolean isSatellite;

    /**
     * @return Name of the graph.
     */
    public String getName() {
        return name;
    }

    /**
     * @return An array of definitions for the relations of the graph.
     */
    public Collection<EdgeDefinition> getEdgeDefinitions() {
        return edgeDefinitions;
    }

    /**
     * @return An array of additional vertex collections. Documents within these collections do not have edges within
     * this graph.
     */
    public Collection<String> getOrphanCollections() {
        return orphanCollections;
    }

    /**
     * @return Number of shards created for every new collection in the graph.
     */
    public Integer getNumberOfShards() {
        return numberOfShards;
    }

    /**
     * @return The internal id value of this graph.
     */
    public String getId() {
        return _id;
    }

    /**
     * @return The revision of this graph. Can be used to make sure to not override concurrent modifications to this
     * graph.
     */
    public String getRev() {
        return _rev;
    }

    /**
     * @return The replication factor used for every new collection in the graph. Can also be satellite for a SmartGraph.
     */
    public ReplicationFactor getReplicationFactor() {
        return replicationFactor;
    }

    /**
     * @return Default write concern for new collections in the graph. It determines how many copies of each shard are
     * required to be in sync on the different DB-Servers. If there are less then these many copies in the cluster a
     * shard will refuse to write. Writes to shards with enough up-to-date copies will succeed at the same time however.
     * The value of writeConcern can not be larger than replicationFactor. (cluster only)
     */
    public Integer getWriteConcern() {
        return writeConcern;
    }

    /**
     * @return Whether the graph is a SmartGraph.
     */
    public Boolean getIsSmart() {
        return isSmart;
    }

    /**
     * @return Whether the graph is a Disjoint SmartGraph.
     */
    public Boolean getIsDisjoint() {
        return isDisjoint;
    }

    /**
     * @return Name of the sharding attribute in the SmartGraph case.
     */
    public String getSmartGraphAttribute() {
        return smartGraphAttribute;
    }

    /**
     * @return Flag if the graph is a SatelliteGraph or not.
     */
    public Boolean getIsSatellite() {
        return isSatellite;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof GraphEntity)) return false;
        GraphEntity that = (GraphEntity) o;
        return Objects.equals(name, that.name) && Objects.equals(edgeDefinitions, that.edgeDefinitions) && Objects.equals(orphanCollections, that.orphanCollections) && Objects.equals(numberOfShards, that.numberOfShards) && Objects.equals(_id, that._id) && Objects.equals(_rev, that._rev) && Objects.equals(replicationFactor, that.replicationFactor) && Objects.equals(writeConcern, that.writeConcern) && Objects.equals(isSmart, that.isSmart) && Objects.equals(isDisjoint, that.isDisjoint) && Objects.equals(smartGraphAttribute, that.smartGraphAttribute) && Objects.equals(isSatellite, that.isSatellite);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, edgeDefinitions, orphanCollections, numberOfShards, _id, _rev, replicationFactor, writeConcern, isSmart, isDisjoint, smartGraphAttribute, isSatellite);
    }
}
