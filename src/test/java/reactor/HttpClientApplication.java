package reactor;

import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.netty.http.client.HttpClient;
import reactor.util.function.Tuple2;

import static io.netty.handler.codec.http.HttpHeaderNames.AUTHORIZATION;

/**
 * @author Michele Rastelli
 */
public class HttpClientApplication {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(HttpClientApplication.class);
    static private final String jwt = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpYXQiOjEuNTY5MzE1NDk2MjcyMjE1NGUrNiwiZXhwIjoxNTcxOTA3NDk2LCJpc3MiOiJhcmFuZ29kYiIsInByZWZlcnJlZF91c2VybmFtZSI6InJvb3QifQ==._z0Lrjl50kaWwE7cpQb-qFBOIJW6D70PCDavxi93oFg=";

    static private HttpClient client = HttpClient.create()
            .baseUrl("http://localhost:8529/_db/_system");

    private static Flux<Integer> createFlux() {
        return Flux.generate(
                () -> 0,
                (state, sink) -> {
                    sink.next(state);
//                    if (state == 10_000) sink.complete();
                    return state + 1;
                });
    }

    private static Mono<String> createRequest() {
        return client
                .headers(h -> h.set(AUTHORIZATION, "bearer " + jwt))
                .get()
                .uri("/_api/version")
                .responseSingle((resp, bytes) -> bytes.asString());
    }

    private static Mono<Integer> createMono(Integer value) {
        return Mono.just(value);
    }

    public static void main(String[] args) {
        Scheduler subscriber = Schedulers.newSingle("subscriber");
        createFlux()
                .flatMap(HttpClientApplication::createMono)
                .flatMap(it -> createRequest())
                .index()
                .map(Tuple2::getT1)
                .filter(it -> it % 10000L == 0)
                .log()
                .doOnRequest(it -> log.info("requested " + it))
                .doOnComplete(subscriber::dispose)
                .publishOn(subscriber)
                .subscribe();
    }
}
