package resilience.connection;

import com.arangodb.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import resilience.SingleServerTest;

import java.net.ConnectException;
import java.net.UnknownHostException;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

/**
 * @author Michele Rastelli
 */
class ConnectionTest extends SingleServerTest {

    static Stream<Protocol> protocolProvider() {
        return Stream.of(
                Protocol.VST,
                Protocol.HTTP_VPACK,
                Protocol.HTTP2_VPACK
        );
    }

    static Stream<ArangoDB> arangoProvider() {
        return Stream.of(
                dbBuilder().protocol(Protocol.VST).build(),
                dbBuilder().protocol(Protocol.HTTP_VPACK).build(),
                dbBuilder().protocol(Protocol.HTTP2_VPACK).build()
        );
    }

    static Stream<ArangoDBAsync> asyncArangoProvider() {
        return arangoProvider().map(ArangoDB::async);
    }

    @ParameterizedTest
    @MethodSource("protocolProvider")
    void nameResolutionFail(Protocol protocol) {
        ArangoDB arangoDB = new ArangoDB.Builder()
                .host("wrongHost", 8529)
                .protocol(protocol)
                .build();

        Throwable thrown = catchThrowable(arangoDB::getVersion);
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
        assertThat(thrown.getMessage()).contains("Cannot contact any host!");
        assertThat(thrown.getCause()).isNotNull();
        assertThat(thrown.getCause()).isInstanceOf(ArangoDBMultipleException.class);
        ((ArangoDBMultipleException) thrown.getCause()).getExceptions().forEach(e -> {
            assertThat(e).isInstanceOf(UnknownHostException.class);
            assertThat(e.getMessage()).contains("wrongHost");
        });
        arangoDB.shutdown();
    }

    @ParameterizedTest
    @MethodSource("protocolProvider")
    void nameResolutionFailAsync(Protocol protocol) {
        ArangoDBAsync arangoDB = new ArangoDB.Builder()
                .host("wrongHost", 8529)
                .protocol(protocol)
                .build()
                .async();

        Throwable thrown = catchThrowable(() -> arangoDB.getVersion().get()).getCause();
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
        assertThat(thrown.getMessage()).contains("Cannot contact any host!");
        assertThat(thrown.getCause()).isNotNull();
        assertThat(thrown.getCause()).isInstanceOf(ArangoDBMultipleException.class);
        ((ArangoDBMultipleException) thrown.getCause()).getExceptions().forEach(e -> {
            assertThat(e).isInstanceOf(UnknownHostException.class);
            assertThat(e.getMessage()).contains("wrongHost");
        });
        arangoDB.shutdown();
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("arangoProvider")
    void connectionFail(ArangoDB arangoDB) {
        getEndpoint().disable();

        Throwable thrown = catchThrowable(arangoDB::getVersion);
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
        assertThat(thrown.getMessage()).contains("Cannot contact any host");
        assertThat(thrown.getCause()).isNotNull();
        assertThat(thrown.getCause()).isInstanceOf(ArangoDBMultipleException.class);
        ((ArangoDBMultipleException) thrown.getCause()).getExceptions().forEach(e ->
                assertThat(e).isInstanceOf(ConnectException.class));
        arangoDB.shutdown();
        getEndpoint().enable();
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("asyncArangoProvider")
    void connectionFailAsync(ArangoDBAsync arangoDB) {
        getEndpoint().disable();

        Throwable thrown = catchThrowable(() -> arangoDB.getVersion().get()).getCause();
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
        assertThat(thrown.getMessage()).contains("Cannot contact any host");
        assertThat(thrown.getCause()).isNotNull();
        assertThat(thrown.getCause()).isInstanceOf(ArangoDBMultipleException.class);
        ((ArangoDBMultipleException) thrown.getCause()).getExceptions().forEach(e ->
                assertThat(e).isInstanceOf(ConnectException.class));
        arangoDB.shutdown();
        getEndpoint().enable();
    }

    @ParameterizedTest(name = "{index}")
    @EnumSource(Protocol.class)
    void authFail(Protocol protocol) {
        ArangoDB adb = new ArangoDB.Builder()
                .host(getEndpoint().getHost(), getEndpoint().getPort())
                .protocol(protocol)
                .password("wrong")
                .build();

        Throwable thrown = catchThrowable(adb::getVersion);
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
        ArangoDBException aEx = (ArangoDBException) thrown;
        assertThat(aEx.getResponseCode()).isEqualTo(401);
        adb.shutdown();
    }

    @ParameterizedTest(name = "{index}")
    @EnumSource(Protocol.class)
    void authFailAsync(Protocol protocol) {
        ArangoDBAsync adb = new ArangoDB.Builder()
                .host(getEndpoint().getHost(), getEndpoint().getPort())
                .protocol(protocol)
                .password("wrong")
                .build()
                .async();

        Throwable thrown = catchThrowable(() -> adb.getVersion().get()).getCause();
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
        ArangoDBException aEx = (ArangoDBException) thrown;
        assertThat(aEx.getResponseCode()).isEqualTo(401);
        adb.shutdown();
    }

}
