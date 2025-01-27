package com.arangodb.config;

import java.util.Properties;

public class ConfigUtils {

    public static ArangoConfigProperties loadConfig() {
        return ArangoConfigProperties.fromFile();
    }

    public static ArangoConfigProperties loadConfig(final String location) {
        return ArangoConfigProperties.fromFile(location);
    }

    public static ArangoConfigProperties loadConfig(final String location, final String prefix) {
        return ArangoConfigProperties.fromFile(location, prefix);
    }

    public static ArangoConfigProperties loadConfig(final Properties properties, final String prefix) {
        return ArangoConfigProperties.fromProperties(properties, prefix);
    }

}
