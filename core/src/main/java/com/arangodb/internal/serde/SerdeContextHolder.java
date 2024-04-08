package com.arangodb.internal.serde;

import com.arangodb.serde.SerdeContext;

public enum SerdeContextHolder {
    INSTANCE;

    private final ThreadLocal<SerdeContext> ctx = ThreadLocal.withInitial(() -> SerdeContext.EMPTY);

    public SerdeContext getCtx() {
        return ctx.get();
    }

    public void setCtx(SerdeContext ctx) {
        this.ctx.set(ctx != null ? ctx : SerdeContext.EMPTY);
    }
}
