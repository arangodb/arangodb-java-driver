package com.arangodb.config;


public interface ConfigPropertiesProvider {

    /**
     * @param key the property key
     * @return the configuration property with the specified key if present, {@code null} otherwise
     */
    String getProperty(String key);

}
