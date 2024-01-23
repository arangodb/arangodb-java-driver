package com.arangodb.arch;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates an internal class referenced in public API, which should be therefore considered part of the public API.
 * The annotated class and/or the referencing public API element should change in the next major release.
 * Referencing element is annotated with {@link UnstableApi}.
 * Architectural tests consider these annotation to tolerate referenced annotated elements.
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
public @interface UsedInApi {
}
