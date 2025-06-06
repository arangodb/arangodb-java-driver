package mp;

import com.arangodb.config.ArangoConfigProperties;
import io.smallrye.config.PropertiesConfigSourceLoader;
import io.smallrye.config.SmallRyeConfig;
import io.smallrye.config.SmallRyeConfigBuilder;

public class ConfigUtilsMP {

    public static ArangoConfigProperties loadConfigMP() {
        return loadConfigMP("arangodb.properties");
    }

    public static ArangoConfigProperties loadConfigMP(final String location) {
        return loadConfigMP(location, "arangodb");
    }

    public static ArangoConfigProperties loadConfigMP(final String location, final String prefix) {
        SmallRyeConfig cfg = new SmallRyeConfigBuilder()
                .withSources(PropertiesConfigSourceLoader.inClassPath(location, 0, ConfigUtilsMP.class.getClassLoader()))
                .withMapping(ArangoConfigPropertiesMPImpl.class, prefix)
                .build();
        return cfg.getConfigMapping(ArangoConfigPropertiesMPImpl.class, prefix);
    }

}
