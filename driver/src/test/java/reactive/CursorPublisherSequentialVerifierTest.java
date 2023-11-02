package reactive;

import org.reactivestreams.Publisher;
import org.reactivestreams.tck.PublisherVerification;
import org.reactivestreams.tck.TestEnvironment;


public class CursorPublisherSequentialVerifierTest extends PublisherVerification<Integer> {

    public CursorPublisherSequentialVerifierTest() {
        super(new TestEnvironment());
    }

    @Override
    public Publisher<Integer> createFailedPublisher() {
        return null;
    }

    @Override
    public Publisher<Integer> createPublisher(long elements) {
        return CursorPublisher.createFluxSequential(elements);
    }

}
