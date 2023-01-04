package com.arangodb.config;

import java.util.Objects;

public class Host {
    private final String name;
    private final int port;

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
