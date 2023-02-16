package com.arangodb.internal.net;


import com.arangodb.Protocol;
import com.arangodb.internal.config.ArangoConfig;
import com.fasterxml.jackson.databind.Module;

public interface ProtocolProvider {

    boolean supportsProtocol(Protocol protocol);

    ConnectionFactory createConnectionFactory();

    CommunicationProtocol createProtocol(ArangoConfig config, HostHandler hostHandler);

    Module protocolModule();
}
