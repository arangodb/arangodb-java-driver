package com.arangodb.entity;

import java.util.Objects;

public final class CursorWarning {

    private Integer code;
    private String message;

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof CursorWarning)) return false;
        CursorWarning that = (CursorWarning) o;
        return Objects.equals(code, that.code) && Objects.equals(message, that.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, message);
    }
}
