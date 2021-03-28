package com.example.gateway;

import java.time.Duration;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

@SpringBootApplication
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }

    @Value("${customer-reactive.uri:http://localhost:8081}")
    private String customerReactiveUri;

    @Value("${order-socket.host:localhost}")
    private String orderSocketHost;

    @Value("${order-socket.port:8181}")
    private Integer orderSocketPort;


    @Bean
    RSocketRequester rSocketRequester(RSocketRequester.Builder builder) {
        return builder.tcp(orderSocketHost, orderSocketPort);
    }

    @Bean
    WebClient http(WebClient.Builder builder) {
        return builder.build();
    }

    @Bean
    RouteLocator gateway(RouteLocatorBuilder rlb) {
        return rlb.routes()
                .route(rs ->
                        rs.path("/proxy").and().host("*.spring.io")
                                .filters(fs -> fs.setPath("/customers"))
                                .uri(customerReactiveUri)
                ).build();
    }
}

@Component
@RequiredArgsConstructor
class CrmClient {

    @Value("${customer-reactive.uri:http://localhost:8081}")
    private String customerReactiveUri;
    private final RSocketRequester rSocketRequester;
    private final WebClient http;

    Flux<Customer> getCustoemers() {
        return this.http.get().uri(customerReactiveUri+"/customers").retrieve().bodyToFlux(Customer.class)
                .retryWhen(Retry.backoff(10, Duration.ofSeconds(10)))
                .onErrorResume(ex -> Flux.empty())
                .timeout(Duration.ofSeconds(5));
    }

    Flux<Order> getOrderFor(String customerId) {
        return this.rSocketRequester.route("orders.{cid}", customerId).retrieveFlux(Order.class)
                .retryWhen(Retry.backoff(10, Duration.ofSeconds(10)))
                .onErrorResume(ex -> Flux.empty())
                .timeout(Duration.ofSeconds(10));

    }

    Flux<CustomerOrders> getCustomerOrders() {
        return this.getCustoemers()
                .flatMap(customer -> Mono.zip(
                        Mono.just(customer),
                        getOrderFor(customer.getId()).collectList()
                )).map(tuple -> new CustomerOrders(tuple.getT1(), tuple.getT2()));
    }

}


@RestController
@RequiredArgsConstructor
class CustomerOrdersController {

    private final CrmClient crmClient;


    @GetMapping("/customerOrders")
    Flux<CustomerOrders> getCustomerOrders() {
        return crmClient.getCustomerOrders();
    }
}


@Data
@AllArgsConstructor
@NoArgsConstructor
class CustomerOrders {
    private Customer customer;
    private List<Order> orders;
}


@Data
@AllArgsConstructor
@NoArgsConstructor
class Order {
    private Integer id;
    private String customerId;
}

@Data
@AllArgsConstructor
@NoArgsConstructor
class Customer {

    private String id;
    private String name;
}