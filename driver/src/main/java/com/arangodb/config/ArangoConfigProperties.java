package com.arangodb.config;

import org.eclipse.microprofile.config.inject.ConfigProperties;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@ConfigProperties(prefix = "arangodb")
public final class ArangoConfigProperties {

    private List<Host> hosts = new ArrayList<>();

    public ArangoConfigProperties host(final Host... host) {
        Collections.addAll(hosts, host);
        return this;
    }

    public List<Host> getHosts() {
        return hosts;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ArangoConfigProperties that = (ArangoConfigProperties) o;
        return Objects.equals(hosts, that.hosts);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hosts);
    }

    @Override
    public String toString() {
        return "ArangoConfigProperties{" +
                "hosts=" + hosts +
                '}';
    }
}
