package com.arangodb.internal.config;

import com.arangodb.ArangoDBException;
import com.arangodb.config.ConfigPropertiesProvider;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * ConfigProvider that reads configuration entries from local file. Properties path prefix can be configured, so that it
 * is possible to distinguish configurations for multiple driver instances in the same file.
 */
public class FileConfigPropertiesProvider implements ConfigPropertiesProvider {
    private static final String DEFAULT_PREFIX = "arangodb";
    private static final String DEFAULT_PROPERTY_FILE = "/arangodb.properties";
    private final Properties properties;
    private final String prefix;

    public FileConfigPropertiesProvider() {
        this(DEFAULT_PREFIX, DEFAULT_PROPERTY_FILE);
    }

    public FileConfigPropertiesProvider(String prefix, String fileName) {
        this.prefix = initPrefix(prefix);
        properties = initProperties(fileName);
    }

    private String initPrefix(String p) {
        if (p == null) {
            return "";
        } else {
            return p + ".";
        }
    }

    private Properties initProperties(String fileName) {
        Properties p = new Properties();
        try (InputStream is = FileConfigPropertiesProvider.class.getResourceAsStream(fileName)) {
            p.load(is);
        } catch (IOException e) {
            throw new ArangoDBException(e);
        }
        return p;
    }

    @Override
    public String getProperty(String key) {
        return properties.getProperty(prefix + key);
    }
}

