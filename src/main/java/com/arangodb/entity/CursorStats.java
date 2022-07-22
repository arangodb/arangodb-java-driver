package com.arangodb.entity;

public final class CursorStats {
    private Long writesExecuted;
    private Long writesIgnored;
    private Long scannedFull;
    private Long scannedIndex;
    private Long filtered;
    private Long fullCount;
    private Double executionTime;
    private Long peakMemoryUsage;

    public Long getWritesExecuted() {
        return writesExecuted;
    }

    public Long getWritesIgnored() {
        return writesIgnored;
    }

    public Long getScannedFull() {
        return scannedFull;
    }

    public Long getScannedIndex() {
        return scannedIndex;
    }

    public Long getFiltered() {
        return filtered;
    }

    public Long getFullCount() {
        return fullCount;
    }

    public Double getExecutionTime() {
        return executionTime;
    }

    public Long getPeakMemoryUsage() {
        return peakMemoryUsage;
    }
}
