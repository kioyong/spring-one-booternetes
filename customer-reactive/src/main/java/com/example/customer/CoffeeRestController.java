package com.example.customer;

import java.time.Duration;
import java.util.logging.Level;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SignalType;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CoffeeRestController {


    // Sample slow request
    @GetMapping(path = "/drip", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> drip() {
        return Flux.interval(Duration.ofSeconds(1)).take(10)
                .map(aLong -> String.format("Brewing drop %d...", aLong));
    }


    // Sample error request
    @GetMapping("/coffee")
    public ResponseEntity<Mono<String>> coffee() {
        return ResponseEntity.status(HttpStatus.I_AM_A_TEAPOT)
                .header("X-Reason", "i-am-a-teapot")
                .body(Mono.just("Out of Coffee, Have some tea!")
                        .log("No Coffee in teapot", Level.SEVERE, SignalType.ON_NEXT)
                );

    }
}
