package com.arangodb.serde;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marker annotation for collections or map fields whose values need to be serialized/deserialized using the user serde.
 */
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@JacksonAnnotationsInside
public @interface UserDataInside {
}
