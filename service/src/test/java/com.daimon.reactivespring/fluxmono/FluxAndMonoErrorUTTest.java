package com.daimon.reactivespring.fluxmono;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;
import reactor.util.retry.Retry;

import java.time.Duration;

public class FluxAndMonoErrorUTTest {

    @Test
    public void fluxErrorHandling() {
        Flux<String> stringFlux = Flux.just("A", "B", "C")
                .concatWith(Flux.error(new RuntimeException("some error")))
                .concatWith(Flux.just("D"))
                .onErrorResume(e -> {
                    System.out.println("Exception is " + e);
                    return Flux.just("default", "default1");
                })
                .log();

        StepVerifier.create(stringFlux)
                .expectSubscription()
                .expectNext("A", "B", "C")
                //.expectError(RuntimeException.class)
                //.verify();
                .expectNext("default", "default1")
                .verifyComplete();
    }

    @Test
    public void fluxErrorHandlingOnErrorReturn() {
        Flux<String> stringFlux = Flux.just("A", "B", "C")
                .concatWith(Flux.error(new RuntimeException("some error")))
                .concatWith(Flux.just("D"))
                .onErrorReturn("default")
                .log();

        StepVerifier.create(stringFlux)
                .expectSubscription()
                .expectNext("A", "B", "C")
                .expectNext("default")
                .verifyComplete();
    }

    @Test
    public void fluxErrorHandlingOnErrorMap() {
        Flux<String> stringFlux = Flux.just("A", "B", "C")
                .concatWith(Flux.error(new RuntimeException("some error")))
                .concatWith(Flux.just("D"))
                .onErrorMap(CustomException::new)
                .log();

        StepVerifier.create(stringFlux)
                .expectSubscription()
                .expectNext("A", "B", "C")
                .expectError(CustomException.class)
                .verify();
    }

    @Test
    public void fluxErrorHandlingOnErrorMapWithRetry() {
        Flux<String> stringFlux = Flux.just("A", "B", "C")
                .concatWith(Flux.error(new RuntimeException("some error")))
                .concatWith(Flux.just("D"))
                .onErrorMap(CustomException::new)
                .retry(2)
                .log();

        StepVerifier.create(stringFlux)
                .expectSubscription()
                .expectNext("A", "B", "C")
                .expectNext("A", "B", "C")
                .expectNext("A", "B", "C")
                .expectError(CustomException.class)
                .verify();
    }

    @Test
    public void fluxErrorHandlingOnErrorMapWithRetryBackoff() {
        Flux<String> stringFlux = Flux.just("A", "B", "C")
                .concatWith(Flux.error(new RuntimeException("some error")))
                .concatWith(Flux.just("D"))
                .onErrorMap(CustomException::new)
                .retryWhen(Retry.backoff(2, Duration.ofSeconds(5)))
                .log();

        StepVerifier.create(stringFlux)
                .expectSubscription()
                .expectNext("A", "B", "C")
                .expectNext("A", "B", "C")
                .expectNext("A", "B", "C")
                .expectError(IllegalStateException.class)
                .verify();
    }
}
