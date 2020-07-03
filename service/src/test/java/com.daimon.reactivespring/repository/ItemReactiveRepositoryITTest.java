package com.daimon.reactivespring.repository;

import com.daimon.reactivespring.document.Item;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.List;

@DataMongoTest
@ExtendWith(SpringExtension.class)
@DirtiesContext
public class ItemReactiveRepositoryITTest {

    @Autowired
    private ItemReactiveRepository itemReactiveRepository;

    private List<Item> items = Arrays.asList(new Item(null, "Samsung TV", 200.0),
            new Item(null, "Apple TV", 300.0), new Item("ABC", "Samsung TV", 400.0));

    @BeforeEach
    public void setup() {
        itemReactiveRepository.deleteAll()
                .thenMany(Flux.fromIterable(items))
                .flatMap(itemReactiveRepository::save)
                .doOnNext(item -> {
                    System.out.println("Saved item: " + item);
                })
                .blockLast();
    }

    @Test
    public void getAllItems() {
        StepVerifier.create(itemReactiveRepository.findAll().log())
                .expectSubscription()
                .expectNextCount(3)
                .verifyComplete();
    }

    @Test
    public void getItemById() {
        StepVerifier.create(itemReactiveRepository.findById("ABC").log())
                .expectSubscription()
                .expectNextMatches(item -> item.getDescription().equals("Samsung TV"))
                .verifyComplete();
    }

    @Test
    public void getItemByDescription() {
        StepVerifier.create(itemReactiveRepository.findByDescription("Samsung TV").log())
                .expectSubscription()
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    public void saveItem() {
        Item item = new Item(null, "Google Home Mini", 30.0);
        Mono<Item> savedItem = itemReactiveRepository.save(item).log();
        StepVerifier.create(savedItem)
                .expectSubscription()
                .expectNextMatches(persistedItem -> persistedItem.getId() != null && persistedItem.getDescription().equals("Google Home Mini"))
                .verifyComplete();
    }

    @Test
    public void updateItem() {
        Flux<Item> updatedItem = itemReactiveRepository.findByDescription("Apple TV")
                .map(item -> {
                    item.setPrice(500.0);
                    return item;
                })
                .flatMap(item -> itemReactiveRepository.save(item));

        StepVerifier.create(updatedItem.log())
                .expectSubscription()
                .expectNextMatches(item -> item.getPrice() == 500.0)
                .verifyComplete();
    }

    @Test
    public void deleteItemById() {
        Mono<Void> deletedItem = itemReactiveRepository.findById("ABC")
                .map(Item::getId)
                .flatMap(itemReactiveRepository::deleteById);

        StepVerifier.create(deletedItem.log())
                .expectSubscription()
                .verifyComplete();

        StepVerifier.create(itemReactiveRepository.findAll().log())
                .expectSubscription()
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    public void deleteItemBy() {
        Mono<Void> deletedItem = itemReactiveRepository.findById("ABC")
                .flatMap(itemReactiveRepository::delete);

        StepVerifier.create(deletedItem.log())
                .expectSubscription()
                .verifyComplete();

        StepVerifier.create(itemReactiveRepository.findAll().log())
                .expectSubscription()
                .expectNextCount(2)
                .verifyComplete();
    }
}
