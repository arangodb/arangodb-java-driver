package arch;

import com.arangodb.config.ArangoConfigProperties;

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
