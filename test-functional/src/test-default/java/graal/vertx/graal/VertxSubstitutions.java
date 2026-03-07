package graal.vertx.graal;

import com.oracle.svm.core.annotate.Alias;
import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;
import io.netty.handler.ssl.*;
import io.vertx.core.Vertx;
import io.vertx.core.dns.AddressResolverOptions;
import io.vertx.core.dns.impl.DefaultAddressResolverProvider;
import io.vertx.core.net.NetServerOptions;
import io.vertx.core.net.impl.NetServerImpl;
import io.vertx.core.spi.dns.AddressResolverProvider;
import io.vertx.core.transport.Transport;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLException;
import javax.net.ssl.TrustManagerFactory;
import java.util.Collection;
import java.util.List;
import java.util.Set;


@TargetClass(className = "io.vertx.core.net.OpenSSLEngineOptions")
final class Target_io_vertx_core_net_OpenSSLEngineOptions {

    @Substitute
    public static boolean isAvailable() {
        return false;
    }

    @Substitute
    public static boolean isAlpnAvailable() {
        return false;
    }
}

@TargetClass(className = "io.vertx.core.transport.Transport")
final class Target_io_vertx_core_transport_Transport {
    @Substitute
    public static Transport nativeTransport() {
        return Transport.NIO;
    }
}

@TargetClass(className = "io.vertx.core.spi.dns.AddressResolverProvider")
interface Target_io_vertx_core_spi_dns_AddressResolverProvider {
    @Substitute
    static AddressResolverProvider factory(Vertx vertx, AddressResolverOptions options) {
        return new DefaultAddressResolverProvider();
    }
}

@TargetClass(className = "io.vertx.core.impl.transports.TransportLoader")
final class Target_io_vertx_core_impl_transports_TransportLoader {
    @Substitute
    public static Transport epoll() {
        return Transport.NIO;
    }

    @Substitute
    public static Transport io_uring() {
        return Transport.NIO;
    }

    @Substitute
    public static Transport kqueue() {
        return Transport.NIO;
    }
}

@TargetClass(className = "io.vertx.core.spi.tls.DefaultSslContextFactory")
final class Target_io_vertx_core_spi_tls_DefaultSslContextFactory {

    @Alias
    private Set<String> enabledCipherSuites;

    @Alias
    private List<String> applicationProtocols;

    @Alias
    private ClientAuth clientAuth;

    @Substitute
    private SslContext createContext(boolean useAlpn, boolean client, KeyManagerFactory kmf, TrustManagerFactory tmf)
            throws SSLException {
        SslContextBuilder builder;
        if (client) {
            builder = SslContextBuilder.forClient();
            if (kmf != null) {
                builder.keyManager(kmf);
            }
        } else {
            throw new SSLException("not supported");
        }
        Collection<String> cipherSuites = enabledCipherSuites;
        builder.sslProvider(SslProvider.JDK);
        if (cipherSuites == null || cipherSuites.isEmpty()) {
            cipherSuites = Target_io_vertx_core_spi_tls_DefaultJDKCipherSuite.get();
        }
        if (tmf != null) {
            builder.trustManager(tmf);
        }
        if (cipherSuites != null && cipherSuites.size() > 0) {
            builder.ciphers(cipherSuites);
        }
        if (useAlpn && applicationProtocols != null && applicationProtocols.size() > 0) {
            builder.applicationProtocolConfig(new ApplicationProtocolConfig(
                    ApplicationProtocolConfig.Protocol.ALPN,
                    ApplicationProtocolConfig.SelectorFailureBehavior.NO_ADVERTISE,
                    ApplicationProtocolConfig.SelectedListenerFailureBehavior.ACCEPT,
                    applicationProtocols));
        }
        if (clientAuth != null) {
            builder.clientAuth(clientAuth);
        }
        return builder.build();
    }
}

@TargetClass(className = "io.vertx.core.spi.tls.DefaultJDKCipherSuite")
final class Target_io_vertx_core_spi_tls_DefaultJDKCipherSuite {
    @Alias
    static List<String> get() {
        return null;
    }
}

@TargetClass(className = "io.vertx.core.impl.VertxImpl")
final class Target_io_vertx_core_impl_VertxImpl {

    @Substitute
    public NetServerImpl createNetServer(NetServerOptions options) {
        return null;
    }
}

public class VertxSubstitutions {

}
