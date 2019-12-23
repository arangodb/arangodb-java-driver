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

/**
 * @author Mark Vollmary
 */
public class LogLevelEntity implements Entity {

    public enum LogLevel {
        FATAL, ERROR, WARNING, INFO, DEBUG, TRACE, DEFAULT
    }

    private LogLevel agency;
    private LogLevel agencycomm;
    private LogLevel cluster;
    private LogLevel collector;
    private LogLevel communication;
    private LogLevel compactor;
    private LogLevel config;
    private LogLevel datafiles;
    private LogLevel graphs;
    private LogLevel heartbeat;
    private LogLevel mmap;
    private LogLevel performance;
    private LogLevel queries;
    private LogLevel replication;
    private LogLevel requests;
    private LogLevel startup;
    private LogLevel threads;
    private LogLevel v8;

    public LogLevelEntity() {
        super();
    }

    public LogLevel getAgency() {
        return agency;
    }

    public void setAgency(final LogLevel agency) {
        this.agency = agency;
    }

    public LogLevel getAgencycomm() {
        return agencycomm;
    }

    public void setAgencycomm(final LogLevel agencycomm) {
        this.agencycomm = agencycomm;
    }

    public LogLevel getCluster() {
        return cluster;
    }

    public void setCluster(final LogLevel cluster) {
        this.cluster = cluster;
    }

    public LogLevel getCollector() {
        return collector;
    }

    public void setCollector(final LogLevel collector) {
        this.collector = collector;
    }

    public LogLevel getCommunication() {
        return communication;
    }

    public void setCommunication(final LogLevel communication) {
        this.communication = communication;
    }

    public LogLevel getCompactor() {
        return compactor;
    }

    public void setCompactor(final LogLevel compactor) {
        this.compactor = compactor;
    }

    public LogLevel getConfig() {
        return config;
    }

    public void setConfig(final LogLevel config) {
        this.config = config;
    }

    public LogLevel getDatafiles() {
        return datafiles;
    }

    public void setDatafiles(final LogLevel datafiles) {
        this.datafiles = datafiles;
    }

    public LogLevel getGraphs() {
        return graphs;
    }

    public void setGraphs(final LogLevel graphs) {
        this.graphs = graphs;
    }

    public LogLevel getHeartbeat() {
        return heartbeat;
    }

    public void setHeartbeat(final LogLevel heartbeat) {
        this.heartbeat = heartbeat;
    }

    public LogLevel getMmap() {
        return mmap;
    }

    public void setMmap(final LogLevel mmap) {
        this.mmap = mmap;
    }

    public LogLevel getPerformance() {
        return performance;
    }

    public void setPerformance(final LogLevel performance) {
        this.performance = performance;
    }

    public LogLevel getQueries() {
        return queries;
    }

    public void setQueries(final LogLevel queries) {
        this.queries = queries;
    }

    public LogLevel getReplication() {
        return replication;
    }

    public void setReplication(final LogLevel replication) {
        this.replication = replication;
    }

    public LogLevel getRequests() {
        return requests;
    }

    public void setRequests(final LogLevel requests) {
        this.requests = requests;
    }

    public LogLevel getStartup() {
        return startup;
    }

    public void setStartup(final LogLevel startup) {
        this.startup = startup;
    }

    public LogLevel getThreads() {
        return threads;
    }

    public void setThreads(final LogLevel threads) {
        this.threads = threads;
    }

    public LogLevel getV8() {
        return v8;
    }

    public void setV8(final LogLevel v8) {
        this.v8 = v8;
    }

}
