package com.arangodb.http;

import com.arangodb.Protocol;
import com.arangodb.internal.net.CommunicationProtocol;
import com.arangodb.internal.net.ConnectionFactory;
import com.arangodb.internal.net.HostHandler;
import com.arangodb.internal.net.ProtocolProvider;
import com.arangodb.internal.serde.InternalSerde;

import javax.net.ssl.SSLContext;

public class HttpProtocolProvider implements ProtocolProvider {

    @Override
    public boolean supportsProtocol(Protocol protocol) {
        return Protocol.HTTP_VPACK.equals(protocol) ||
                Protocol.HTTP_JSON.equals(protocol) ||
                Protocol.HTTP2_VPACK.equals(protocol) ||
                Protocol.HTTP2_JSON.equals(protocol);
    }

    @Override
    public ConnectionFactory createConnectionFactory(
            final Integer timeout,
            final String user,
            final String password,
            final Boolean useSsl,
            final SSLContext sslContext,
            final Boolean verifyHost,
            final InternalSerde util,
            final Protocol protocol,
            final Long connectionTtl
    ) {
        return new HttpConnectionFactory(timeout, user, password, useSsl, sslContext, verifyHost, util, protocol, connectionTtl);
    }

    @Override
    public CommunicationProtocol createProtocol(final HostHandler hostHandler, final InternalSerde serde) {
        return new HttpProtocol(new HttpCommunication(hostHandler, serde));
    }
}
