package com.arangodb.internal.velocystream;

import com.arangodb.async.internal.velocystream.VstCommunicationAsync;
import com.arangodb.internal.config.ArangoConfig;
import com.arangodb.internal.net.AsyncCommunication;
import com.arangodb.internal.net.AsyncProtocolProvider;
import com.arangodb.internal.net.HostHandler;

public class VstAsyncProtocolProvider extends VstProtocolProvider implements AsyncProtocolProvider {
    @Override
    public AsyncCommunication createAsyncCommunication(final ArangoConfig config, final HostHandler hostHandler) {
        return new VstCommunicationAsync.Builder().config(config).hostHandler(hostHandler).build();
    }
}
