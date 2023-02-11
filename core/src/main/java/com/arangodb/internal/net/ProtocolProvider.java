package com.arangodb.internal.net;


import com.arangodb.Protocol;
import com.arangodb.internal.config.ArangoConfig;

public interface ProtocolProvider {

    boolean supportsProtocol(Protocol protocol);

    ConnectionFactory createConnectionFactory(ArangoConfig config);

    CommunicationProtocol createProtocol(ArangoConfig config, HostHandler hostHandler);

}
