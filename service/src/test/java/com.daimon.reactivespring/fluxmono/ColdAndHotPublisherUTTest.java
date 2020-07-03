package com.daimon.reactivespring.fluxmono;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.ConnectableFlux;
import reactor.core.publisher.Flux;

import java.time.Duration;

public class ColdAndHotPublisherUTTest {

    @Test
    public void coldPublisherTest() throws InterruptedException {
        Flux<String> stringFlux = Flux.just("A", "B", "C", "D", "E", "F")
                .delayElements(Duration.ofSeconds(1));

        stringFlux.subscribe(s -> System.out.println("Subscriber 1 " + s));
        Thread.sleep(2000);
        stringFlux.subscribe(s -> System.out.println("Subscriber 2 " + s));
        Thread.sleep(4000);
    }

    @Test
    public void hotPublisherTest() throws InterruptedException {
        Flux<String> stringFlux = Flux.just("A", "B", "C", "D", "E", "F")
                .delayElements(Duration.ofSeconds(1));

        ConnectableFlux<String> connectableFLux = stringFlux.publish();
        connectableFLux.connect();

        connectableFLux.subscribe(s -> System.out.println("Subscriber 1 " + s));
        Thread.sleep(3000);
        connectableFLux.subscribe(s -> System.out.println("Subscriber 2 " + s));
        Thread.sleep(4000);
    }
}
