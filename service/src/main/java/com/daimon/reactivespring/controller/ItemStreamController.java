package com.daimon.reactivespring.controller;

import com.daimon.reactivespring.document.ItemCapped;
import com.daimon.reactivespring.repository.ItemReactiveCappedRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequiredArgsConstructor
@RequestMapping("/items/capped")
public class ItemStreamController {

    private final ItemReactiveCappedRepository itemReactiveCappedRepository;

    @GetMapping(value = "/stream", produces = MediaType.APPLICATION_STREAM_JSON_VALUE)
    public Flux<ItemCapped> getAllItemsCapped() {
        return itemReactiveCappedRepository.findItemsBy();
    }
}
