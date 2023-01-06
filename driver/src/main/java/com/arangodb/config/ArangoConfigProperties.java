package com.arangodb.config;

import com.arangodb.Protocol;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public final class ArangoConfigProperties {

//    public static final Boolean DEFAULT_VERIFY_HOST = true;
//    public static final int MAX_CONNECTIONS_HTTP_DEFAULT = 20;
//    public static final int MAX_CONNECTIONS_HTTP2_DEFAULT = 1;
//    public static final boolean DEFAULT_ACQUIRE_HOST_LIST = false;
//    public static final int DEFAULT_ACQUIRE_HOST_LIST_INTERVAL = 60 * 60 * 1000; // hour
//    public static final LoadBalancingStrategy DEFAULT_LOAD_BALANCING_STRATEGY = LoadBalancingStrategy.NONE;
//    public static final int DEFAULT_RESPONSE_QUEUE_TIME_SAMPLES = 10;

    // default values
    public static final Protocol DEFAULT_PROTOCOL = Protocol.HTTP2_JSON;
    public static final String DEFAULT_USER = "root";
    public static final Integer DEFAULT_TIMEOUT = 0;
    public static final Boolean DEFAULT_USE_SSL = false;

    // VST default values

    private static final int INTEGER_BYTES = Integer.SIZE / Byte.SIZE;
    private static final int LONG_BYTES = Long.SIZE / Byte.SIZE;
    //    public static final int CHUNK_MIN_HEADER_SIZE = INTEGER_BYTES + INTEGER_BYTES + LONG_BYTES;
    //    public static final int CHUNK_MAX_HEADER_SIZE = CHUNK_MIN_HEADER_SIZE + LONG_BYTES;
    //    public static final int MAX_CONNECTIONS_VST_DEFAULT = 1;
    //    public static final Long CONNECTION_TTL_VST_DEFAULT = null;
    public static final int DEFAULT_CHUNK_SIZE = 30_000;

    private Optional<List<Host>> hosts;
    private Optional<Protocol> protocol;
    private Optional<String> user;
    private Optional<String> password;
    private Optional<String> jwt;
    private Optional<Integer> timeout;
    private Optional<Boolean> useSsl;
    private Optional<Integer> vstChunkSize;


    public ArangoConfigProperties host(final Host... host) {
        if (hosts == null || !hosts.isPresent()) {
            hosts = Optional.of(new ArrayList<>());
        }
        Collections.addAll(hosts.get(), host);
        return this;
    }

    public ArangoConfigProperties protocol(final Protocol protocol) {
        this.protocol = Optional.of(protocol);
        return this;
    }

    public ArangoConfigProperties user(final String user) {
        this.user = Optional.of(user);
        return this;
    }

    public ArangoConfigProperties password(final String password) {
        this.password = Optional.of(password);
        return this;
    }

    public ArangoConfigProperties jwt(final String jwt) {
        this.jwt = Optional.of(jwt);
        return this;
    }

    public ArangoConfigProperties timeout(final Integer timeout) {
        this.timeout = Optional.of(timeout);
        return this;
    }

    public ArangoConfigProperties useSsl(final Boolean useSsl) {
        this.useSsl = Optional.of(useSsl);
        return this;
    }

    public ArangoConfigProperties vstChunkSize(final Integer vstChunkSize) {
        this.vstChunkSize = Optional.of(vstChunkSize);
        return this;
    }

    public List<Host> getHosts() {
        return resolve(hosts, Collections.emptyList());
    }

    public Protocol getProtocol() {
        return resolve(protocol, DEFAULT_PROTOCOL);
    }

    public String getUser() {
        return resolve(user, DEFAULT_USER);
    }

    public Optional<String> getPassword() {
        return password == null ? Optional.empty() : password;
    }

    public Optional<String> getJwt() {
        return jwt == null ? Optional.empty() : jwt;
    }

    public Integer getTimeout() {
        return resolve(timeout, DEFAULT_TIMEOUT);
    }

    public Boolean getUseSsl() {
        return resolve(useSsl, DEFAULT_USE_SSL);
    }

    public Integer getVstChunkSize() {
        return resolve(vstChunkSize, DEFAULT_CHUNK_SIZE);
    }

    private <T> T resolve(Optional<T> field, T defValue) {
        return field == null ? defValue : field.orElse(defValue);
    }
}
