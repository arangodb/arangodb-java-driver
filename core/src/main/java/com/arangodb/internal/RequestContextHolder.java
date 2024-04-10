package com.arangodb.internal;

import com.arangodb.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;

public enum RequestContextHolder {
    INSTANCE;

    private final static Logger LOGGER = LoggerFactory.getLogger(RequestContextHolder.class);

    private final ThreadLocal<RequestContext> ctx = ThreadLocal.withInitial(() -> RequestContext.EMPTY);
    private final ThreadLocal<Boolean> runningWithinCtx = ThreadLocal.withInitial(() -> false);

    public <T> T runWithCtx(RequestContext requestContext, Supplier<T> fun) {
        try {
            if (runningWithinCtx.get()) {
                throw new IllegalStateException("re-entrant invocation is not supported");
            }

            runningWithinCtx.set(true);

            if (requestContext != null) {
                LOGGER.debug("setting RequestContext: {}", requestContext);
                ctx.set(requestContext);
            }

            return fun.get();
        } finally {
            LOGGER.debug("removing RequestContext");
            ctx.remove();
            runningWithinCtx.remove();
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
