package com.arangodb.internal.net;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class HostSet {
    private static final Logger LOGGER = LoggerFactory.getLogger(HostSet.class);

    private final ArrayList<Host> hosts = new ArrayList<>();
    private volatile String jwt = null;

    public HostSet() {
        super();
    }

    public HostSet(List<Host> hosts) {
        super();

        for (Host host : hosts) {
            addHost(host);
        }

    }

    public List<Host> getHostsList() {
        return Collections.unmodifiableList(hosts);
    }

    public void addHost(Host newHost) {
        if (hosts.contains(newHost)) {
            LOGGER.debug("Host" + newHost + " already in Set");
            for (Host host : hosts) {
                if (host.equals(newHost)) {
                    host.setMarkforDeletion(false);
                }
            }
        } else {
            newHost.setJwt(jwt);
            hosts.add(newHost);
            LOGGER.debug("Added Host " + newHost + " - now " + hosts.size() + " Hosts in List");
        }
    }

    public void close() {
        LOGGER.debug("Close all Hosts in Set");

        for (Host host : hosts) {
            try {

                LOGGER.debug("Try to close Host " + host);
                host.close();

            } catch (IOException e) {
                LOGGER.warn("Error during closing the Host " + host, e);
            }
        }
    }

    public void markAllForDeletion() {

        for (Host host : hosts) {
            host.setMarkforDeletion(true);
        }

    }

    public void clearAllMarkedForDeletion() {

        LOGGER.debug("Clear all Hosts in Set with markForDeletion");

        Iterator<Host> iterable = hosts.iterator();
        while (iterable.hasNext()) {
            Host host = iterable.next();
            if (host.isMarkforDeletion()) {
                try {
                    LOGGER.debug("Try to close Host " + host);
                    host.close();
                } catch (IOException e) {
                    LOGGER.warn("Error during closing the Host " + host, e);
                } finally {
                    iterable.remove();
                }
            }
        }

    }

    public void clear() {
        LOGGER.debug("Clear all Hosts in Set");

        close();
        hosts.clear();
    }

    public void setJwt(String jwt) {
        this.jwt = jwt;
        for (Host h : hosts) {
            h.setJwt(jwt);
        }
    }

}
