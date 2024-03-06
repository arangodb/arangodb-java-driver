package com.arangodb.entity;

import com.arangodb.entity.arangosearch.ArangoSearchCompression;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author Michele Rastelli
 * @since ArangoDB 3.10
 */
public final class InvertedIndexPrimarySort {
    private final List<Field> fields = new ArrayList<>();
    private ArangoSearchCompression compression;
    private Boolean cache;

    public List<Field> getFields() {
        return fields;
    }

    /**
     * @param fields An array of the fields to sort the index by and the direction to sort each field in.
     * @return this
     */
    public InvertedIndexPrimarySort fields(Field... fields) {
        Collections.addAll(this.fields, fields);
        return this;
    }

    public ArangoSearchCompression getCompression() {
        return compression;
    }

    /**
     * @param compression Defines how to compress the primary sort data.
     * @return this
     */
    public InvertedIndexPrimarySort compression(ArangoSearchCompression compression) {
        this.compression = compression;
        return this;
    }

    public Boolean getCache() {
        return cache;
    }

    /**
     * @param cache If you enable this option, then the primary sort columns are always cached in memory. This can
     *              improve the performance of queries that utilize the primary sort order. Otherwise, these values are
     *              memory-mapped and it is up to the operating system to load them from disk into memory and to evict
     *              them from memory (Enterprise Edition only).
     * @return this
     * @since ArangoDB 3.10.2
     */
    public InvertedIndexPrimarySort cache(Boolean cache) {
        this.cache = cache;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InvertedIndexPrimarySort that = (InvertedIndexPrimarySort) o;
        return Objects.equals(fields, that.fields) && compression == that.compression && Objects.equals(cache, that.cache);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fields, compression, cache);
    }

    public static class Field {
        private final String field;
        private final Direction direction;

        /**
         * @param field     An attribute path. The . character denotes sub-attributes.
         * @param direction The sorting direction.
         */
        @JsonCreator
        public Field(@JsonProperty("field") String field, @JsonProperty("direction") Direction direction) {
            this.field = field;
            this.direction = direction;
        }

        public String getField() {
            return field;
        }

        public Direction getDirection() {
            return direction;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Field field1 = (Field) o;
            return Objects.equals(field, field1.field) && direction == field1.direction;
        }

        @Override
        public int hashCode() {
            return Objects.hash(field, direction);
        }

        public enum Direction {
            asc,
            desc
        }

    }

}
