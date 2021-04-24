package com.polarbookshop.orderservice.book;

import org.springframework.web.reactive.function.client.WebClientResponseException;
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

    Project Reactor provides an onErrorResume() operator to define a fallback when a specific error occurs.
    You can add it to the reactive stream after the timeout() operator and before the retryWhen(),
    so that if a 404 response is received (WebClientResponseException.NotFound exception),
    the retry operator is not triggered.
     */
    public Mono<Book> getBookByIsbn(String isbn) {
        return webClient.get().uri(isbn)
                .retrieve()
                .bodyToMono(Book.class)
                .timeout(Duration.ofSeconds(1), Mono.empty())
                .onErrorResume(WebClientResponseException.NotFound.class, exception -> Mono.empty())
                .retryWhen(Retry.backoff(3, Duration.ofMillis(100)));
    }
}