package com.arangodb.vst;

import com.arangodb.Protocol;
import com.arangodb.internal.config.ArangoConfig;
import com.arangodb.internal.net.*;
import com.arangodb.vst.async.VstCommunicationAsync;
import com.arangodb.vst.async.VstConnectionFactoryAsync;
import com.fasterxml.jackson.databind.Module;

public class VstAsyncProtocolProvider implements AsyncProtocolProvider {
    @Override
    public boolean supportsProtocol(Protocol protocol) {
        return Protocol.VST.equals(protocol);
    }

    @Override
    public ConnectionFactory createConnectionFactory() {
        return new VstConnectionFactoryAsync();
    }

    @Override
    public AsyncCommunication createCommunication(final ArangoConfig config, final HostHandler hostHandler) {
        return new VstCommunicationAsync.Builder().config(config).hostHandler(hostHandler).build();
    }

    @Override
    public Module protocolModule() {
        return VstModule.INSTANCE.get();
    }
}
