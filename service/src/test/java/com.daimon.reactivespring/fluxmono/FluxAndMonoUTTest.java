package com.daimon.reactivespring.fluxmono;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

public class FluxAndMonoUTTest {

    @Test
    public void fluxTest() {
        Flux<String> stringFlux = Flux.just("Spring", "Spring boot", "Spring Reactive")
                //.concatWith(Flux.error(new RuntimeException("some error")))
                .concatWith(Flux.just("Last one"))
                .log();

        stringFlux
                .subscribe(System.out::println,
                        (ex) -> System.out.println("message " + ex.getMessage()),
                        () -> System.out.println("Completed"));
    }

    @Test
    public void fluxTestElementsWithoutError() {
        Flux<String> stringFlux = Flux.just("Spring", "Spring boot", "Spring Reactive")
                .log();

        StepVerifier.create(stringFlux)
                .expectNext("Spring")
                .expectNext("Spring boot")
                .expectNext("Spring Reactive")
                .verifyComplete();
    }

    @Test
    public void fluxTestElementsWithoutError1() {
        Flux<String> stringFlux = Flux.just("Spring", "Spring boot", "Spring Reactive")
                .log();

        StepVerifier.create(stringFlux)
                .expectNext("Spring", "Spring boot", "Spring Reactive")
                .verifyComplete();
    }

    @Test
    public void fluxTestElementsWithErrors() {
        Flux<String> stringFlux = Flux.just("Spring", "Spring boot", "Spring Reactive")
                .concatWith(Flux.error(new RuntimeException("some error")))
                .log();

        StepVerifier.create(stringFlux)
                .expectNext("Spring", "Spring boot", "Spring Reactive")
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    public void fluxTestElementsWithErrors1() {
        Flux<String> stringFlux = Flux.just("Spring", "Spring boot", "Spring Reactive")
                .concatWith(Flux.error(new RuntimeException("some error")))
                .log();

        StepVerifier.create(stringFlux)
                .expectNext("Spring", "Spring boot", "Spring Reactive")
                .expectErrorMessage("some error")
                .verify();
    }

    @Test
    public void monoTest() {
        Mono<String> stringMono = Mono.just("Spring").log();

        StepVerifier.create(stringMono)
                .expectNext("Spring")
                .verifyComplete();
    }

    @Test
    public void monoTestWithError() {
        Mono<Object> monoError = Mono.error(new RuntimeException("some error")).log();

        StepVerifier.create(monoError)
                .expectError(RuntimeException.class)
                .verify();
    }

}
