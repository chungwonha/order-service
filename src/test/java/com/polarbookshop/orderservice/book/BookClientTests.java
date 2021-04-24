package com.polarbookshop.orderservice.book;

import java.io.IOException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import org.springframework.web.reactive.function.client.WebClient;

class BookClientTests {

    private MockWebServer mockWebServer;
    private BookClient bookClient;

    @BeforeEach
    void setup() throws IOException {
        this.mockWebServer = new MockWebServer();
        this.mockWebServer.start();

        BookClientProperties bookClientProperties = new BookClientProperties();
        bookClientProperties.setCatalogServiceUrl(mockWebServer.url("/").uri()); //set a fake url for catalog service
        this.bookClient = new BookClient(bookClientProperties, WebClient.builder());

    }

    @AfterEach
    void clean() throws IOException {
        this.mockWebServer.shutdown();
    }

    @Test
    void whenBookExistsThenReturnBook() {
        String bookIsbn = "1234567890";

        //define the response from the mocked catalog service
        MockResponse mockResponse = new MockResponse()
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .setBody("{\"isbn\":\"" + bookIsbn + "\",\"title\":\"Book Title\", \"author\":\"Book Author\", \"price\":\"9.90\"}");
        //Put the pre-defined response in the queue for the mocked server to process
        mockWebServer.enqueue(mockResponse);

        Mono<Book> book = bookClient.getBookByIsbn(bookIsbn);
//        if(book==null){
//            System.out.println("book is null");
//        }else{
//            System.out.println("book is not null");
//            book.subscribe(b->System.out.println("b.getIsbn(): "+b.getIsbn()));
        book.subscribe();
////            if(book.block()==null){
////                System.out.println("book.block() is null");
////            }else{
////                System.out.println("book.block() is not null");
////                System.out.println("book.block().getIsbn(): "+book.block().getIsbn());
////            }
//        }

        //use StepVerifier to verify with the returned object from the mock server
        StepVerifier.create(book)
                .expectNextMatches(b -> {
                    System.out.println("b.getIsbn(): "+b.getIsbn());
                    return b.getIsbn().equals(bookIsbn);})
                .verifyComplete();
    }
}