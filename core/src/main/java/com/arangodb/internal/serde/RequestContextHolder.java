package com.arangodb.internal.serde;

import com.arangodb.serde.RequestContext;

public enum RequestContextHolder {
    INSTANCE;

    private final ThreadLocal<RequestContext> ctx = ThreadLocal.withInitial(() -> RequestContext.EMPTY);

    public RequestContext getCtx() {
        return ctx.get();
    }

    public void setCtx(RequestContext ctx) {
        this.ctx.set(ctx != null ? ctx : RequestContext.EMPTY);
    }
}
