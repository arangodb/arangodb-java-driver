package com.arangodb.config;

import com.arangodb.Protocol;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public final class ArangoConfigProperties {

//    public static final int MAX_CONNECTIONS_HTTP_DEFAULT = 20;
//    public static final int MAX_CONNECTIONS_HTTP2_DEFAULT = 1;
//    public static final int DEFAULT_ACQUIRE_HOST_LIST_INTERVAL = 60 * 60 * 1000; // hour
//    public static final LoadBalancingStrategy DEFAULT_LOAD_BALANCING_STRATEGY = LoadBalancingStrategy.NONE;
//    public static final int DEFAULT_RESPONSE_QUEUE_TIME_SAMPLES = 10;

    // default values
    public static final Protocol DEFAULT_PROTOCOL = Protocol.HTTP2_JSON;
    public static final String DEFAULT_USER = "root";
    public static final Integer DEFAULT_TIMEOUT = 0;
    public static final Boolean DEFAULT_USE_SSL = false;
    public static final Boolean DEFAULT_VERIFY_HOST = true;
    public static final Boolean DEFAULT_ACQUIRE_HOST_LIST = false;

    // VST default values

    private static final int INTEGER_BYTES = Integer.SIZE / Byte.SIZE;
    private static final int LONG_BYTES = Long.SIZE / Byte.SIZE;
    //    public static final int CHUNK_MIN_HEADER_SIZE = INTEGER_BYTES + INTEGER_BYTES + LONG_BYTES;
    //    public static final int CHUNK_MAX_HEADER_SIZE = CHUNK_MIN_HEADER_SIZE + LONG_BYTES;
    //    public static final int MAX_CONNECTIONS_VST_DEFAULT = 1;
    public static final int DEFAULT_CHUNK_SIZE = 30_000;

    private Optional<List<Host>> hosts;
    private Optional<Protocol> protocol;
    private Optional<String> user;
    private Optional<String> password;
    private Optional<String> jwt;
    private Optional<Integer> timeout;
    private Optional<Boolean> useSsl;
    private Optional<Boolean> verifyHost;
    private Optional<Integer> vstChunkSize;
    private Optional<Integer> maxConnections;
    private Optional<Long> connectionTtl;
    private Optional<Integer> keepAliveInterval;
    private Optional<Boolean> acquireHostList;

    public List<Host> getHosts() {
        return hosts.orElse(Collections.emptyList());
    }

    public Protocol getProtocol() {
        return protocol.orElse(DEFAULT_PROTOCOL);
    }

    public String getUser() {
        return user.orElse(DEFAULT_USER);
    }

    public Optional<String> getPassword() {
        return password;
    }

    public Optional<String> getJwt() {
        return jwt;
    }

    public Integer getTimeout() {
        return timeout.orElse(DEFAULT_TIMEOUT);
    }

    public Boolean getUseSsl() {
        return useSsl.orElse(DEFAULT_USE_SSL);
    }

    public Boolean getVerifyHost() {
        return verifyHost.orElse(DEFAULT_VERIFY_HOST);
    }

    public Integer getVstChunkSize() {
        return vstChunkSize.orElse(DEFAULT_CHUNK_SIZE);
    }

    public Optional<Integer> getMaxConnections() {
        return maxConnections;
    }

    public Optional<Long> getConnectionTtl() {
        return connectionTtl;
    }

    public Optional<Integer> getKeepAliveInterval() {
        return keepAliveInterval;
    }

    public Boolean getAcquireHostList() {
        return acquireHostList.orElse(DEFAULT_ACQUIRE_HOST_LIST);
    }

}
