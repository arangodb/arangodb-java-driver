package com.arangodb.internal;

import com.arangodb.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.function.Supplier;

public enum RequestContextHolder {
    INSTANCE;

    private final static Logger LOGGER = LoggerFactory.getLogger(RequestContextHolder.class);

    private final ThreadLocal<Boolean> runningWithinCtx = ThreadLocal.withInitial(() -> false);
    private final ThreadLocal<RequestContext> ctx = ThreadLocal.withInitial(() -> RequestContext.EMPTY);

    public <T> T runWithCtx(RequestContext requestContext, Supplier<T> fun) {
        Objects.requireNonNull(requestContext);
        RequestContext old = null;
        try {
            if (runningWithinCtx.get()) {
                // re-entrant invocation, keep track of old ctx to restore later
                old = ctx.get();
            }
            LOGGER.debug("setting RequestContext: {}", requestContext);
            ctx.set(requestContext);
            runningWithinCtx.set(true);
            return fun.get();
        } finally {
            if (old == null) {
                LOGGER.debug("removing RequestContext");
                ctx.remove();
                runningWithinCtx.remove();
            } else {
                // re-entrant invocation, restore old ctx
                LOGGER.debug("restore RequestContext: {}", old);
                ctx.set(old);
            }
        }
    }

    public RequestContext getCtx() {
        if (!runningWithinCtx.get()) {
            throw new IllegalStateException("Not within ctx!");
        }

        RequestContext requestContext = ctx.get();
        LOGGER.debug("returning RequestContext: {}", requestContext);
        return requestContext;
    }
}
