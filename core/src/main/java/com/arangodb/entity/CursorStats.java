package com.arangodb.entity;

import com.fasterxml.jackson.annotation.JsonAnySetter;

import java.util.HashMap;
import java.util.Map;

public final class CursorStats {

    private Long fullCount;
    private final Map<String, Object> properties = new HashMap<>();

    @JsonAnySetter
    public void add(String key, Object value) {
        properties.put(key, value);
    }

    public Long getFullCount() {
        return fullCount;
    }

    public Object get(String key) {
        return properties.get(key);
    }

}
