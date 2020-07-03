package com.daimon.reactivespring.fluxmono;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.List;

public class FluxAndMonoMapUTTest {

    @Test
    public void fluxTestMap() {
        Flux<String> stringFlux = Flux.just("anna", "alena", " petya", "kolya")
                .map(String::toUpperCase)
                .log();

        StepVerifier.create(stringFlux)
                .expectNext("ANNA", "ALENA", " PETYA", "KOLYA")
                .verifyComplete();
    }

    @Test
    public void fluxTestMapWithRepeat() {
        Flux<Integer> stringFlux = Flux.just("anna", "alena", "petya", "kolya")
                .map(String::length)
                .repeat(1)
                .log();

        StepVerifier.create(stringFlux)
                .expectNext(4, 5, 5, 5, 4, 5, 5, 5)
                .verifyComplete();
    }

    @Test
    public void transformUsingFlatMap() {
        Flux<String> stringFlux = Flux.just("A", "B", "C", "D", "E", "F")
                .flatMap(s -> {
                    return Flux.fromIterable(convertToList(s));
                }) //db call or external call. s -> Flux<String>
                .log();

        StepVerifier.create(stringFlux)
                .expectNextCount(12)
                .verifyComplete();
    }

    @Test
    public void transformUsingFlatMapUsingParallel() {
        Flux<String> stringFlux = Flux.just("A", "B", "C", "D", "E", "F") // Flux<String>
                .window(2) // Flux<Flux<String>> -> (A,B), (C,D), (E,F)
                .flatMap(s -> {
                    return s.map(this::convertToList).subscribeOn(Schedulers.parallel()); // Flux<List<String>>
                })
                .flatMap(Flux::fromIterable) // Flux<String>
                .log();

        StepVerifier.create(stringFlux)
                .expectNextCount(12)
                .verifyComplete();
    }

    @Test
    public void transformUsingFlatMapUsingParallelSaveOrder() {
        Flux<String> stringFlux = Flux.just("A", "B", "C", "D", "E", "F") // Flux<String>
                .window(2) // Flux<Flux<String>> -> (A,B), (C,D), (E,F)
                .flatMapSequential(s -> {
                    return s.map(this::convertToList).subscribeOn(Schedulers.parallel()); // Flux<List<String>>
                })
                .flatMap(Flux::fromIterable) // Flux<String>
                .log();

        StepVerifier.create(stringFlux)
                .expectNextCount(12)
                .verifyComplete();
    }

    private List<String> convertToList(String s) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return Arrays.asList(s, "newValue");
    }
}
