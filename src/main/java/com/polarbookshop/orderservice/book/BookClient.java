package com.polarbookshop.orderservice.book;

import reactor.core.publisher.Mono;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class BookClient {

    private final WebClient webClient;

    public BookClient(BookClientProperties bookClientProperties, WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
                .baseUrl(bookClientProperties.getCatalogServiceUrl().toString())
                .build();
    }

    public Mono<Book> getBookByIsbn(String isbn) {
        return webClient.get().uri(isbn)
                .retrieve()
                .bodyToMono(Book.class);
    }
}