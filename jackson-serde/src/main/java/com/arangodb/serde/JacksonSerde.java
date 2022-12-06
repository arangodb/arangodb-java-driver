package com.arangodb.serde;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.function.Consumer;

/**
 * User data serde based on Jackson Databind. Not shaded in arangodb-java-driver-shaded.
 */
public interface JacksonSerde extends ArangoSerde {

    /**
     * Allows configuring the underlying Jackson ObjectMapper
     *
     * @param configureFunction function to configure the Jackson ObjectMapper
     */
    void configure(final Consumer<ObjectMapper> configureFunction);

}
