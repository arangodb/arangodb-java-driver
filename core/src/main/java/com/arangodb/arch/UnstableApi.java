package com.arangodb.arch;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates a public API that has references to internal classes and that should change in the next major release.
 * Referenced internal classes are annotated with {@link UsedInApi}.
 * Architectural tests consider these annotation to tolerate referencing annotated elements.
 */
@Retention(RetentionPolicy.CLASS)
@Target({
        ElementType.TYPE,
        ElementType.METHOD,
        ElementType.PARAMETER,
        ElementType.FIELD
})
public @interface UnstableApi {
}
