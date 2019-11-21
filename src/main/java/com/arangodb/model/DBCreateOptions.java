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

import com.arangodb.entity.MinReplicationFactor;
import com.arangodb.entity.ReplicationFactor;

/**
 * @author Mark Vollmary
 *
 */
public class DBCreateOptions {

	private String name;

	private final ReplicationFactor replicationFactor;
	private final MinReplicationFactor minReplicationFactor;
	private String sharding;

	public DBCreateOptions() {
		super();
		replicationFactor = new ReplicationFactor();
		minReplicationFactor = new MinReplicationFactor();
	}

	public String getName() {
		return name;
	}

	public Integer getReplicationFactor() {
		return replicationFactor.getReplicationFactor();
	}

	public Integer getMinReplicationFactor() {
		return minReplicationFactor.getMinReplicationFactor();
	}

	/**
	 * @return whether the collection is a satellite collection. Only in an enterprise cluster setup (else returning null).
	 */
	public Boolean getSatellite() {
		return this.replicationFactor.getSatellite();
	}

	public String getSharding() {
		return sharding;
	}

	/**
	 * @param name
	 *            Has to contain a valid database name
	 * @return options
	 */
	public DBCreateOptions name(final String name) {
		this.name = name;
		return this;
	}

	public DBCreateOptions replicationFactor(final Integer replicationFactor) {
		this.replicationFactor.setReplicationFactor(replicationFactor);
		return this;
	}

	public DBCreateOptions minReplicationFactor(final Integer minReplicationFactor) {
		this.minReplicationFactor.setMinReplicationFactor(minReplicationFactor);
		return this;
	}

	public DBCreateOptions satellite(final Boolean satellite) {
		this.replicationFactor.setSatellite(satellite);
		return this;
	}

	/**
	 * TODO
	 *
	 * @param sharding
	 * @return TODO
	 * @since ArangoDB 3.6.0
	 */
	public DBCreateOptions sharding(String sharding) {
		this.sharding = sharding;
		return this;
	}

}
