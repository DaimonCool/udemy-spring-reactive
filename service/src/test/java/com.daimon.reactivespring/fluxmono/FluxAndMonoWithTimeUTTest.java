package com.daimon.reactivespring.fluxmono;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.Duration;

public class FluxAndMonoWithTimeUTTest {

    @Test
    public void infiniteSequence() throws InterruptedException {
        Flux<Long> longFlux = Flux.interval(Duration.ofMillis(200))
                .log();

        longFlux.subscribe(aLong -> System.out.println("values is " + aLong));

        Thread.sleep(3000);
    }

    @Test
    public void infiniteSequenceTest() {
        Flux<Long> longFlux = Flux.interval(Duration.ofMillis(200))
                .take(3)
                .log();

        StepVerifier.create(longFlux)
                .expectSubscription()
                .expectNext(0L, 1L, 2L)
                .verifyComplete();
    }

    @Test
    public void infiniteSequenceMap() {
        Flux<Integer> longFlux = Flux.interval(Duration.ofMillis(200))
                .map(Long::intValue)
                .take(3)
                .log();

        StepVerifier.create(longFlux)
                .expectSubscription()
                .expectNext(0, 1, 2)
                .verifyComplete();
    }

    @Test
    public void infiniteSequenceMapWithDelay() {
        Flux<Integer> longFlux = Flux.interval(Duration.ofMillis(200))
                .delayElements(Duration.ofSeconds(1))
                .map(Long::intValue)
                .take(3)
                .log();

        StepVerifier.create(longFlux)
                .expectSubscription()
                .expectNext(0, 1, 2)
                .verifyComplete();
    }
}
