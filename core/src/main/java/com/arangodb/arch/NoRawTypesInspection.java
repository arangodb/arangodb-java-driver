package com.arangodb.arch;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Skip invoking {@code JavaType#getAllInvolvedRawTypes()} on the target class during arch tests.
 * Prevents StackOverflowError caused by <a href="https://github.com/TNG/ArchUnit/issues/1237">this</a>.
 * FIXME: remove this when <a href="https://github.com/TNG/ArchUnit/issues/1237">this</a> is fixed and released
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
public @interface NoRawTypesInspection {
}
