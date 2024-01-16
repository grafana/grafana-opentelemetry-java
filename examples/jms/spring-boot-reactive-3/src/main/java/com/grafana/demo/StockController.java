/*
 * Copyright Grafana Labs
 * SPDX-License-Identifier: Apache-2.0
 */

package com.grafana.demo;

import org.springframework.jms.core.JmsTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class StockController {

  private final JmsTemplate jmsTemplate;

  public StockController(JmsTemplate jmsTemplate) {
    this.jmsTemplate = jmsTemplate;
  }

  @GetMapping("/stock")
  public Mono<String> getStock() {
    jmsTemplate.convertAndSend("jms_destination", new Product());
    return Mono.just("Stock updated");
  }
}
