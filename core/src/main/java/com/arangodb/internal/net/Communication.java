package com.arangodb.internal.net;

import com.arangodb.ArangoDBException;
import com.arangodb.config.HostDescription;
import com.arangodb.internal.InternalRequest;
import com.arangodb.internal.InternalResponse;
import com.arangodb.internal.RequestType;
import com.arangodb.internal.config.ArangoConfig;
import com.arangodb.internal.serde.InternalSerde;
import com.arangodb.internal.util.HostUtils;
import com.arangodb.internal.util.RequestUtils;
import com.arangodb.internal.util.ResponseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;

public abstract class Communication implements Closeable {
    private static final Logger LOGGER = LoggerFactory.getLogger(Communication.class);
    protected final HostHandler hostHandler;
    protected final InternalSerde serde;
    private final AtomicLong reqCount;


    protected Communication(final ArangoConfig config, final HostHandler hostHandler) {
        this.hostHandler = hostHandler;
        serde = config.getInternalSerde();
        reqCount = new AtomicLong();
    }

    protected abstract void connect(final Connection conn) throws IOException;

    @Override
    public void close() throws IOException {
        hostHandler.close();
    }

    public CompletableFuture<InternalResponse> executeAsync(final InternalRequest request, final HostHandle hostHandle) {
        return executeAsync(request, hostHandle, hostHandler.get(hostHandle, RequestUtils.determineAccessType(request)), 0);
    }

    private CompletableFuture<InternalResponse> executeAsync(final InternalRequest request, final HostHandle hostHandle, final Host host, final int attemptCount) {
        long reqId = reqCount.getAndIncrement();
        return doExecuteAsync(request, hostHandle, host, attemptCount, host.connection(), reqId);
    }

    private CompletableFuture<InternalResponse> doExecuteAsync(
            final InternalRequest request, final HostHandle hostHandle, final Host host, final int attemptCount, Connection connection, long reqId
    ) {
        if (LOGGER.isDebugEnabled()) {
            String body = request.getBody() == null ? "" : serde.toJsonString(request.getBody());
            LOGGER.debug("Send Request [id={}]: {} {}", reqId, request, body);
        }
        final CompletableFuture<InternalResponse> rfuture = new CompletableFuture<>();
        try {
            connect(connection);
        } catch (IOException e) {
            handleException(true, e, hostHandle, request, host, reqId, attemptCount, rfuture);
            return rfuture;
        }

        connection.executeAsync(request)
                .whenComplete((response, e) -> {
                    try {
                        if (e instanceof SocketTimeoutException) {
                            // SocketTimeoutException exceptions are wrapped and rethrown.
                            TimeoutException te = new TimeoutException(e.getMessage());
                            te.initCause(e);
                            rfuture.completeExceptionally(ArangoDBException.of(te, reqId));
                        } else if (e instanceof TimeoutException) {
                            rfuture.completeExceptionally(ArangoDBException.of(e, reqId));
                        } else if (e instanceof ConnectException) {
                            handleException(true, e, hostHandle, request, host, reqId, attemptCount, rfuture);
                        } else if (e != null) {
                            handleException(isSafe(request), e, hostHandle, request, host, reqId, attemptCount, rfuture);
                        } else {
                            if (LOGGER.isDebugEnabled()) {
                                String body = response.getBody() == null ? "" : serde.toJsonString(response.getBody());
                                LOGGER.debug("Received Response [id={}]: {} {}", reqId, response, body);
                            }
                            ArangoDBException errorEntityEx = ResponseUtils.translateError(serde, response);
                            if (errorEntityEx instanceof ArangoDBRedirectException) {
                                if (attemptCount >= 3) {
                                    rfuture.completeExceptionally(errorEntityEx);
                                } else {
                                    final String location = ((ArangoDBRedirectException) errorEntityEx).getLocation();
                                    final HostDescription redirectHost = HostUtils.createFromLocation(location);
                                    hostHandler.failIfNotMatch(redirectHost, errorEntityEx);
                                    mirror(
                                            executeAsync(request, new HostHandle().setHost(redirectHost), hostHandler.get(hostHandle, RequestUtils.determineAccessType(request)), attemptCount + 1),
                                            rfuture
                                    );
                                }
                            } else if (errorEntityEx instanceof ArangoDBUnavailableException) {
                                handleException(true, errorEntityEx, hostHandle, request, host, reqId, attemptCount, rfuture);
                            } else if (errorEntityEx != null) {
                                rfuture.completeExceptionally(errorEntityEx);
                            } else {
                                hostHandler.success();
                                rfuture.complete(response);
                            }
                        }
                    } catch (Exception ex) {
                        rfuture.completeExceptionally(ArangoDBException.of(ex, reqId));
                    }
                });
        return rfuture;
    }

    private void handleException(boolean isSafe, Throwable e, HostHandle hostHandle, InternalRequest request, Host host,
                                 long reqId, int attemptCount, CompletableFuture<InternalResponse> rfuture) {
        IOException ioEx = wrapIOEx(e);
        hostHandler.fail(ioEx);
        if (hostHandle != null && hostHandle.getHost() != null) {
            hostHandle.setHost(null);
        }
        hostHandler.checkNext(hostHandle, RequestUtils.determineAccessType(request));
        if (isSafe) {
            Host nextHost = hostHandler.get(hostHandle, RequestUtils.determineAccessType(request));
            LOGGER.warn("Could not connect to {} while executing request [id={}]",
                    host.getDescription(), reqId, ioEx);
            LOGGER.debug("Try connecting to {}", nextHost.getDescription());
            mirror(
                    executeAsync(request, hostHandle, nextHost, attemptCount),
                    rfuture
            );
        } else {
            ArangoDBException aEx = ArangoDBException.of(ioEx, reqId);
            rfuture.completeExceptionally(aEx);
        }
    }

    private void mirror(CompletableFuture<InternalResponse> up, CompletableFuture<InternalResponse> down) {
        up.whenComplete((v, err) -> {
            if (err != null) {
                down.completeExceptionally(err instanceof CompletionException ? err.getCause() : err);
            } else {
                down.complete(v);
            }
        });
    }

    private static IOException wrapIOEx(Throwable t) {
        if (t instanceof IOException) {
            return (IOException) t;
        } else {
            return new IOException(t);
        }
    }

    private boolean isSafe(final InternalRequest request) {
        RequestType type = request.getRequestType();
        return type == RequestType.GET || type == RequestType.HEAD || type == RequestType.OPTIONS;
    }

}
