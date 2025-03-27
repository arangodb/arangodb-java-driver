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

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * @author Mark Vollmary
 */
public final class LogLevelEntity {

    private LogLevel all;
    private LogLevel agency;
    private LogLevel agencycomm;
    private LogLevel agencystore;
    private LogLevel backup;
    private LogLevel bench;
    private LogLevel cluster;
    private LogLevel communication;
    private LogLevel authentication;
    private LogLevel config;
    private LogLevel crash;
    private LogLevel dump;
    private LogLevel engines;
    private LogLevel cache;
    private LogLevel security;
    private LogLevel startup;
    private LogLevel trx;
    private LogLevel supervision;
    private LogLevel threads;
    private LogLevel ttl;
    private LogLevel ssl;
    private LogLevel replication2;
    private LogLevel restore;
    private LogLevel memory;
    private LogLevel validation;
    private LogLevel statistics;
    private LogLevel v8;
    private LogLevel syscall;
    private LogLevel libiresearch;
    private LogLevel license;
    private LogLevel deprecation;
    private LogLevel rocksdb;
    private LogLevel requests;
    @JsonProperty("rep-wal")
    private LogLevel repWal;
    private LogLevel arangosearch;
    private LogLevel views;
    @JsonProperty("rep-state")
    private LogLevel repState;
    private LogLevel authorization;
    private LogLevel queries;
    private LogLevel aql;
    private LogLevel graphs;
    private LogLevel maintenance;
    private LogLevel development;
    private LogLevel replication;
    private LogLevel httpclient;
    private LogLevel heartbeat;
    private LogLevel flush;
    private LogLevel general;

    public LogLevelEntity() {
        super();
    }

    public LogLevel getAll() {
        return all;
    }

    public void setAll(LogLevel all) {
        this.all = all;
    }

    public LogLevel getAgency() {
        return agency;
    }

    public void setAgency(LogLevel agency) {
        this.agency = agency;
    }

    public LogLevel getAgencycomm() {
        return agencycomm;
    }

    public void setAgencycomm(LogLevel agencycomm) {
        this.agencycomm = agencycomm;
    }

    public LogLevel getAgencystore() {
        return agencystore;
    }

    public void setAgencystore(LogLevel agencystore) {
        this.agencystore = agencystore;
    }

    public LogLevel getBackup() {
        return backup;
    }

    public void setBackup(LogLevel backup) {
        this.backup = backup;
    }

    public LogLevel getBench() {
        return bench;
    }

    public void setBench(LogLevel bench) {
        this.bench = bench;
    }

    public LogLevel getCluster() {
        return cluster;
    }

    public void setCluster(LogLevel cluster) {
        this.cluster = cluster;
    }

    public LogLevel getCommunication() {
        return communication;
    }

    public void setCommunication(LogLevel communication) {
        this.communication = communication;
    }

    public LogLevel getAuthentication() {
        return authentication;
    }

    public void setAuthentication(LogLevel authentication) {
        this.authentication = authentication;
    }

    public LogLevel getConfig() {
        return config;
    }

    public void setConfig(LogLevel config) {
        this.config = config;
    }

    public LogLevel getCrash() {
        return crash;
    }

    public void setCrash(LogLevel crash) {
        this.crash = crash;
    }

    public LogLevel getDump() {
        return dump;
    }

    public void setDump(LogLevel dump) {
        this.dump = dump;
    }

    public LogLevel getEngines() {
        return engines;
    }

    public void setEngines(LogLevel engines) {
        this.engines = engines;
    }

    public LogLevel getCache() {
        return cache;
    }

    public void setCache(LogLevel cache) {
        this.cache = cache;
    }

    public LogLevel getSecurity() {
        return security;
    }

    public void setSecurity(LogLevel security) {
        this.security = security;
    }

    public LogLevel getStartup() {
        return startup;
    }

    public void setStartup(LogLevel startup) {
        this.startup = startup;
    }

    public LogLevel getTrx() {
        return trx;
    }

    public void setTrx(LogLevel trx) {
        this.trx = trx;
    }

    public LogLevel getSupervision() {
        return supervision;
    }

    public void setSupervision(LogLevel supervision) {
        this.supervision = supervision;
    }

    public LogLevel getThreads() {
        return threads;
    }

    public void setThreads(LogLevel threads) {
        this.threads = threads;
    }

    public LogLevel getTtl() {
        return ttl;
    }

    public void setTtl(LogLevel ttl) {
        this.ttl = ttl;
    }

    public LogLevel getSsl() {
        return ssl;
    }

    public void setSsl(LogLevel ssl) {
        this.ssl = ssl;
    }

    public LogLevel getReplication2() {
        return replication2;
    }

    public void setReplication2(LogLevel replication2) {
        this.replication2 = replication2;
    }

    public LogLevel getRestore() {
        return restore;
    }

    public void setRestore(LogLevel restore) {
        this.restore = restore;
    }

    public LogLevel getMemory() {
        return memory;
    }

    public void setMemory(LogLevel memory) {
        this.memory = memory;
    }

    public LogLevel getValidation() {
        return validation;
    }

    public void setValidation(LogLevel validation) {
        this.validation = validation;
    }

    public LogLevel getStatistics() {
        return statistics;
    }

