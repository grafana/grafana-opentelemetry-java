package com.grafana.springbootdemo;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class CartController {

    private final RestTemplate client = new RestTemplate();
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final Logger logger;

    public CartController(KafkaTemplate<String, String> template, Logger logger) {
        this.kafkaTemplate = template;
        this.logger = logger;
    }

    @GetMapping("/cart")
    public String getCart() {
        logger.info("getting cart");
        try {
            kafkaTemplate.send("kafkaTopic", "test");
        } catch (Exception e) {
            logger.warn("could not send kafka message", e);
        }
        client.getForEntity("http://localhost:8080/customer", String.class);
        return client.getForObject("http://localhost:8080/stock", String.class);
    }

}
