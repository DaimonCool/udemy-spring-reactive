package com.daimon.reactivespring.client.controller;

import com.daimon.reactivespring.client.domain.Item;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/client")
public class ItemClientController {

    private WebClient webClient = WebClient.create("http://localhost:8080");

    @GetMapping("/retrieve")
    public Flux<Item> getAllItems() {
        return webClient.get()
                .uri("/v1/fun/items")
                .retrieve()
                .bodyToFlux(Item.class)
                .log();
    }

    @GetMapping("/exchange")
    public Flux<Item> getAllItemsUsingExchange() {
        return webClient.get()
                .uri("/v1/fun/items")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .flatMapMany(clientResponse -> clientResponse.bodyToFlux(Item.class))
                .log();
    }

    @GetMapping("/retrieve/singleitem/{id}")
    public Mono<Item> getItemUsingRetrieve(@PathVariable String id) {
        return webClient.get()
                .uri("/v1/fun/items/{id}", id)
                .retrieve()
                .bodyToMono(Item.class)
                .log();
    }

    @GetMapping("/exchange/singleitem/{id}")
    public Mono<Item> getItemUsingExchange(@PathVariable String id) {
        return webClient.get()
                .uri("/v1/fun/items/{id}", id)
                .exchange()
                .flatMap(clientResponse -> clientResponse.bodyToMono(Item.class))
                .log();
    }

    @PostMapping("/createitem")
    public Mono<Item> createItem(@RequestBody Item item) {
        return webClient.post()
                .uri("/v1/fun/items")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(item), Item.class)
                .retrieve()
                .bodyToMono(Item.class)
                .log();
    }

    @PutMapping("/updateitem/{id}")
    public Mono<Item> updateItem(@PathVariable String id, @RequestBody Item item) {
        return webClient.put()
                .uri("/v1/fun/items/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(item), Item.class)
                .exchange()
                .flatMap(clientResponse -> clientResponse.bodyToMono(Item.class))
                .log();
    }

    @DeleteMapping("/deleteitem/{id}")
    public Mono<Void> deleteIteam(@PathVariable String id) {
        return webClient.delete()
                .uri("/v1/fun/items/{id}", id)
                .retrieve()
                .bodyToMono(Void.class)
                .log();
    }

    @GetMapping("/retrieve/exception")
    public Flux<Item> errorRetrieve() {
        return webClient.get()
                .uri("/v1/items/exception")
                .retrieve()
                .onStatus(HttpStatus::is5xxServerError, clientResponse -> {
                    Mono<String> exceptionMono = clientResponse.bodyToMono(String.class);
                    return exceptionMono.flatMap(message -> {
                        throw new RuntimeException(message);
                    });
                })
                .bodyToFlux(Item.class);
    }

    @GetMapping("/exchange/exception")
    public Flux<Item> errorExchange() {
        return webClient.get()
                .uri("/v1/items/exception")
                .exchange()
                .flatMapMany(clientResponse -> {
                    if (clientResponse.statusCode().is5xxServerError()) {
                        return clientResponse.bodyToMono(String.class)
                                .flatMap(message -> {
                                    throw new RuntimeException(message);
                                });
                    } else {
                        return clientResponse.bodyToFlux(Item.class);
                    }
                });
    }

}
