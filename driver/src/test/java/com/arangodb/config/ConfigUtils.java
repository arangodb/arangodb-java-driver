package com.arangodb.config;

import io.smallrye.config.PropertiesConfigSourceProvider;
import io.smallrye.config.SmallRyeConfig;
import io.smallrye.config.SmallRyeConfigBuilder;

public class ConfigUtils {

    public static ArangoConfigProperties loadConfigMP() {
        return loadConfigMP("arangodb.properties");
    }

    public static ArangoConfigProperties loadConfigMP(final String location) {
        return loadConfigMP(location, "arangodb");
    }

    public static ArangoConfigProperties loadConfigMP(final String location, final String prefix) {
        SmallRyeConfig cfg = new SmallRyeConfigBuilder()
                .withSources(new PropertiesConfigSourceProvider(location, ConfigUtils.class.getClassLoader(), false))
                .withMapping(ArangoConfigPropertiesMPImpl.class, prefix)
                .build();
        return cfg.getConfigMapping(ArangoConfigPropertiesMPImpl.class, prefix);
    }

    public static ArangoConfigProperties loadConfig() {
        return ArangoConfigProperties.fromFile();
    }

    public static ArangoConfigProperties loadConfig(final String location) {
        return ArangoConfigProperties.fromFile(location);
    }

    public static ArangoConfigProperties loadConfig(final String location, final String prefix) {
        return ArangoConfigProperties.fromFile(location, prefix);
    }

}