    public void setStatistics(LogLevel statistics) {
        this.statistics = statistics;
    }

    public LogLevel getV8() {
        return v8;
    }

    public void setV8(LogLevel v8) {
        this.v8 = v8;
    }

    public LogLevel getSyscall() {
        return syscall;
    }

    public void setSyscall(LogLevel syscall) {
        this.syscall = syscall;
    }

    public LogLevel getLibiresearch() {
        return libiresearch;
    }

    public void setLibiresearch(LogLevel libiresearch) {
        this.libiresearch = libiresearch;
    }

    public LogLevel getLicense() {
        return license;
    }

    public void setLicense(LogLevel license) {
        this.license = license;
    }

    public LogLevel getDeprecation() {
        return deprecation;
    }

    public void setDeprecation(LogLevel deprecation) {
        this.deprecation = deprecation;
    }

    public LogLevel getRocksdb() {
        return rocksdb;
    }

    public void setRocksdb(LogLevel rocksdb) {
        this.rocksdb = rocksdb;
    }

    public LogLevel getRequests() {
        return requests;
    }

    public void setRequests(LogLevel requests) {
        this.requests = requests;
    }

    public LogLevel getRepWal() {
        return repWal;
    }

    public void setRepWal(LogLevel repWal) {
        this.repWal = repWal;
    }

    public LogLevel getArangosearch() {
        return arangosearch;
    }

    public void setArangosearch(LogLevel arangosearch) {
        this.arangosearch = arangosearch;
    }

    public LogLevel getViews() {
        return views;
    }

    public void setViews(LogLevel views) {
        this.views = views;
    }

    public LogLevel getRepState() {
        return repState;
    }

    public void setRepState(LogLevel repState) {
        this.repState = repState;
    }

    public LogLevel getAuthorization() {
        return authorization;
    }

    public void setAuthorization(LogLevel authorization) {
        this.authorization = authorization;
    }

    public LogLevel getQueries() {
        return queries;
    }

    public void setQueries(LogLevel queries) {
        this.queries = queries;
    }

    public LogLevel getAql() {
        return aql;
    }

    public void setAql(LogLevel aql) {
        this.aql = aql;
    }

    public LogLevel getGraphs() {
        return graphs;
    }

    public void setGraphs(LogLevel graphs) {
        this.graphs = graphs;
    }

    public LogLevel getMaintenance() {
        return maintenance;
    }

    public void setMaintenance(LogLevel maintenance) {
        this.maintenance = maintenance;
    }

    public LogLevel getDevelopment() {
        return development;
    }

    public void setDevelopment(LogLevel development) {
        this.development = development;
    }

    public LogLevel getReplication() {
        return replication;
    }

    public void setReplication(LogLevel replication) {
        this.replication = replication;
    }

    public LogLevel getHttpclient() {
        return httpclient;
    }

    public void setHttpclient(LogLevel httpclient) {
        this.httpclient = httpclient;
    }

    public LogLevel getHeartbeat() {
        return heartbeat;
    }

    public void setHeartbeat(LogLevel heartbeat) {
        this.heartbeat = heartbeat;
    }

    public LogLevel getFlush() {
        return flush;
    }

    public void setFlush(LogLevel flush) {
        this.flush = flush;
    }

    public LogLevel getGeneral() {
        return general;
    }

    public void setGeneral(LogLevel general) {
        this.general = general;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof LogLevelEntity)) return false;
        LogLevelEntity that = (LogLevelEntity) o;
        return all == that.all && agency == that.agency && agencycomm == that.agencycomm && agencystore == that.agencystore && backup == that.backup && bench == that.bench && cluster == that.cluster && communication == that.communication && authentication == that.authentication && config == that.config && crash == that.crash && dump == that.dump && engines == that.engines && cache == that.cache && security == that.security && startup == that.startup && trx == that.trx && supervision == that.supervision && threads == that.threads && ttl == that.ttl && ssl == that.ssl && replication2 == that.replication2 && restore == that.restore && memory == that.memory && validation == that.validation && statistics == that.statistics && v8 == that.v8 && syscall == that.syscall && libiresearch == that.libiresearch && license == that.license && deprecation == that.deprecation && rocksdb == that.rocksdb && requests == that.requests && repWal == that.repWal && arangosearch == that.arangosearch && views == that.views && repState == that.repState && authorization == that.authorization && queries == that.queries && aql == that.aql && graphs == that.graphs && maintenance == that.maintenance && development == that.development && replication == that.replication && httpclient == that.httpclient && heartbeat == that.heartbeat && flush == that.flush && general == that.general;
    }

    @Override
    public int hashCode() {
        return Objects.hash(all, agency, agencycomm, agencystore, backup, bench, cluster, communication, authentication, config, crash, dump, engines, cache, security, startup, trx, supervision, threads, ttl, ssl, replication2, restore, memory, validation, statistics, v8, syscall, libiresearch, license, deprecation, rocksdb, requests, repWal, arangosearch, views, repState, authorization, queries, aql, graphs, maintenance, development, replication, httpclient, heartbeat, flush, general);
    }

    public enum LogLevel {
        FATAL, ERROR, WARNING, INFO, DEBUG, TRACE, DEFAULT
    }

}
