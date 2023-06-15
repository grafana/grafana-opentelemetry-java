package com.grafana.springbootdemo;

import org.springframework.jms.JmsException;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class CustomerController {

    private final CustomerMongoRepository repository;
    private final JmsTemplate jmsTemplate;
    private final Logger logger;

    public CustomerController(CustomerMongoRepository repository, JmsTemplate jmsTemplate, Logger logger) {
        this.repository = repository;
        this.jmsTemplate = jmsTemplate;
        this.logger = logger;
    }

    @GetMapping("/customer")
    public Mono<String> getCustomer() {
        try {
            jmsTemplate.convertAndSend("jms_destination", new Customer());
        } catch (JmsException e) {
            logger.warn("could not send JMS message", e);
        }

        return repository.findByFirstName("LGTM").map(customer -> customer.id).next();
    }

}
