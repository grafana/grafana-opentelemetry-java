package com.grafana.springbootdemo;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
public class CartController {

    private final WebClient client = WebClient.create("http://localhost:8080");
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final Logger logger;

    public CartController(KafkaTemplate<String, String> template, Logger logger) {
        this.kafkaTemplate = template;
        this.logger = logger;
    }

    @GetMapping("/cart")
    public Mono<String> getCart() {
        logger.info("getting cart");
        try {
            kafkaTemplate.send("kafkaTopic", "test");
        } catch (Exception e) {
            logger.warn("could not send kafka message", e);
        }

        return Flux.merge(
                        client.get().uri("/customer").retrieve().bodyToMono(String.class),
                        client.get().uri("/stock").retrieve().bodyToMono(String.class))
                .reduce((s, s2) -> s + "-" + s2);
    }
}
