package com.arangodb.serde.jackson;

import com.arangodb.ContentType;
import com.arangodb.serde.ArangoSerdeProvider;
import com.arangodb.serde.jackson.internal.JacksonMapperProvider;
import com.arangodb.serde.jackson.internal.JacksonSerdeImpl;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ServiceLoader;

public class JacksonSerdeProvider implements ArangoSerdeProvider {

    private final static Logger LOG = LoggerFactory.getLogger(JacksonSerdeProvider.class);

    /**
     * Creates a new JacksonSerde with default settings for the specified data type.
     * Registers all the Jackson modules ({@link com.fasterxml.jackson.databind.Module}) discovered via SPI.
     *
     * @param contentType serialization target data type
     * @return the created JacksonSerde
     */
    @Override
    public JacksonSerde of(final ContentType contentType) {
        JacksonSerde serde = create(JacksonMapperProvider.of(contentType));
        ServiceLoader<Module> loader = ServiceLoader.load(Module.class);
        serde.configure(mapper -> mapper.registerModules(loader));
        return serde;
    }

    /**
     * Creates a new JacksonSerde using the provided ObjectMapper.
     *
     * @param mapper Jackson ObjectMapper to use
     * @return the created JacksonSerde
     */
    static JacksonSerde create(final ObjectMapper mapper) {
        return new JacksonSerdeImpl(mapper);
    }

}
