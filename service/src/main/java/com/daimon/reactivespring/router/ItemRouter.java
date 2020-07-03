package com.daimon.reactivespring.router;

import com.daimon.reactivespring.handler.ItemHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;

@Configuration
public class ItemRouter {

    @Bean
    public RouterFunction<ServerResponse> itemsRoute(ItemHandler itemHandler) {
        return RouterFunctions
                .route(GET("/v1/fun/items").and(accept(MediaType.APPLICATION_JSON)), itemHandler::getAllItems)
                .andRoute(GET("/v1/fun/items/{id}").and(accept(MediaType.APPLICATION_JSON)), itemHandler::getItemById)
                .andRoute(POST("/v1/fun/items").and(contentType(MediaType.APPLICATION_JSON)), itemHandler::createItem)
                .andRoute(DELETE("/v1/fun/items/{id}"), itemHandler::deleteItemById)
                .andRoute(PUT("v1/fun/items/{id}").and(contentType(MediaType.APPLICATION_JSON).and(accept(MediaType.APPLICATION_JSON))),
                        itemHandler::updateItem);
    }

    @Bean
    public RouterFunction<ServerResponse> errorRoute(ItemHandler itemHandler) {
        return RouterFunctions
                .route(GET("/v1/fun/exception"), itemHandler::itemException);
    }

    @Bean
    public RouterFunction<ServerResponse> itemStreamRoute(ItemHandler itemHandler) {
        return RouterFunctions.
                route(GET("/v1/fun/items/capped/stream"), itemHandler::itemStream);
    }
}
