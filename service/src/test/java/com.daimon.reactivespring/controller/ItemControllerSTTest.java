package com.daimon.reactivespring.controller;

import com.daimon.reactivespring.document.Item;
import com.daimon.reactivespring.repository.ItemReactiveRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.List;

@SpringBootTest
@AutoConfigureWebTestClient
@DirtiesContext
@ActiveProfiles("test")
public class ItemControllerSTTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ItemReactiveRepository itemReactiveRepository;

    private final List<Item> items = Arrays.asList(new Item(null, "Samsung TV", 200.0),
            new Item(null, "Apple TV", 300.0), new Item("ABC", "LG TV", 400.0));

    @BeforeEach
    public void setup() {
        itemReactiveRepository.deleteAll()
                .thenMany(Flux.fromIterable(items))
                .flatMap(itemReactiveRepository::save)
                .doOnNext(item -> System.out.println("Inserted item in test " + item))
                .blockLast();
    }

    @Test
    public void shouldReturnAllItems() {
        webTestClient.get()
                .uri("/v1/items")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(Item.class)
                .hasSize(3);
    }

    @Test
    public void shouldReturnAllItemsApproach2() {
        webTestClient.get()
                .uri("/v1/items")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(Item.class)
                .hasSize(3)
                .consumeWith(response -> {
                    List<Item> items = response.getResponseBody();
                    items.forEach(item -> {
                        Assertions.assertNotNull(item.getId());
                    });
                });
    }

    @Test
    public void shouldReturnAllItemsApproach3() {
        Flux<Item> responseBody = webTestClient.get()
                .uri("/v1/items")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .returnResult(Item.class)
                .getResponseBody();

        StepVerifier.create(responseBody.log())
                .expectSubscription()
                .expectNextCount(3)
                .verifyComplete();
    }

    @Test
    public void shouldReturnItemById() {
        webTestClient.get()
                .uri("/v1/items/{id}/", "ABC")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Item.class)
                .consumeWith(itemResult -> {
                    Assertions.assertEquals(400.0, itemResult.getResponseBody().getPrice());
                });
    }

    @Test
    public void shouldReturnNotFoundStatusWhenGetItemByNotExistingId() {
        webTestClient.get()
                .uri("/v1/items/{id}/", "ABCC")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    public void shouldCreateItem() {
        Item item = new Item(null, "Some TV", 500.0);
        webTestClient.post()
                .uri("/v1/items")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(item), Item.class)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").isNotEmpty()
                .jsonPath("$.description").isEqualTo("Some TV")
                .jsonPath("$.price").isEqualTo(500.0);
    }

    @Test
    public void shouldDeleteItemById() {
        webTestClient.delete()
                .uri("/v1/items/{id}", "ABC")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Void.class);

        webTestClient.get()
                .uri("/v1/items")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Item.class)
                .hasSize(2);
    }

    @Test
    public void shouldUpdateItem() {
        Item updateItem = new Item("ABC", "new description", 100.0);

        webTestClient.put()
                .uri("/v1/items/{id}", updateItem.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(updateItem), Item.class)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo("ABC")
                .jsonPath("$.description").isEqualTo("new description")
                .jsonPath("$.price").isEqualTo(100.0);
    }


    @Test
    public void shouldReturnNotFoundStatusWhenUpdateItemWithInvalidID() {
        Item updateItem = new Item("ABCC", "new description", 100.0);

        webTestClient.put()
                .uri("/v1/items/{id}", updateItem.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(updateItem), Item.class)
                .exchange()
                .expectStatus().isNotFound();
    }
}
