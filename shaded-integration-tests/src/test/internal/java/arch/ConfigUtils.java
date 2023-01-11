package arch;

import com.arangodb.config.ArangoConfigProperties;
import io.smallrye.config.PropertiesConfigSourceProvider;
import io.smallrye.config.SmallRyeConfig;
import io.smallrye.config.SmallRyeConfigBuilder;

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

}
