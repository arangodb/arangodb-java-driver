package com.arangodb.config;

import java.util.Objects;

public class Host {
    private final String name;
    private final int port;

    /**
     * Factory method used by MicroProfile Config as
     * <a href="https://download.eclipse.org/microprofile/microprofile-config-3.0.2/microprofile-config-spec-3.0.2.html#_automatic_converters">automatic converter</a>.
     *
     * @param value hostname:port
     * @return Host
     */
    public static Host parse(CharSequence value) {
        Objects.requireNonNull(value);
        final String[] split = value.toString().split(":");
        if (split.length != 2) {
            throw new IllegalArgumentException("Could not parse host. Expected hostname:port, but got: " + value);
        }
        return new Host(split[0], Integer.parseInt(split[1]));
    }

    public Host(String name, int port) {
        this.name = name;
        this.port = port;
    }

    public String getName() {
        return name;
    }

    public int getPort() {
        return port;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Host host = (Host) o;
        return port == host.port && Objects.equals(name, host.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, port);
    }

    @Override
    public String toString() {
        return "Host{" +
                "name='" + name + '\'' +
                ", port=" + port +
                '}';
    }
}
