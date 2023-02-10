package com.arangodb.internal.net;


import com.arangodb.Protocol;
import com.arangodb.internal.serde.InternalSerde;

import javax.net.ssl.SSLContext;

public interface ProtocolProvider {

    boolean supportsProtocol(Protocol protocol);

    ConnectionFactory createConnectionFactory(Integer timeout, String user, String password, Boolean useSsl,
                                              SSLContext sslContext, Boolean verifyHost,
                                              InternalSerde util, Protocol protocol, Long connectionTtl);

    CommunicationProtocol createProtocol(HostHandler hostHandler, InternalSerde serde);

}
