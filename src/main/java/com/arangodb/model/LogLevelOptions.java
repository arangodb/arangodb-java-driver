package com.arangodb.model;

public class LogLevelOptions {
    private String serverId;

    public String getServerId() {
        return serverId;
    }

    public LogLevelOptions serverId(final String serverId) {
        this.serverId = serverId;
        return this;
    }
}
