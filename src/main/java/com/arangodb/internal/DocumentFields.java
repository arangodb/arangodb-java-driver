package com.arangodb.internal;

import java.util.Arrays;
import java.util.List;

public final class DocumentFields {

    private DocumentFields() {
    }

    public static final String ID = "_id";
    public static final String KEY = "_key";
    public static final String REV = "_rev";
    public static final String FROM = "_from";
    public static final String TO = "_to";

    public static List<String> values() {
        return Arrays.asList(ID, KEY, REV, FROM, TO);
    }
}
