package resilience;

import eu.rekawek.toxiproxy.Proxy;

/**
 * class representing a proxied db endpoint
 */
public class Endpoint {
    private final String name;
    private final String host;
    private final int port;
    private final String upstream;
    private Proxy proxy;

    public Endpoint(String name, String host, int port, String upstream) {
        this.name = name;
        this.host = host;
        this.port = port;
        this.upstream = upstream;
    }

    public String getName() {
        return name;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getUpstream() {
        return upstream;
    }

    public Proxy getProxy() {
        return proxy;
    }

    public void setProxy(Proxy proxy) {
        this.proxy = proxy;
    }
}
