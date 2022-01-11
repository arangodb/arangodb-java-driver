package qa.overload;

import com.arangodb.ArangoDB;
import com.arangodb.ArangoDBException;
import com.arangodb.BaseTest;
import com.arangodb.velocypack.VPackParser;
import com.arangodb.velocystream.Request;
import com.arangodb.velocystream.RequestType;
import com.arangodb.velocystream.Response;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;

@RunWith(Parameterized.class)
public class OverloadControlTest extends BaseTest {
    private static final VPackParser PARSER = new VPackParser.Builder().build();
    private static final String QUEUE_TIME_HEADER = "X-Arango-Queue-Time-Seconds";

    public OverloadControlTest(final ArangoDB arangoDB) {
        super(arangoDB);
    }

    private static Response createCursor(ArangoDB arangoDB, Double queueTime) {
        Request req = new Request("_system", RequestType.POST, "/_api/cursor");
        if (queueTime != null) {
            req.putHeaderParam(QUEUE_TIME_HEADER, String.valueOf(queueTime));
        }
        req.setBody(PARSER.fromJson("""
                {"query": "FOR i IN 1..100 RETURN SLEEP(1)", "batchSize": 1, "options": {"stream": true}}"""
        ));
        return arangoDB.execute(req);
    }

    private static class CursorRequest implements Runnable {
        private final ArangoDB arangoDB;
        private final Double queueTime;
        private volatile Response response;

        public CursorRequest(ArangoDB arangoDB, Double queueTime) {
            this.arangoDB = arangoDB;
            this.queueTime = queueTime;
        }

        @Override
        public void run() {
            response = createCursor(arangoDB, queueTime);
        }

        public Response getResponse() {
            return response;
        }

        public String getCursorId() {
            return response.getBody().get("id").getAsString();
        }

        public double getExecutionTime() {
            return Double.parseDouble(response.getMeta().get(QUEUE_TIME_HEADER));
        }
    }

    @Test
    public void queueTimeValues() throws InterruptedException {
        List<CursorRequest> reqsWithNoQT = IntStream.range(0, 50)
                .mapToObj(__ -> new CursorRequest(arangoDB, null))
                .toList();

        List<CursorRequest> reqsWithHighQT = IntStream.range(0, 10)
                .mapToObj(__ -> new CursorRequest(arangoDB, 20.0))
                .toList();

        List<CursorRequest> reqsWithLowQT = IntStream.range(0, 10)
                .mapToObj(__ -> new CursorRequest(arangoDB, 1.0))
                .toList();

        List<CursorRequest> reqs = Stream.concat(reqsWithNoQT.stream(), reqsWithHighQT.stream()).toList();

        List<Thread> threads = reqs.stream()
                .map(Thread::new)
                .toList();

        for (Thread t : threads) {
            t.start();
        }

        int errorCount = 0;
        for (CursorRequest r : reqsWithLowQT) {
            try {
                r.run();
            } catch (ArangoDBException e) {
                assertThat(e.getResponseCode(), is(412));
                assertThat(e.getErrorNum(), is(21004));
                errorCount++;
            }
        }
        assertThat(errorCount, greaterThan(0));
        System.out.println("queue time violated errors: " + errorCount + "/" + reqsWithLowQT.size());

        for (Thread t : threads) {
            t.join();
        }

        long nonZeroQTCount = reqs.stream().filter(r -> r.getExecutionTime() > 0.0).count();
        System.out.println("response with queue time > 0: " + nonZeroQTCount + "/"+ reqs.size());

        assertThat((int) nonZeroQTCount, is(greaterThan(0)));
    }
}
