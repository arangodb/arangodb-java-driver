package com.arangodb.entity;

import com.arangodb.internal.DocumentFields;
import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Michele Rastelli
 */
// TODO: in v7 add targets ElementType.METHOD and ElementType.PARAMETER
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@JacksonAnnotationsInside
@JsonProperty(DocumentFields.FROM)
@JsonInclude(JsonInclude.Include.NON_NULL)
public @interface From {
}
