package com.arangodb.internal.net;


import com.arangodb.internal.config.ArangoConfig;

public interface AsyncProtocolProvider extends ProtocolProvider {

    AsyncCommunication createAsyncCommunication(ArangoConfig config, HostHandler hostHandler);

}
