package arch;

import com.arangodb.ArangoDB;
import com.arangodb.entity.ArangoDBVersion;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;

class AdbTest extends BaseTest {
    @ParameterizedTest
    @MethodSource("adbByProtocol")
    void getVersion(ArangoDB adb) {
        final ArangoDBVersion version = adb.getVersion();
        assertThat(version.getServer()).isNotNull();
        assertThat(version.getVersion()).isNotNull();
    }
}
