package com.example.customer;

import reactor.core.publisher.Flux;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class CustomerApplication {

    public static void main(String[] args) {
        SpringApplication.run(CustomerApplication.class, args);
    }

//    @Bean
//    ApplicationListener<ApplicationReadyEvent> ready(CustomerRepository customerRepository) {
//        return applicationReadyEvent -> {
//            customerRepository.deleteAll().subscribe();
//            var saved = Flux.just("A", "B", "C", "D")
//                    .map(name -> new Customer(name, name))
//                    .flatMap(customerRepository::save);
//            saved.subscribe(System.out::println);
//        };
//    }

}
