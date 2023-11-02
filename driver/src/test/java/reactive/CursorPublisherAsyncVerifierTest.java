package reactive;

import org.reactivestreams.Publisher;
import org.reactivestreams.tck.PublisherVerification;
import org.reactivestreams.tck.TestEnvironment;


public class CursorPublisherAsyncVerifierTest extends PublisherVerification<Integer> {

    public CursorPublisherAsyncVerifierTest() {
        super(new TestEnvironment());
    }

    @Override
    public Publisher<Integer> createFailedPublisher() {
        return null;
    }

    @Override
    public Publisher<Integer> createPublisher(long elements) {
        return CursorPublisher.createFluxAsync(elements);
    }

}
