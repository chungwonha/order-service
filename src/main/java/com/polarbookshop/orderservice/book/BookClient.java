package com.polarbookshop.orderservice.book;

import reactor.core.publisher.Mono;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.util.retry.Retry;

import java.time.Duration;

@Service
public class BookClient {

    private final WebClient webClient;

    public BookClient(BookClientProperties bookClientProperties, WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
                .baseUrl(bookClientProperties.getCatalogServiceUrl().toString())
                .build();
    }

    /*
    Project Reactor provides a retry() operator to retry an operation in case it fails.
    You can add it to the reactive stream after the timeout() operator. The time limiter is applied first.
    If the timeout expires, the retry() operator kicks in and tries the request again.
     */
    public Mono<Book> getBookByIsbn(String isbn) {
        return webClient.get().uri(isbn)
                .retrieve()
                .bodyToMono(Book.class)
                .timeout(Duration.ofSeconds(1), Mono.empty())  //timeout operator
                .retryWhen(Retry.backoff(3, Duration.ofMillis(100))); //retry with backoff when timeout occurs
    }
}