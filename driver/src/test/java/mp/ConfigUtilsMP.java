package mp;

import com.arangodb.config.ArangoConfigProperties;
import io.smallrye.config.PropertiesConfigSourceProvider;
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
                .withSources(new PropertiesConfigSourceProvider(location, ConfigUtilsMP.class.getClassLoader(), false))
                .withMapping(ArangoConfigPropertiesMPImpl.class, prefix)
                .build();
        return cfg.getConfigMapping(ArangoConfigPropertiesMPImpl.class, prefix);
    }

}
