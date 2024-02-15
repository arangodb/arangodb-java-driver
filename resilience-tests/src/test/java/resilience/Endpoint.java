package resilience;

import eu.rekawek.toxiproxy.Proxy;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * class representing a proxied db endpoint
 */
public class Endpoint {
    private final String name;
    private final String host;
    private final int port;
    private final String upstream;
    private Proxy proxy;
    private String serverId;

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

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public void enable() {
        try {
            getProxy().enable();
            Thread.sleep(100);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void disableNow() {
        try {
            getProxy().disable();
            Thread.sleep(100);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public void disable(long delay) {
        ScheduledExecutorService es = Executors.newSingleThreadScheduledExecutor();
        es.schedule(this::disableNow, delay, TimeUnit.MILLISECONDS);
        es.shutdown();
    }
}
