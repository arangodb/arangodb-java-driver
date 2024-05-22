package com.arangodb.internal.net;


import com.arangodb.Protocol;
import com.arangodb.arch.UsedInApi;
import com.arangodb.internal.config.ArangoConfig;
import com.fasterxml.jackson.databind.Module;

@UsedInApi
public interface ProtocolProvider {

    boolean supportsProtocol(Protocol protocol);

    /**
     * @deprecated use {@link #createConnectionFactory(ArangoConfig)} instead
     */
    @Deprecated
    default ConnectionFactory createConnectionFactory() {
        throw new UnsupportedOperationException();
    }

    default ConnectionFactory createConnectionFactory(ArangoConfig config) {
        return createConnectionFactory();
    }

    CommunicationProtocol createProtocol(ArangoConfig config, HostHandler hostHandler);

    Module protocolModule();
}
