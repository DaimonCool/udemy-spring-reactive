package com.daimon.reactivespring.fluxmono;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

public class FluxAndMonoFilterUTTest {

    @Test
    public void fluxFilterTest() {
        Flux<String> stringFlux = Flux.just("anna", "alena", "kolya", "petya")
                .filter(s -> s.startsWith("a"))
                .log();

        StepVerifier.create(stringFlux)
                .expectNext("anna", "alena")
                .verifyComplete();
    }
}
