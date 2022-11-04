package com.arangodb.config;

public enum ConfigPropertyKey {
    HOSTS("hosts"),
    TIMEOUT("timeout"),
    USER("user"),
    PASSWORD("password"),
    JWT("jwt"),
    USE_SSL("ssl"),
    VERIFY_HOST("verifyHost"),
    VST_CHUNK_SIZE("chunkSize"),
    MAX_CONNECTIONS("connections.max"),
    CONNECTION_TTL("connections.ttl"),
    KEEP_ALIVE_INTERVAL("connections.keepAlive.interval"),
    ACQUIRE_HOST_LIST("acquireHostList"),
    ACQUIRE_HOST_LIST_INTERVAL("acquireHostList.interval"),
    LOAD_BALANCING_STRATEGY("loadBalancingStrategy"),
    RESPONSE_QUEUE_TIME_SAMPLES("metrics.responseQueueTimeSamples"),
    PROTOCOL("protocol");

    private final String value;

    ConfigPropertyKey(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}
