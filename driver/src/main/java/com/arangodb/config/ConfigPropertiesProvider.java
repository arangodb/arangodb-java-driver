package com.arangodb.config;


public interface ConfigPropertiesProvider {

    /**
     * @param key the property key
     * @return the configuration property with the specified key if present, {@code null} otherwise
     */
    String getProperty(String key);

    default String getProperty(ConfigPropertyKey key) {
        return getProperty(key.getValue());
    }

    default String getProperty(String key, String defaultValue) {
        String p = getProperty(key);
        return p != null ? p : defaultValue;
    }

    default String getProperty(ConfigPropertyKey key, String defaultValue) {
        String p = getProperty(key);
        return p != null ? p : defaultValue;
    }
}
