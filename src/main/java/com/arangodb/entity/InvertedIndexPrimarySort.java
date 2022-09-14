package com.arangodb.entity;

import com.arangodb.entity.arangosearch.ArangoSearchCompression;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * TODO: add documentation
 *
 * @author Michele Rastelli
 * @see <a href="https://www.arangodb.com/docs/stable/http/indexes-inverted.html">API Documentation</a>
 * @since ArangoDB 3.10
 */
public class InvertedIndexPrimarySort implements Entity {
    private final List<Field> fields = new ArrayList<>();
    private ArangoSearchCompression compression;

    public List<Field> getFields() {
        return fields;
    }

    public InvertedIndexPrimarySort fields(Field... fields) {
        Collections.addAll(this.fields, fields);
        return this;
    }

    public ArangoSearchCompression getCompression() {
        return compression;
    }

    public InvertedIndexPrimarySort compression(ArangoSearchCompression compression) {
        this.compression = compression;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InvertedIndexPrimarySort that = (InvertedIndexPrimarySort) o;
        return Objects.equals(fields, that.fields) && compression == that.compression;
    }

    @Override
    public int hashCode() {
        return Objects.hash(fields, compression);
    }

    public static class Field {
        private final String field;
        private final Direction direction;

        public Field(String field, Direction direction) {
            this.field = field;
            this.direction = direction;
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
