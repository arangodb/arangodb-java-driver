package com.arangodb.internal.net;


import com.arangodb.Protocol;
import com.arangodb.arch.UsedInApi;
import com.arangodb.config.ProtocolConfig;
import com.arangodb.internal.config.ArangoConfig;
import com.fasterxml.jackson.databind.Module;

@UsedInApi
public interface ProtocolProvider {

    boolean supportsProtocol(Protocol protocol);

    default ConnectionFactory createConnectionFactory(ProtocolConfig config) {
        throw new UnsupportedOperationException();
    }

    CommunicationProtocol createProtocol(ArangoConfig config, HostHandler hostHandler);

    Module protocolModule();
}
