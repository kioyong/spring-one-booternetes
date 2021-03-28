package com.example.customer;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CustomerController {
//    private final CustomerRepository customerRepository;

    @GetMapping("/customers")
    public Flux<Customer> get() {
//        return customerRepository.findAll();
        return Flux.just("A", "B", "C", "D")
                .map(name -> new Customer(name, name));
    }

}
