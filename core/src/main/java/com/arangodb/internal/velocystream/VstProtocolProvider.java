package com.arangodb.internal.velocystream;

import com.arangodb.Protocol;
import com.arangodb.internal.config.ArangoConfig;
import com.arangodb.internal.net.CommunicationProtocol;
import com.arangodb.internal.net.ConnectionFactory;
import com.arangodb.internal.net.HostHandler;
import com.arangodb.internal.net.ProtocolProvider;

public class VstProtocolProvider implements ProtocolProvider {
    @Override
    public boolean supportsProtocol(Protocol protocol) {
        return Protocol.VST.equals(protocol);
    }

    @Override
    public ConnectionFactory createConnectionFactory(ArangoConfig config) {
        return new VstConnectionFactorySync(config);
    }

    @Override
    public CommunicationProtocol createProtocol(ArangoConfig config, HostHandler hostHandler) {
        return new VstProtocol(new VstCommunicationSync.Builder().config(config).hostHandler(hostHandler).build());
    }
}
