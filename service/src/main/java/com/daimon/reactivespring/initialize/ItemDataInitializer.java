package com.daimon.reactivespring.initialize;

import com.daimon.reactivespring.document.Item;
import com.daimon.reactivespring.document.ItemCapped;
import com.daimon.reactivespring.repository.ItemReactiveCappedRepository;
import com.daimon.reactivespring.repository.ItemReactiveRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.CollectionOptions;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
@Profile("!test")
public class ItemDataInitializer implements CommandLineRunner {

    private final ItemReactiveRepository itemReactiveRepository;
    private final ItemReactiveCappedRepository itemReactiveCappedRepository;
    private final MongoOperations mongoOperations;

    @Override
    public void run(String... args) throws Exception {
        initialDatSetup();
        createCappedCollection();
    }

    private void initialDatSetup() {
        itemReactiveRepository.deleteAll()
                .thenMany(Flux.fromIterable(initItems()))
                .flatMap(itemReactiveRepository::save)
                .subscribe(item -> System.out.println("Item inserted " + item));
    }

    private List<Item> initItems() {
        return Arrays.asList(new Item(null, "Samsung TV", 200.0),
                new Item(null, "Apple TV", 300.0), new Item(null, "LG TV", 400.0));
    }

    private void createCappedCollection() {
        mongoOperations.dropCollection(ItemCapped.class);
        mongoOperations.createCollection(ItemCapped.class, CollectionOptions.empty().maxDocuments(20).size(50000).capped());
        setupCappedCollection();
    }

    private void setupCappedCollection() {
        Flux<ItemCapped> itemCappedFlux = Flux.interval(Duration.ofSeconds(1))
                .map(value -> new ItemCapped(null, "Random Item " + value, 100.0 + value));

        itemReactiveCappedRepository.insert(itemCappedFlux)
                .subscribe(System.out::println);
    }
}
