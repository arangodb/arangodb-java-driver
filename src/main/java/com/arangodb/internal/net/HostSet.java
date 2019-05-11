package com.arangodb.internal.net;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HostSet {
	private static final Logger LOGGER = LoggerFactory.getLogger(HostSet.class);

	private ArrayList<Host> hosts = new ArrayList<Host>();

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
		if(hosts.contains(newHost)) {
			LOGGER.debug("Host" + newHost + " allready in Set");
		} else {
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
	
	public void clear() {
		LOGGER.debug("Clear all Hosts in Set");
		
		close();
		hosts.clear();
	}
}
