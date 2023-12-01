package com.arangodb.vst;

import com.arangodb.Protocol;
import com.arangodb.internal.config.ArangoConfig;
import com.arangodb.internal.net.CommunicationProtocol;
import com.arangodb.internal.net.ConnectionFactory;
import com.arangodb.internal.net.HostHandler;
import com.arangodb.internal.net.ProtocolProvider;
import com.fasterxml.jackson.databind.Module;

public class VstProtocolProvider implements ProtocolProvider {
    @Override
    public boolean supportsProtocol(Protocol protocol) {
        return Protocol.VST.equals(protocol);
    }

    @Override
    public ConnectionFactory createConnectionFactory() {
        return new VstConnectionFactoryAsync();
    }

    @Override
    public CommunicationProtocol createProtocol(ArangoConfig config, HostHandler hostHandler) {
        return new VstProtocol(new VstCommunication(config, hostHandler));
    }

    @Override
    public Module protocolModule() {
        return VstModule.INSTANCE.get();
    }

}
