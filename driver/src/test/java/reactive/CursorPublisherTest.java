package reactive;

import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;



public class CursorPublisherTest {

    @Test
    void testSequentialPublisher() {
        StepVerifier.create(CursorPublisher.createFluxSequential(10).log())
                .expectNext(1, 2, 3, 4, 5)
                .expectNextCount(5)
                .expectComplete()
                .verify();

    }

    @Test
    void testCancelSequentialPublisher() {
        StepVerifier
                .create(CursorPublisher.createFluxSequential(10).log())
                .thenConsumeWhile(i -> i < 5)
                .thenCancel()
                .verify();
    }

    @Test
    void testAsyncPublisher() {
        StepVerifier.create(CursorPublisher.createFluxAsync(10).log())
                .expectNext(1, 2, 3, 4, 5)
                .expectNextCount(5)
                .expectComplete()
                .verify();

    }

    @Test
    void testCancelAsyncPublisher() {
        StepVerifier
                .create(CursorPublisher.createFluxAsync(10).log())
                .thenConsumeWhile(i -> i < 5)
                .thenCancel()
                .verify();
    }

}
