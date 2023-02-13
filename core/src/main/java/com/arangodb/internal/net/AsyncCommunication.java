package com.arangodb.internal.net;

import com.arangodb.internal.InternalRequest;
import com.arangodb.internal.InternalResponse;

import java.io.Closeable;
import java.util.concurrent.CompletableFuture;

public interface AsyncCommunication extends Closeable {

    CompletableFuture<InternalResponse> execute(final InternalRequest request, final HostHandle hostHandle);
    void setJwt(String jwt);

}
