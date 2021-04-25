package com.polarbookshop.orderservice.book;

import org.slf4j.Logger;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.util.retry.Retry;

import java.time.Duration;
import org.slf4j.LoggerFactory;

@Service
public class BookClient {
    Logger logger = LoggerFactory.getLogger(BookClient.class);
    private final WebClient webClient;
    private String bookUrl;
    public BookClient(BookClientProperties bookClientProperties, WebClient.Builder webClientBuilder) {
        System.out.println("bookClientProperties.getCatalogServiceUrl().toString(): "+bookClientProperties.getCatalogServiceUrl().toString());
        this.webClient = webClientBuilder
                .baseUrl(bookClientProperties.getCatalogServiceUrl().toString())
                .build();
        this.bookUrl=bookClientProperties.getCatalogServiceBooksUrl();
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
        System.out.println("getBookByIsbn isbn: "+isbn);
        System.out.println("webClient.get().toString(): "+webClient.get().toString());
        return webClient.get().uri(this.bookUrl+isbn)
                .retrieve()
                .bodyToMono(Book.class)
                .timeout(Duration.ofSeconds(1), Mono.empty())
                .onErrorResume(WebClientResponseException.NotFound.class, exception -> Mono.empty())
                .retryWhen(Retry.backoff(3, Duration.ofMillis(100)));
    }
}