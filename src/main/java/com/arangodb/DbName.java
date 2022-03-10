package com.arangodb;

import com.arangodb.internal.ArangoRequestParam;
import com.arangodb.internal.util.EncodeUtils;
import com.arangodb.util.UnicodeUtils;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * Class to represent a NFC-normalized database name (as required by ArangoDB extended naming convention).
 * To instantiate use {@link DbName#of(String)} or {@link DbName#normalize(String)}.
 *
 * @see <a href="http://https://www.arangodb.com/docs/stable/data-modeling-naming-conventions-database-names.html">
 * API Documentation</a>
 */
public final class DbName implements Supplier<String> {

    /**
     * DbName of the "_system" database.
     */
    public static final DbName SYSTEM = DbName.of(ArangoRequestParam.SYSTEM);

    private final String value;

    /**
     * Creates a new {@link DbName} instance with the provided value. If the provided value is not
     * <a href="https://en.wikipedia.org/wiki/Unicode_equivalence#Normal_forms">NFC-normalized</a>, throws
     * {@link IllegalArgumentException}. No transformation is applied to the provided value.
     * Use {@link #normalize(String)} to create a DbName from a non-NFC-normalized value.
     *
     * @param value desired db name
     * @return the created {@link DbName} instance
     * @see <a href="https://en.wikipedia.org/wiki/Unicode_equivalence#Normal_forms">NFC normalization</a>
     * @see <a href="http://https://www.arangodb.com/docs/stable/data-modeling-naming-conventions-database-names.html">
     * API Documentation</a>
     */
    public static DbName of(final String value) {
        UnicodeUtils.checkNormalized(value);
        return new DbName(value);
    }

    /**
     * Creates a new {@link DbName} instance with the NFC normal form of the provided value.
     * Note that the created {@link DbName} will hold a value potentially different from the provided one.
     *
     * @param value desired db name
     * @return the created {@link DbName} instance
     * @see <a href="https://en.wikipedia.org/wiki/Unicode_equivalence#Normal_forms">NFC normalization</a>
     * @see <a href="http://https://www.arangodb.com/docs/stable/data-modeling-naming-conventions-database-names.html">
     * API Documentation</a>
     */
    public static DbName normalize(final String value) {
        return new DbName(UnicodeUtils.normalize(value));
    }

    private DbName(final String value) {
        this.value = value;
    }

    @Override
    public String get() {
        return value;
    }

    /**
     * @return the value encoded for usage in HTTP urls.
     */
    public String getEncoded() {
        return EncodeUtils.encodeURIComponent(value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DbName dbName = (DbName) o;
        return Objects.equals(value, dbName.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return get();
    }
}
