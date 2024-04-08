package com.arangodb.internal;

import com.arangodb.RequestContext;

import java.util.function.Supplier;

public enum RequestContextHolder {
    INSTANCE;

    private final ThreadLocal<RequestContext> ctx = ThreadLocal.withInitial(() -> RequestContext.EMPTY);

    public <T> T runWithCtx(RequestContext ctx, Supplier<T> fun) {
        this.ctx.set(ctx != null ? ctx : RequestContext.EMPTY);
        try {
            return fun.get();
        } finally {
            this.ctx.remove();
        }
    }

    public RequestContext getCtx() {
        return ctx.get();
    }
}
