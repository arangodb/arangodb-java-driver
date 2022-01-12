package qa.overload;

import com.arangodb.ArangoDB;
import com.arangodb.ArangoDBException;
import com.arangodb.Protocol;
import com.arangodb.entity.ServerRole;
import com.arangodb.velocypack.VPackParser;
import com.arangodb.velocystream.Request;
import com.arangodb.velocystream.RequestType;
import com.arangodb.velocystream.Response;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.junit.Assume.assumeTrue;

/**
 * Implementation of Overload Control QA.
 * Test plan: https://arangodb.atlassian.net/wiki/spaces/AQA/pages/1753579579/Overload+control
 */
@Ignore("Manual test only, remove arangodb.properties before running.")
@RunWith(Parameterized.class)
public class OverloadControlTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(OverloadControlTest.class);
    private static final VPackParser PARSER = new VPackParser.Builder().build();
    private static final String QUEUE_TIME_HEADER = "X-Arango-Queue-Time-Seconds";
    private final Protocol protocol;
    private final ArangoDB arangoDB1;

    @Parameterized.Parameters
    public static List<Protocol> builders() {
        return Arrays.asList(
                Protocol.VST,
                Protocol.HTTP_JSON,
                Protocol.HTTP_VPACK
        );
    }

    public OverloadControlTest(final Protocol protocol) {
        this.protocol = protocol;
        arangoDB1 = new ArangoDB.Builder()
                .maxConnections(500)
                .host("172.17.0.1", 8529)
                .password("test")
                .useProtocol(protocol)
                .build();
    }

    @After
    public void shutdown() {
        arangoDB1.shutdown();
    }

    private boolean isCluster() {
        return arangoDB1.getRole() == ServerRole.COORDINATOR;
    }

    private static Response createCursor(ArangoDB arangoDB, Double queueTime) {
        Request req = new Request("_system", RequestType.POST, "/_api/cursor");
        if (queueTime != null) {
            req.putHeaderParam(QUEUE_TIME_HEADER, String.valueOf(queueTime));
        }
        req.setBody(PARSER.fromJson("{\"query\": \"FOR i IN 1..100 RETURN SLEEP(1)\", \"batchSize\": 1, \"options\": {\"stream\": true}}"));
        return arangoDB.execute(req);
    }

    private static Response readCursor(ArangoDB arangoDB, String cursorId, Double queueTime) {
        Request req = new Request("_system", RequestType.POST, "/_api/cursor/" + cursorId);
        if (queueTime != null) {
            req.putHeaderParam(QUEUE_TIME_HEADER, String.valueOf(queueTime));
        }
        return arangoDB.execute(req);
    }

    private static class CreateCursorRequest implements Runnable {
        private final ArangoDB arangoDB;
        private final Double queueTime;
        private volatile Response response;

        public CreateCursorRequest(ArangoDB arangoDB, Double queueTime) {
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

    private static class ReadCursorRequest implements Runnable {
        private final ArangoDB arangoDB;
        private final String cursorId;
        private final Double queueTime;
        private volatile Response response;

        public ReadCursorRequest(ArangoDB arangoDB, String cursorId, Double queueTime) {
            this.arangoDB = arangoDB;
            this.cursorId = cursorId;
            this.queueTime = queueTime;
        }

        @Override
        public void run() {
            readCursor(arangoDB, cursorId, null);
            response = readCursor(arangoDB, cursorId, queueTime);
        }

        public Response getResponse() {
            return response;
        }

        public double getExecutionTime() {
            return Double.parseDouble(response.getMeta().get(QUEUE_TIME_HEADER));
        }
    }

    @Test
    public void queueTimeValues() throws InterruptedException {
        List<CreateCursorRequest> reqsWithNoQT = IntStream.range(0, 50)
                .mapToObj(__ -> new CreateCursorRequest(arangoDB1, null))
                .collect(Collectors.toList());

        List<CreateCursorRequest> reqsWithHighQT = IntStream.range(0, 10)
                .mapToObj(__ -> new CreateCursorRequest(arangoDB1, 20.0))
                .collect(Collectors.toList());

        List<CreateCursorRequest> reqsWithLowQT = IntStream.range(0, 10)
                .mapToObj(__ -> new CreateCursorRequest(arangoDB1, 1.0))
                .collect(Collectors.toList());

        List<CreateCursorRequest> reqs = Stream.concat(reqsWithNoQT.stream(), reqsWithHighQT.stream()).collect(Collectors.toList());

        List<Thread> threads = reqs.stream()
                .map(Thread::new)
                .collect(Collectors.toList());

        for (Thread t : threads) {
            t.start();
        }

        int errorCount = 0;
        for (CreateCursorRequest r : reqsWithLowQT) {
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
        System.out.println("response with queue time > 0: " + nonZeroQTCount + "/" + reqs.size());

        assertThat((int) nonZeroQTCount, is(greaterThan(0)));
    }

    @Test
    public void queueTimeValuesCluster() throws InterruptedException {
        assumeTrue(isCluster());

        ArangoDB arangoDB2 = new ArangoDB.Builder()
                .maxConnections(500)
                .host("172.17.0.1", 8539)
                .password("test")
                .useProtocol(protocol)
                .build();

        List<CreateCursorRequest> createCursorReqs = IntStream.range(0, 50)
                .mapToObj(__ -> new CreateCursorRequest(arangoDB1, null))
                .collect(Collectors.toList());

        List<Thread> threads = createCursorReqs.stream()
                .map(Thread::new)
                .collect(Collectors.toList());

        for (Thread t : threads) {
            t.start();
        }

        for (Thread t : threads) {
            t.join();
        }

        LOGGER.info("starting ReadCursorRequests");
        List<ReadCursorRequest> reqsWithNoQT = createCursorReqs.stream()
                .limit(40)
                .map(CreateCursorRequest::getCursorId)
                .map(cid -> new ReadCursorRequest(arangoDB1, cid, null))
                .collect(Collectors.toList());

        List<Thread> threads2 = reqsWithNoQT.stream()
                .map(Thread::new)
                .collect(Collectors.toList());

        for (Thread t : threads2) {
            t.start();
        }

        List<ReadCursorRequest> reqsWithLowQT = createCursorReqs.stream()
                .skip(40)
                .map(CreateCursorRequest::getCursorId)
                .map(cid -> new ReadCursorRequest(arangoDB2, cid, 2.0))
                .collect(Collectors.toList());

        int errorCount = 0;
        for (ReadCursorRequest r : reqsWithLowQT) {
            try {
                r.run();
            } catch (ArangoDBException e) {
                errorCount++;
                e.printStackTrace();
            }
        }

        for (Thread t : threads2) {
            t.join();
        }

        assertThat(errorCount, is(0));
        LOGGER.info("completed ReadCursorRequests");

        long nonZeroQTCount = reqsWithNoQT.stream().filter(r -> r.getExecutionTime() > 0.0).count();
        System.out.println("response with queue time > 0: " + nonZeroQTCount + "/" + reqsWithNoQT.size());
        assertThat((int) nonZeroQTCount, is(greaterThan(0)));

    }

}
