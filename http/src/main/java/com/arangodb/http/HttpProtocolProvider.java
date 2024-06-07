package com.arangodb.http;

import com.arangodb.Protocol;
import com.arangodb.arch.UnstableApi;
import com.arangodb.config.ProtocolConfig;
import com.arangodb.internal.config.ArangoConfig;
import com.arangodb.internal.net.CommunicationProtocol;
import com.arangodb.internal.net.ConnectionFactory;
import com.arangodb.internal.net.HostHandler;
import com.arangodb.internal.net.ProtocolProvider;
import com.fasterxml.jackson.databind.Module;

@UnstableApi
public class HttpProtocolProvider implements ProtocolProvider {

    @Override
    public boolean supportsProtocol(Protocol protocol) {
        return Protocol.HTTP_VPACK.equals(protocol) ||
                Protocol.HTTP_JSON.equals(protocol) ||
                Protocol.HTTP2_VPACK.equals(protocol) ||
                Protocol.HTTP2_JSON.equals(protocol);
    }

    @Override
    @UnstableApi
    public ConnectionFactory createConnectionFactory(@UnstableApi ProtocolConfig config) {
        return new HttpConnectionFactory((HttpProtocolConfig) config);
    }

    @Override
    @UnstableApi
    public CommunicationProtocol createProtocol(@UnstableApi ArangoConfig config, @UnstableApi HostHandler hostHandler) {
        return new HttpProtocol(new HttpCommunication(config, hostHandler));
    }

    @Override
    public Module protocolModule() {
        return null;
    }

}
