package com.arangodb.entity;

import com.fasterxml.jackson.annotation.JsonAnySetter;

import java.util.HashMap;
import java.util.Map;

public final class CursorStats {
    private final Map<String, Object> properties = new HashMap<>();
    private Long writesExecuted;
    private Long writesIgnored;
    private Long scannedFull;
    private Long scannedIndex;
    private Long cursorsCreated;
    private Long cursorsRearmed;
    private Long cacheHits;
    private Long cacheMisses;
    private Long filtered;
    private Long httpRequests;
    private Long fullCount;
    private Double executionTime;
    private Long peakMemoryUsage;
    private Integer documentLookups;
    private Integer intermediateCommits;
    private Integer seeks;

    @JsonAnySetter
    public void add(String key, Object value) {
        properties.put(key, value);
    }

    public Object get(String key) {
        return properties.get(key);
    }

    /**
     * @return The total number of data-modification operations successfully executed.
     */
    public Long getWritesExecuted() {
        return writesExecuted;
    }

    /**
     * @return The total number of data-modification operations that were unsuccessful, but have been ignored because of
     * the ignoreErrors query option.
     */
    public Long getWritesIgnored() {
        return writesIgnored;
    }

    /**
     * @return The total number of documents iterated over when scanning a collection without an index. Documents
     * scanned by subqueries are included in the result, but operations triggered by built-in or user-defined AQL
     * functions are not.
     */
    public Long getScannedFull() {
        return scannedFull;
    }

    /**
     * @return The total number of documents iterated over when scanning a collection using an index. Documents scanned
     * by subqueries are included in the result, but operations triggered by built-in or user-defined AQL functions are
     * not.
     */
    public Long getScannedIndex() {
        return scannedIndex;
    }

    /**
     * @return The total number of cursor objects created during query execution. Cursor objects are created for index
     * lookups.
     */
    public Long getCursorsCreated() {
        return cursorsCreated;
    }

    /**
     * @return The total number of times an existing cursor object was repurposed. Repurposing an existing cursor object
     * is normally more efficient compared to destroying an existing cursor object and creating a new one from scratch.
     */
    public Long getCursorsRearmed() {
        return cursorsRearmed;
    }

    /**
     * @return The total number of index entries read from in-memory caches for indexes of type edge or persistent. This
     * value is only non-zero when reading from indexes that have an in-memory cache enabled, and when the query allows
     * using the in-memory cache (i.e. using equality lookups on all index attributes).
     */
    public Long getCacheHits() {
        return cacheHits;
    }

    /**
     * @return The total number of cache read attempts for index entries that could not be served from in-memory caches
     * for indexes of type edge or persistent. This value is only non-zero when reading from indexes that have an
     * in-memory cache enabled, the query allows using the in-memory cache (i.e. using equality lookups on all index
     * attributes) and the looked up values are not present in the cache.
     */
    public Long getCacheMisses() {
        return cacheMisses;
    }

    /**
     * @return The total number of documents removed after executing a filter condition in a FilterNode or another node
     * that post-filters data. Note that nodes of the IndexNode type can also filter documents by selecting only the
     * required index range from a collection, and the filtered value only indicates how much filtering was done by a
     * post filter in the IndexNode itself or following FilterNode nodes. Nodes of the EnumerateCollectionNode and
     * TraversalNode types can also apply filter conditions and can report the number of filtered documents.
     */
    public Long getFiltered() {
        return filtered;
    }

    /**
     * @return The total number of cluster-internal HTTP requests performed.
     */
    public Long getHttpRequests() {
        return httpRequests;
    }

    /**
     * @return The total number of documents that matched the search condition if the query’s final top-level LIMIT
     * operation were not present. This attribute may only be returned if the fullCount option was set when starting the
     * query and only contains a sensible value if the query contains a LIMIT operation on the top level.
     */
    public Long getFullCount() {
        return fullCount;
    }

    /**
     * @return The query execution time (wall-clock time) in seconds.
     */
    public Double getExecutionTime() {
        return executionTime;
    }

    /**
     * @return The maximum memory usage of the query while it was running. In a cluster, the memory accounting is done
     * per shard, and the memory usage reported is the peak memory usage value from the individual shards. Note that to
     * keep things lightweight, the per-query memory usage is tracked on a relatively high level, not including any
     * memory allocator overhead nor any memory used for temporary results calculations (e.g. memory
     * allocated/deallocated inside AQL expressions and function calls).
     */
    public Long getPeakMemoryUsage() {
        return peakMemoryUsage;
    }

    public Integer getDocumentLookups() {
        return documentLookups;
    }

    /**
     * @return The total number of intermediate commits the query has performed. This number can only be greater than
     * zero for data-modification queries that perform modifications beyond the `--rocksdb.intermediate-commit-count`
     * or `--rocksdb.intermediate-commit-size` thresholds. In a cluster, the intermediate commits are tracked per
     * DB-Server that participates in the query and are summed up in the end.
     */
    public Integer getIntermediateCommits() {
        return intermediateCommits;
    }

    public Integer getSeeks() {
        return seeks;
    }
}
