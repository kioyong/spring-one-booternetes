package com.example.order;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import reactor.core.publisher.Flux;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@SpringBootApplication
public class OrderRSocketApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderRSocketApplication.class, args);
    }

}


@Controller
class OrderRSocketController {


    private HashMap<String, Collection<Order>> db = new HashMap<>();

    {
        List.of("A", "B", "C", "D").forEach(customerId -> db.put(customerId, randomOrdersFor(customerId)));

    }

    private Collection<Order> randomOrdersFor(String customerId) {
        ArrayList<Order> customers = new ArrayList<>();
        int max = (int) (Math.random() * 1000);
        for (int i = 0; i < max; i++) {
            customers.add(new Order(i, customerId));
        }
        return customers;
    }

    @MessageMapping("orders.{customerId}")
    public Flux<Order> getOrdersFor(@DestinationVariable String customerId) {
        return Flux.fromIterable(this.db.get(customerId));
    }

}

@Data
@AllArgsConstructor
@NoArgsConstructor
class Order {
    private Integer id;
    private String customerId;
}