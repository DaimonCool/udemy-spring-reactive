package com.daimon.reactivespring.handler;

import com.daimon.reactivespring.document.Item;
import com.daimon.reactivespring.document.ItemCapped;
import com.daimon.reactivespring.repository.ItemReactiveCappedRepository;
import com.daimon.reactivespring.repository.ItemReactiveRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class ItemHandler {

    private final ItemReactiveRepository itemReactiveRepository;
    private final ItemReactiveCappedRepository itemReactiveCappedRepository;

    public Mono<ServerResponse> getAllItems(ServerRequest serverRequest) {
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(itemReactiveRepository.findAll(), Item.class);
    }

    public Mono<ServerResponse> getItemById(ServerRequest serverRequest) {
        String itemId = serverRequest.pathVariable("id");
        Mono<Item> monoItem = itemReactiveRepository.findById(itemId);

        return monoItem.flatMap(item ->
                ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(BodyInserters.fromValue(item)))
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> createItem(ServerRequest serverRequest) {
        Mono<Item> requestItem = serverRequest.bodyToMono(Item.class);
        return requestItem.flatMap(item ->
                ServerResponse.status(HttpStatus.CREATED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(itemReactiveRepository.save(item), Item.class));
    }

    public Mono<ServerResponse> deleteItemById(ServerRequest serverRequest) {
        String itemId = serverRequest.pathVariable("id");
        return ServerResponse.ok()
                .body(itemReactiveRepository.deleteById(itemId), Void.class);
    }

    public Mono<ServerResponse> updateItem(ServerRequest serverRequest) {
        String itemId = serverRequest.pathVariable("id");
        Mono<Item> updatedItem = serverRequest.bodyToMono(Item.class)
                .flatMap(item ->
                        itemReactiveRepository.findById(itemId)
                                .flatMap(currentItem -> {
                                    currentItem.setDescription(item.getDescription());
                                    currentItem.setPrice(item.getPrice());
                                    return itemReactiveRepository.save(currentItem);
                                }));

        return updatedItem.flatMap(item ->
                ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(BodyInserters.fromValue(item)))
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> itemException(ServerRequest serverRequest) {
        throw new RuntimeException("Some exception occurred");
    }

    public Mono<ServerResponse> itemStream(ServerRequest serverRequest) {
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_STREAM_JSON)
                .body(itemReactiveCappedRepository.findItemsBy(), ItemCapped.class);
    }
}
