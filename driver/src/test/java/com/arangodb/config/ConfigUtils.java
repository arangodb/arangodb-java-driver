package com.arangodb.config;

import io.smallrye.config.PropertiesConfigSourceProvider;
import io.smallrye.config.SmallRyeConfig;
import io.smallrye.config.SmallRyeConfigBuilder;

public class ConfigUtils {

    public static ArangoConfigProperties loadConfig() {
        return loadConfig("arangodb.properties");
    }

    public static ArangoConfigProperties loadConfig(final String location) {
        return loadConfig(location, "arangodb");
    }

    public static ArangoConfigProperties loadConfig(final String location, final String prefix) {
        SmallRyeConfig cfg = new SmallRyeConfigBuilder()
                .withSources(new PropertiesConfigSourceProvider(location, Thread.currentThread().getContextClassLoader(), false))
                .withMapping(ArangoConfigProperties.class, prefix)
                .build();
        return cfg.getConfigMapping(ArangoConfigProperties.class, prefix);
    }

}
