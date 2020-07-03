package com.daimon.reactivespring.controller;

import com.daimon.reactivespring.document.ItemCapped;
import com.daimon.reactivespring.repository.ItemReactiveCappedRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.CollectionOptions;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.Duration;

@SpringBootTest
@AutoConfigureWebTestClient
@DirtiesContext
@ActiveProfiles("test")
public class ItemStreamControllerSTTest {

    @Autowired
    private ItemReactiveCappedRepository itemReactiveCappedRepository;

    @Autowired
    private MongoOperations mongoOperations;

    @Autowired
    private WebTestClient webTestClient;

    @BeforeEach
    public void setup() {
        mongoOperations.dropCollection(ItemCapped.class);
        mongoOperations.createCollection(ItemCapped.class, CollectionOptions.empty().maxDocuments(20).size(50000).capped());
        Flux<ItemCapped> itemCappedFlux = Flux.interval(Duration.ofMillis(100))
                .map(value -> new ItemCapped(null, "Random Item " + value, 100.0 + value))
                .take(5);
        itemReactiveCappedRepository.insert(itemCappedFlux)
                .doOnNext(System.out::println)
                .blockLast();
    }

    @Test
    public void shouldGetItemsCappedStream() {
        Flux<ItemCapped> responseItemsCapped = webTestClient.get()
                .uri("/items/capped/stream")
                .exchange()
                .expectStatus().isOk()
                .returnResult(ItemCapped.class)
                .getResponseBody()
                .take(5);

        StepVerifier.create(responseItemsCapped)
                .expectNextCount(5)
                .thenCancel()
                .verify();
    }

}
