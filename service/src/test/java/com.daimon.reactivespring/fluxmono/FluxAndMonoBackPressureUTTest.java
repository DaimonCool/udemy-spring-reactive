package com.daimon.reactivespring.fluxmono;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

public class FluxAndMonoBackPressureUTTest {

    @Test
    public void backPressureTest() {
        Flux<Integer> integerFlux = Flux.range(1, 10)
                .log();

        StepVerifier.create(integerFlux)
                .expectSubscription()
                .thenRequest(1)
                .expectNext(1)
                .thenRequest(1)
                .expectNext(2)
                .thenCancel()
                .verify();
    }

    @Test
    public void backPressure() {
        Flux<Integer> integerFlux = Flux.range(1, 10)
                .log();

        integerFlux.subscribe(System.out::println,
                throwable -> System.err.println("Exception is " + throwable),
                () -> System.out.println("Done"),
                subscription -> subscription.request(2));
    }

    @Test
    public void backPressureWithCancel() {
        Flux<Integer> integerFlux = Flux.range(1, 10)
                .log();

        integerFlux.subscribe(System.out::println,
                throwable -> System.err.println("Exception is " + throwable),
                () -> System.out.println("Done"),
                subscription -> {
                    subscription.request(2);
                    subscription.cancel();
                });
    }

    @Test
    public void backPressureCustomize() {
        Flux<Integer> integerFlux = Flux.range(1, 10)
                .log();

        integerFlux.subscribe(new BaseSubscriber<Integer>() {
            @Override
            protected void hookOnNext(Integer value) {
                request(1);
                System.out.println("Value " + value);
                if (value == 4) {
                    cancel();
                }
            }
        });
    }
}
