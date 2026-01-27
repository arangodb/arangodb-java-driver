package mp;

import com.arangodb.config.ArangoConfigProperties;
import io.smallrye.config.PropertiesConfigSource;
import io.smallrye.config.SmallRyeConfig;
import io.smallrye.config.SmallRyeConfigBuilder;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;

public class ConfigUtilsMP {

    public static ArangoConfigProperties loadConfigMP() {
        return loadConfigMP("arangodb.properties");
    }

    public static ArangoConfigProperties loadConfigMP(final String location) {
        return loadConfigMP(location, "arangodb");
    }

    public static ArangoConfigProperties loadConfigMP(final String location, final String prefix) {
        URL url = ConfigUtilsMP.class.getClassLoader().getResource(location);
        if (url == null) {
            throw new IllegalStateException("Configuration file not found: " + location);
        }
        try {
            SmallRyeConfig cfg = new SmallRyeConfigBuilder()
                    .withSources(new PropertiesConfigSource(url))
                    .withMapping(ArangoConfigPropertiesMPImpl.class, prefix)
                    .build();
            return cfg.getConfigMapping(ArangoConfigPropertiesMPImpl.class, prefix);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

}
