package com.arangodb.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * @since ArangoDB 3.10
 */
public class ComputedValue {
    private String name;
    private String expression;
    private Boolean overwrite;
    private Set<ComputeOn> computeOn;
    private Boolean keepNull;
    private Boolean failOnWarning;

    public enum ComputeOn {
        insert, update, replace
    }

    public ComputedValue() {
        super();
    }

    /**
     * @param name (required) The name of the target attribute. Can only be a top-level attribute, but you may return
     *             a nested object. Cannot be _key, _id, _rev, _from, _to, or a shard key attribute.
     * @return this
     */
    public ComputedValue name(final String name) {
        this.name = name;
        return this;
    }

    /**
     * @param expression (required) An AQL RETURN operation with an expression that computes the desired value. See
     *                   <a href="https://github.com/arangodb/docs/blob/main/3.10/data-modeling-documents-computed-values.md#computed-value-expressions">Computed Value Expressions</a>
     *                   for details.
     * @return this
     */
    public ComputedValue expression(final String expression) {
        this.expression = expression;
        return this;
    }

    /**
     * @param overwrite (required) Whether the computed value shall take precedence over a user-provided or existing
     *                  attribute.
     * @return this
     */
    public ComputedValue overwrite(final Boolean overwrite) {
        this.overwrite = overwrite;
        return this;
    }

    /**
     * @param computeOn (optional) An array of operations to define on which write operations the value shall be
     *                  computed. The default is ["insert", "update", "replace"].
     * @return this
     */
    public ComputedValue computeOn(final ComputeOn... computeOn) {
        if (this.computeOn == null) {
            this.computeOn = new HashSet<>();
        }
        Collections.addAll(this.computeOn, computeOn);
        return this;
    }

    /**
     * @param keepNull (optional) Whether the target attribute shall be set if the expression evaluates to null. You
     *                 can set the option to false to not set (or unset) the target attribute if the expression
     *                 returns null. The default is true.
     * @return this
     */
    public ComputedValue keepNull(final Boolean keepNull) {
        this.keepNull = keepNull;
        return this;
    }

    /**
     * @param failOnWarning (optional) Whether to let the write operation fail if the expression produces a warning.
     *                      The default is false.
     * @return this
     */
    public ComputedValue failOnWarning(final Boolean failOnWarning) {
        this.failOnWarning = failOnWarning;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ComputedValue that = (ComputedValue) o;
        return Objects.equals(name, that.name) && Objects.equals(expression, that.expression) && Objects.equals(overwrite, that.overwrite) && Objects.equals(computeOn, that.computeOn) && Objects.equals(keepNull, that.keepNull) && Objects.equals(failOnWarning, that.failOnWarning);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, expression, overwrite, computeOn, keepNull, failOnWarning);
    }
}
