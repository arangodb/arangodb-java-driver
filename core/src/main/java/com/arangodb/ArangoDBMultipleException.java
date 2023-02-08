package com.arangodb;

import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

public class ArangoDBMultipleException extends RuntimeException {

    private final List<Throwable> exceptions;

    public ArangoDBMultipleException(List<Throwable> exceptions) {
        super();
        this.exceptions = exceptions;
    }

    public List<Throwable> getExceptions() {
        return exceptions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ArangoDBMultipleException that = (ArangoDBMultipleException) o;
        return Objects.equals(exceptions, that.exceptions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(exceptions);
    }

    @Override
    public String toString() {
        StringJoiner joiner = new StringJoiner("\n\t", "ArangoDBMultipleException{\n\t", "\n}");
        for (Throwable t : exceptions) {
            StringJoiner tJoiner = new StringJoiner("\n\t\t", "\n\t\t", "");
            for (StackTraceElement stackTraceElement : t.getStackTrace())
                tJoiner.add("at " + stackTraceElement);
            joiner.add(t + tJoiner.toString());
        }
        return joiner.toString();
    }
}
